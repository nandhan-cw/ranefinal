package com.steering.testrane;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HomeFragment extends Fragment {
    ImageView steeringwheel;
    ImageView bltbtn;
    ImageView leftkey;
    ImageView rightkey;
    ImageView volume;
    static ImageView lwheel;
    static ImageView rwheel;
    static ImageView l1wheel;
    static ImageView r1wheel;
    static ImageView l2wheel;
    static ImageView r2wheel;
    static ImageView wheelL;
    static ImageView wheelR;
    BluetoothDevice device;
    TextView angletext, connectStatus;
    ProgressDialog progressDialog;
    Dialog blueToothListPopup;
    MediaPlayer mediaPlayer;
    private float initialTouchAngle = 0f;
    private float currentRotationAngle = 0f;
    private static final int TOUCH_SENSITIVITY_THRESHOLD = 10; // Touch sensitivity threshold
    private static final float ROTATION_STEP = 10f; // Rotation step in degrees
    private Set<Float> angleSet = new HashSet<>();
    static RelativeLayout carbody;
    static RelativeLayout truckbody;
    static RelativeLayout tractorbody;
    ListView listView;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;

    SendReceive sendReceive;

    InputStream inputStream;
    OutputStream outputStream;
    StringBuilder sbb = new StringBuilder();


    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    int MY_BLUETOOTH_PERMISSION_REQUEST = 1;

    int REQUEST_ENABLE_BLUETOOTH = 1;
    private static float MAX_ROTATION_ANGLE;
    private int previousVolumeLevel;
    private static final String APP_NAME = "BTChat";
    private static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    int divisor = 2; // Initial divisor
    float startAngle = 90f; // Initial start angle
    private static final long VIBRATION_DURATION = 100;
    boolean isRotationInProgress = false;
    private static final long ROTATION_DELAY = 2000; // 2000 milliseconds (2 seconds)

    private Handler rotationHandler = new Handler();
    private boolean isAutoRotationEnabled = false;
    float touchAngle;
    ArrayList anglelist;
    Set<Float> uniqueAnglesSet;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        steeringwheel = view.findViewById(R.id.steeringwheel);
        angletext = view.findViewById(R.id.angletext);
        connectStatus = view.findViewById(R.id.connectStatus);
        bltbtn = view.findViewById(R.id.bltbtn);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        leftkey = view.findViewById(R.id.leftkey);
        rightkey = view.findViewById(R.id.rightkey);
        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.indicator); // R.raw.audio is the reference to your audio file
        mediaPlayer.setLooping(true);
        volume = view.findViewById(R.id.volume);
        lwheel = view.findViewById(R.id.lwheel);
        rwheel = view.findViewById(R.id.rwheel);
        l1wheel = view.findViewById(R.id.l1wheel);
        r1wheel = view.findViewById(R.id.r1wheel);
        l2wheel = view.findViewById(R.id.l2wheel);
        r2wheel = view.findViewById(R.id.r2wheel);
        Button write = view.findViewById(R.id.write);
        uniqueAnglesSet = new HashSet<>();
        touchAngle = 0f;
        carbody = view.findViewById(R.id.carbody);
        truckbody = view.findViewById(R.id.truckbody);
        tractorbody = view.findViewById(R.id.tractorbody);
        Handler handler = new Handler();
        anglelist = new ArrayList();



        Runnable rotateToZero = new Runnable() {
            @Override
            public void run() {
                if (currentRotationAngle > 0) {
                    currentRotationAngle -= ROTATION_STEP;
                    currentRotationAngle = Math.max(0, currentRotationAngle); // Ensure it doesn't go below 0
                    steeringwheel.setRotation(currentRotationAngle);
                    handler.postDelayed(this, ROTATION_DELAY); // Delay for ROTATION_DELAY milliseconds
                } else {
                    isRotationInProgress = false; // Rotation completed, set the flag to false
                }
            }
        };
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

//        if (SteeringVariables.vehicle.equals("truck")){
//            carbody.setVisibility(View.GONE);
//            tractorbody.setVisibility(View.GONE);
//            truckbody.setVisibility(View.VISIBLE);
//        }else if(SteeringVariables.vehicle.equals("tractor")){
//            carbody.setVisibility(View.GONE);
//            truckbody.setVisibility(View.GONE);
//            tractorbody.setVisibility(View.VISIBLE);
//        } else {
//            truckbody.setVisibility(View.GONE);
//            tractorbody.setVisibility(View.GONE);
//        }


        final AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        MAX_ROTATION_ANGLE = Float.parseFloat(SteeringVariables.max_angle);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SteeringVariables.steeringStatus = sharedPreferences.getString("steeringStatus", "not_locked");
        SteeringVariables.max_angle = sharedPreferences.getString("max_angle", "0");

//        try {
//            MAX_ROTATION_ANGLE = Float.parseFloat(SteeringVariables.max_angle);
//        } catch (NumberFormatException e) {
//            // Handle the exception, set a default value, or show an error message
//            MAX_ROTATION_ANGLE = 180f; // Set a default value
//            Log.e(TAG, "Invalid max_angle value: " + SteeringVariables.max_angle);
//        }
        Log.d(TAG, "onCreateView:steering stsu " + SteeringVariables.steeringStatus.toString());
        Log.d(TAG, "onCreateView:max angle " + SteeringVariables.max_angle.toString());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH}, MY_BLUETOOTH_PERMISSION_REQUEST);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
        }

        // Check if Bluetooth is enabled and request to enable if not
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(getContext(), "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }


//        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
//        String[] strings = new String[bt.size()];
//        btArray = new BluetoothDevice[bt.size()];
//        int index = 0;
//
//        if (bt.size() > 0) {
//            for (BluetoothDevice device : bt) {
//                btArray[index] = device;
//                strings[index] = device.getName();
//                if (device.getName().equals("HC-05")){
//                    ClientClass clientClass = new ClientClass(btArray[index]);
//                    clientClass.start();
//                    connectStatus.setText("Connecting");
//                }
//                index++;
//            }
//
//        }


        rightkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    leftkey.setEnabled(true);
                    rightkey.setImageResource(R.drawable.rightkey);
                    // Pause the audio if it's currently playing
                } else {
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
                    // Pause the audio if it's currently playing
                } else {
                    mediaPlayer.start();
                    rightkey.setEnabled(false);
                    leftkey.setImageResource(R.drawable.leftkey_light);// Start or resume the audio if it's paused
                }
            }
        });


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


        if (SteeringVariables.steeringStatus.equals("locked")) {
            steeringwheel.setEnabled(false);
        } else {

            steeringwheel.setEnabled(true);
            steeringwheel.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float x = event.getX();
                    float y = event.getY();

//                    if(SteeringVariables.release==true) {
//                        Log.d("angle","true");
//                        SteeringVariables.release=false;
//                        touchAngle = 0f;
//                        initialTouchAngle = touchAngle;
//                    }
                    touchAngle = calculateAngle(x, y);
//                    SteeringVariables.release = false;

//                    Log.d("angle","touch "+touchAngle);

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startVibration(1f);
                            initialTouchAngle = touchAngle;
                            uniqueAnglesSet.clear();// Clear the angle set when a new touch is initiated
                            break;
                        // Inside MotionEvent.ACTION_MOVE case:
                        // Define a Set to store unique angles


// Inside MotionEvent.ACTION_MOVE case:
                        case MotionEvent.ACTION_MOVE:
//                            float rotationAngleDiff = touchAngle - initialTouchAngle;
//                            // Check if the rotation step is greater than the threshold
//                            if (Math.abs(rotationAngleDiff) >= TOUCH_SENSITIVITY_THRESHOLD) {
//                                // Calculate the new rotation angle
//                                currentRotationAngle += (rotationAngleDiff > 0) ? ROTATION_STEP : -ROTATION_STEP;
//                                // Ensure the rotation angle is within 360 degrees
//                                currentRotationAngle = Math.min(MAX_ROTATION_ANGLE, Math.max(-MAX_ROTATION_ANGLE, currentRotationAngle));
//
//                                // Add the angle to the uniqueAnglesSet (filtering out duplicates)
//                                uniqueAnglesSet.add(currentRotationAngle);
//
//                                // Update the steering wheel rotation
//                                steeringwheel.setRotation(currentRotationAngle);
//                                rotateLWheel(currentRotationAngle);
//                                // Update the angle TextView
//                                angletext.setText("Angles: " + uniqueAnglesSet.toString());
//
//                                // Prepare the data to be sent over Bluetooth
//                                StringBuilder angleData = new StringBuilder();
//                                for (Float angle : uniqueAnglesSet) {
//                                    angleData.append(angle).append(","); // Separate angles with commas
//                                }
//
//                                // Convert the StringBuilder to String and send it over Bluetooth
//                                byte[] formattedData = formatAndConvertData(currentRotationAngle);
//
//                                // Convert the formattedData string to bytes and send it over Bluetooth three times
////                                byte[] formattedDataBytes = hexStringToByteArray(formattedData);
//                                Log.d("value steerin variable data: ", "data1 " + SteeringVariables.data5[0] + "data2 " + SteeringVariables.data5[1] + "sign " + SteeringVariables.data3);
//
//                                //for (int i = 0; i < 3; i++) {
////                                SteeringVariables.data3 = formattedDataBytes;
//
//                                if (sendReceive != null) {
////                                        sendReceive.write(formattedDataBytes);
////                                        SteeringVariables.data3 = formattedDataBytes;
//                                    // Your other actions after sending the data
//                                } else {
//                                    // Handle the case where Bluetooth connection or message sending is not available
//                                }
//                                // }
//
//                                // Update the initial touch angle
//                                initialTouchAngle = touchAngle;                                                       ro
                            rotationAngleProcess();
                            float vibrationIntensity = calculateVibrationIntensity(currentRotationAngle);
                                startVibration(vibrationIntensity);

                            break;


                        case MotionEvent.ACTION_UP:
                            touchAngle = 0f;
                            initialTouchAngle = 0f;
                            currentRotationAngle = 0f;
                            SteeringVariables.release = true;
                            stopVibration();
                            angleSet.clear();
                            if ("on".equals(SteeringVariables.steeringauto) && !isRotationInProgress) {
                                // Enable auto rotation and start rotation after ROTATION_DELAY milliseconds
                                rotationHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        float rotationAngleDiff = touchAngle - initialTouchAngle;
                                        // Check if the rotation step is greater than the threshold
                                        if (Math.abs(rotationAngleDiff) >= TOUCH_SENSITIVITY_THRESHOLD) {
                                            // Calculate the new rotation angle
                                            currentRotationAngle += (rotationAngleDiff > 0) ? ROTATION_STEP : -ROTATION_STEP;
                                            // Ensure the rotation angle is within 360 degrees
                                            currentRotationAngle = Math.min(MAX_ROTATION_ANGLE, Math.max(-MAX_ROTATION_ANGLE, currentRotationAngle));


                                        }
                                        Float currAngle = currentRotationAngle;



                                        for (float i=currAngle;i>=0;i-=10){
                                            if (anglelist.contains(i)) {

                                            } else {
                                                anglelist.add(i);
                                            }
                                            Log.d(TAG, "run: "+anglelist.toString());
                                        }

//                                        rotateLWheel(Float.parseFloat(temp.toString()));
                                        ObjectAnimator rotateAnimator1 = ObjectAnimator.ofFloat(wheelL, "rotation", lwheel.getRotation(), Float.parseFloat("0"));
                                        rotateAnimator1.setDuration(2000); // Set the duration for the rotation animation (in milliseconds)
                                        rotateAnimator1.start();
                                        ObjectAnimator rotateAnimator2 = ObjectAnimator.ofFloat(wheelR, "rotation", rwheel.getRotation(), Float.parseFloat("0"));
                                        rotateAnimator2.setDuration(2000); // Set the duration for the rotation animation (in milliseconds)
                                        rotateAnimator2.start();
                                        rotateSteeringWheel(0); // Rotate to 0 degrees
                                        touchAngle=0f;
                                    }
                                }, ROTATION_DELAY);
                            }
                            // Clear the angle set when touch is released
                            angleSet.clear();
                            break;
                    }
                    return true;
                }
            });
        }


        bltbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                blueToothListPopup(getContext());
            }
        });
        return view;

    }

    @Override
    public void onPause() {
        super.onPause();
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
    private void rotateSteeringWheel(float targetAngle) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(steeringwheel, "rotation", steeringwheel.getRotation(), targetAngle);
        rotateAnimator.setDuration(ROTATION_DELAY); // Set the duration for the rotation animation (in milliseconds)
        rotateAnimator.start();
        touchAngle=0f;
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
    public static void vehicleChange(){
        if (SteeringVariables.vehicle.equals("truck")){
            wheelL = l1wheel;
            wheelR = r1wheel;
            truckbody.setVisibility(View.VISIBLE);
            tractorbody.setVisibility(View.GONE);
            carbody.setVisibility(View.GONE);
        }
        else if (SteeringVariables.vehicle.equals("tractor")){
            wheelL = l2wheel;
            wheelR = r2wheel;
            truckbody.setVisibility(View.GONE);
            tractorbody.setVisibility(View.VISIBLE);
            carbody.setVisibility(View.GONE);
        }else {
            wheelL = lwheel;
            wheelR = rwheel;
            truckbody.setVisibility(View.GONE);
            tractorbody.setVisibility(View.GONE);
            carbody.setVisibility(View.VISIBLE);
        }
    }

    public void rotationAngleProcess(){
//        touchAngle = newangle;
//        Log.d("angle","fun "+touchAngle);
        float rotationAngleDiff = touchAngle - initialTouchAngle;
        // Check if the rotation step is greater than the threshold
        if (Math.abs(rotationAngleDiff) >= TOUCH_SENSITIVITY_THRESHOLD) {
            // Calculate the new rotation angle
            currentRotationAngle += (rotationAngleDiff > 0) ? ROTATION_STEP : -ROTATION_STEP;
            // Ensure the rotation angle is within 360 degrees
            currentRotationAngle = Math.min(MAX_ROTATION_ANGLE, Math.max(-MAX_ROTATION_ANGLE, currentRotationAngle));

            // Add the angle to the uniqueAnglesSet (filtering out duplicates)
            uniqueAnglesSet.add(currentRotationAngle);

            // Update the steering wheel rotation
            steeringwheel.setRotation(currentRotationAngle);
            rotateLWheel(currentRotationAngle);
            // Update the angle TextView
            angletext.setText("Angles: " + uniqueAnglesSet.toString());

            // Prepare the data to be sent over Bluetooth
            StringBuilder angleData = new StringBuilder();
            for (Float angle : uniqueAnglesSet) {
                angleData.append(angle).append(","); // Separate angles with commas
            }

            // Convert the StringBuilder to String and send it over Bluetooth
            byte[] formattedData = formatAndConvertData(currentRotationAngle);

            // Convert the formattedData string to bytes and send it over Bluetooth three times
//                                byte[] formattedDataBytes = hexStringToByteArray(formattedData);
            Log.d("value steerin variable data: ", "data1 " + SteeringVariables.data5[0] + "data2 " + SteeringVariables.data5[1] + "sign " + SteeringVariables.data3);

            //for (int i = 0; i < 3; i++) {
//                                SteeringVariables.data3 = formattedDataBytes;

            if (sendReceive != null) {
//                                        sendReceive.write(formattedDataBytes);
//                                        SteeringVariables.data3 = formattedDataBytes;
                // Your other actions after sending the data
            } else {
                // Handle the case where Bluetooth connection or message sending is not available
            }
            // }

            // Update the initial touch angle
            initialTouchAngle = touchAngle;
        }
    }
    private void startVibration(float intensity) {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            // Calculate the vibration duration based on intensity (assuming VIBRATION_DURATION is the total duration)
            long vibrationDuration = Math.max(1, (long) (VIBRATION_DURATION * intensity));

            // Create a vibration pattern with the calculated duration
            long[] pattern = { 0, vibrationDuration };
            // Vibrate with the pattern and default amplitude
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            }
        }

    }

    private void stopVibration() {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            // Cancel ongoing vibrations
            vibrator.cancel();
        }
    }
    private byte[] formatAndConvertData(float angle) {


        int angleValue = (int) angle;
        Log.d("value11", "angle1 : " + angleValue);


        if (angleValue < 0) {
            angleValue = Math.abs(angleValue);
            SteeringVariables.data3 = 0x01;
        } else {
            SteeringVariables.data3 = 0x00;
        }

        Log.d("value11", "angle2 : " + angleValue);

        // Cast angle to int

        String hexAngle = Integer.toHexString(angleValue);

        Log.d("value11", "hexstring : " + hexAngle);

        // Pad the hexadecimal string with leading zeros if needed
//        while (hexAngle.length() < 4) {
//            hexAngle = "0" + hexAngle;
//        }

        // Parse the hexadecimal string to bytes
//        byte[] angleBytes = new byte[2];
//        angleBytes[0] = (byte) Integer.parseInt(hexAngle.substring(0, 2), 16); // High byte
//        angleBytes[1] = (byte) Integer.parseInt(hexAngle.substring(2), 16);     // Low byte

        if (angleValue <= 255) {
            byte[] byteArray = new byte[2];
            byteArray[0] = 0x00;
            byteArray[1] = (byte) Integer.parseInt(hexAngle, 16);
            SteeringVariables.data5 = byteArray;
            Log.d("value11", "data1 : " + byteArray[0] + " data2: " + byteArray[1]);
            return byteArray;
        } else {
            // If angle is more than 255, store it in a double byte array
            byte[] doubleByteArray = new byte[2];
            doubleByteArray[0] = (byte) Integer.parseInt(hexAngle.substring(0, 2), 16);
            doubleByteArray[1] = (byte) Integer.parseInt(hexAngle.substring(2), 16);
            Log.d("value11", "data1 : " + doubleByteArray[0] + " data2: " + doubleByteArray[1]);
            SteeringVariables.data5 = doubleByteArray;
            return doubleByteArray;
        }

//        String test = String.valueOf(angleValue);
//        byte[] value = {};
//        if(angle>255){

//            String a = test.substring(0,3);
//            String b = test.substring(3,5);
//
//            angleValue = Integer.parseInt(a);
//            String hexAngle1 = String.format("%02X", angleValue);
//            if (hexAngle1.length() % 2 != 0) {
//                hexAngle1 = "0" + hexAngle1;
//            }
//
//            int angleValue2 = Integer.parseInt(b);
//            String hexAngle2 = String.format("%02X", angleValue2);
//            if (hexAngle2.length() % 2 != 0) {
//                hexAngle2 = "0" + hexAngle2;
//            }
//
//            value[0]  = Byte.parseByte(hexAngle1);
//            value[1]= Byte.parseByte(hexAngle2);

//        }
//        else if(test.length()==2){
//            String hexAngle2 = String.format("%02X", angleValue);
//            if (hexAngle2.length() % 2 != 0) {
//                hexAngle2 = "0" + hexAngle2;
//            }
//
//            value[0]  = Byte.parseByte(hexAngle2);
//            value[1]= 0x00;
//        }


//         Convert angle to hexadecimal
//        String hexAngle = String.format("%02X", angleValue);
//
//        // Calculate the number of bytes required for the angle in hexadecimal
//        int angleBytes = hexAngle.length() / 2;
//
//        StringBuilder formattedData = new StringBuilder();
//
//        if (hexAngle.length() % 2 != 0) {
//            hexAngle = "0" + hexAngle;
//        }
//
//        formattedData.append(hexAngle);
//
//        Log.d(TAG, "Formatted Data: " + formattedData.toString());

//        String hexString = "0x"+formattedData;
////        byte[] temp = new byte[];
//        if (hexString.startsWith("0x")) {
//            hexString = hexString.substring(2);
//        }
//        try {
//            int parsedValue = 0;
//            for (char c : hexString.toCharArray()) {
//                parsedValue = parsedValue * 16 + Character.digit(c, 16);
//            }
//            if (parsedValue >= 0 && parsedValue <= 255) {
//                byte byteValue = (byte) parsedValue;
//                byte tempvalue = 0x00;
//                byte[] temp = new byte[]{byteValue, tempvalue};
//                Log.d("value","temp1 "+temp[1]);
//                SteeringVariables.data3 =temp;
////                System.out.println("Byte Value: 0x" + String.format("%02X", byteValue));
//            } else {
//                byte[] temp = convertShortToBytes((short) parsedValue);
//                SteeringVariables.data3 = temp;
//                Log.d("value","temp2 "+temp[1]);
////                System.out.println("Value out of range for byte.");
//            }
//
//        } catch (NumberFormatException e) {
//            Log.d("aaaaaaa",e.getMessage());
////            System.out.println("Invalid hexadecimal format.");
//        }
//        return formattedData.toString();
    }

    private byte[] hexStringToByteArray(String s) {
        // Remove commas and any other unwanted characters
        s = s.replaceAll("[^A-Fa-f0-9]", "");

        int len = s.length();
        if (len % 2 != 0) {
            // If the length is odd, pad a zero at the beginning to make it even
            s = "0" + s;
            len = s.length();
        }

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            try {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            } catch (Exception e) {
                // Handle invalid characters if any
                e.printStackTrace();
            }
        }
//        SteeringVariables.data5 = data;
        return data;
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
        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
        String[] strings = new String[bt.size()];
        btArray = new BluetoothDevice[bt.size()];
        int index = 0;

        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()
                && bluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothAdapter.STATE_CONNECTED){
            Toast.makeText(getContext(),"Already Connected",Toast.LENGTH_SHORT);
        }
        else{
            if (bt.size() > 0) {
                for (BluetoothDevice device : bt) {
                    btArray[index] = device;
                    strings[index] = device.getName();
                    index++;
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, strings);
                listView.setAdapter(arrayAdapter);
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    device = btArray[i];
                    ClientClass clientClass = new ClientClass(btArray[i]);
                    clientClass.start();
                    connectStatus.setText("Connecting");
                    dialog.dismiss();

                }
            });


            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

        }



    }

    private void rotateLWheel(float rotationAngle) {
        int divisor = calculateDivisor(MAX_ROTATION_ANGLE); // Your DIVISOR value
        float maxWheelRotation = MAX_ROTATION_ANGLE/divisor; // Your maxLeftWheelRotation value

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
        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
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
        if (requestCode == 2) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(getContext(), "666666666", Toast.LENGTH_SHORT).show();
            }
            Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
            String[] strings = new String[bt.size()];
            btArray = new BluetoothDevice[bt.size()];
            int index = 0;
            if (bt.size() > 0) {
                for (BluetoothDevice device : bt) {
                    btArray[index] = device;
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

    private class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1) {
            device = device1;

            try {
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                }
                ParcelUuid[] pu = device.getUuids();

//                for(ParcelUuid puid : pu){
//                    UUID uid = puid.getUuid();
//                    socket = device.createInsecureRfcommSocketToServiceRecord(uid);
//                    Log.e("11111111111111111111111111", "uid" + uid);
//                    try{
//                        Log.e("11111111111111111111111111", "socket try");
//                        socket.connect();
//                        Log.e("11111111111111111111111111", "socket success");
//                    }
//                    catch (Exception e){
//                        Log.e("11111111111111111111111111", "socket failed ...");
//                    }
//                }

//                MY_UUID = device.getUuids()[1].getUuid();
//                Log.e("11111111111111111111111111", "trying fallback..." + MY_UUID);

//                UUID puid = UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb");
//                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//                device = "F0:65:AE:0A:8C:67";
//                socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.e("11111111111111111111111111", "socket conf");
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            try {
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, MY_BLUETOOTH_PERMISSION_REQUEST);
                }
                if (inputStream != null && outputStream != null) {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, MY_BLUETOOTH_PERMISSION_REQUEST);
                    }
                    else {
                        bluetoothAdapter.cancelDiscovery();
                    }
                    socket.connect();
                    Log.e("11111111111111111111111111", "trying 1...");
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    Log.e("11111111111111111111111111", "trying 2...");

                    sendReceive = new SendReceive(socket);
                    Log.e("11111111111111111111111111", "trying 3...");

                    sendReceive.start();
                    Log.e("11111111111111111111111111", "trying 4..."+socket);

                }
                else {
//                    Toast.makeText(getContext(), "io", Toast.LENGTH_SHORT).show();
                    Log.e("11111111111111111111111111", "trying 4... else");
                }

            }
            catch (IOException e) {

                Log.e("11111111111111111111111111", "ex 1 "+e);

                try {
                    Log.e("11111111111111111111111111", "123 trying fallback...");
                    socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocketToServiceRecord",UUID.class).invoke(device, 1);
//                    socket.getInputStream();
//                    socket.getOutputStream();
                    socket.connect();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();

                    Log.e("11111111111111111111111111", "Connected");
                } catch (Exception e2) {
                    Log.e("11111111111111111111111111", "123 Couldn't establish Bluetooth connection! " + e2);
                }

                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
//                Log.d("11111111111111111111111111", e.getMessage());
                handler.sendMessage(message);
            }
        }
    }
    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass() {
            try {
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                }
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("11111111111111111111111111","uuid ");
            }
        }

        public void run() {
            BluetoothSocket socket = null;

            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    Log.d("11111111111111111111111111","uuid before");

                    socket = serverSocket.accept();
                    Log.d("11111111111111111111111111","uuid after");

                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    Log.d("11111111111111111111111111", e.getMessage());
                    handler.sendMessage(message);
                }

                if (socket != null) {
                    Log.d("11111111111111111111111111","socket not null");
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();

                    break;
                }
            }
        }
    }
    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            Log.e("11111111111111111111111111", "trying 5...");

            bluetoothSocket = socket;
//            inputStream = ip;
//            outputStream = op;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
                Log.e("11111111111111111111111111", "trying 6..."+tempIn+" "+tempOut);

            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
            Log.e("11111111111111111111111111", "trying 7..."+inputStream+" "+outputStream);

        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                    Log.e("11111111111111111111111111", "trying 8...");
                } catch (IOException e) {
                    Log.e("11111111111111111111111111", "trying 9...");
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {

                outputStream.write(bytes);
                Log.e("11111111111111111111111111", "trying 10...");
            } catch (IOException e) {
                Log.e("11111111111111111111111111", "trying 11...");
                e.printStackTrace();
            }
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case STATE_LISTENING:
                    connectStatus.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    connectStatus.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    connectStatus.setText("Connected");
//                    final byte[] hexData = {0x40, 0x00, 0x00, 0x08, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D, 0x0A};

//                    sbb.append(SteeringVariables.data5.toString());



//                            SteeringVariables.frameId1,SteeringVariables.dlc,SteeringVariables.data1,SteeringVariables.data2,SteeringVariables.data3,SteeringVariables.data4,SteeringVariables.data5,SteeringVariables.data6,SteeringVariables.data7,SteeringVariables.data8,  SteeringVariables.endId1,SteeringVariables.endId2};
                    while(sendReceive==null){
                        Log.d("11111111111111111111111111","sendrec1: "+sendReceive);
                        if (sendReceive != null) {
                            Log.d("11111111111111111111111111","sendrec3: "+sendReceive);
                            break;
                        }
                        else {
                            Log.d("11111111111111111111111111","sendrec2: "+sendReceive);
//                            Toast.makeText(getContext(), "hellloooooo", Toast.LENGTH_SHORT).show();
                            // Handle the case where Bluetooth connection or message sending is not available
                        }
                    }
                    if (sendReceive != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (true) {
                                    try {

                                        byte[] frameId = convertShortToBytes(SteeringVariables.frameId1);
                                        byte[] hexData1 = {SteeringVariables.startId};
                                        byte[] hexData2 = {SteeringVariables.dlc,SteeringVariables.data1,SteeringVariables.data2,SteeringVariables.data3};


                                        byte[] angleData = SteeringVariables.data5;

                                        byte[] hexData3 = {SteeringVariables.data6,SteeringVariables.data7,SteeringVariables.data8,SteeringVariables.endId1,SteeringVariables.endId2};

                                        int totalLength = frameId.length + hexData1.length + hexData2.length + angleData.length + hexData3.length;

                                        byte[] concatenatedArray = new byte[totalLength];

                                        int offset = 0;

                                        System.arraycopy(hexData1, 0, concatenatedArray, offset, hexData1.length);
                                        offset += hexData1.length;

                                        System.arraycopy(frameId, 0, concatenatedArray, offset, frameId.length);
                                        offset += frameId.length;

                                        System.arraycopy(hexData2, 0, concatenatedArray, offset, hexData2.length);
                                        offset += hexData2.length;

                                        Log.d("value","value sent 1 "+SteeringVariables.data5[0]+"value sent 2 : "+SteeringVariables.data5[1]);

                                        System.arraycopy(SteeringVariables.data5, 0, concatenatedArray, offset, SteeringVariables.data5.length);
                                        offset += SteeringVariables.data5.length;

                                        System.arraycopy(hexData3, 0, concatenatedArray, offset, hexData3.length);

//                                        byte[] testval = {0x40,0x07,0x0E,0x08,0x1,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0D,0x0A};
                                        sendReceive.write(concatenatedArray);
//                                        byte[] angleData = SteeringVariables.data5;
                                        StringBuilder sb = new StringBuilder();
                                        for (byte b : SteeringVariables.data5) {
                                            sb.append(String.format("%02X", b)); // %02X formats the byte as a two-digit hexadecimal number
                                        }
                                        Log.d("value","value sent builder "+sb);
//                                        Toast.makeText(getContext(), ""+SteeringVariables.data3, Toast.LENGTH_SHORT).show();
                                        Thread.sleep(200); // Delay for 1 second (1000 milliseconds)
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();
                    }
                    else {
                        Log.d("11111111111111111111111111","sendrec5: "+sendReceive);
//                            Toast.makeText(getContext(), "hellloooooo", Toast.LENGTH_SHORT).show();
                        // Handle the case where Bluetooth connection or message sending is not available
                    }



                    bltbtn.setImageResource(R.drawable.baseline_bluetooth_24);
                    int blueColor = ContextCompat.getColor(getContext(), R.color.blue); // R.color.blue should be defined in your resources
                    PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
                    bltbtn.setColorFilter(blueColor, mode);
                    break;
                case STATE_CONNECTION_FAILED:
                    connectStatus.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    break;

            }
            return true;


        }
    });

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
