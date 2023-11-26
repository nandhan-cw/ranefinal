package com.steering.testrane;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static com.steering.testrane.SteeringVariables.angle_value;
import static com.steering.testrane.SteeringVariables.current_value;
import static com.steering.testrane.SteeringVariables.ecu_value;
import static com.steering.testrane.SteeringVariables.listOfByteArrays;
import static com.steering.testrane.SteeringVariables.listOfStringReceive;
import static com.steering.testrane.SteeringVariables.motor_value;
import static com.steering.testrane.SteeringVariables.r_value;
import static com.steering.testrane.SteeringVariables.steeringStatus;
import static com.steering.testrane.SteeringVariables.torque_value;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HomeFragment extends Fragment {
    ImageView steeringwheel;
    static LinearLayout bltbtn;
    ImageView leftkey;
    ImageView rightkey,lockicon,bltbtnimg;
    ImageView volume;
    static ImageView lwheel;
    static ImageView rwheel;
    static ImageView wheelL;
    static ImageView wheelR;
    BluetoothDevice device;
    TextView angletext;
    static TextView connectStatus;
    ProgressDialog progressDialog;
    Dialog blueToothListPopup;
    MediaPlayer mediaPlayer;
    private float initialTouchAngle = 0f;
    private static float currentRotationAngle = 0f;
    private float currentRotationAngle1 = 0f;
    private static final int TOUCH_SENSITIVITY_THRESHOLD = 10; // Touch sensitivity threshold
    private static final float ROTATION_STEP = 10f; // Rotation step in degrees
    private static final float ROTATION_STEP1 = 1f; // Rotation step in degrees
    private Set<Float> angleSet = new HashSet<>();

    ListView listView;
    static BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;


    static InputStream inputStream;
    static OutputStream outputStream;
    StringBuilder sbb = new StringBuilder();
    static Button write;


    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    static final int STATE_CANCELLED = 7;
    int MY_BLUETOOTH_PERMISSION_REQUEST = 1;

    int REQUEST_ENABLE_BLUETOOTH = 1;
    private static float MAX_ROTATION_ANGLE;
    private int previousVolumeLevel;
    private static final String APP_NAME = "BTChat";
    private static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    int divisor = 2; // Initial divisor
    float startAngle = 90f; // Initial start angle
    static ImageView l1wheel;
    static ImageView r1wheel;
    static ImageView l2wheel;
    static ImageView r2wheel;
    private boolean isAutoRotationEnabled = false;
    float touchAngle;
    ArrayList<Float> anglelist;
    static ArrayList<Float> uniqueAnglesSetSendVal;
    Set<Float> uniqueAnglesSet;
    static RelativeLayout carbody;
    static RelativeLayout truckbody;
    static RelativeLayout tractorbody;
    private Handler rotationHandler = new Handler();
    private static final long VIBRATION_DURATION = 100;
    boolean isRotationInProgress = false;
    private static final long ROTATION_DELAY = 2000;
    SharedPreferences sharedPreferences;
    private static float initial_current1=0f;
    byte[] datainitial = SteeringVariables.data5; // 2-byte array representing a 16-bit integer
    float floatValue;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        steeringwheel = view.findViewById(R.id.steeringwheel);

        SteeringVariables.currentFragment = "home";
        SteeringVariables.home_thread_flag = true;
        SteeringVariables.status_thread_flag = false;

        angletext = view.findViewById(R.id.angletext);
        connectStatus = view.findViewById(R.id.connectStatus);
        bltbtn = view.findViewById(R.id.bltbtn);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        SteeringVariables.bluetoothAdapter = bluetoothAdapter;
        leftkey = view.findViewById(R.id.leftkey);
        rightkey = view.findViewById(R.id.rightkey);
        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.indicator); // R.raw.audio is the reference to your audio file
        mediaPlayer.setLooping(true);
        volume = view.findViewById(R.id.volume);
        lwheel = view.findViewById(R.id.lwheel);
        rwheel = view.findViewById(R.id.rwheel);
        write = view.findViewById(R.id.write);
        lockicon = view.findViewById(R.id.lockicon);
        bltbtnimg = view.findViewById(R.id.bltbtnimg);


        final AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        Log.d("dataa", angle_value);
        MAX_ROTATION_ANGLE = Float.parseFloat(SteeringVariables.max_angle);
        sharedPreferences = getActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SteeringVariables.steeringStatus = sharedPreferences.getString("steeringstatus", "not_locked");
        SteeringVariables.max_angle = sharedPreferences.getString("max_angle", "0");

//        SteeringVariables.home_thread_flag= true;
//        SteeringVariables.status_thread_flag=false;

        l1wheel = view.findViewById(R.id.l1wheel);
        r1wheel = view.findViewById(R.id.r1wheel);
        l2wheel = view.findViewById(R.id.l2wheel);
        r2wheel = view.findViewById(R.id.r2wheel);
        Button write = view.findViewById(R.id.write);
        uniqueAnglesSet = new HashSet<>();
        uniqueAnglesSetSendVal = new ArrayList<>();
        touchAngle = 0f;
        carbody = view.findViewById(R.id.carbody);
        truckbody = view.findViewById(R.id.truckbody);
        tractorbody = view.findViewById(R.id.tractorbody);
        Handler handler = new Handler();
        anglelist = new ArrayList();


        short shortValue = (short) ((datainitial[0] & 0xFF) << 8 | (datainitial[1] & 0xFF));
        int decimalValue = (datainitial[0] & 0xFF) << 8 | (datainitial[1] & 0xFF);
        if (SteeringVariables.data3 == 0x00) {
            floatValue = (float) decimalValue;
        } else {
            floatValue = (float) (-decimalValue);
        }
        initialTouchAngle = floatValue;
        currentRotationAngle = floatValue;
        rotationAngleProcess();

//        if(SteeringVariables.steeringauto.equals("on")){
//            SteeringVariables.data5 = new byte[]{0x00,0x00};
//        }


        Log.d("value123",""+SteeringVariables.data5[0]+SteeringVariables.data5[1]);
        byte[] datainitial = SteeringVariables.data5; // 2-byte array representing a 16-bit integer
        float floatValue;
        // Convert little-endian 16-bit integer to float manually

//        byte[] datainitial = (byte) Integer.parseInt(byteStr, 16);
//        int decimalValue = (datainitial[0] & 0xFF) << 8 | (datainitial[1] & 0xFF);
//        if (SteeringVariables.data3 == 0x00) {
//            floatValue = (float) decimalValue;
//        } else {
//            floatValue = (float) (-decimalValue);
//        }
//        Log.d("check", "onCreateView: " + floatValue);

//        touchAngle = 0f;

        vehicleChange();

//        rotateLWheel(floatValue);
        Log.d("checksteer","3 "+SteeringVariables.steeringStatus);
        if (SteeringVariables.steeringStatus.equals("not_locked")) {
            steeringwheel.setEnabled(true);

            lockicon.setImageResource(R.drawable.lock2);
        } else {
            steeringwheel.setEnabled(false);
            lockicon.setImageResource(R.drawable.lock1);
        }


        if (SteeringVariables.bluetooth) {
            bltbtnimg.setImageResource(R.drawable.baseline_bluetooth_24);
            int blueColor = ContextCompat.getColor(getContext(), R.color.blue); // R.color.blue should be defined in your resources
            PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
            bltbtnimg.setColorFilter(blueColor, mode);
        }

//        angle_value

        Runnable rotateToZero = new Runnable() {
            @Override
            public void run() {
                if (currentRotationAngle > 0) {
                    currentRotationAngle -= ROTATION_STEP;
                    currentRotationAngle1 -= 1f;
                    currentRotationAngle = Math.max(0, currentRotationAngle); // Ensure it doesn't go below 0
                    currentRotationAngle1 = Math.max(0, currentRotationAngle1);
                    steeringwheel.setRotation(currentRotationAngle);
                    handler.postDelayed(this, ROTATION_DELAY); // Delay for ROTATION_DELAY milliseconds
                } else {
                    isRotationInProgress = false; // Rotation completed, set the flag to false
                }
            }
        };

        lockicon.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                Log.d("steeringCheck", "check status: " + steeringStatus.toString());
                if (SteeringVariables.steeringStatus.equals("not_locked")) {
//                    Toast.makeText(getActivity(), "if steering status", Toast.LENGTH_SHORT).show();
                    SteeringVariables.steeringStatus = "locked";
                    steeringwheel.setEnabled(false);
                    Toast.makeText(getContext(), "Locked", Toast.LENGTH_SHORT).show();
                    lockicon.setImageResource(R.drawable.lock1);
                    saveSteeringStatus("locked");
                } else {
//                    Toast.makeText(getActivity(), "else  steering status", Toast.LENGTH_SHORT).show();
                    SteeringVariables.steeringStatus = "not_locked";
                    lockicon.setImageResource(R.drawable.lock2);
                    steeringwheel.setEnabled(true);
                    Toast.makeText(getContext(), "Unlocked", Toast.LENGTH_SHORT).show();
                    saveSteeringStatus("not_locked");
                }
                startSendData();
//                }
            }

        });

        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput("config.txt", Context.MODE_PRIVATE));
                    outputStreamWriter.write(sbb.toString());
                    write.setText(sbb.toString());
                    outputStreamWriter.close();
                } catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }
            }
        });
        vehicleChange();

        Log.d("status", "onCreateView:status" + SteeringVariables.status_thread_flag.toString());


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH}, MY_BLUETOOTH_PERMISSION_REQUEST);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN}, 3);
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN}, 3);
        } else {
            // Permission already granted, perform Bluetooth operations here
        }


        // Check if Bluetooth is enabled and request to enable if not
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        SteeringVariables.bluetoothAdapter = bluetoothAdapter;
        if (SteeringVariables.bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(getContext(), "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
        } else {
            if (!SteeringVariables.bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }

        rightkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    leftkey.setEnabled(true);
                    rightkey.setImageResource(R.drawable.rightkey);
                    SteeringVariables.data8 = (byte) 0x00;
                    // Pause the audio if it's currently playing
                } else {
                    SteeringVariables.data8 = (byte) 0x55;
                    leftkey.setEnabled(false);
                    mediaPlayer.start();
                    rightkey.setImageResource(R.drawable.rightkey_light);// Start or resume the audio if it's paused
                }
            }
        });

        leftkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    rightkey.setEnabled(true);
                    leftkey.setImageResource(R.drawable.leftkey);
                    SteeringVariables.data8 = (byte) 0x00;
                    // Pause the audio if it's currently playing
                } else {
                    SteeringVariables.data8 = (byte) 0xaa;
                    mediaPlayer.start();
                    rightkey.setEnabled(false);
                    leftkey.setImageResource(R.drawable.leftkey_light);// Start or resume the audio if it's paused
                }
            }
        });

        if (SteeringVariables.bluetooth == true) {
            connectStatus.setText("Connected");
            Message message = Message.obtain();
            message.what = STATE_CONNECTED;
            handler.sendMessage(message);
        } else connectStatus.setText("Not Connected");

// Method to send message via Bluetooth

        // Inside the OnClickListener for your volume button (ImageView)
        volume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the AudioManager from the activity context
                AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

                // Check if the media volume is currently muted
                boolean isMute = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;

                if (isMute) {
                    // Unmute: Set the volume to the previous volume level (assuming it was not zero before muting)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            previousVolumeLevel, // Set it to the previous non-zero volume level
                            0); // Flags, 0 for no special behavior

                    // Update the volume button state (assuming you have separate images for mute and unmute states)
                    volume.setImageResource(R.drawable.baseline_volume_up_24);
                } else {
                    // Mute: Save the current volume level, then set the volume to 0
                    previousVolumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            0, // Volume level, 0 indicates mute
                            0); // Flags, 0 for no special behavior

                    // Update the volume button state (assuming you have separate images for mute and unmute states)
                    volume.setImageResource(R.drawable.baseline_volume_off_24);
                }
            }
        });

            steeringwheel.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (SteeringVariables.steeringStatus.equals("locked")) {
                        steeringwheel.setEnabled(false);
                    }
                    else {

                        steeringwheel.setEnabled(true);
                        float x = event.getX();
                        float y = event.getY();
                        touchAngle = calculateAngle(x, y);

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialTouchAngle = touchAngle;
                                uniqueAnglesSet.clear();// Clear the angle set when a new touch is initiated
                                break;

                            // Inside MotionEvent.ACTION_MOVE case:
                            // Define a Set to store unique angles
// Inside MotionEvent.ACTION_MOVE case:

                            case MotionEvent.ACTION_MOVE:
                                Log.d("touchangle",""+touchAngle);
//                                Log.d("checkinsert", "1");
                                rotationAngleProcess();

                                float vibrationIntensity = calculateVibrationIntensity(currentRotationAngle);
                                startVibration(Float.parseFloat(SteeringVariables.vibration));

                                break;


                            case MotionEvent.ACTION_UP:
                                SteeringVariables.release = true;
                                stopVibration();
                                angleSet.clear();
                                if ("on".equals(SteeringVariables.steeringauto) && !isRotationInProgress) {
//                                SteeringVariables.data5 = new byte[]{0x00, 0x00};
                                    // Enable auto rotation and start rotation after ROTATION_DELAY milliseconds
//                                    SteeringVariables.home_thread_flag = false;
                                    Log.d("checkvalue1", "ca: " + currentRotationAngle + " ta: " + touchAngle + " ita: " + initialTouchAngle);
                                    Float tempangle = touchAngle;
                                    Float tempia = initialTouchAngle;
                                    Float temoca = currentRotationAngle;
                                    final Float[] tempca = {currentRotationAngle};
                                    float rotationAngleDiff = tempangle - tempia;
                                    // Check if the rotation step is greater than the threshold
                                    if (Math.abs(rotationAngleDiff) >= TOUCH_SENSITIVITY_THRESHOLD) {
                                        temoca += (rotationAngleDiff > 0) ? ROTATION_STEP : -ROTATION_STEP;
                                        temoca = Math.min(MAX_ROTATION_ANGLE, Math.max(-MAX_ROTATION_ANGLE, temoca));
                                    }
                                    Log.d("checkvalue:", "value of new angle: " + temoca + " " + tempangle + " " + tempia);
                                    Float finalTemoca = temoca;

                                    if(temoca<0){
                                        for(float i=temoca; i<=0;i++){
//                                            Log.d("unique: ",""+i+" "+uniqueAnglesSetSendVal.contains(i));
                                                uniqueAnglesSetSendVal.add(i);
                                        }
                                    }
                                    else{
                                        for(float i=temoca; i>=0;i--){
//                                                Log.d("unique: ",""+i);
                                                uniqueAnglesSetSendVal.add(i);
                                        }
                                    }
                                    initial_current1 = 0f;

                                    touchAngle = 0f;
                                    initialTouchAngle = 0f;
                                    currentRotationAngle = 0f;
                                    rotationHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            ObjectAnimator rotateAnimator1 = ObjectAnimator.ofFloat(wheelL, "rotation", wheelL.getRotation(), Float.parseFloat("0"));
                                            rotateAnimator1.setDuration(2000); // Set the duration for the rotation animation (in milliseconds)
                                            rotateAnimator1.start();
                                            ObjectAnimator rotateAnimator2 = ObjectAnimator.ofFloat(wheelR, "rotation", wheelR.getRotation(), Float.parseFloat("0"));
                                            rotateAnimator2.setDuration(2000); // Set the duration for the rotation animation (in milliseconds)
                                            rotateAnimator2.start();
                                            rotateSteeringWheel(0); // Rotate to 0 degrees
//                                        touchAngle = 0f;
                                        }
                                    }, ROTATION_DELAY);
                                    // Iterate through the list and send each value via Bluetooth

                                }
                                break;
                        }


                    }
                    return true;
                }
            });

            /// load status
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        if(listOfStringReceive.size()>0) {
                            String curr_value = listOfStringReceive.get(0);
                            String one = curr_value.substring(0, 2);
                            String two = curr_value.substring(2, 4);
                            String three = curr_value.substring(4, 6);
                            String four = curr_value.substring(6, 8);
                            String five = curr_value.substring(8, 10);
                            String six = curr_value.substring(10, 12);
                            String seven = curr_value.substring(12, 14);
                            String eight = curr_value.substring(14, 16);
                            String nine = curr_value.substring(16, 18);
                            String ten = curr_value.substring(18, 20);
                            String eleven = curr_value.substring(20, 22);
                            String twelve = curr_value.substring(22, 24);
                            String thirteen = curr_value.substring(24, 26);
                            String fourteen = curr_value.substring(26, 28);
                            if (one.equals("40") && thirteen.equals("0d") && fourteen.equals("0a")) {
                                if (five.equals("02")) {
                                    byte fourdata = (byte) Integer.parseInt(eight, 16);
                                    byte fivedata = (byte) Integer.parseInt(nine, 16);
                                    int decimalValue = (fourdata & 0xFF) << 8 | (fivedata & 0xFF);
                                    String temp = "";
                                    if (seven.equals("00")) {
                                        temp = "" + decimalValue;
//                                    astatus.setText(temp);
                                        SteeringVariables.angle_value = temp;
                                    } else if (seven.equals("01")) {
                                        temp = "-" + decimalValue;
//                                    astatus.setText(temp);
                                        SteeringVariables.angle_value = temp;
                                    }
                                }
                                if (five.equals("03")) {
                                    if (eight.toLowerCase().equals("3e")) {
                                        // change light to green in motor and ecu
//                                    mstatus.setText("ok");
//                                    mstatus.setTextColor(getResources().getColor(R.color.green));
//                                    mlight.setImageResource(R.drawable.grnbtn);
                                        motor_value = true;
                                    } else if (eight.toLowerCase().equals("7e")) {
                                        // change light to red in motor and ecu
//                                    mstatus.setText("err");
//                                    mstatus.setTextColor(getResources().getColor(R.color.red));
//                                    mlight.setImageResource(R.drawable.redbtn);
                                        motor_value = false;
                                    }
                                    if (nine.toLowerCase().equals("3e")) {
                                        // change light to green in motor and ecu
//                                    estatus.setText("ok");
//                                    estatus.setTextColor(getResources().getColor(R.color.green));
//                                    elight.setImageResource(R.drawable.grnbtn);
                                        ecu_value = true;
                                    } else if (nine.toLowerCase().equals("7e")) {
                                        // change light to red in motor and ecu
//                                    estatus.setText("err");
//                                    estatus.setTextColor(getResources().getColor(R.color.red));
//                                    elight.setImageResource(R.drawable.redbtn);
                                        ecu_value = false;
                                    }

                                }
                                if (five.equals("04")) {
//                                    Log.d("shibhu", "" + five + " " + eleven);
                                    if (eleven.equals("3e")) {
//                                    tstatus.setText("ok");
//                                    tstatus.setTextColor(getResources().getColor(R.color.green));
//                                    tlight.setImageResource(R.drawable.grnbtn);
                                        torque_value = true;
                                    } else if (eleven.equals("7e")) {
//                                    tstatus.setText("err");
//                                    tstatus.setTextColor(getResources().getColor(R.color.red));
//                                    tlight.setImageResource(R.drawable.redbtn);
                                        torque_value = false;
                                    }
                                }
                                if (five.equals("05")) {
//                                    Log.d("shibhu", "" + five + " " + eleven + " " + twelve);
                                    byte curent1 = (byte) Integer.parseInt(eleven, 16);
                                    byte curent2 = (byte) Integer.parseInt(twelve, 16);
                                    int decimalValue = (curent1 & 0xFF) << 8 | (curent2 & 0xFF);
                                    current_value = decimalValue+"";
//                                cstatus.setText(""+decimalValue);
                                }

                            }
                            listOfStringReceive.remove(0);
                            Thread.sleep(100);
                        }
                    }
                    catch (Exception e){
                        Log.d("Error","Can't load data");
                    }
                }
            }
        }).start();



        bltbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Socket close", "" + SteeringVariables.bluetooth);
                if (SteeringVariables.bluetooth) {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    bluetoothAdapter.disable();
                    bltbtnimg.setImageResource(R.drawable.baseline_bluetooth_disabled_24);
                    bltbtnimg.setColorFilter(ContextCompat.getColor(getContext(), R.color.grey_light), android.graphics.PorterDuff.Mode.MULTIPLY);
                    connectStatus.setText("Not Connected");
                    SteeringVariables.bluetooth = false;
                    //handler - send
                    Toast.makeText(getContext(), "Disonnected", Toast.LENGTH_SHORT);

                } else {
                    bluetoothAdapter.enable();
                    blueToothListPopup(getContext());

                }
            }
        });
        return view;

    }

    private void saveSteeringStatus(String status) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("steeringstatus", status);
        editor.apply();
    }

    private void startSendData(){

        if(SteeringVariables.steeringStatus.equals("locked")){
            SteeringVariables.data6=0x01;
        }
        else{
            SteeringVariables.data6=0x00;
        }
    }

    public void rotationAngleProcess() {

        float rotationAngleDiff = touchAngle - initialTouchAngle;
        if (Math.abs(rotationAngleDiff) >= TOUCH_SENSITIVITY_THRESHOLD) {
            // Calculate the new rotation angle
            currentRotationAngle += (rotationAngleDiff > 0) ? ROTATION_STEP : -ROTATION_STEP;
            // Ensure the rotation angle is within 360 degrees
            currentRotationAngle = Math.min(MAX_ROTATION_ANGLE, Math.max(-MAX_ROTATION_ANGLE, currentRotationAngle));
            steeringwheel.setRotation(currentRotationAngle);
            rotateLWheel(currentRotationAngle);
            // Update the angle TextView
            angletext.setText("Angles: " + uniqueAnglesSet.toString());
            if(currentRotationAngle<=0){
                if(initial_current1>currentRotationAngle) {
                    for (float i = initial_current1; i >= currentRotationAngle; i--) {
//                    Log.d("unique: ",""+i+" "+uniqueAnglesSetSendVal.contains(i));
                        uniqueAnglesSetSendVal.add(i);
                        Log.d("checkrotation", "" + i);

                    }
                }
                else if(initial_current1<currentRotationAngle) {
                    for (float i = initial_current1; i <= currentRotationAngle; i++) {
//                    Log.d("unique: ",""+i+" "+uniqueAnglesSetSendVal.contains(i));
                        uniqueAnglesSetSendVal.add(i);
                        Log.d("checkrotation", "" + i);
                    }
                }
            }
            else if(currentRotationAngle>=0){
                if(initial_current1<currentRotationAngle) {
                    for (float i = initial_current1; i <= currentRotationAngle; i++) {
                        uniqueAnglesSetSendVal.add(i);
                        Log.d("checkrotation", "" + i);
                    }
                }
                else if(initial_current1>currentRotationAngle) {
                    for (float i = initial_current1; i >= currentRotationAngle; i--) {
                        uniqueAnglesSetSendVal.add(i);
                        Log.d("checkrotation", "" + i);
                    }
                }
            }
            for(float i:uniqueAnglesSetSendVal){
//                Log.d("touchangle","angle "+i);
            }

//            byte[] formattedData = formatAndConvertData(currentRotationAngle);
            Log.d("touchangle","rotateangle "+uniqueAnglesSetSendVal.size());
            initial_current1 = currentRotationAngle;
            initialTouchAngle = touchAngle;
        }
    }

    private void stopVibration() {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            // Cancel ongoing vibrations
            vibrator.cancel();
        }
    }

    private void startVibration(float intensity) {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            // Calculate the vibration duration based on intensity (assuming VIBRATION_DURATION is the total duration)
            long vibrationDuration = Math.max(1, (long) (VIBRATION_DURATION * intensity));

            // Create a vibration pattern with the calculated duration
            long[] pattern = {0, vibrationDuration};
            // Vibrate with the pattern and default amplitude
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            }
        }

    }

    private void rotateSteeringWheel(float targetAngle) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(steeringwheel, "rotation", steeringwheel.getRotation(), targetAngle);
        rotateAnimator.setDuration(ROTATION_DELAY); // Set the duration for the rotation animation (in milliseconds)
        rotateAnimator.start();
        touchAngle = 0f;
    }

    private float calculateVibrationIntensity(float angle) {
        // Calculate intensity based on the rotation angle
        float maxAngle = MAX_ROTATION_ANGLE; // Assuming MAX_ROTATION_ANGLE is the maximum angle
        float minAngle = -MAX_ROTATION_ANGLE; // Assuming 0 is the minimum angle

        // If the angle is 0, vibrate with intensity 1 for 1 second
        if (angle == 0) {
            return 2f;
        }

        // If the angle is at its maximum, vibrate with intensity 1f for 1 second
        if (angle >= maxAngle) {
            return 2f;
        }
        if (angle == minAngle) {
            return 2f;
        }
        // For other angles, use a fixed intensity (adjust this value as needed)
        return 0.2f;
    }

    @Override
    public void onPause() {
        super.onPause();
        SteeringVariables.data8 = 0x00;
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private static byte[] formatAndConvertData(float angle) {
        int angleValue = (int) angle;
        if (angleValue < 0) {
            angleValue = Math.abs(angleValue);
            SteeringVariables.data3 = 0x01;
        } else {
            SteeringVariables.data3 = 0x00;
        }

        String hexAngle = Integer.toHexString(angleValue);


        if (angleValue <= 255) {
            byte[] byteArray = new byte[2];
            byteArray[0] = 0x00;
            byteArray[1] = (byte) Integer.parseInt(hexAngle, 16);
//            SteeringVariables.data5 = byteArray;
            return byteArray;
        }
        else {
            // If angle is more than 255, store it in a double byte array
            byte[] doubleByteArray = new byte[2];
            doubleByteArray[0] = (byte) Integer.parseInt(hexAngle.substring(0, 2), 16);
            doubleByteArray[1] = (byte) Integer.parseInt(hexAngle.substring(2), 16);
//            SteeringVariables.data5 = doubleByteArray;
            return doubleByteArray;
        }

    }

    public static void vehicleChange() {
        if (SteeringVariables.vehicle.equals("truck")) {
            wheelL = l1wheel;
            wheelR = r1wheel;
            truckbody.setVisibility(View.VISIBLE);
            tractorbody.setVisibility(View.GONE);
            carbody.setVisibility(View.GONE);
        } else if (SteeringVariables.vehicle.equals("tractor")) {
            wheelL = l2wheel;
            wheelR = r2wheel;
            truckbody.setVisibility(View.GONE);
            tractorbody.setVisibility(View.VISIBLE);
            carbody.setVisibility(View.GONE);
        } else {
            wheelL = lwheel;
            wheelR = rwheel;
            truckbody.setVisibility(View.GONE);
            tractorbody.setVisibility(View.GONE);
            carbody.setVisibility(View.VISIBLE);
        }
    }


    public static void updAngle() {
        MAX_ROTATION_ANGLE = Float.parseFloat(SteeringVariables.max_angle);
    }

    // Method to show the exit confirmation dialog
    public void showExitConfirmationDialog() {
        Dialog exitDialog = new Dialog(requireContext());
        exitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        exitDialog.setCancelable(true);
        exitDialog.setContentView(R.layout.exit_confirmation_dialog);
        Button confirmButton = exitDialog.findViewById(R.id.confirmButton);
        exitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Button cancelButton = exitDialog.findViewById(R.id.cancelButton);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the exit action here (e.g., call getActivity().finish())
                getActivity().finish();
                exitDialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog if cancel is clicked
                exitDialog.dismiss();
            }
        });

        // Show the exit confirmation dialog
        exitDialog.show();
    }


    public void blueToothListPopup(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.bluetoothconnectionpopup);
        dialog.setCanceledOnTouchOutside(true);
        listView = dialog.findViewById(R.id.listview);

//        implementListeners();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(context, "9999999999", Toast.LENGTH_SHORT).show();
        }

        Set<BluetoothDevice> bt = SteeringVariables.bluetoothAdapter.getBondedDevices();
        String[] strings = new String[bt.size()];
        btArray = new BluetoothDevice[bt.size()];
        int index = 0;

        if (SteeringVariables.bluetoothAdapter != null && SteeringVariables.bluetoothAdapter.isEnabled()
                && SteeringVariables.bluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothAdapter.STATE_CONNECTED) {
            Toast.makeText(getContext(), "Already Connected", Toast.LENGTH_SHORT);
        } else {
            if (bt.size() > 0) {
                for (BluetoothDevice device : bt) {
                    btArray[index] = device;
                    strings[index] = device.getName();
                    index++;
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, strings);
                listView.setAdapter(arrayAdapter);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            } else {
                dialog.dismiss();
                blueToothListPopup(context);
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    device = btArray[i];
                    SteeringVariables.device = device;
                    ClientClass clientClass = new ClientClass(SteeringVariables.device);
                    clientClass.start();
                    connectStatus.setText("Connecting");
                    dialog.dismiss();

                }
            });




        }


    }

    private void rotateLWheel(float rotationAngle) {
        int divisor = calculateDivisor(MAX_ROTATION_ANGLE); // Your DIVISOR value
        float maxWheelRotation = MAX_ROTATION_ANGLE / divisor; // Your maxLeftWheelRotation value

        // Calculate the rotation angle for the left wheel based on the input rotation angle
        float leftWheelRotation = Math.max(-maxWheelRotation, Math.min(maxWheelRotation, rotationAngle / divisor));

        // Rotate the lwheel and rwheel images based on the calculated rotation angle
        wheelL.setRotation(leftWheelRotation);
        wheelR.setRotation(leftWheelRotation);
    }

    private int calculateDivisor(float angle) {


        // Calculate divisor based on the input angle
        while (angle > startAngle) {
            divisor += 2; // Increase divisor by 2 every time angle exceeds startAngle
            startAngle += 90f; // Increase start angle by 90 degrees
        }

        return divisor;
    }

    private void implementListeners() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);

        }
        Set<BluetoothDevice> bt = SteeringVariables.bluetoothAdapter.getBondedDevices();
        String[] strings = new String[bt.size()];
        btArray = new BluetoothDevice[bt.size()];
        int index = 0;
        if (bt.size() > 0) {
            for (BluetoothDevice device : bt) {
                btArray[index] = device;
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);

                }
                strings[index] = device.getName();
                index++;
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_2, strings);
            listView.setAdapter(arrayAdapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClientClass clientClass = new ClientClass(btArray[i]);
                clientClass.start();

                connectStatus.setText("Connecting");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 3) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, perform Bluetooth operations here
                Toast.makeText(getContext(), "permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
            }
//            Toast.makeText(getContext(), "Bluetooth scan added", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == 2) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(getContext(), "666666666", Toast.LENGTH_SHORT).show();
            }
            Set<BluetoothDevice> bt = SteeringVariables.bluetoothAdapter.getBondedDevices();
            String[] strings = new String[bt.size()];
            btArray = new BluetoothDevice[bt.size()];
            int index = 0;
            if (bt.size() > 0) {
                for (BluetoothDevice device : bt) {
                    btArray[index] = device;
                    SteeringVariables.device = device;
                    strings[index] = device.getName();
                    index++;
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_2, strings);
                listView.setAdapter(arrayAdapter);
            }
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    device = btArray[i];
                    ClientClass clientClass = new ClientClass(btArray[i]);
                    clientClass.start();

                    connectStatus.setText("Connecting");
                }
            });
        }
    }

    public class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1) {
            device = SteeringVariables.device;

            try {
//                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                }
                ParcelUuid[] pu = device.getUuids();

                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                }
                try{
                    SteeringVariables.bluetoothAdapter.cancelDiscovery();
                }catch (Exception e){
                    Log.d("Bluetooth","cancelDiscovery() not working");
                }
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() throws IOException {
            try{
                socket.close();
                try {
                    SteeringVariables.bluetoothAdapter.startDiscovery();
                }catch (Exception e){}
                Message message = Message.obtain();
                message.what = STATE_CANCELLED;
                handler.sendMessage(message);
            }catch (Exception e){
                Log.d("Socket close","Socket not closed");
            }

        }

        public void run() {
            try {
                if (inputStream != null && outputStream != null) {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                    }
//                    SteeringVariables.bluetoothAdapter.cancelDiscovery();
                    socket.connect();
                    SteeringVariables.bluetooth = true;
//                    readDataInput();
//                    loadDataInput();
                    Log.e("11111111111111111111111111", "trying 1...");
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    SteeringVariables.sendReceive = new SendReceive(socket);
                    SteeringVariables.sendReceive.start();


                }
                else {
                    SteeringVariables.bluetooth = false;
//                    Toast.makeText(getContext(), "io", Toast.LENGTH_SHORT).show();
                    Log.e("Exception", "Input & output streams are null");
                }

            }
            catch (IOException e) {
                SteeringVariables.bluetooth = false;
                try {
                    Log.e("Socket connection", "trying fallback...");
                    socket = (BluetoothSocket) SteeringVariables.device.getClass().getMethod("createRfcommSocketToServiceRecord",UUID.class).invoke(SteeringVariables.device, 1);
//                    socket.getInputStream();
//                    socket.getOutputStream();

                    socket.connect();
                    SteeringVariables.bluetooth = true;
//                    readDataInput();
//                    loadDataInput();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    SteeringVariables.sendReceive = new SendReceive(socket);
                    SteeringVariables.sendReceive.start();
                    Log.d("Scoket connection", "Connected");
                } catch (Exception e2) {
                    SteeringVariables.bluetooth = false;
                    Log.e("Exception", "Couldn't establish Bluetooth connection! " + e2);
                }
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }
    public class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass() {
            try {
//                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                }
                serverSocket = SteeringVariables.bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;

            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();

                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    SteeringVariables.sendReceive = new SendReceive(socket);
                    SteeringVariables.sendReceive.start();
                    break;
                }
            }
        }
    }
    public class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            String value = "";
            while (true) {
                try {
//                    Log.d("Reading","trying to get value");
                    bytes = inputStream.read(buffer);
                    //                    Log.d("Reading","trying to get value "+bytes);
                    if (bytes != -1) {
                        r_value = true;
                        byte[] receivedDataBytes = Arrays.copyOf(buffer, bytes);
                        String receivedDataHex = byteArrayToHexString(receivedDataBytes).toLowerCase();
                        if(value.startsWith("40") && value.endsWith("0d0a") && value.length()==28){
                            if(listOfStringReceive.size()<0){
                                listOfStringReceive.clear();
                            }
                            listOfStringReceive.add(value);
                            value = "";
                        }
                        if(value.length()!=28){
                            value = value+receivedDataHex;
                        }

                        Log.d("shibhusetangle",SteeringVariables.setangle);

//                        if(SteeringVariables.setangle.equals("zero")){
//                            if (SteeringVariables.data3 == 0x00) {
//                                floatValue = (float) 0;
//                            } else {
//                                floatValue = (float) (-0);
//                            }
//                            initialTouchAngle = floatValue;
//                            currentRotationAngle = floatValue;
//                            rotationAngleProcess();
//                        }else if(SteeringVariables.setangle.equals("angle")){
//                            if (SteeringVariables.data3 == 0x00) {
//                                floatValue = (float) 0;
//                            } else {
//                                floatValue = (float) (-0);
//                            }
//                            initialTouchAngle = floatValue;
//                            currentRotationAngle = floatValue;
//                            rotationAngleProcess();
//                        }
                    }
                    else{
                        r_value = false;
                    }
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
//                    Log.d("Reading","Sent to handler");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                Log.e("Write", "Value sending");
            } catch (IOException e) {
                Log.e("Write", "Error while sending value");
                e.printStackTrace();
            }
        }
    }

        public static byte[] hexStringToByteArray(String hexString) {
            int len = hexString.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                        + Character.digit(hexString.charAt(i + 1), 16));
            }
            return data;
        }

        public static String byteArrayToHexString(byte[] byteArray) {
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : byteArray) {
                stringBuilder.append(String.format("%02X", b));
            }
            return stringBuilder.toString();
        }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case STATE_CANCELLED:
                    Toast.makeText(getContext(), "Bluetooth disconnected", Toast.LENGTH_SHORT).show();
                    SteeringVariables.bluetooth=false;
                    Log.d("Socket close","STATE_CANCELLED");
                    SteeringVariables.sendReceive=null;
//                    SteeringVariables.home_thread_flag=true;
                    SteeringVariables.sendReceive=null;
                    SteeringVariables.bluetoothAdapter = null;
//                    bltbtn.setImageResource(R.drawable.baseline_bluetooth_disabled_24);
                    connectStatus.setText("Not Connected");
                    break;
                case STATE_LISTENING:
                    connectStatus.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    connectStatus.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    connectStatus.setText("Connected");
                    SteeringVariables.bluetooth=true;
//                    if(SteeringVariables.setangle.equals("zero")) {
//                        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(wheelL, "rotation", wheelL.getRotation(), Float.parseFloat("0"));
//                        rotateAnimator.setDuration(2000); // Set the duration for the rotation animation (in milliseconds)
//                        rotateAnimator.start();
//                        ObjectAnimator rotateAnimator2 = ObjectAnimator.ofFloat(wheelR, "rotation", wheelR.getRotation(), Float.parseFloat("0"));
//                        rotateAnimator2.setDuration(2000); // Set the duration for the rotation animation (in milliseconds)
//                        rotateAnimator2.start();
//                        rotateSteeringWheel(0);
//                    }
//                    else if(SteeringVariables.setangle.equals("angle")){
//
//                        float angle = Float.parseFloat(angle_value);
//                        Log.d("angleshibhu",""+angle);
//                        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(wheelL, "rotation", angle, Float.parseFloat("0"));
//                        rotateAnimator.setDuration(2000); // Set the duration for the rotation animation (in milliseconds)
//                        rotateAnimator.start();
//                        ObjectAnimator rotateAnimator2 = ObjectAnimator.ofFloat(wheelR, "rotation", angle, Float.parseFloat("0"));
//                        rotateAnimator2.setDuration(2000); // Set the duration for the rotation animation (in milliseconds)
//                        rotateAnimator2.start();
//                        rotateSteeringWheel(angle);
//                    }
                    while(SteeringVariables.sendReceive==null){
                        if (SteeringVariables.sendReceive != null) {
                            break;
                        }
                        else {
//                            Toast.makeText(getContext(), "hellloooooo", Toast.LENGTH_SHORT).show();
                            // Handle the case where Bluetooth connection or message sending is not available
                        }
                    }
                    sendDataTo();
                    bltbtnimg.setImageResource(R.drawable.baseline_bluetooth_24);
                    int blueColor = ContextCompat.getColor(getContext(), R.color.blue); // R.color.blue should be defined in your resources
                    PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
                    bltbtnimg.setColorFilter(blueColor, mode);
                    break;
                case STATE_CONNECTION_FAILED:
                    connectStatus.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    listOfByteArrays.add(readBuff);
                    String hexString = bytesToHex(readBuff);
                    if(byteToHex(SteeringVariables.RxSignal).toLowerCase().equals("3e")){
                        SteeringVariables.bluetoothstatus = true;
                        write.setText("POS");
                    }
                    else if(byteToHex(SteeringVariables.RxSignal).toLowerCase().equals("7e")){
                        SteeringVariables.bluetoothstatus = false;
                        write.setText("NEG");
                    }
                    break;
            }
            return true;
        }
    });

    public static void sendDataTo(){
        if (SteeringVariables.sendReceive != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] curent_send_val = new byte[0];
                    while (true) {
//                                    Log.d("touchangle","sendreceive "+uniqueAnglesSetSendVal.size());
                        try {
                            if (uniqueAnglesSetSendVal.size() > 1) {
                                Log.d("touchangle","sendreceive "+uniqueAnglesSetSendVal.size());
                                float cur_val = uniqueAnglesSetSendVal.get(0);
                                curent_send_val = formatAndConvertData(cur_val);
                                uniqueAnglesSetSendVal.remove(0);
//                                            Log.d("anglecheckshibhu",cur_val+"");
//                                            byte[] frameId = convertShortToBytes(SteeringVariables.frameId1);
//                                            byte[] concatenatedArray = {SteeringVariables.startId, frameId[0], frameId[1], SteeringVariables.dlc, SteeringVariables.data1, SteeringVariables.data2, SteeringVariables.data3, curent_send_val[0], curent_send_val[1], SteeringVariables.data6, SteeringVariables.data7, SteeringVariables.data8, SteeringVariables.endId1, SteeringVariables.endId2};
//                                            SteeringVariables.sendReceive.write(concatenatedArray);
//                                            Thread.sleep(10);
                            }
                            else if(uniqueAnglesSetSendVal.size()==1){
                                float cur_val = uniqueAnglesSetSendVal.get(0);
                                curent_send_val = formatAndConvertData(cur_val);
                            }
                            else{
//                                            curent_send_val = curent_send_val;
//                                            Log.d("shibhuu",""+currentRotationAngle);
                                curent_send_val = formatAndConvertData(currentRotationAngle);
                            }

                            byte[] frameId = convertShortToBytes(SteeringVariables.frameId1);
//                                        Log.d("anglecheckshibhu","list else "+curent_send_val[0]+" "+curent_send_val[1]);

                            byte[] concatenatedArray = {SteeringVariables.startId, frameId[0], frameId[1], SteeringVariables.dlc, SteeringVariables.data1, SteeringVariables.data2, SteeringVariables.data3, curent_send_val[0], curent_send_val[1], SteeringVariables.data6, SteeringVariables.data7, SteeringVariables.data8, SteeringVariables.endId1, SteeringVariables.endId2};
                            SteeringVariables.sendReceive.write(concatenatedArray);
                            Thread.sleep(20);
                        }
                        catch (Exception e){
                            Log.d("SendValue",""+e);
                        }


                    }
                }
            }).start();
        }
        else {
//                            Toast.makeText(getContext(), "hellloooooo", Toast.LENGTH_SHORT).show();
            // Handle the case where Bluetooth connection or message sending is not available
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        SteeringVariables.RxSignal = bytes[5];
        for(byte b: bytes) {
            if (sb.length() <= 26) {
                sb.append(String.format("%02x", b));
            }
        }
        SteeringVariables.receivedSignal = sb.toString();
        return sb.toString();
    }

    public static String byteToHex(byte b){
        return String.format("%02x", b);
    }

    public static byte[] convertShortToBytes(short value) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) ((value >> 8) & 0xFF); // High byte
        bytes[1] = (byte) (value & 0xFF);        // Low byte
        return bytes;
    }

    private float calculateAngle(float x, float y) {
        float centerX = steeringwheel.getWidth() / 2f;
        float centerY = steeringwheel.getHeight() / 2f;
        float angle = (float) Math.toDegrees(Math.atan2(y - centerY, x - centerX));
        return (angle < 0) ? angle + 360 : angle;
    }


}
