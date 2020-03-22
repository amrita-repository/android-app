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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class WifiStatusActivity extends AppCompatActivity {
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_status);

        dialog = new ProgressDialog(WifiStatusActivity.this);
        dialog.setCancelable(false);


        Button refresh = findViewById(R.id.wifi_status_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isConnected(WifiStatusActivity.this)) {
                    showProgress();
                    new scrapeWifiData().execute();
                } else
                    Utils.showSnackBar(WifiStatusActivity.this, "Device not connected to internet");
            }
        });

        if (Utils.isConnected(WifiStatusActivity.this)) {
            showProgress();
            new scrapeWifiData().execute();
        } else
            Utils.showSnackBar(WifiStatusActivity.this, "Device not connected to internet");
    }

    private void showProgress() {
        dialog.setMessage("Loading..");
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onDestroy();
    }

    @SuppressLint("StaticFieldLeak")
    private class scrapeWifiData extends AsyncTask<Void, Void, Void> {
        private HashMap<String, Boolean> status = new HashMap<>();
        private TextView textView1 = findViewById(R.id.text1);
        private TextView textView2 = findViewById(R.id.text2);
        private TextView textView3 = findViewById(R.id.text3);
        private TextView textView4 = findViewById(R.id.text4);
        private TextView textView5 = findViewById(R.id.text5);
        private TextView textView6 = findViewById(R.id.text6);
        private ImageView image1 = findViewById(R.id.image1);
        private ImageView image2 = findViewById(R.id.image2);
        private ImageView image3 = findViewById(R.id.image3);
        private ArrayList<TextView> titles = new ArrayList<>();
        private ArrayList<TextView> messages = new ArrayList<>();
        private ArrayList<ImageView> images = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            titles.add(textView1);
            titles.add(textView3);
            titles.add(textView5);

            messages.add(textView2);
            messages.add(textView4);
            messages.add(textView6);

            images.add(image1);
            images.add(image2);
            images.add(image3);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document document = Jsoup.connect("https://" + getString(R.string.intranet) + "/CampusWifiStatus/CampusWifiStatus.php").get();
                Elements rows = document.select("tbody > tr");
                for (Element row : rows) {
                    Elements columns = row.getElementsByTag("td");
                    if (Html.escapeHtml(columns.get(1).text()).equals("&#10004;")) {
                        status.put(columns.get(0).text(), true);
                    } else {
                        status.put(columns.get(0).text(), false);
                    }
                    Log.v("COLUMN", status.toString());
                }
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(WifiStatusActivity.this, "An unexpected error occurred. Please try again later.");
                        finish();
                    }
                });

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!status.isEmpty()) {
                Iterator iterator = status.entrySet().iterator();
                int count = 0;
                while (iterator.hasNext()) {
                    Map.Entry pair = (Map.Entry) iterator.next();
                    titles.get(count).setText((CharSequence) pair.getKey());
                    boolean status = (boolean) pair.getValue();
                    messages.get(count).setText(status ? "Working" : "Not working");
                    messages.get(count).setTextColor(Color.parseColor(status ? "#51F11F" : "#FF201B"));
                    images.get(count).setImageResource(status ? R.drawable.ic_tick : R.drawable.ic_error);
                    count++;
                }
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            } else {
                Utils.showToast(WifiStatusActivity.this, "An unexpected error occurred. Please try again later.");
                finish();
            }
            super.onPostExecute(aVoid);
        }
    }
}