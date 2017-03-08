package com.example.truedemon.combinetest;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by truedemon on 6/3/2017.
 */

public class BluetoothService extends Service {

    public final static String EXTRA_MESSAGE = "com.example.shaun.MESSAGE";
    BluetoothAdapter mBluetoothAdapter;

    public static String mConnectedDeviceName = null;
    //    private MainActivity mChatService = null;
    //Array for storing stuff
    private ArrayAdapter<String> mBTArrayAdapter;
    private BluetoothAdapter mBTAdapter;

    private static final String TAG = "BluetoothService";
    private ArrayAdapter<String> mConversationArrayAdapter;
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    private int mState;
    private int mNewState;
    private TextView mBluetoothStatus;
    //    private Handler mHandler; // Our main handler that will receive callback notifications // bluetooth background worker thread to send and receive data
    private BluetoothService.ConnectThread mConnectThread;
    private BluetoothService.ConnectedThread mConnectedThread;
    private BluetoothService.AcceptThread mAcceptThread;


    //    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    // #defines for identifying shared types between calling functions
    //private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    //private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    //private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    //    private int connectStatus=0;
    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    private IBinder mBinder = new BTBinder();    //< -- binder

    public interface MessageConstants {
        // Message types sent from the BluetoothChatService Handler
        public static final int MESSAGE_STATE_CHANGE = 1;
        public static final int MESSAGE_READ = 2;
        public static final int MESSAGE_WRITE = 3;
        public static final int MESSAGE_DEVICE_NAME = 4;
        public static final int MESSAGE_TOAST = 5;
        public static final int MESSAGE_CONTROL = 6;
        // Key names received from the BluetoothChatService Handler
        public static final String DEVICE_NAME = "device_name";
        public static final String TOAST = "toast";

    }

    public BluetoothService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getBTState() {
        return mState;
    }
    /**
     * Set the current connection state.
     */
    public synchronized void setBTState(int state) {
        mState = state;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent)
    {
        super.onRebind(intent);
//        return true;
    }

    public class BTBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    /**
     * start service
     */
//    @Override
    public void startService(){
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }




    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param //secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (getBTState() == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new BluetoothService.ConnectThread(device);
        mConnectThread.start();
        setBTState(STATE_CONNECTING);
        // Update UI title
    }
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {
        Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
//        if (mInsecureAcceptThread != null) {
//            mInsecureAcceptThread.cancel();
//            mInsecureAcceptThread = null;
//        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new BluetoothService.ConnectedThread(socket);
        // new ConnectedThread(socket,socketType);

        mConnectedThread.start();
        // Send the name of the connected device back to the UI Activity
        Message msg = AppServiceController.getInstance().mHandler.obtainMessage(MessageConstants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MessageConstants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
//        mHandler.sendMessage(msg);
        setBTState(STATE_CONNECTED);
        // Update UI title
//        updateUserInterfaceTitle();
    }



    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;
//            mBluetoothStatus.setText("Starting ConnectThread");
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
                //mBluetoothStatus.setText("Create Socket");
            } catch (IOException e) {
//                mBluetoothStatus.setText("Socket creation failed");
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
            //setBTState(STATE_CONNECTING);
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                    //mBluetoothStatus.setText("Could not close client socket");
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //mBluetoothStatus.setText("Connected!");
            Message msg = AppServiceController.getInstance().mHandler.obtainMessage(BluetoothService.MessageConstants.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(BluetoothService.MessageConstants.DEVICE_NAME, mmDevice.getName());
            msg.setData(bundle);
            AppServiceController.getInstance().mHandler.sendMessage(msg);
            setBTState(STATE_CONNECTED);
            if (mState==STATE_CONNECTED) {
                Log.d(TAG, "CONNECTED to: " + mmDevice);
                //mBluetoothStatus.setText("Connected to: " +mmDevice);
            }
            //Reset connectthread when done
            mConnectThread = null;
            // Start the connected thread
            connected(mmSocket, mmDevice);
            // Update UI title
//            updateUserInterfaceTitle();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //Log.e(TAG, "Could not close the client socket", e);
            }
        }

    }
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;



        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            //mState = STATE_CONNECTED;
        }

        public void run() {
            byte[] mmBuffer = new byte[1024];
            //mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()
            //String msg ="";
            // Keep listening to the InputStream until an exception occurs.
//            while (mState == STATE_CONNECTED) {
            while (getBTState() == STATE_CONNECTED) {
                try {
                    //mConversationArrayAdapter.clear();
                    // Read from the InputStream.
                    Log.d(TAG, "1");
                    numBytes = mmInStream.read(mmBuffer);
                    Log.d(TAG, "2");
                    //String message = numBytes.toString();
                    // Send the obtained bytes to the UI activity.
                    //String message = new String(mmInStream.read(mmBuffer), "UTF-8");
                    Message readMsg = AppServiceController.getInstance().mHandler.obtainMessage(BluetoothService.MessageConstants.MESSAGE_READ, numBytes, -1, mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }
        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                // Share the sent message with the UI activity.
//                Message writtenMsg = mHandler.obtainMessage(
//                        MessageConstants.MESSAGE_WRITE, -1, -1, bytes);
//                writtenMsg.sendToTarget();
                AppServiceController.getInstance().mHandler.obtainMessage(BluetoothService.MessageConstants.MESSAGE_WRITE, -1, -1, bytes)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                //Handler mHandler = new Handler();
                Message writeErrorMsg =
                        AppServiceController.getInstance().mHandler.obtainMessage(BluetoothService.MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                AppServiceController.getInstance().mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }


    }
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see BluetoothService.ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        BluetoothService.ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mBTAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            BTMODULEUUID);
                    //tmp = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
//                } else {
//                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
//                            NAME_INSECURE, MY_UUID_INSECURE);
                }
          }
            catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = AppServiceController.getInstance().mHandler.obtainMessage(BluetoothService.MessageConstants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothService.MessageConstants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        AppServiceController.getInstance().mHandler.sendMessage(msg);

        mState = STATE_NONE;
        // Update UI title
//        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        BluetoothService.this.startService();
    }
}
