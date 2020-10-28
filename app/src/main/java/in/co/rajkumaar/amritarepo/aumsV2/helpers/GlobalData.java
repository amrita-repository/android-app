/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aumsV2.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.loopj.android.http.AsyncHttpClient;

import java.security.KeyStore;
import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.aums.helpers.CustomSSLFactory;
import in.co.rajkumaar.amritarepo.aumsV2.models.Semester;
import in.co.rajkumaar.amritarepo.helpers.EncryptedPrefsUtils;

public class GlobalData {
    final public static String auth = "Basic YWRtaW46YWRtaW5AQW5kQVBQ";
    final public static String loginToken = "logintoken";
    private static String username;
    private static String dob;
    private static String name;
    private static String email;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private static String token;
    private static ArrayList<Semester> attendanceSemesters;
    private static ArrayList<Semester> gradeSemesters;

    public static AsyncHttpClient getClient() {
        // This is literally an unsafe hack to bypass the intermediate CA check by the HTTP client.
        // This was necessary because AUMS doesn't send its intermediate CA in its certificate chain.
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            CustomSSLFactory socketFactory = new CustomSSLFactory(trustStore);
            client.setSSLSocketFactory(socketFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        GlobalData.username = username;
    }

    public static String getDob() {
        return dob;
    }

    public static void setDob(String dob) {
        GlobalData.dob = dob;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        GlobalData.name = name;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        GlobalData.email = email;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        GlobalData.token = token;
    }

    public static void resetUser(Context context) {
        SharedPreferences preferences = EncryptedPrefsUtils.get(context, "aums_v2");
        preferences.edit().putBoolean("logged-in", false).apply();
        attendanceSemesters = gradeSemesters = null;
    }

    public static ArrayList<Semester> getAttendanceSemesters() {
        return attendanceSemesters;
    }

    public static void setAttendanceSemesters(ArrayList<Semester> attendanceSemesters) {
        GlobalData.attendanceSemesters = attendanceSemesters;
    }

    public static ArrayList<Semester> getGradeSemesters() {
        return gradeSemesters;
    }

    public static void setGradeSemesters(ArrayList<Semester> gradeSemesters) {
        GlobalData.gradeSemesters = gradeSemesters;
    }
}
