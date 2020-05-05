/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.helpers;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class UserData {
    public static boolean loggedin = false;
    public static String name;
    public static String CGPA;
    public static String username;

    public static String uuid;
    public static int refIndex = 1;

    public static AsyncHttpClient client;
    public static String domain;
    public static Integer domainIndex;
    public static ArrayList<String> domains = new ArrayList<>();

    private static String sessionAction;
    private static String sessionID;
    private static String userName;
    private static String password;

    public static void setUserName(String uName) {
        userName = uName;
    }

    public static void setPassword(String pWord) {
        password = pWord;
    }

    public static void initDomains() {
        domainIndex = 0;
        UserData.domains.clear();
        UserData.domains.add("https://amritavidya2.amrita.edu:8444");
        UserData.domains.add("https://amritavidya1.amrita.edu:8444");
        UserData.domains.add("https://amritavidya.amrita.edu:8444");
    }

    public static void getSession(final LogInResponse logInResponse) {
        RequestParams params = new RequestParams();
        params.put("service", domain + "/aums/Jsp/Common/index.jsp");

        client.get(domain + "/cas/login", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                Element form = doc.select("#fm1").first();
                Element hiddenInput = doc.select("input[name=lt]").first();
                try {
                    sessionAction = form.attr("action");
                    sessionID = hiddenInput.attr("value");
                    logInResponse.onSuccess();
                } catch (Exception e) {
                    logInResponse.onException(e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                logInResponse.onFailure();
            }
        });
    }

    public static void login(final LogInResponse logInResponse) {
        RequestParams params = new RequestParams();
        params.put("username", userName);
        params.put("password", password);
        params.put("_eventId", "submit");
        params.put("lt", sessionID);
        params.put("submit", "LOGIN");

        client.post(domain + sessionAction, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                logInResponse.onSuccess();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                logInResponse.onFailure();
            }
        });
    }
}
