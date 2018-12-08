package in.co.rajkumaar.amritarepo.aums;


import com.loopj.android.http.AsyncHttpClient;

import java.io.File;


class UserData {
    static boolean loggedin = false;
    static String name,CGPA,username;
    static File image;
    static String uuid;
    static int refIndex=1;

    static AsyncHttpClient client;
    static String domain;
}
