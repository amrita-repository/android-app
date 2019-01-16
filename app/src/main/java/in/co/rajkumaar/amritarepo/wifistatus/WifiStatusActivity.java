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

package in.co.rajkumaar.amritarepo.wifistatus;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.google.android.gms.ads.AdView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class WifiStatusActivity extends AppCompatActivity {
    String mUrl="https://anokha.amrita.edu/app/wifi.php";
    List<String> result;
    ProgressDialog dialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_status);
        dialog= new ProgressDialog(WifiStatusActivity.this);


        Utils.displayAd(this,(AdView)findViewById(R.id.adView));
        Button refresh=findViewById(R.id.wifi_status_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.isConnected(WifiStatusActivity.this))
                    getWifiData();
                else
                    Utils.showSnackBar(WifiStatusActivity.this,"Device not connected to internet");
            }
        });

        if(Utils.isConnected(WifiStatusActivity.this)){
            dialog.setMessage("Loading..");
            dialog.setCancelable(false);
            dialog.show();
            getWifiData();
        }
        else
            Utils.showSnackBar(WifiStatusActivity.this,"Device not connected to internet");


    }

    @Override
    protected void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onDestroy();
    }


    private void getWifiData(){
        AsyncHttpClient client = new AsyncHttpClient();
        client.setEnableRedirects(true);
        client.get("http://dev.rajkumaar.co.in/utils/wifi.php", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e("WIFI RESPONSE",new String(responseBody));
                TextView textView1 = findViewById(R.id.text1);
                TextView textView2 = findViewById(R.id.text2);
                TextView textView3 = findViewById(R.id.text3);
                TextView textView4 = findViewById(R.id.text4);
                TextView textView5 = findViewById(R.id.text5);
                TextView textView6 = findViewById(R.id.text6);
                ImageView image1   = findViewById(R.id.image1);
                ImageView image2   = findViewById(R.id.image2);
                ImageView image3   = findViewById(R.id.image3);
                ArrayList<TextView> titles = new ArrayList<>();
                ArrayList<TextView> messages = new ArrayList<>();
                ArrayList<ImageView> images = new ArrayList<>();

                titles.add(textView1);
                titles.add(textView3);
                titles.add(textView5);

                messages.add(textView2);
                messages.add(textView4);
                messages.add(textView6);

                images.add(image1);
                images.add(image2);
                images.add(image3);

                try {
                    JSONArray response = new JSONArray(new String(responseBody));
                    for(int i=0;i<response.length();++i){
                        titles.get(i).setText(response.getJSONObject(i).getString("connection"));
                        boolean status = response.getJSONObject(i).getBoolean("status");
                        messages.get(i).setText(status ? "Working" : "Not working");
                        images.get(i).setImageResource(status ? R.mipmap.ic_tick : R.mipmap.ic_error);
                    }
                } catch (JSONException e) {
                    Utils.showToast(WifiStatusActivity.this,"An unexpected error occurred. Please try again later.");
                    finish();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showToast(WifiStatusActivity.this,"An unexpected error occurred. Please try again later.");
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                finish();
            }

            @Override
            public void onFinish() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                super.onFinish();
            }
        });
    }
}