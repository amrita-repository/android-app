package in.co.rajkumaar.amritarepo.aums;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;

public class LoginActivity extends AppCompatActivity {

    EditText username,password;
    Button login;
    Client mainClient;

    String sessionAction,sessionID;
    String rmusername,rmpassword;
    String domain;

    String name,studentHashId;

    ProgressDialog dialog;
    CheckBox remember;

    private FirebaseAnalytics mFirebaseAnalytics;

    Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseAnalytics=FirebaseAnalytics.getInstance(this);

        List<String> campusDataSet = new LinkedList<>(Arrays.asList(
                "Ettimadai",
                "Amritapuri",
                "Bangalore",
                "Mysore",
                "AIMS",
                "Business schools",
                "ASAS Kochi"
        ));

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,campusDataSet);
        spinner.setAdapter(arrayAdapter);

        UserData.domain = getServer(spinner.getSelectedItemPosition());
        domain = UserData.domain;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UserData.domain = getServer(position);
                domain = UserData.domain;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        AdView mAdView;
        MobileAds.initialize(this, getResources().getString(R.string.banner_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);

        SharedPreferences pref = getSharedPreferences("user", Context.MODE_PRIVATE) ;
        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        login=findViewById(R.id.login);
        remember=findViewById(R.id.remember_me);
        mainClient=new Client(this);
        UserData.client = mainClient.getClient();
        rmusername = pref.getString("username",null);
        rmpassword = pref.getString("password",null);
        spinner.setSelection(pref.getInt("campus",0));

        username.setText(rmusername);
        password.setText(rmpassword);
        if(!TextUtils.isEmpty(username.getText().toString())){
            remember.setChecked(true);
        }
        try {
            username.setSelection(rmusername.length());
            password.setSelection(rmpassword.length());
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences("user",Context.MODE_PRIVATE) ;
                SharedPreferences.Editor ed = pref.edit();
                if (remember.isChecked()){
                    ed.putString("username",username.getText().toString());
                    ed.putString("password",password.getText().toString());
                    ed.putInt("campus",spinner.getSelectedItemPosition());
                    ed.apply();
                }else{
                    ed.putString("username",null);
                    ed.putString("password",null);
                    ed.putInt("campus",0);
                    ed.apply();
                }
                if(Utils.isConnected(LoginActivity.this)) {
                    dialog.setMessage("Creating a session");
                    dialog.show();
                    getSession(mainClient.getClient());
                }
                else{
                    Toast.makeText(LoginActivity.this,"Please connect to internet",Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    void closeLoginDialog(){
        if(dialog.isShowing())
            dialog.dismiss();
    }

    void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

    void getSession(final AsyncHttpClient client){
        RequestParams params = new RequestParams();
        params.put("service", domain+"/aums/Jsp/Common/index.jsp");

        client.get(domain+"/cas/login", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Document doc = Jsoup.parse(new String(responseBody));
                        Element form = doc.select("#fm1").first();
                        Element hiddenInput = doc.select("input[name=lt]").first();
                        try {
                           sessionAction=form.attr("action");
                           sessionID=hiddenInput.attr("value");
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   dialog.setMessage("Logging in");
                                   login(client);
                               }
                           });
                        } catch (Exception e) {
                            closeLoginDialog();
                            showToast("Site'sF structure has changed. Please wait until I catch up.");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        showToast("An error occurred while connecting to server");
                        closeLoginDialog();
                    }
        });
    }

    void login(final AsyncHttpClient client){
        RequestParams params = new RequestParams();
        params.put("username", username.getText().toString());
        params.put("password", password.getText().toString());
        params.put("_eventId", "submit");
        params.put("lt", sessionID);
        params.put("submit", "LOGIN");

        Log.e("LOGIN",domain+sessionAction);
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
                showToast("An error occurred while connecting to server");
            }
        });

    }


    void getUserData(final AsyncHttpClient client){
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

                }else {
                    closeLoginDialog();
                    showToast("Invalid credentials");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                closeLoginDialog();
            }
        });
    }


    void getCGPA(final AsyncHttpClient client){
        RequestParams params = new RequestParams();
        params.put("action", "UMS-EVAL_STUDPERFORMSURVEY_INIT_SCREEN");
        params.put("isMenu", "true");
        client.get(domain+"/aums/Jsp/StudentGrade/StudentPerformanceWithSurvey.jsp", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                try {
                    Element CGPA = doc.select("td[width=19%].rowBG1").last();
                    UserData.CGPA=CGPA.text().trim();
                    UserData.username = username.getText().toString();
                    UserData.loggedin=true;
                    Bundle params = new Bundle();
                    params.putString("Name", UserData.name);
                    params.putString("Username",username.getText().toString());
                    Log.e("Name",name);
                    mFirebaseAnalytics.logEvent("AUMSLogin", params);
                    closeLoginDialog();
                    finish();
                    startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                } catch (Exception e) {
                    showToast("An error occurred while connecting to server");
                    closeLoginDialog();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showToast("An error occurred while connecting to server");
                closeLoginDialog();
            }
        });
    }

    public static String getServer(int identifier) {
        switch (identifier) {
            case 0:
                return "https://amritavidya.amrita.edu:8444";
            case 1:
                return "https://aums-students-am.amrita.edu:8443";
            case 2:
                return "https://aums-blr.amrita.edu:8444";
            case 3:
                return "https://amritavidya-am-student.amrita.edu:8444";
            case 4:
                return "https://amritavidya-aims.amrita.edu:8444";
            case 5:
                return "https://amritavidya-am-student.amrita.edu:8444";
            case 6:
                return "https://amritavidya-am-student.amrita.edu:8444";
            default:
                return null;
        }
    }

}
