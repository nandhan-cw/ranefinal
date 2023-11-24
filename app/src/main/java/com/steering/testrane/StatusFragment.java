package com.steering.testrane;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.steering.testrane.SteeringVariables.angle_value;
import static com.steering.testrane.SteeringVariables.current_value;
import static com.steering.testrane.SteeringVariables.ecu_value;
import static com.steering.testrane.SteeringVariables.motor_value;
import static com.steering.testrane.SteeringVariables.sendReceive;
import static com.steering.testrane.SteeringVariables.torque_value;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;

public class StatusFragment extends Fragment {

    Button steerControl;
    FragmentManager fragmentManager;
    ImageView alight,mlight,tlight,elight,clight;
    static TextView astatus,mstatus,tstatus,estatus,cstatus;
    static final int STATE_MESSAGE_RECEIVED = 5;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        steerControl = view.findViewById(R.id.steercontrol);

        SteeringVariables.home_thread_flag = false;
        SteeringVariables.status_thread_flag = true;

        Log.d("check","flag "+SteeringVariables.status_thread_flag);

        astatus = view.findViewById(R.id.astatus);
        mstatus = view.findViewById(R.id.mstatus);
        tstatus= view.findViewById(R.id.tstatus);
        estatus = view.findViewById(R.id.estatus);
        cstatus = view.findViewById(R.id.cstatus);
        alight = view.findViewById(R.id.alight);
        mlight = view.findViewById(R.id.mlight);
        tlight  = view.findViewById(R.id.tlight);
        elight = view.findViewById(R.id.elight);
        clight = view.findViewById(R.id.clight);
        steerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Replace the current fragment with LockSteeringFragment
                Fragment fragTwo = new LockSteeringFragment();
                replaceFragment(fragTwo);
            }
        });

//        SteeringVariables.home_thread_flag= false;
//        SteeringVariables.status_thread_flag=true;

        Log.d("status", "onCreateView:status home" + SteeringVariables.home_thread_flag.toString());
        Log.d("status", "onCreateView:status status" + SteeringVariables.status_thread_flag.toString());

        ///// bluetooth

//        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//        }
//        if (SteeringVariables.bluetoothAdapter != null && SteeringVariables.bluetoothAdapter.isEnabled()
//                && SteeringVariables.bluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothAdapter.STATE_CONNECTED) {
//            Toast.makeText(getContext(), "Already Connected", Toast.LENGTH_SHORT);
//        }
//        else {
//            HomeFragment.ClientClass clientClass = new HomeFragment.ClientClass(SteeringVariables.device);
//            clientClass.start();
//            Log.d("Status","Connecting");
//        }
//
        if(HomeFragment.inputStream!=null && HomeFragment.outputStream!=null && sendReceive!=null){
            sendData();
            readData();
        }
        else{
//            Toast.makeText(getContext(), "Connect to bluetooth", Toast.LENGTH_SHORT).show();
        }


//        HomeFragment.sendReceive.write();

        return view;
    }

    // Method to replace a fragment
    private void replaceFragment(Fragment fragment) {
        if (getActivity() != null) {
            fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, fragment,null);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    // Handle back press to navigate back to LockSteeringFragment
//    public static String bytesToHex(byte[] bytes) {
//        StringBuilder sb = new StringBuilder(bytes.length * 2);
//        Log.d("value","length: "+bytes.length);
//        SteeringVariables.RxSignal = bytes[5];
//        for(byte b: bytes) {
//            if (sb.length() <= 26) {
//                sb.append(String.format("%02x", b));
//            }
//        }
//        SteeringVariables.receivedSignal = sb.toString();
//        return sb.toString();
//    }

    private void loadDataReceived(){
        if(ecu_value){
            estatus.setText("ok");
            estatus.setTextColor(getResources().getColor(R.color.green));
            elight.setImageResource(R.drawable.grnbtn);
        }
        else{
            estatus.setText("err");
            estatus.setTextColor(getResources().getColor(R.color.red));
            elight.setImageResource(R.drawable.redbtn);
        }
        if(motor_value){
            mstatus.setText("ok");
            mstatus.setTextColor(getResources().getColor(R.color.green));
            mlight.setImageResource(R.drawable.grnbtn);
        }
        else{
            mstatus.setText("err");
            mstatus.setTextColor(getResources().getColor(R.color.red));
            mlight.setImageResource(R.drawable.redbtn);
        }
        if(torque_value){
            tstatus.setText("ok");
            tstatus.setTextColor(getResources().getColor(R.color.green));
            tlight.setImageResource(R.drawable.grnbtn);
        }
        else{
            tstatus.setText("err");
            tstatus.setTextColor(getResources().getColor(R.color.red));
            tlight.setImageResource(R.drawable.redbtn);
        }
        if(current_value){
            cstatus.setText("ok");
            cstatus.setTextColor(getResources().getColor(R.color.green));
            clight.setImageResource(R.drawable.grnbtn);
        }else{
            cstatus.setText("err");
            cstatus.setTextColor(getResources().getColor(R.color.red));
            clight.setImageResource(R.drawable.redbtn);
        }

    }

    private void sendData(){
        while(sendReceive==null){
            Log.d("status","sendreceive no");
            if(sendReceive!=null) break;
        }
        if(sendReceive!=null){
            Log.d("status","sendreceive yes");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("status","status run()");
                    while (true && SteeringVariables.status_thread_flag==true) {
                        Log.d("status","status while()");
                        try {

                            byte[] frameId = HomeFragment.convertShortToBytes(SteeringVariables.frameIdRX);
                            byte[] concatenatedArray02 = {SteeringVariables.startId,frameId[0],frameId[1], SteeringVariables.dlc,0x02,SteeringVariables.data2,SteeringVariables.data3,SteeringVariables.data5[0],SteeringVariables.data5[1],SteeringVariables.data6,SteeringVariables.data7,SteeringVariables.data8,SteeringVariables.endId1,SteeringVariables.endId2};
                            byte[] concatenatedArray03 = {SteeringVariables.startId,frameId[0],frameId[1], SteeringVariables.dlc,0x03,SteeringVariables.data2,SteeringVariables.data3,SteeringVariables.data5[0],SteeringVariables.data5[1],SteeringVariables.data6,SteeringVariables.data7,SteeringVariables.data8,SteeringVariables.endId1,SteeringVariables.endId2};
                            byte[] concatenatedArray04 = {SteeringVariables.startId,frameId[0],frameId[1], SteeringVariables.dlc,0x04,SteeringVariables.data2,SteeringVariables.data3,SteeringVariables.data5[0],SteeringVariables.data5[1],SteeringVariables.data6,SteeringVariables.data7,SteeringVariables.data8,SteeringVariables.endId1,SteeringVariables.endId2};
                            byte[] concatenatedArray05 = {SteeringVariables.startId,frameId[0],frameId[1], SteeringVariables.dlc,0x05,SteeringVariables.data2,SteeringVariables.data3,SteeringVariables.data5[0],SteeringVariables.data5[1],SteeringVariables.data6,SteeringVariables.data7,SteeringVariables.data8,SteeringVariables.endId1,SteeringVariables.endId2};

                            sendReceive.write(concatenatedArray02);
                            sendReceive.write(concatenatedArray03);
                            sendReceive.write(concatenatedArray04);
                            sendReceive.write(concatenatedArray05);
                            Thread.sleep(500); // Delay for 1 second (1000 milliseconds)
                        } catch (InterruptedException e) {
                            Log.d("status","thread ex "+e);
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }
    }


    private void readData(){
        byte[] buffer = new byte[1024];
        final int[] bytes = new int[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        bytes[0] = HomeFragment.inputStream.read(buffer);
                        handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes[0], -1, buffer).sendToTarget();
                    } catch (Exception e) {
                        Log.d("value", "ex: " + e);
                        e.printStackTrace();
                    }
                }

            }
        }).start();

    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (!isAdded()) {
                // Fragment is not attached; avoid accessing resources
                return false;
            }
//            64 4 14 8 4 126 0 0 0 0 62 0 13 10
            switch (msg.what) {
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    Log.d("check_value", "received " + readBuff[0] + " " + readBuff[1] + " " + readBuff[2] + " " + readBuff[3] + " " + readBuff[4] + " " + readBuff[5] + " " + readBuff[6] + " " + readBuff[7] + " " + readBuff[8] + " " + readBuff[9] + " " + readBuff[10] + " " + readBuff[11]+ " " + readBuff[12]+ " " + readBuff[13]);

                    byte[] frameId = HomeFragment.convertShortToBytes(SteeringVariables.frameIdRX);
//                    readBuff[1]==frameId[0] && readBuff[2]==frameId[1] &&
                    if(readBuff[0]==0x40 && readBuff[1]==frameId[0] && readBuff[2]==frameId[1] && readBuff[13]==0x0A && readBuff[12]==0x0D) {
                        byte onedata = readBuff[4];
                        Log.d("check_value",""+onedata + " "+readBuff[10]);

                        if (onedata == 0x02 && readBuff[9]==0x00 && readBuff[10]==0x00  && readBuff[11]==0x00) {

                            byte threedata = readBuff[6];
                            byte fourdata = readBuff[7];
                            byte fivedata = readBuff[8];
                            int decimalValue = (fourdata & 0xFF) << 8 | (fivedata & 0xFF);
                            if (HomeFragment.byteToHex(threedata).toLowerCase().equals("00")) {
                                String temp = "" + decimalValue;
                                astatus.setText(temp);
                                angle_value=temp;
//                                alight.setImageResource(R.drawable.grnbtn);
                            } else if (HomeFragment.byteToHex(threedata).toLowerCase().equals("01")) {
                                String temp = "-" + decimalValue;
                                astatus.setText(temp);
//                                alight.setImageResource(R.drawable.redbtn);
                            }
                        }

                        if (onedata == 0x03) {
                            byte fourdata = readBuff[7];
                            byte fivedata = readBuff[8];
                            if (HomeFragment.byteToHex(fourdata).toLowerCase().equals("3e")) {
                                // change light to green in motor and ecu
                                mstatus.setText("ok");
                                mstatus.setTextColor(getResources().getColor(R.color.green));
                                mlight.setImageResource(R.drawable.grnbtn);
                            } else if (HomeFragment.byteToHex(fourdata).toLowerCase().equals("7e")) {
                                // change light to red in motor and ecu
                                mstatus.setText("err");
                                mstatus.setTextColor(getResources().getColor(R.color.red));
                                mlight.setImageResource(R.drawable.redbtn);
                            }

                            if (HomeFragment.byteToHex(fivedata).toLowerCase().equals("3e")) {
                                // change light to green in motor and ecu
                                estatus.setText("ok");
                                estatus.setTextColor(getResources().getColor(R.color.green));
                                elight.setImageResource(R.drawable.grnbtn);
                            } else if (HomeFragment.byteToHex(fivedata).toLowerCase().equals("7e")) {
                                // change light to red in motor and ecu
                                estatus.setText("err");
                                estatus.setTextColor(getResources().getColor(R.color.red));
                                elight.setImageResource(R.drawable.redbtn);
                            }

                        }

                        if (onedata == 0x04) {
                            byte sevendata = readBuff[10];
                            byte fourdata = readBuff[7];
                            byte fivedata = readBuff[8];
                            if (HomeFragment.byteToHex(sevendata).toLowerCase().equals("3e")) {
                                tstatus.setText("ok");
                                tstatus.setTextColor(getResources().getColor(R.color.green));
                                tlight.setImageResource(R.drawable.grnbtn);
                            } else if (HomeFragment.byteToHex(sevendata).toLowerCase().equals("7e")) {
                                tstatus.setText("err");
                                tstatus.setTextColor(getResources().getColor(R.color.red));
                                tlight.setImageResource(R.drawable.redbtn);
                            }

                        }

                        if (onedata == 0x05) {
                            byte fourdata = readBuff[7];
                            byte fivedata = readBuff[8];
                            if (HomeFragment.byteToHex(fivedata).toLowerCase().equals("3e")) {
                                cstatus.setText("ok");
                                cstatus.setTextColor(getResources().getColor(R.color.green));
                                clight.setImageResource(R.drawable.grnbtn);
                            } else if (HomeFragment.byteToHex(fivedata).toLowerCase().equals("7e")) {
                                cstatus.setText("err");
                                cstatus.setTextColor(getResources().getColor(R.color.red));
                                clight.setImageResource(R.drawable.redbtn);
                            }
                        }
                    }

                    break;
            }
            return true;
        }
    });


}
