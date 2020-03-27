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

package in.co.rajkumaar.amritarepo.aums.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.materialtextfield.MaterialTextField;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.StringReader;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.aums.helpers.UserData;
import in.co.rajkumaar.amritarepo.aums.models.Client;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Client mainClient;

    private String sessionAction;
    private String sessionID;
    private String domain;

    private String name;
    private String studentHashId;

    private ProgressDialog dialog;
    private CheckBox remember;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        findViewById(R.id.container).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utils.hideKeyboard(LoginActivity.this);
                return false;
            }
        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        UserData.domain = "https://amritavidya.amrita.edu:8444";
        domain = UserData.domain;


        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);

        SharedPreferences pref = getSharedPreferences("user", Context.MODE_PRIVATE);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        Button login = findViewById(R.id.login);
        remember = findViewById(R.id.remember_me);
        mainClient = new Client(this);
        mainClient.clearCookie();
        UserData.client = mainClient.getClient();
        String rmusername = pref.getString("username", null);
        String rmpassword = pref.getString("password", null);

        username.setText(rmusername);
        password.setText(rmpassword);
        if (!TextUtils.isEmpty(username.getText().toString())) {
            remember.setChecked(true);
            ((MaterialTextField) findViewById(R.id.username_container)).setHasFocus(true);
        }
        if (!password.getText().toString().isEmpty()) {
            ((MaterialTextField) findViewById(R.id.password_container)).setHasFocus(true);
        }
        try {
            username.setSelection(rmusername.length());
            password.setSelection(rmpassword.length());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        actionDoneCloseInput();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(LoginActivity.this);
                SharedPreferences pref = getSharedPreferences("user", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = pref.edit();
                if (remember.isChecked()) {
                    ed.putString("username", username.getText().toString());
                    ed.putString("password", password.getText().toString());
                    ed.apply();
                } else {
                    ed.putString("username", null);
                    ed.putString("password", null);
                    ed.apply();
                }
                if (validate()) {
                    if (Utils.isConnected(LoginActivity.this)) {
                        dialog.setMessage("Creating a session");
                        dialog.show();
                        getSession(mainClient.getClient());
                    } else {
                        Toast.makeText(LoginActivity.this, "Please connect to internet", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


    }

    private boolean validate() {
        if (username.getText().toString().isEmpty()) {
            username.setError("This field cannot be empty");
            return false;
        } else if (password.getText().toString().isEmpty()) {
            password.setError("This field cannot be empty");
            return false;
        }
        return true;
    }


    private void actionDoneCloseInput() {
        username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Utils.hideKeyboard(LoginActivity.this);
                }
                return false;
            }
        });
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Utils.hideKeyboard(LoginActivity.this);
                }
                return false;
            }
        });

    }


    private void closeLoginDialog() {
        if (dialog.isShowing())
            dialog.dismiss();
    }


    private void getSession(final AsyncHttpClient client) {
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.setMessage("Logging in");
                            login(client);
                        }
                    });
                } catch (Exception e) {
                    closeLoginDialog();
                    Utils.showToast(LoginActivity.this, getString(R.string.site_change));
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showToast(LoginActivity.this, "An error occurred while connecting to server");
                closeLoginDialog();
            }
        });
    }

    private void login(final AsyncHttpClient client) {
        RequestParams params = new RequestParams();
        params.put("username", username.getText().toString());
        params.put("password", password.getText().toString());
        params.put("_eventId", "submit");
        params.put("lt", sessionID);
        params.put("submit", "LOGIN");

        Log.e("LOGIN", domain + sessionAction);
        client.post(domain + sessionAction, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getUserData(client);
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                closeLoginDialog();
                Utils.showToast(LoginActivity.this, "An error occurred while connecting to server");
            }
        });

    }


    private void getUserData(final AsyncHttpClient client) {
        client.get(domain + "/aums/Jsp/Core_Common/index.jsp?task=off", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                Elements TableElements = doc.getElementsByTag("td");

                try {
                    Elements scripts = doc.select("script[language=JavaScript]");
                    String script = scripts.get(3).html();
                    BufferedReader bufReader = new BufferedReader(new StringReader(script));
                    String line;
                    while ((line = bufReader.readLine()) != null) {
                        if (line.trim().startsWith("var myVar")) {
                            studentHashId = line.split("\"")[1];
                            UserData.uuid = studentHashId;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                for (Element tableElement : TableElements) {
                    if (tableElement.attr("class").equals("style3") && tableElement.attr("width").equals("70%")) {
                        name = tableElement.text();
                    }
                }
                if (name != null && !name.equals("")) {

                    try {
                        name = name.replace("Welcome ", "");
                        name = name.replace(")", "");
                        String[] result = name.split("\\(");
                        name = result[0];
                        UserData.name = name;
                        dialog.setMessage("Retrieving data");
                        getCGPA(client);
                    } catch (Exception e) {
                        closeLoginDialog();
                    }

                } else {
                    closeLoginDialog();
                    Utils.showToast(LoginActivity.this, "Invalid credentials");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                closeLoginDialog();
            }
        });
    }


    private void getCGPA(final AsyncHttpClient client) {
        RequestParams params = new RequestParams();
        params.put("action", "UMS-EVAL_STUDPERFORMSURVEY_INIT_SCREEN");
        params.put("isMenu", "true");
        client.get(domain + "/aums/Jsp/StudentGrade/StudentPerformanceWithSurvey.jsp", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                try {
                    Element CGPA = doc.select("td[width=19%].rowBG1").last();
                    UserData.CGPA = CGPA.text().trim();
                    UserData.username = username.getText().toString();
                    UserData.loggedin = true;
                    Bundle params = new Bundle();
                    params.putString("User", UserData.name + "[" + UserData.username + "]");
                    mFirebaseAnalytics.logEvent("AUMSLogin", params);
                    closeLoginDialog();
                    finish();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                } catch (Exception e) {
                    Utils.showToast(LoginActivity.this, "An error occurred while connecting to server");
                    closeLoginDialog();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showToast(LoginActivity.this, "An error occurred while connecting to server");
                closeLoginDialog();
            }
        });
    }

}
