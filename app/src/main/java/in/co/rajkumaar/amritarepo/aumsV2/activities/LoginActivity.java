/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aumsV2.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.github.florent37.materialtextfield.MaterialTextField;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.aumsV2.helpers.GlobalData;
import in.co.rajkumaar.amritarepo.helpers.EncryptedPrefsUtils;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class LoginActivity extends BaseActivity {

    SharedPreferences pref;
    AsyncHttpClient client = GlobalData.getClient();
    private EditText username;
    private EditText dob;
    private Calendar dobDate = Calendar.getInstance();
    private ProgressDialog progressDialog;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        Objects.requireNonNull(getSupportActionBar()).setSubtitle("Lite Version");
        username = findViewById(R.id.username);
        dob = findViewById(R.id.dob);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Logging in..");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        pref = EncryptedPrefsUtils.get(this, "aums_v2");
        String rmUsername = pref.getString("username", null);
        String rmDob = pref.getString("dob", null);
        if (rmUsername != null && rmDob != null) {
            rmUsername = new String(Base64.decode(rmUsername, Base64.DEFAULT));
            username.setText(rmUsername);
            username.setSelection(rmUsername.length());
            ((MaterialTextField) findViewById(R.id.username_container)).setHasFocus(true);
            rmDob = new String(Base64.decode(rmDob, Base64.DEFAULT));
            dob.setText(rmDob);
            ((MaterialTextField) findViewById(R.id.dob_container)).setHasFocus(true);
        }
    }

    public void selectDOB(View view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                DecimalFormat mFormat = new DecimalFormat("00");
                mFormat.setRoundingMode(RoundingMode.DOWN);
                dob.setText(year + "-" + mFormat.format(Double.valueOf(month + 1)) + "-" + mFormat.format(Double.valueOf(dayOfMonth)));
                dobDate.set(year, month, dayOfMonth);
            }
        }, dobDate.get(Calendar.YEAR),
                dobDate.get(Calendar.MONTH),
                dobDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    public void login(View view) {
        Utils.hideKeyboard(this);
        if (!Utils.isConnected(getBaseContext())) {
            Utils.showInternetError(getBaseContext());
            return;
        }
        if (username.getText().toString().isEmpty() || dob.getText().toString().isEmpty()) {
            Utils.showSnackBar(this, "Please enter all fields");
            return;
        }
        progressDialog.show();
        client.addHeader("Authorization", GlobalData.auth);
        client.addHeader("token", GlobalData.loginToken);
        client.get("https://amritavidya.amrita.edu:8444/DataServices/rest/authRes?rollno="
                + username.getText().toString() + "&dob=" + dob.getText().toString() + "&user_type=Student", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(bytes));
                    if (jsonObject.has("Status")) {
                        if (jsonObject.getString("Status").equals("OK")) {
                            GlobalData.setUsername(username.getText().toString());
                            GlobalData.setDob(dob.getText().toString());
                            GlobalData.setEmail(jsonObject.getString("Email"));
                            GlobalData.setName(jsonObject.getString("NAME"));
                            verifyOTP();
                        } else {
                            Utils.showSnackBar(LoginActivity.this, jsonObject.getString("Status"));
                        }
                    }
                } catch (JSONException e) {
                    Utils.showUnexpectedError(LoginActivity.this);
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                progressDialog.dismiss();
                Utils.showUnexpectedError(LoginActivity.this);
            }
        });

    }

    public void verifyOTP() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
        alertDialog.setMessage("Enter the OTP you just received on your number registered in AUMS : ");
        LinearLayout layout = new LinearLayout(LoginActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(40, 0, 50, 0);
        final EditText otpEditText = new EditText(LoginActivity.this);
        otpEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(otpEditText, params);
        alertDialog.setView(layout);
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressDialog.setMessage("Verifying OTP..");
                progressDialog.show();
                client.addHeader("Authorization", GlobalData.auth);
                client.addHeader("token", GlobalData.loginToken);
                client.get("https://amritavidya.amrita.edu:8444/DataServices/rest/authRes/register?rollno=" +
                        username.getText().toString() + "&otp=" + otpEditText.getText().toString(), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(bytes));
                            if (jsonObject.getString("Status").equals("Y")) {
                                GlobalData.setToken(jsonObject.getString("Token"));
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putBoolean("logged-in", true);
                                editor.putString("username", Base64.encodeToString(GlobalData.getUsername().getBytes(), Base64.DEFAULT));
                                editor.putString("email", Base64.encodeToString(GlobalData.getEmail().getBytes(), Base64.DEFAULT));
                                editor.putString("dob", Base64.encodeToString(GlobalData.getDob().getBytes(), Base64.DEFAULT));
                                editor.putString("name", Base64.encodeToString(GlobalData.getName().getBytes(), Base64.DEFAULT));
                                editor.putString("token", Base64.encodeToString(GlobalData.getToken().getBytes(), Base64.DEFAULT));
                                editor.apply();
                                Bundle params = new Bundle();
                                params.putString("User", GlobalData.getName() + "[" + GlobalData.getUsername() + "]");
                                mFirebaseAnalytics.logEvent("AUMSLogin", params);
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                Utils.showSnackBar(LoginActivity.this, "Invalid OTP");
                            }
                        } catch (Exception e) {
                            Utils.showUnexpectedError(LoginActivity.this);
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                        progressDialog.dismiss();
                        Utils.showUnexpectedError(LoginActivity.this);
                    }
                });
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertDialog.show();
    }
}
