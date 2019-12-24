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

package in.co.rajkumaar.amritarepo.papers;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;
import in.co.rajkumaar.amritarepo.helpers.clearCache;

public class AssessmentsActivity extends AppCompatActivity {

    String href;
    String externLink;
    List<String> assessments = new ArrayList<>();
    int statusCode;
    List<String> links = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        new clearCache().clear();
        String protocol = getString(R.string.protocol);
        String cloudSpace = getString(R.string.clouDspace);
        String amrita = getString(R.string.amrita);
        String port = getString(R.string.port);
        externLink = protocol + cloudSpace + amrita + port;
        //semUrl=externLink+getString(R.string.xmlUi+R.string.numbers);
        Bundle bundle = getIntent().getExtras();
        href = bundle.get("href").toString();
        this.setTitle("" + bundle.get("pageTitle"));


        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);

        TextView textView = findViewById(R.id.empty_view);
        textView.setVisibility(View.GONE);
        TextView wifiwarning = findViewById(R.id.wifiwarning);
        wifiwarning.setVisibility(View.GONE);
        if (Utils.isConnected(AssessmentsActivity.this))
            new Load().execute();
        else
            Utils.showSnackBar(AssessmentsActivity.this, "Device not connected to Internet");
    }

    @SuppressLint("PrivateResource")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    @SuppressLint("StaticFieldLeak")
    private class Load extends AsyncTask<Void, Void, Void> {
        String proxy = getString(R.string.proxyurl);

        Document document = null;

        @Override
        protected Void doInBackground(Void... voids) {
            assessments.clear();
            links.clear();
            // Connect to the web site

            try {
                statusCode = Jsoup.connect(href).execute().statusCode();
                document = Jsoup.connect(href).get();
            } catch (IOException e) {
                try {
                    document = (Jsoup.connect(proxy).method(Connection.Method.POST).data("data", href).data("hash", BuildConfig.SECRET_HASH).execute().parse());
                    statusCode = (Jsoup.connect(proxy).method(Connection.Method.POST).data("data", href).data("hash", BuildConfig.SECRET_HASH).execute().statusCode());

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();

            } finally {

                try {
                    if (document != null) {
                        Elements elements = document.select("div[id=aspect_artifactbrowser_CommunityViewer_div_community-view]").select("ul[xmlns:i18n=http://apache.org/cocoon/i18n/2.1]");
                        if (elements.size() > 1) elements = elements.get(1).select("a[href]");
                        else elements = elements.get(0).select("a[href]");
                        for (int i = 0; i < elements.size(); ++i) {
                            Log.e("ASSESSMENTS " + i, elements.get(i).text());
                            assessments.add(elements.get(i).text());
                            links.add(elements.get(i).attr("href"));
                        }
                    }
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showUnexpectedError(AssessmentsActivity.this);
                            finish();
                        }
                    });
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            findViewById(R.id.loading_indicator).setVisibility(View.GONE);
            if (statusCode != 200) {
                TextView emptyView = findViewById(R.id.empty_view);
                emptyView.setVisibility(View.VISIBLE);
                TextView wifiwarning = findViewById(R.id.wifiwarning);
                wifiwarning.setVisibility(View.VISIBLE);
            } else {
                TextView textView = findViewById(R.id.empty_view);
                textView.setVisibility(View.GONE);

                TextView wifiwarning = findViewById(R.id.wifiwarning);
                wifiwarning.setVisibility(View.GONE);
                ListView listView = findViewById(R.id.list);
                PaperAdapter semsAdapter = new PaperAdapter(AssessmentsActivity.this, assessments, "assessments");
                semsAdapter.setCustomListener(new PaperAdapter.customListener() {
                    @Override
                    public void onItemClickListener(int i) {
                        if (Utils.isConnected(AssessmentsActivity.this)) {
                            Intent intent = new Intent(AssessmentsActivity.this, SubjectsActivity.class);
                            intent.putExtra("href", externLink + links.get(i));
                            intent.putExtra("pageTitle", assessments.get(i));
                            startActivity(intent);
                        } else
                            Utils.showSnackBar(AssessmentsActivity.this, "Device not connected to Internet");
                    }
                });
                listView.setAdapter(semsAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                });
                listView.setVisibility(View.VISIBLE);
            }
        }
    }
}
