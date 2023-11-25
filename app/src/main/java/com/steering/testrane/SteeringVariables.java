package com.steering.testrane;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

public class SteeringVariables {
    public static String currentFragment = "";
    public static BluetoothDevice device;
    public static BluetoothAdapter bluetoothAdapter;
    public static HomeFragment.SendReceive sendReceive;
    public static Boolean home_thread_flag = true;
    public static Boolean status_thread_flag = true;
    public static Boolean lock_thread_flag = true;
    public static Boolean bluetooth = false;
    public static String steeringStatus="not_locked";
    public static String max_angle="180";
    public static String steeringauto="off";
    public  static String vibration = "0.4";
    public static String loginstatus="on";
    public static String getCurrentFragment() {
        return currentFragment;
    }

//    public static String vibration = "0.5";

    public static void setCurrentFragment(String fragment) {
        currentFragment = fragment;
    }
    public static Boolean release = false;
    public static Boolean bluetoothstatus = true;
    public static byte RxSignal;
    public static String receivedSignal="";
    public static byte startId = 0x40;
    public static short frameId1 = 0x70E;
    public static short frameIdRX = 0x71E;
//    public static byte frameId2 = 0x0E;
    public static byte dlc = 0x08;
    public static byte data1 = 0x01;
    public static byte data2 = 0x00;
    public static byte data3 = 0x00;

    public static String vehicle="";

    public static byte[] data5 = {0x00,0x00};

//    public static byte data4 = 0x00;

    public static byte data6 = 0x00;
    public static byte data7 = 0x00;
    public static byte data8 = 0x00;

//    public static byte[] data = {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    public static byte endId1 = 0x0D;
    public static byte endId2 = 0x0A;

    public static boolean ecu_value = true;

    public static boolean motor_value = true;
    public static String current_value = "0";
    public static boolean torque_value = true;
    public static String angle_value="0";
    public static ArrayList<byte[]> listOfByteArrays = new ArrayList<>();

    public static ArrayList<String> listOfStringReceive = new ArrayList<>();

}
