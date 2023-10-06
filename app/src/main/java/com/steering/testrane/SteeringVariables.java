package com.steering.testrane;

public class SteeringVariables {
    private static String currentFragment = "";

    public static String steeringStatus="not_locked";

    public static String getCurrentFragment() {
        return currentFragment;
    }

    public static void setCurrentFragment(String fragment) {
        currentFragment = fragment;
    }
}
