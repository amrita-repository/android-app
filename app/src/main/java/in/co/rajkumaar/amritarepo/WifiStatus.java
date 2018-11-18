package in.co.rajkumaar.amritarepo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class WifiStatus extends AppCompatActivity {
    String mUrl="https://anokha.amrita.edu/app/wifi.php";
    List<String> result;
    ProgressDialog dialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_status);
        dialog= new ProgressDialog(WifiStatus.this);


        MobileAds.initialize(this, getResources().getString(R.string.banner_id));
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
        Button refresh=findViewById(R.id.wifi_status_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable())
                    new Load().execute();
                else
                    showSnackbar("Device not connected to internet");
            }
        });

        if(isNetworkAvailable())
        new Load().execute();
        else
            showSnackbar("Device not connected to internet");


    }
    private void showSnackbar(String message) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onDestroy();
    }

    private class Load extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            dialog.setMessage("Loading..");
            dialog.setCancelable(false);
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
                try {
                    TextView textView1 = findViewById(R.id.text1);
                    TextView textView2 = findViewById(R.id.text2);
                    TextView textView3 = findViewById(R.id.text3);
                    TextView textView4 = findViewById(R.id.text4);
                    TextView textView5 = findViewById(R.id.text5);
                    TextView textView6 = findViewById(R.id.text6);
                    ImageView image1 = findViewById(R.id.image1);
                    ImageView image2 = findViewById(R.id.image2);
                    ImageView image3 = findViewById(R.id.image3);

                    textView1.setText(result.get(0));
                    if (result.get(1).trim().equals("true")) {
                        image1.setImageResource(R.mipmap.ic_tick);
                        textView2.setText(getText(R.string.wifiworking));
                    } else {
                        image1.setImageResource(R.mipmap.ic_error);
                        textView2.setText(getText(R.string.wifinotworking));
                    }
                    textView3.setText(result.get(2));

                    if (result.get(3).trim().equals("true")) {
                        textView4.setText(getText(R.string.wifiworking));
                        image2.setImageResource(R.mipmap.ic_tick);
                    } else {
                        image2.setImageResource(R.mipmap.ic_error);
                        textView4.setText(getText(R.string.wifinotworking));
                    }
                    textView5.setText(result.get(4));

                    if (result.get(5).trim().equals("true")) {
                        textView6.setText(getText(R.string.wifiworking));
                        image3.setImageResource(R.mipmap.ic_tick);
                    } else {
                        image3.setImageResource(R.mipmap.ic_error);
                        textView6.setText(getText(R.string.wifinotworking));
                    }
                    for (int i = 0; i < result.size(); ++i)
                        Log.e("JSON Parsed data", result.get(i));
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(WifiStatus.this,"Unexpected error. Please try again later",Toast.LENGTH_SHORT).show();
                    WifiStatus.this.finish();
                }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(String... strings) {
            result=QueryUtils.fetchBooks(mUrl);
            return null;
        }
    }
}