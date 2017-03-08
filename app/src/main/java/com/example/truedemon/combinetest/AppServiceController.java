package com.example.truedemon.combinetest;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import java.math.BigInteger;

/**
 * Created by truedemon on 2/3/2017.
 */

public class AppServiceController extends Application {
    private String input;
    private Context mContext;
    private static AppServiceController mInstance;
    private BluetoothService mService; //= new BluetoothService(mInstance);
//    Intent intent = new Intent(this, BluetoothService.class);
    Intent mIntent;
    public static synchronized AppServiceController getInstance() {
        return mInstance;
    }


    private MessageDecoder m_messageDecoder;
    @Override
    public void onCreate() {
//        mContext = this;
        mInstance = this;
        mService = new BluetoothService();
        m_messageDecoder = new MessageDecoder();
        // TODO Auto-generated method stub
        startService();
        super.onCreate();
    }

    /**
     * The Handler that gets information
     */
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AppServiceController activity = AppServiceController.this;
            //setSupportActionBar(MainActivity);
            // getSupportActionBar().setDisplayShowHomeEnabled(true);
            switch (msg.what) {
                case BluetoothService.MessageConstants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            input="Successfully connected";
                            //BluetoothCommActivity.mBluetoothStatus.setText("Successfully connected!");
                            //mBluetoothStatus.setText("Successfully connected to: "+ BluetoothService.mConnectedDeviceName);
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            input="Connection in progress";
                            //BluetoothCommActivity.mBluetoothStatus.setText("Connection in progress");
                            break;
                        ///case STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            input="Disconnected";
                            //BluetoothCommActivity.mBluetoothStatus.setText("Disconnected!");
                            break;
                    }
                    break;
                case BluetoothService.MessageConstants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    input=writeMessage;
                    BluetoothCommActivity.mConversationArrayAdapter.add("Me:  " + writeMessage);
                    BluetoothCommActivity.mConversationArrayAdapter.notifyDataSetChanged();
                    break;
                case BluetoothService.MessageConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    System.out.println(readMessage);
                    BluetoothCommActivity.mConversationArrayAdapter.add(mService.mConnectedDeviceName+ ":  " + readMessage);
                    BluetoothCommActivity.mConversationArrayAdapter.notifyDataSetChanged();
                    //Toast.makeText(getApplicationContext(),readMessage, Toast.LENGTH_SHORT).show();
                    input=readMessage;

                    if(readMessage.contains("status")){
                        System.out.println(readMessage);
                        //MainActivity.getInstance().updateRobotStatus(readMessage);
                        if(readMessage.contains("exploring")){
                            MainActivity.statusMesg="exploring";
                            MainActivity.getInstance().updateRobotStatus("exploring");
                        }
//                            mHandler.postDelayed(new Runnable()
//                            {
//                                public void run()
//                                {
//
// MainActivity.getInstance().updateRobotStatus("exploring");// Actions to do after 3 seconds
//                                }
//                            }, 800);
//
//                        }
                        if(readMessage.contains("fastest path")){
                            MainActivity.statusMesg="fastest path";
                            //MainActivity.getInstance().updateRobotStatus("fastest path");
                            MainActivity.getInstance().updateRobotStatus("fastest path");
                        }
                        if(readMessage.contains("turning left")){
                            MainActivity.statusMesg="turning left";
                            //MainActivity.getInstance().updateRobotStatus("turning left");
                            MainActivity.getInstance().updateRobotStatus("turning left");
                        }
                        if(readMessage.contains("turning right")){
                            MainActivity.statusMesg="turning right";
                            //MainActivity.getInstance().updateRobotStatus("turning right");
                            MainActivity.getInstance().updateRobotStatus("TURNING RIGHT");
                        }
                        if(readMessage.contains("moving forward")){
                            MainActivity.statusMesg="moving forward";
                            //MainActivity.getInstance().updateRobotStatus("moving forward");
                            MainActivity.getInstance().updateRobotStatus("MOVING FORWARD");
                        }
                        if(readMessage.contains("reversing")){
                            MainActivity.statusMesg="reversing";
                            //MainActivity.getInstance().updateRobotStatus("reversing");
                            MainActivity.getInstance().updateRobotStatus("REVERSING");
                        }




                    }

                    else{
                        //KY Messenge decoder
                        if(readMessage.length() == 77 && readMessage.charAt(1) == '1')
                        {
                            System.out.println("Map Desc 1 :");
                            m_messageDecoder.getMapDescriptor1(readMessage);
                        }
                        else if(readMessage.length() == 77 && readMessage.charAt(1) == '2')
                        {
                            System.out.println("Map Desc 2 :");
                            m_messageDecoder.getMapDescriptor2(readMessage);
                        }
                        else if(readMessage.charAt(1) == 'r')
                        {
                            System.out.println("getCoords :");
                            m_messageDecoder.getCoords(readMessage);
                        }
                        else if(readMessage.charAt(1) == '1' && readMessage.charAt(2) == 'f' && readMessage.length() == 78)
                        {
                            System.out.println("Map Desc 1 to print :");
                            String str_decoded = m_messageDecoder.getMD1toPrint(readMessage);
                            BluetoothCommActivity.mConversationArrayAdapter.add("MD1toPrint:  " + str_decoded);
                            BluetoothCommActivity.mConversationArrayAdapter.notifyDataSetChanged();
                        }
                        else if(readMessage.charAt(1) == '2' && readMessage.charAt(2) == 'f' && readMessage.length() == 78)
                        {
                            System.out.println("Map Desc 2 to print :");
                            m_messageDecoder.getMD2toPrint(readMessage);
                            String str_decoded = m_messageDecoder.getMD1toPrint(readMessage);
                            BluetoothCommActivity.mConversationArrayAdapter.add("MD2toPrint:  " + str_decoded);
                            BluetoothCommActivity.mConversationArrayAdapter.notifyDataSetChanged();
                        }
                        if(readMessage.length()==75)
                        {
                            String mapData = toBinary(readMessage);
                            System.out.println(mapData);
                        }
                    }
                    break;
                case BluetoothService.MessageConstants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                   mService.mConnectedDeviceName = msg.getData().getString(BluetoothCommActivity.MessageConstants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to ", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BluetoothService.MessageConstants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(BluetoothService.MessageConstants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
//                case MessageConstants.MESSAGE_CONTROL:
//                    byte[] controlBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String controlMessage = new String(controlBuf);
//                    mConversationArrayAdapter.add("Me:  " + controlMessage);
//                    mConversationArrayAdapter.notifyDataSetChanged();
//                    break;
            }
        }
    };

    public void startService(){
        //start your service

        mService.startService();
        mIntent = new Intent(this, BluetoothService.class);
        bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
    }
    public void stopService(){
        //stop service
    }

    public String toBinary(String hex) {
        return new BigInteger("1" + hex, 16).toString(2).substring(1);
    }
    public String getMessage(){
        return input;
    }
    public BluetoothService getService(){
        return mService;
    }
    private boolean mBound = false;
    private static final String TAG = "Batman service controller";
    public MessageDecoder getMessageDecode()
    {
        return m_messageDecoder;
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            BluetoothService.BTBinder binder = (BluetoothService.BTBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
//            Log.e(TAG, "onServiceDCed");
            mBound = false;
        }
    };
}
