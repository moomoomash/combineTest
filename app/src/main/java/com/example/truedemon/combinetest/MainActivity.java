package com.example.truedemon.combinetest;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.support.constraint.solver.widgets.Rectangle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;

import java.util.Timer;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    // Buttons
    private Button buttonAutoView;
    private Button buttonManualView;
    private Button left;
    private Button right;
    private Button forward;
    private Button backward;
    private Button start;
    private Button sendStart;
    private Button F1;
    private Button F2;
    private Button F3;
    private Button F4;
    private Button F5;
    private Button F6;
    private Button turnLeft;
    private Button turnRight;
    private Button fast;
//    private Button explore;

    ActionBar actionBar;
    private TextView timerValue;

    private ToggleButton auto_or_manual;
    private ToggleButton explore_btn;
    private Button update;
    private GridView view1;
    private RelativeLayout gridLayout;
    private RelativeLayout mainLayout;
    private TextView feedbackTextView;
    private Switch slidingSwitch;

    //for timer
    private long startTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;




    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 150;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    public static String statusMesg="";
    public TextView mBTStatus;
    public TextView mRobotStatus;
    //public ArrayAdapter<String> mRobotStatusAdapter;
//    private Button button_bt_settings;
    private BluetoothService mService;
    private boolean mBound;
    public String control="n";
    private static final String TAG = "BATMAN";
    private BluetoothCommActivity BTui;
    Timer timer=new Timer();
    public static MainActivity mInstance;
    public static MainActivity getInstance() {
        return mInstance;
    }

    SurfaceView surfaceView;
    SurfaceHolder sf_Holder;
    //SurfaceHolder robotHolder;
    Bitmap robot;
    float p,q;

    private MessageDecoder m_message_decoder;
    private int [][] mapdescriptor3_2d;
    private int [] robot_coords;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.maze_fragment);

        //map
        surfaceView = (SurfaceView) findViewById(R.id.mygrid);
        robot = BitmapFactory.decodeResource(getResources(),R.drawable.rob);
        p = 0;
        q = 0;

        sf_Holder = surfaceView.getHolder();
        sf_Holder.addCallback(this);

        setupControl();

        BTui = new BluetoothCommActivity();
//        AppServiceController.getInstance().startService();
        mInstance = this;
        mRobotStatus = (TextView)findViewById(R.id.feedback);

        mapdescriptor3_2d = new int [20][15];
        robot_coords = new int [2];
        setupColor();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bluetoothsettings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.bluetooth_settings:
                startBTServ();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public String getControl(){
        return control;
        //ctrlMsg(control);
    }

    public void setupControl(){
        // Turn left
        left = (Button) findViewById(R.id.ButtonLeft);
        left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //String msg = "a";
                //sendMessage(msg);
                //outputFeedback("MovingL");
                control="sl";
                sendControl(control);
                //endControl(control);
            }
        });

        // Turn Right
        right = (Button) findViewById(R.id.ButtonRight);
        right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                control="sr";
                sendControl(control);
                //sendControl(control);
                //String msg = "d";
                //sendMessage(msg);
                //outputFeedback("MovingR");

            }
        });

        // Turn Back
        backward = (Button) findViewById(R.id.ButtonDown);

        backward.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                control="ms10";
                sendControl(control);
                //sendControl(control);
                //String msg = "s";
                //sendMessage(msg);
                //outputFeedback("MovingB");
                // autoSend = false;
            }
        });

        // Go Forward
        forward = (Button) findViewById(R.id.ButtonUP);
        forward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                control="mw10";
                sendControl(control);
                //String msg = "w";
                //sendMessage(msg);
                //("MovingF");
                //sendControl(control);

            }

        });

        //Rotate Left
        turnLeft = (Button) findViewById(R.id.ButtonRotateLeft);
        turnLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                control="ma";
                sendControl(control);;

            }

        });

        //Rotate Left
        turnRight = (Button) findViewById(R.id.ButtonRotateRight);
        turnRight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                control="mr";
                sendControl(control);;

            }

        });

        //Rotate Left
        fast = (Button) findViewById(R.id.Buttonfastestpath);
        fast.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                control="fastest";
                sendControl(control);;

            }

        });

        sendStart = (Button) findViewById(R.id.sendStart);
        sendStart.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //String msg = getData().get("Start");
                //sendMessage(msg);
//                outputFeedback("Start Coordinates sent");

            }
        });

        // Button F1

        F1 = (Button) findViewById(R.id.ButtonF1);
        F1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //String msg = getData().get("F1");
                //sendMessage(msg);
                //outputFeedback(msg);
//                outputFeedback("Button F1");

            }
        });

        // Button F2
        F2 = (Button) findViewById(R.id.ButtonF2);
        F2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //String msg = getData().get("F2");
                //sendMessage(msg);
                //outputFeedback(msg);
//                outputFeedback("Button F1");
            }
        });

        // Button F3
        F3 = (Button) findViewById(R.id.ButtonF3);
        F3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //String msg = getData().get("F3");
                //sendMessage(msg);
                //outputFeedback(msg);
//                outputFeedback("Button F3");
            }
        });

        // Button F4
        F4 = (Button) findViewById(R.id.ButtonF4);
        F4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //String msg = getData().get("F4");
                //sendMessage(msg);
                //outputFeedback(msg);
//                outputFeedback("Button F4");
            }
        });
        // Button F5
        F5 = (Button) findViewById(R.id.ButtonF5);
        F5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //String msg = getData().get("F5");
                //sendMessage(msg);
                //outputFeedback(msg);
//                outputFeedback("Button F5");

            }
        });

        // Button F6
        F6 = (Button) findViewById(R.id.ButtonF6);
        F6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //String msg = getData().get("F6");
                //sendMessage(msg);
                //outputFeedback(msg);
//                outputFeedback("Button F6");

            }
        });
// auto_or_manual button
        auto_or_manual = (ToggleButton) findViewById(R.id.gridManual_Auto);

        auto_or_manual.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                // Changes text when first click on manual button
                if (auto_or_manual.isChecked()) {
                    update.setClickable(false);
                    update.setEnabled(false);
                    update.setVisibility(View.INVISIBLE);

                    // Sends request to continuous return feedbacks on grid
                    //sendMessage("ko");
//                    outputFeedback("Auto");
                } else {
                    // in auto mode
                    auto_or_manual.setText("Manual Mode");
                    update.setClickable(true);
                    update.setEnabled(true);
                    update.setVisibility(View.VISIBLE);
//                    outputFeedback("Manual");
                    // Sends request to turn off continuous feedback of grid
                    //sendMessage("kf");
                }
            }
        });

        // auto_or_manual button
        explore_btn = (ToggleButton) findViewById(R.id.explore);

        explore_btn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                // Changes text when first click on manual button
                if (explore_btn.isChecked()) {
                    explore_btn.setText("Stop Explore");
                    // Sends request to continuous return feedbacks on grid

                    // reset time to 0
                    timeSwapBuff = 0L;
                    timeInMilliseconds = 0L;
                    updatedTime = 0L;
                    startTime = 0L;

                    customHandler.removeCallbacks(updateTimerThread);
                    resetTimer();

                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);

                    //sendMessage("Started Explore");
//                    outputFeedback("Explore started");
//                    updateRobotStatus("Explore started");
                } else {
                    // in explore not started
                    timeSwapBuff += timeInMilliseconds;
                    customHandler.removeCallbacks(updateTimerThread);
                    explore_btn.setText("Explore");

                    // Sends request to turn off continuous feedback of grid
                    //sendMessage("Stopped Explore");
//                    outputFeedback("Explore ended");
                }
            }
        });
        final ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher1);

        Animation slide_in_left = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        Animation slide_out_right = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);

        viewSwitcher.setInAnimation(slide_in_left);
        viewSwitcher.setOutAnimation(slide_out_right);

        // btnAutoMode

        buttonAutoView = (Button) findViewById(R.id.btnVSauto);
        buttonAutoView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                viewSwitcher.showPrevious();
            }
        });
        // btnManualMode
        buttonManualView = (Button) findViewById(R.id.btnVSmanual);
        buttonManualView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                viewSwitcher.showNext();
            }
        });
        update = (Button) findViewById(R.id.gridUpdate);
        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //String msg = getData().get("F5");
                //sendMessage(msg);
                //outputFeedback(msg);
//                outputFeedback("Button F5");

                //get decode message
                //draw map
//                AppServiceController.getInstance().getMessageDecode().
                m_message_decoder.getMapDescriptor3();
                mapdescriptor3_2d = m_message_decoder.getMD3Matrix();
                robot_coords = m_message_decoder.getCoordinates();
                if (mapdescriptor3_2d !=null)
                    drawMaze(sf_Holder);

            }
        });
    }


    private void outputFeedback(String readMessage) {
        feedbackTextView = (TextView) findViewById(R.id.feedback);
        feedbackTextView.setText(readMessage);
    }
    public void sendControl(String input){
        if (input.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            //this.write(send);
//            BTui.write(input.getBytes());
//            btServ.write("lolll".getBytes());
            mService.write(input.getBytes());
        }

    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            timerValue.setText("" + mins + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            customHandler.postDelayed(this, 0);
        }

    };

    private void resetTimer(){
        // reset time to 0
        timeSwapBuff =0L;
        timeInMilliseconds=0L;
        updatedTime = 0L;
        startTime = 0L;

        customHandler.removeCallbacks(updateTimerThread);

        int secs = (int) (updatedTime / 1000);
        int mins = secs / 60;
        secs = secs % 60;
        int milliseconds = (int) (updatedTime % 1000);

        timerValue.setText("" + mins + ":" + String.format("%02d", secs)
                + ":" + String.format("%03d", milliseconds));
    }

    public void sendStartCoordinate(int input){
        if (input!=-1) {
            // Get the message bytes and tell the BluetoothChatService to write
            //this.write(send);
//            BTui.write(input.getBytes());
//            btServ.write("lolll".getBytes());
            //mService.write(input.toBytes());
//            byte[] b;
//            b[0]=input;
//            input=b[0];
//            Byte b = new Byte(input);
//            int i = b.intValue();

            //mService.write(b.getBytes());
        }

    }

    @Override
    public void onStart(){
        super.onStart();
//        Intent intent = new Intent(this, MyService.class);
//        startService(intent);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        mService = AppServiceController.getInstance().getService();
        m_message_decoder = AppServiceController.getInstance().getMessageDecode();
//        if (mService.getBTState()==mService.STATE_CONNECTED)
//        {
//            mBTStatus = (TextView)findViewById(R.id.btStatus);
//            mBTStatus.setText("Connected to: "+mService.mConnectedDeviceName);
//        }
    }
    //    private ServiceConnection mConnection = new ServiceConnection() {
//        // Called when the connection with the service is established
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            // Because we have bound to an explicit
//            // service that is running in our own process, we can
//            // cast its IBinder to a concrete class and directly access it.
//            MyService.BTBinder binder = (MyService.BTBinder) service;
//            mService = binder.getService();
//            mBound = true;
//        }
//
//        // Called when the connection with the service disconnects unexpectedly
//        public void onServiceDisconnected(ComponentName className) {
//            Log.e(TAG, "onServiceDisconnected");
//            mBound = false;
//        }
//    };
//    public void startBTServ(View view){
//        //BluetoothService BTServ = new BluetoothService();
////        Intent intent = new Intent(this, BluetoothService.class);
////        startActivity(intent);
////        BTui.start()
//        Intent intent = new Intent(this, BluetoothService.class);
//        startActivity(intent);
//    }
//    public void startBTServ(View view){
        //BluetoothService BTServ = new BluetoothService();
//        Intent intent = new Intent(this, BluetoothService.class);
//        startActivity(intent);
//        Intent bt_Screen = new Intent(getApplicationContext(), BluetoothCommActivity.class);
    public void startBTServ(){
        Intent intent = new Intent(this, BluetoothCommActivity.class);
        startActivity(intent);
//        Intent intent = new Intent(this, BluetoothService.class);
//        startActivity(intent);
    }

    public synchronized void updateRobotStatus(String toThis) {
        System.out.println(toThis);
        mRobotStatus.invalidate();
        mRobotStatus.setText(statusMesg);
    }


    // DRAWING MAP
    private int origin_X = 1;
    private int origin_Y = 1;

    private Paint unexplored_color = new Paint();
    private Paint explored_color = new Paint();
    private Paint blocked_color = new Paint();
    private Paint robot_color=new Paint();
    private Paint start_box_color = new Paint();
    private Paint end_box_color = new Paint();

    private static final int SURFACE_CANVAS_LEFT = 200; //200
    private static final int SURFACE_CANVAS_UP = 480;  //385
    private static final int SURFACE_CANVAS_RIGHT = 220; //215
    private static final int SURFACE_CANVAS_DOWN = 500; //400

    public void setupColor()
    {
        unexplored_color=new Paint();
        unexplored_color.setColor(Color.GRAY);

        explored_color = new Paint();
        explored_color.setColor(Color.GREEN);
        explored_color.setAlpha(80);

        blocked_color = new Paint();
        blocked_color.setColor(Color.RED);

        robot_color=new Paint();
        robot_color.setColor(Color.BLUE);

        start_box_color = new Paint();
        start_box_color.setColor(Color.YELLOW);
        start_box_color.setAlpha(80);

        end_box_color = new Paint();
        end_box_color.setColor(Color.MAGENTA);
        end_box_color.setAlpha(75);
    }


    @Override
    public synchronized void surfaceCreated(SurfaceHolder surfaceHolder) {
        Canvas c = surfaceHolder.lockCanvas();

        int sleft=SURFACE_CANVAS_LEFT; //0
        int sup=SURFACE_CANVAS_UP;  //0
        int sright=SURFACE_CANVAS_RIGHT; //30
        int sdown=SURFACE_CANVAS_DOWN; //30
//        setupColor();
        c.drawColor(Color.WHITE);

        for (int i=0;i<20;i++){   //20
            for (int j=0;j<15;j++) //15
            {
                c.drawRect(sleft,sup,sright,sdown,unexplored_color);
                sright=sright+25; //35
                sleft=sleft+25;  //35
            }
            sleft=200;  //200
            sright=220; //215
            sup-=25; //20
            sdown=sup+20; //315
        }
        //c.drawRect(robotPositionOnMap(6,9),robot);
        c.drawRect(robotPositionOnMap(1,1),start_box_color);
        c.drawRect(robotPositionOnMap(18,13),end_box_color);

        surfaceHolder.unlockCanvasAndPost(c);
    }

    public synchronized void drawMaze(SurfaceHolder surfaceHolder)
    {
        int sleft=SURFACE_CANVAS_LEFT; //0
        int sup=SURFACE_CANVAS_UP;  //0
        int sright=SURFACE_CANVAS_RIGHT; //30
        int sdown=SURFACE_CANVAS_DOWN; //30

//        surfaceCreated(surfaceHolder);

        Canvas c = surfaceHolder.lockCanvas();
//        setupColor();
        c.drawColor(Color.WHITE);
        robot_coords = AppServiceController.getInstance().getMessageDecode().getCoordinates();
        for (int i=0;i<20;i++){   //20
            for (int j=0;j<15;j++) //15
            {
                if (mapdescriptor3_2d[i][j]==1)
                    c.drawRect(sleft,sup,sright,sdown,explored_color);
                else if (mapdescriptor3_2d[i][j]==2)
                    c.drawRect(sleft,sup,sright,sdown,blocked_color);
                else if (mapdescriptor3_2d[i][j] == 0)
                {
                    System.out.print(mapdescriptor3_2d[i][j]);
                    c.drawRect(sleft,sup,sright,sdown,unexplored_color);
                }
                sright=sright+25; //35
                sleft=sleft+25;  //35
            }
            sleft=200;  //200
            sright=220; //215
            sup-=25; //20
            sdown=sup+20; //315
        }
        c.drawRect(robotPositionOnMap(1,1),start_box_color);
        c.drawRect(robotPositionOnMap(18,13),end_box_color);
        //robotPositionOnMap(18,10);

        Log.d("drawMaze","Y:"+Integer.toString(robot_coords[0])+", X:"+Integer.toString(robot_coords[1]));

        if (robot_coords[0]!=0 && robot_coords[0]!=0)
            c.drawRect(robotPositionOnMap(robot_coords[0],robot_coords[1]),robot_color);
        surfaceHolder.unlockCanvasAndPost(c);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    public Rect robotPositionOnMap(int yCell, int xCell)
    {
        int left=200; //200
        int right = left + 70;
        int down=500; //400
        int up = down -70;

        int rLeft = left + (xCell-1)*25;
        int rRight = right + (xCell-1)*25;
        int rDown = down - (yCell-1)*25;
        int rUp = up-(yCell-1)*25;

        return new Rect(rLeft,rUp,rRight,rDown);
    }

}
