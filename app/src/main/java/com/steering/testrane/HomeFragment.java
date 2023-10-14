package com.steering.testrane;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HomeFragment extends Fragment {
    ImageView steeringwheel, bltbtn, leftkey, rightkey, volume, lwheel, rwheel;
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

    ListView listView;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;

    SendReceive sendReceive;

    InputStream inputStream;
    OutputStream outputStream;
    StringBuilder sbb = new StringBuilder();
    Button write;


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
        write = view.findViewById(R.id.write);



        Set<Float> uniqueAnglesSet = new HashSet<>();
        final AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        MAX_ROTATION_ANGLE = Float.parseFloat(SteeringVariables.max_angle);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SteeringVariables.steeringStatus = sharedPreferences.getString("steeringStatus", "not_locked");
        SteeringVariables.max_angle = sharedPreferences.getString("max_angle", "0");

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
        }
        else {
            if (!bluetoothAdapter.isEnabled()) {
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
        }
        else {

            steeringwheel.setEnabled(true);
            steeringwheel.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float x = event.getX();
                    float y = event.getY();
                    float touchAngle = calculateAngle(x, y);

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialTouchAngle = touchAngle;
                            uniqueAnglesSet.clear();// Clear the angle set when a new touch is initiated
                            break;
                        // Inside MotionEvent.ACTION_MOVE case:
                        // Define a Set to store unique angles


// Inside MotionEvent.ACTION_MOVE case:
                        case MotionEvent.ACTION_MOVE:
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
                            break;


                        case MotionEvent.ACTION_UP:
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
        lwheel.setRotation(leftWheelRotation);
        rwheel.setRotation(leftWheelRotation);
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
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();

                }
                else {
//                    Toast.makeText(getContext(), "io", Toast.LENGTH_SHORT).show();
                    Log.e("Exception", "Input & output streams are null");
                }

            }
            catch (IOException e) {
                try {
                    Log.e("Socket connection", "trying fallback...");
                    socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocketToServiceRecord",UUID.class).invoke(device, 1);
//                    socket.getInputStream();
//                    socket.getOutputStream();
                    socket.connect();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                    Log.d("Scoket connection", "Connected");
                } catch (Exception e2) {
                    Log.e("Exception", "Couldn't establish Bluetooth connection! " + e2);
                }

                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
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

            while (true) {
                try {
//                    Log.d("Reading","trying to get value");
                    bytes = inputStream.read(buffer);
//                    Log.d("Reading","trying to get value "+bytes);
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


                                        ///////// rx byte[]

                                        byte[] frameIdRx = convertShortToBytes(SteeringVariables.frameIdRX);


                                        byte[] concatenatedArrayRX = new byte[totalLength];

                                        int offsetrx = 0;

                                        System.arraycopy(hexData1, 0, concatenatedArrayRX, offsetrx, hexData1.length);
                                        offsetrx += hexData1.length;

                                        System.arraycopy(frameIdRx, 0, concatenatedArrayRX, offsetrx, frameIdRx.length);
                                        offsetrx += frameIdRx.length;

                                        System.arraycopy(hexData2, 0, concatenatedArrayRX, offsetrx, hexData2.length);
                                        offsetrx += hexData2.length;

                                        System.arraycopy(SteeringVariables.data5, 0, concatenatedArrayRX, offsetrx, SteeringVariables.data5.length);
                                        offsetrx += SteeringVariables.data5.length;

                                        System.arraycopy(hexData3, 0, concatenatedArrayRX, offsetrx, hexData3.length);

//                                        byte[] testval = {0x40,0x07,0x1E,0x08,0x1,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0D,0x0A};
                                        sendReceive.write(concatenatedArray);
                                        sendReceive.write(concatenatedArrayRX);
                                        Thread.sleep(2000); // Delay for 1 second (1000 milliseconds)
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
                    String hexString = bytesToHex(readBuff);
                    Log.d("value","Received msg "+byteToHex(SteeringVariables.RxSignal));

                    if(byteToHex(SteeringVariables.RxSignal).toLowerCase().equals("3e")){
                        write.setText("POS");
                    }
                    else if(byteToHex(SteeringVariables.RxSignal).toLowerCase().equals("7e")){
                        write.setText("NEG");
                    }



                    break;

            }
            return true;


        }
    });

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        Log.d("value","length: "+bytes.length);
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
