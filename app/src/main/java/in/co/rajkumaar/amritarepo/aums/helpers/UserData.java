/*
 * MIT License
 *
 * Copyright (c) 2018  RAJKUMAR S
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
