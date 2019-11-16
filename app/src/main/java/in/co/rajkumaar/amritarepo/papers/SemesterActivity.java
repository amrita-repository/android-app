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
import android.view.View;
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

public class SemesterActivity extends AppCompatActivity {

    String semUrl;
    String externLink;
    int statusCode;
    List<String> sems = new ArrayList<>();
    List<String> links = new ArrayList<>();
    PaperAdapter semsAdapter;

    @SuppressLint("PrivateResource")
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
        String xmlUi = getString(R.string.xmlUi);
        String numbers = getString(R.string.numbers);
        semUrl = externLink + xmlUi + numbers;
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        Bundle b = getIntent().getExtras();
        assert b != null;
        int course = Integer.parseInt("" + b.get("course"));
        this.setTitle("" + b.get("pageTitle"));


        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);

        TextView textView = findViewById(R.id.empty_view);
        textView.setVisibility(View.GONE);
        TextView wifiwarning = findViewById(R.id.wifiwarning);
        wifiwarning.setVisibility(View.GONE);
        switch (course) {
            case 0:
                semUrl += "150";
                break;
            case 1:
                semUrl += "893";
                break;
            case 2:
                semUrl += "894";
                break;
            case 3:
                semUrl += "903";
                break;
            case 4:
                semUrl += "331";
                break;
            case 5:
                semUrl += "393";
                break;
            case 6:
                semUrl += "279";
                break;
            case 7:
                semUrl += "2415";
                break;

        }

        if (Utils.isConnected(this))
            new Load().execute();
        else
            Utils.showSnackBar(this, "Device not connected to Internet");
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
        Elements elements;

        @Override
        protected Void doInBackground(Void... voids) {
            sems.clear();
            links.clear();

            try {
                statusCode = Jsoup.connect(semUrl).execute().statusCode();
                document = Jsoup.connect(semUrl).get();
            } catch (IOException e1) {
                try {
                    document = Jsoup.connect(proxy).method(Connection.Method.POST).data("data", semUrl).data("hash", BuildConfig.SECRET_HASH).execute().parse();
                    statusCode = Jsoup.connect(proxy).method(Connection.Method.POST).data("data", semUrl).data("hash", BuildConfig.SECRET_HASH).execute().statusCode();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                e1.printStackTrace();
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (document != null) {
                    elements = document.select("div[id=aspect_artifactbrowser_CommunityViewer_div_community-view]").select("ul[xmlns:i18n=http://apache.org/cocoon/i18n/2.1]").get(0).select("a[href]");
                    for (int i = 0; i < elements.size(); ++i) {
                        sems.add(elements.get(i).text());
                        links.add(elements.get(i).attr("href"));
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                Toast.makeText(SemesterActivity.this, "Some error occurred. Please try using Amrita Wi-Fi. If problem still persists, report to the developer.", Toast.LENGTH_LONG).show();
                SemesterActivity.this.finish();
            }
            findViewById(R.id.loading_indicator).setVisibility(View.GONE);
            if (statusCode != 200) {
                TextView emptyView = findViewById(R.id.empty_view);
                emptyView.setVisibility(View.VISIBLE);
                TextView wifiWarning = findViewById(R.id.wifiwarning);
                wifiWarning.setVisibility(View.VISIBLE);
            } else {
                if (elements != null) {
                    TextView textView = findViewById(R.id.empty_view);
                    textView.setVisibility(View.GONE);
                    TextView wifiwarning = findViewById(R.id.wifiwarning);
                    wifiwarning.setVisibility(View.GONE);
                    ListView listView = findViewById(R.id.list);
                    semsAdapter = new PaperAdapter(SemesterActivity.this, sems, "sem");
                    semsAdapter.setCustomListener(new PaperAdapter.customListener() {
                        @Override
                        public void onItemClickListener(int i) {
                            if (Utils.isConnected(SemesterActivity.this)) {
                                Intent intent = new Intent(SemesterActivity.this, AssessmentsActivity.class);
                                intent.putExtra("href", externLink + links.get(i));
                                intent.putExtra("pageTitle", sems.get(i));
                                startActivity(intent);
                            } else
                                Utils.showSnackBar(SemesterActivity.this, "Device not connected to Internet");
                        }
                    });
                    listView.setAdapter(semsAdapter);
                    listView.setVisibility(View.VISIBLE);
                } else {
                    TextView textView = findViewById(R.id.empty_view);
                    textView.setText("An unexpected error occurred. Please report to the developer.");
                    textView.setVisibility(View.VISIBLE);

                }
            }
        }

    }
}
