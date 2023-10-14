package com.steering.testrane;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.steering.testrane.SteeringVariables.sendReceive;

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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.Set;

public class StatusFragment extends Fragment {

    Button steerControl;
    FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        steerControl = view.findViewById(R.id.steercontrol);
        steerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Replace the current fragment with LockSteeringFragment
                Fragment fragTwo = new LockSteeringFragment();
                replaceFragment(fragTwo);
            }
        });

        SteeringVariables.home_thread_flag= false;
        SteeringVariables.status_thread_flag=true;

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

        while(sendReceive==null){
            Log.d("status","sendreceive no");
            if(sendReceive!=null) break;
        }
        if(sendReceive!=null){
            Log.d("status","sendreceive yes");
//            SendData sendData = new SendData();
//            sendData.start();

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

//                            Log.d("status","01: "+concatenatedArray02[0]);
//                            Log.d("status","01: "+concatenatedArray03[0]);
//                            Log.d("status","01: "+concatenatedArray04[0]);
//                            Log.d("status","01: "+concatenatedArray05[0]);

//                                        byte[] testval = {0x40,0x07,0x1E,0x08,0x1,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0D,0x0A};
                            sendReceive.write(concatenatedArray02);
                            sendReceive.write(concatenatedArray03);
                            sendReceive.write(concatenatedArray04);
                            sendReceive.write(concatenatedArray05);
                            Thread.sleep(2000); // Delay for 1 second (1000 milliseconds)
                        } catch (InterruptedException e) {
                            Log.d("status","thread ex "+e);
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

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

    private static class SendData extends Thread {
        public void run() {
            Log.d("status","run()");


        }

    }


}
