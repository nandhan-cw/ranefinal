package com.steering.testrane;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.steering.testrane.SteeringVariables.angle_value;
import static com.steering.testrane.SteeringVariables.current_value;
import static com.steering.testrane.SteeringVariables.ecu_value;
import static com.steering.testrane.SteeringVariables.getCurrentFragment;
import static com.steering.testrane.SteeringVariables.listOfByteArrays;
import static com.steering.testrane.SteeringVariables.listOfStringReceive;
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
    static final int STATE_MESSAGE_RECEIVED = 5,STATE_MESSAGE_NOT_RECEIVED=6;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        steerControl = view.findViewById(R.id.steercontrol);

        SteeringVariables.currentFragment = "status";

//        SteeringVariables.home_thread_flag = false;
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

        /// load status variables
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true){
//                    try {
//                        String curr_value = listOfStringReceive.get(0);
//                        String one = curr_value.substring(0, 2);
//                        String two = curr_value.substring(2, 4);
//                        String three = curr_value.substring(4,6);
//                        String four = curr_value.substring(6,8);
//                        String five = curr_value.substring(8,10);
//                        String six = curr_value.substring(10,12);
//                        String seven = curr_value.substring(12,14);
//                        String eight = curr_value.substring(14,16);
//                        String nine = curr_value.substring(16,18);
//                        String ten = curr_value.substring(18,20);
//                        String eleven = curr_value.substring(20,22);
//                        String twelve = curr_value.substring(22,24);
//                        String thirteen = curr_value.substring(24,26);
//                        String fourteen = curr_value.substring(26,28);
//                        if(one.equals("40") && thirteen.equals("0d") && fourteen.equals("0a")){
//                            if (five.equals("02")) {
//                                byte fourdata = (byte) Integer.parseInt(eight, 16);
//                                byte fivedata = (byte) Integer.parseInt(nine, 16);
//                                int decimalValue = (fourdata & 0xFF) << 8 | (fivedata & 0xFF);
//                                String temp="";
//                                if (seven.equals("00")) {
//                                    temp = "" + decimalValue;
//                                    astatus.setText(temp);
//                                    SteeringVariables.angle_value = temp;
//                                }
//                                else if (seven.equals("01")) {
//                                    temp = "-" + decimalValue;
//                                    astatus.setText(temp);
//                                    SteeringVariables.angle_value = temp;
//                                }
//                            }
//                            if (five.equals("03")) {
//                                if (eight.toLowerCase().equals("3e")) {
//                                    // change light to green in motor and ecu
//                                    mstatus.setText("ok");
//                                    mstatus.setTextColor(getResources().getColor(R.color.green));
//                                    mlight.setImageResource(R.drawable.grnbtn);
//                                    motor_value = true;
//                                } else if (eight.toLowerCase().equals("7e")) {
//                                    // change light to red in motor and ecu
//                                    mstatus.setText("err");
//                                    mstatus.setTextColor(getResources().getColor(R.color.red));
//                                    mlight.setImageResource(R.drawable.redbtn);
//                                    motor_value = false;
//                                }
//                                if (nine.toLowerCase().equals("3e")) {
//                                    // change light to green in motor and ecu
//                                    estatus.setText("ok");
//                                    estatus.setTextColor(getResources().getColor(R.color.green));
//                                    elight.setImageResource(R.drawable.grnbtn);
//                                    ecu_value = true;
//                                } else if (nine.toLowerCase().equals("7e")) {
//                                    // change light to red in motor and ecu
//                                    estatus.setText("err");
//                                    estatus.setTextColor(getResources().getColor(R.color.red));
//                                    elight.setImageResource(R.drawable.redbtn);
//                                    ecu_value = false;
//                                }
//
//                            }
//                            if (five.equals("04")) {
//                                Log.d("shibhu",""+five+" "+eleven);
//                                if (eleven.equals("3e")) {
//                                    tstatus.setText("ok");
//                                    tstatus.setTextColor(getResources().getColor(R.color.green));
//                                    tlight.setImageResource(R.drawable.grnbtn);
//                                    torque_value = true;
//                                } else if (eleven.equals("7e")) {
//                                    tstatus.setText("err");
//                                    tstatus.setTextColor(getResources().getColor(R.color.red));
//                                    tlight.setImageResource(R.drawable.redbtn);
//                                    torque_value = false;
//                                }
//                            }
//                            if (five.equals("05")) {
//                                Log.d("shibhu",""+five+" "+eleven+" "+twelve);
//                                byte curent1 = (byte) Integer.parseInt(eleven, 16);
//                                byte curent2 = (byte) Integer.parseInt(twelve, 16);
//                                int decimalValue = (curent1 & 0xFF) << 8 | (curent2 & 0xFF);
//                                cstatus.setText(""+decimalValue);
//                            }
//
//                        }
//                        listOfStringReceive.remove(0);
//                        Thread.sleep(100);
//                    }
//                    catch (Exception e){
//                        Log.d("Error","Can't load data");
//                    }
//                }
//            }
//        }).start();


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if(SteeringVariables.currentFragment.equals("status")) {
                            loadDataReceived();
                        }
                        else{
                            break;
                        }
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
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
//            sendData();
//            readData();
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

    public void loadDataReceived(){

        Log.d("shibhu",""+angle_value+" "+current_value+ " "+ecu_value+" "+motor_value+" "+torque_value);

        astatus.setText(angle_value);
        cstatus.setText(current_value);
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

                            byte[] frameId = HomeFragment.convertShortToBytes(SteeringVariables.frameId1);
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




}
