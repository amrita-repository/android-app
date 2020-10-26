/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
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
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.StringReader;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.aums.helpers.LogInResponse;
import in.co.rajkumaar.amritarepo.aums.helpers.UserData;
import in.co.rajkumaar.amritarepo.aums.models.Client;
import in.co.rajkumaar.amritarepo.helpers.EncryptedPrefsUtils;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class LoginActivity extends BaseActivity {

    private EditText username;
    private EditText password;

    private String domain;

    private String name;
    private String studentHashId;

    private ProgressDialog dialog;
    private CheckBox remember;

    private FirebaseAnalytics mFirebaseAnalytics;
    private Client mainClient;

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

        UserData.initDomains();
        UserData.domain = UserData.domains.get(UserData.domainIndex);
        domain = UserData.domain;


        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);


        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        Button login = findViewById(R.id.login);
        remember = findViewById(R.id.remember_me);
        mainClient = new Client(this);
        mainClient.clearCookie();
        UserData.client = mainClient.getClient();

        final SharedPreferences pref = EncryptedPrefsUtils.get(this, "aums_v1");
        String rmUsername = pref.getString("username", null);
        String rmPassword = pref.getString("password", null);
        if (rmUsername != null && rmPassword != null) {
            rmUsername = new String(Base64.decode(rmUsername, Base64.DEFAULT));
            username.setText(rmUsername);
            username.setSelection(rmUsername.length());
            rmPassword = new String(Base64.decode(rmPassword, Base64.DEFAULT));
            password.setText(rmPassword);
            password.setSelection(rmPassword.length());
        }

        if (!TextUtils.isEmpty(username.getText().toString())) {
            remember.setChecked(true);
            ((MaterialTextField) findViewById(R.id.username_container)).setHasFocus(true);
        }
        if (!password.getText().toString().isEmpty()) {
            ((MaterialTextField) findViewById(R.id.password_container)).setHasFocus(true);
        }

        actionDoneCloseInput();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(LoginActivity.this);
                SharedPreferences.Editor ed = pref.edit();
                if (remember.isChecked()) {
                    try {
                        String encName = Base64.encodeToString(username.getText().toString().getBytes(), Base64.DEFAULT);
                        String encPass = Base64.encodeToString(password.getText().toString().getBytes(), Base64.DEFAULT);
                        ed.putString("username", encName);
                        ed.putString("password", encPass);
                        ed.apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ed.putString("username", null);
                    ed.putString("password", null);
                    ed.apply();
                }
                if (validate()) {
                    if (Utils.isConnected(LoginActivity.this)) {
                        dialog.setMessage("Creating a session");
                        dialog.show();
                        getSession();
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

    private void getSession() {
        UserData.getSession(new LogInResponse() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setMessage("Logging in");
                        login();
                    }
                });
            }

            @Override
            public void onFailure() {
                Utils.showToast(LoginActivity.this, getString(R.string.server_error));
                closeLoginDialog();
            }

            @Override
            public void onException(Exception e) {
                closeLoginDialog();
                Utils.showToast(LoginActivity.this, getString(R.string.site_change));
                e.printStackTrace();
            }
        });
    }

    private void login() {
        UserData.setUserName(username.getText().toString());
        UserData.setPassword(password.getText().toString());
        UserData.login(new LogInResponse() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    mainClient.setJSessionIDPathAsRoot();
                    getUserData(UserData.client);
                });
            }

            @Override
            public void onFailure() {
                closeLoginDialog();
                Utils.showToast(LoginActivity.this, getString(R.string.server_error));
            }

            @Override
            public void onException(Exception e) {
                closeLoginDialog();
                Utils.showToast(LoginActivity.this, getString(R.string.site_change));
                e.printStackTrace();
            }
        });

    }

    private void getUserData(final AsyncHttpClient client) {
        client.get(domain + "/aums/Jsp/Core_Common/index.jsp?task=off", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));

                try {
                    Elements scripts = doc.select("script[language=JavaScript]");
                    String script = scripts.get(1).html();
                    BufferedReader bufReader = new BufferedReader(new StringReader(script));
                    String line;
                    while ((line = bufReader.readLine()) != null) {
                        if (line.trim().startsWith("var myVar")) {
                            studentHashId = line.split("\"")[1];
                            UserData.uuid = studentHashId;
                        }
                    }

                    name = doc.getElementById("userLoginNameToDisplay").attr("value");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (name != null && !name.equals("")) {

                    try {
                        name = name.replace("Welcome ", "");
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
                    UserData.CGPA = doc.select("#orphan-label-7").text();
                    UserData.username = username.getText().toString();
                    UserData.loggedin = true;
                    Bundle params = new Bundle();
                    params.putString("User", UserData.name + "[" + UserData.username + "]");
                    mFirebaseAnalytics.logEvent("AUMSLogin", params);
                    closeLoginDialog();
                    finish();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.showToast(LoginActivity.this, getString(R.string.server_error));
                    closeLoginDialog();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showToast(LoginActivity.this, getString(R.string.server_error));
                closeLoginDialog();
            }
        });
    }
}
