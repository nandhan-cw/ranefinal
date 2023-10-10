package com.steering.testrane;

public class SteeringVariables {
    private static String currentFragment = "";

    public static String steeringStatus="not_locked";
    public static String max_angle="180";

    public static String steeringauto="off";
    public static String loginstatus="on";
    public static String getCurrentFragment() {
        return currentFragment;
    }

    public static void setCurrentFragment(String fragment) {
        currentFragment = fragment;
    }
}
