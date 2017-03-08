package com.example.truedemon.combinetest;


//import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by truedemon on 6/3/2017.
 */

public class BluetoothCommActivity extends AppCompatActivity{

    // GUI Components
    public final static String EXTRA_MESSAGE = "com.example.shaun.MESSAGE";
    //BluetoothAdapter mBluetoothAdapter;
    public TextView mBluetoothStatus;
    private ListView mDevicesListView;
    private ListView mChatListView;
    private static final String TAG = "BluetoothService";
    private String mConnectedDeviceName = null;
    //Array for storing stuff
    public ArrayAdapter<String> mBTArrayAdapter;
    public static ArrayAdapter<String> mConversationArrayAdapter;
    //    public BluetoothService BTServ = new BluetoothService();
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;
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

    private BluetoothService BTServ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        BTServ = AppServiceController.getInstance().getService();// <--added


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        //mConversationArrayAdapter.add("");
        //mNewState= mState;
        //mOnBtn = (Button) findViewById(R.id.on);
        mBluetoothStatus = (TextView)findViewById(R.id.bluetoothStatus);
        mConversationArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        mDevicesListView = (ListView)findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);
        mChatListView = (ListView)findViewById(R.id.chatView);
        mChatListView.setAdapter(mConversationArrayAdapter);
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        //if (mBluetoothAdapter.isEnabled()) {
        //    mBluetoothStatus.setText("Bluetooth ON");
        // }
        // else {
        //    mBluetoothStatus.setText("Bluetooth OFF");
        // }
    }
    @Override
    public void onStart() {
        super.onStart();

    }

    /**
     * The Handler that gets information
     */
//    public final Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            AppCompatActivity activity = BluetoothUI.this;
//            //setSupportActionBar(MainActivity);
//            // getSupportActionBar().setDisplayShowHomeEnabled(true);
//            switch (msg.what) {
//                case MessageConstants.MESSAGE_STATE_CHANGE:
//                    switch (msg.arg1) {
//                        case STATE_CONNECTED:
//                            mBluetoothStatus.setText("Successfully connected to: "+ mConnectedDeviceName);
//                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
//                            mConversationArrayAdapter.clear();
//                            break;
//                        case STATE_CONNECTING:
//                            mBluetoothStatus.setText("Connection in progress");
//                            break;
//                        ///case STATE_LISTEN:
//                        case STATE_NONE:
//                            mBluetoothStatus.setText("Disconnected!");
//                            break;
//                    }
//                    break;
//                case MessageConstants.MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String writeMessage = new String(writeBuf);
//                    mConversationArrayAdapter.add("Me:  " + writeMessage);
//                    mConversationArrayAdapter.notifyDataSetChanged();
//                    break;
//                case MessageConstants.MESSAGE_READ:
//                    byte[] readBuf = (byte[]) msg.obj;
//                    // construct a string from the valid bytes in the buffer
//                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    mConversationArrayAdapter.add(mConnectedDeviceName+ ":  " + readMessage);
//                    mConversationArrayAdapter.notifyDataSetChanged();
//                    Log.d(TAG, readMessage);
//                    Toast.makeText(getApplicationContext(),readMessage, Toast.LENGTH_SHORT).show();
//                    break;
//                case MessageConstants.MESSAGE_DEVICE_NAME:
//                    // save the connected device's name
//                    mConnectedDeviceName = msg.getData().getString(MessageConstants.DEVICE_NAME);
//                    if (null != activity) {
//                        Toast.makeText(activity, "Connected to "
//                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                    }
//                    break;
//                case MessageConstants.MESSAGE_TOAST:
//                    if (null != activity) {
//                        Toast.makeText(activity, msg.getData().getString(MessageConstants.TOAST),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                    break;
//                case MessageConstants.MESSAGE_CONTROL:
//                    byte[] controlBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String controlMessage = new String(controlBuf);
//                    mConversationArrayAdapter.add("Me:  " + controlMessage);
//                    mConversationArrayAdapter.notifyDataSetChanged();
////                    break;
//            }
//        }
//    };

    //Methods
    //Bluetooth on clicked
    public void onBT(View view) {
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getApplicationContext(),"Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            if(mBluetoothAdapter.isEnabled()) {
                mBluetoothStatus.setText("Bluetooth On");
                Toast.makeText(getApplicationContext(), "Bluetooth turned On", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //Bluetooth off clicked
    public void offBT(View view) {
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getApplicationContext(),"Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            mBluetoothStatus.setText("Bluetooth Off");
        }
    }

    //Show paired devices button action
    public void pairedBT(View view) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(mBluetoothAdapter.isEnabled()) {
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    mBTArrayAdapter.add(deviceName + "\n" + deviceHardwareAddress);
                    mBTArrayAdapter.notifyDataSetChanged();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "No paired devices", Toast.LENGTH_SHORT).show();
            }

        }
        else{
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
        }
    }

    //Discover devices button action
    public void discoverBT(View view) {
// Check if the device is already discovering
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBluetoothAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBluetoothAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                mBTArrayAdapter.add(deviceName + "\n" + deviceHardwareAddress);
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if(!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            // Get the device MAC address, which is the last 17 chars in the View
            final String name = info.substring(0,info.length() - 17);
            System.out.print("d");
            System.out.print(address);
            BTServ.setBTState(BluetoothService.STATE_CONNECTING);
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            Toast.makeText(getBaseContext(), address, Toast.LENGTH_LONG).show();
            mBluetoothStatus.setText("Connecting to:"+device.getName());
            BTServ.connect(device);
            if (BTServ.STATE_CONNECTED==STATE_CONNECTED) {
                mBluetoothStatus.setText("Connected to: "+device.getName());
            }
        }
    };


    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        // Check that there's actually something to send
        //Intent intent = new Intent(this, DisplayMessageActivity.class);
        byte[] send = null;
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            send = message.getBytes();
            BTServ.write(send);
            //mChatService.write(send);
        }
    }


    public void controller(View view) {
//        //RobotControl RC = new RobotControl();
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
        finish();
    }

//        intent.putExtra(EXTRA_MESSAGE, message);
//        startActivity(intent);


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.activity_main, menu);
//        return true;
//    }


}