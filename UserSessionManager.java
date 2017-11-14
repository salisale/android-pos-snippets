package com.example.havensbee.myposapp.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.Calendar;
import java.util.Random;

public class UserSessionManager {

    SharedPreferences sharedPref;
    Editor editor;
    Context context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared pref file name
    private static final String PREFER_NAME = "AndroidMyPOSPref";

    // User name (make variable public to access from outside)
    public static final String KEY_USER_1 = "user1"; // Restaurant ID
    public static final String KEY_PASS_1 = "pass1";
    public static final String KEY_USER_2 = "user2"; // Employee ID
    public static final String KEY_PASS_2 = "pass2";
    public static final String FNAME = "fname", LNAME = "lname", USER_ID = "user_id", HOST_IP = "host_ip";
    public static final String IS_LOGGED_IN = "isLoggedIn";
    public static final String IS_FIRST_LAUNCH = "isFirstLaunch";
    public static final String RECORDED_DAY_OF_THE_WEEK = "recordedDayOfTheWeek";
    public static final String MAC_ID = "mac_id";
    public static final String PLAN = "plan";
    public static final String PLAN3_TRIAL_LIMIT = "plan3_trial_limit";
    public static final String PLAN3_SUCCESSFUL_UNLOCK = "plan3_successful_unlock";
    public static final String IS_FIRST_LAUNCH_REST_LOGIN = "isFirstLaunchRestLogin";

    // Whatever; recording category position of customised items
    public static final String CAT_POS_CUST_OPTION = "categoryPositionOfKeyzoneS";

    // Constructor
    public UserSessionManager(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        this.editor = sharedPref.edit();
    }

    public void createUserLogin1(String user) {
        Log.i(getClass().getSimpleName(), "Recording restaurant ID: " + user);
        editor.putString(KEY_USER_1, user);
        editor.commit();
    }

    public void createPasswordLogin1(String pass) {
        Log.i(getClass().getSimpleName(), "Recording restuarant password: " + pass);
        editor.putString(KEY_PASS_1, pass);
        editor.commit();
    }

    public void createUserLogin2(String user) {
        Log.i(getClass().getSimpleName(), "Recording employee ID: " + user);
        editor.putString(KEY_USER_2, user);
        editor.commit();
    }

    public void createPasswordLogin2(String pass) {
        Log.i(getClass().getSimpleName(), "Recording employee password" + pass);
        editor.putString(KEY_PASS_2, pass);
        editor.commit();
    }

    public String[] getUserData1() { // Retrieve last restaurant login for auto-fill
        String[] out = new String[2];
        out[0] = sharedPref.getString(KEY_USER_1, null);
        out[1] = sharedPref.getString(KEY_PASS_1, null);
        Log.i(getClass().getSimpleName(), "Get restaurant ID: " + out[0] + "-" + out[1]);
        return out;
    }

    public String[] getUserData2() { // Retrieve last employee login for auto-fill
        String[] out = new String[2];
        out[0] = sharedPref.getString(KEY_USER_2, null);
        out[1] = sharedPref.getString(KEY_PASS_2, null);
        Log.i(getClass().getSimpleName(), "Get employee ID:" + out[0] + "-" + out[1]);
        return out;
    }

    /**
     * Record UserProfileData when user's logged in
     */
    public void recordUserProfileData(String[] str) { // fname, lname, user_id
        Log.i(getClass().getSimpleName(), "Recording " + str[0] + ", " + str[1] + ", " + str[2]);
        editor.putString(FNAME, str[0]);
        editor.putString(LNAME, str[1]);
        editor.putString(USER_ID, str[2]);
        editor.commit();
    }

    /**
     * Get UserProfileData for latest login
     */
    public String[] getUserProfileData() {
        String[] out = new String[3];
        out[0] = sharedPref.getString(FNAME, null);
        out[1] = sharedPref.getString(LNAME, null);
        out[2] = sharedPref.getString(USER_ID, null);
        Log.i(getClass().getSimpleName(), "get UserProfile " + out[0] + ", " + out[1] + ", " + out[2]);
        return out;
    }

    /**
     * Record IP Daily, therefore if local IP is changed, it must not be done during the day
     */
    public void recordHostIP(String ip) {
        Log.i(getClass().getSimpleName(), "Record Host IP: " + ip);
        editor.putString(HOST_IP, ip);
        editor.commit();
    }

    public String getHostIP() {
        return sharedPref.getString(HOST_IP, "192.168.1.111:80");
    }

    public void recordUserLogIn() {
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.commit();
    }

    public void recordUserLogOut() {
        editor.putBoolean(IS_LOGGED_IN, false);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return sharedPref.getBoolean(IS_LOGGED_IN, false);
    }

    public boolean isFirstLaunchToday() {
        int todayDOW = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return todayDOW != getRecordedDayOfTheWeek();
    }

    /**
     * After successful login of the day; that is, cache item is successfully recorded
     */
    public void recordFirstLaunchSuccessfulToday() { // Use in LoginActivity before moving on?
        recordTodayDayOfTheWeek(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
    }

    public boolean isFirstLaunch() {
        return sharedPref.getBoolean(IS_FIRST_LAUNCH, true);
    }
    public void recordFirstLaunch() {
        editor.putBoolean(IS_FIRST_LAUNCH, false);
        editor.commit();
    }

    private int getRecordedDayOfTheWeek() {
        return sharedPref.getInt(RECORDED_DAY_OF_THE_WEEK, 0);
    }
    private void recordTodayDayOfTheWeek(int dayOfTheWeek) {
        editor.putInt(RECORDED_DAY_OF_THE_WEEK, dayOfTheWeek);
        editor.commit();
    }
    /**
     * When user explicit logs out
     */
    public void removeProfileData() {
        editor.remove(FNAME); editor.remove(LNAME); editor.remove(USER_ID);
        editor.commit();
    }

    /**
     * For InstallVerifActivity after claim
     */
    public void recordMacID(String mac_id) {
        editor.putString(MAC_ID, mac_id);
        editor.commit();
    }
    public String getMacID() {
        return sharedPref.getString(MAC_ID, "Dxx");
    }

    public void recordPlan(int plan) {
        editor.putInt(PLAN, plan);
        editor.commit();
    }
    public int getPlan() {return sharedPref.getInt(PLAN, 99);}

    /**
     * For Plan3
     */
    public void recordPlan3_successful_unlock() {
        editor.putBoolean(PLAN3_SUCCESSFUL_UNLOCK, true);
        editor.commit();
    }
    public boolean getPlan3_successful_unlock() {
        return sharedPref.getBoolean(PLAN3_SUCCESSFUL_UNLOCK, false);
    }

    // temp
    public void removeEverything() {
        editor.clear();
        editor.commit();
    }
    // temp
    public void makeFirstLaunchToday() {
        Random rnd = new Random();
        recordTodayDayOfTheWeek(rnd.nextInt(300));
    }
    // Called in FoodMenuRetriever (pulled from server)
    public void recordKeyzoneXCatPos(int pos) {
        editor.putInt(CAT_POS_CUST_OPTION, pos);
        editor.commit();
    }
    // Called in FoodMenuRetrieverCache
    public int getKeyzoneXCatPos() {
        return sharedPref.getInt(CAT_POS_CUST_OPTION, 0);
    }

    public void recordFirstLaunchRestLogin() {
        editor.putBoolean(IS_FIRST_LAUNCH_REST_LOGIN, false);
        editor.commit();
    }
    public boolean isFirstLaunchRestLogin() {
        return sharedPref.getBoolean(IS_FIRST_LAUNCH_REST_LOGIN, true);
    }
