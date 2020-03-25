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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import in.co.rajkumaar.amritarepo.helpers.DownloadTask;
import in.co.rajkumaar.amritarepo.helpers.OpenTask;
import in.co.rajkumaar.amritarepo.helpers.Utils;
import in.co.rajkumaar.amritarepo.helpers.clearCache;

public class SubjectsActivity extends AppCompatActivity {

    private String href;
    private String externLink;
    private List<String> assessments = new ArrayList<>();
    private int statusCode;
    private String proxy;
    private List<String> links = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subjects_listview);

        proxy = BuildConfig.PROXY_URL;
        String protocol = getString(R.string.protocol);
        String cloudSpace = getString(R.string.clouDspace);
        String amrita = getString(R.string.amrita);
        String port = getString(R.string.port);

        externLink = protocol + cloudSpace + amrita + port;
        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);

        TextView textView = findViewById(R.id.empty_view);
        textView.setVisibility(View.GONE);
        TextView wifiWarning = findViewById(R.id.wifiwarning);
        wifiWarning.setVisibility(View.GONE);
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        href = "" + bundle.get("href");
        this.setTitle("" + bundle.get("pageTitle"));
        new clearCache().clear(this);
        new Load().execute();
    }

    @SuppressLint("PrivateResource")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    @SuppressLint("StaticFieldLeak")
    private class Load extends AsyncTask<Void, Void, Void> {
        private Document nextDoc;
        private Document document = null;

        @Override
        protected Void doInBackground(Void... voids) {
            assessments.clear();
            links.clear();
            try {
                // Connect to the web site
                statusCode = Jsoup.connect(href).execute().statusCode();
                document = Jsoup.connect(href).get();
            } catch (IOException e) {

                try {
                    document = (Jsoup.connect(proxy).method(Connection.Method.POST).data("hash", BuildConfig.SECRET_HASH).data("data", href).execute().parse());
                    statusCode = (Jsoup.connect(proxy).method(Connection.Method.POST).data("hash", BuildConfig.SECRET_HASH).data("data", href).execute().statusCode());
                } catch (IOException v) {
                    v.printStackTrace();
                }
                e.printStackTrace();

            } finally {
                try {
                    if (document != null) {
                        Elements elements = document.select("div[xmlns=http://di.tamu.edu/DRI/1.0/]").get(0).select("ul").get(0).select("li").get(0).select("a[href]");
                        String nextUrl = externLink + elements.get(0).attr("href");
                        nextDoc = null;
                        try {
                            nextDoc = Jsoup.connect(nextUrl).get();

                        } catch (IOException ex) {
                            try {
                                nextDoc = (Jsoup.connect(proxy).method(Connection.Method.POST).data("hash", BuildConfig.SECRET_HASH).data("data", nextUrl).execute().parse());
                            } catch (IOException r) {
                                r.printStackTrace();
                            }

                            ex.printStackTrace();
                        }
                    }
                } catch (NullPointerException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (nextDoc != null) {
                    Elements nextLinks = nextDoc.select("div[id=aspect_artifactbrowser_ItemViewer_div_item-view]").get(0).select("div[xmlns:i18n=http://apache.org/cocoon/i18n/2.1]").select("a[href]");
                    Elements nextElements = nextDoc.select("div[id=aspect_artifactbrowser_ItemViewer_div_item-view]").get(0).select("span[xmlns:i18n=http://apache.org/cocoon/i18n/2.1]");
                    for (int i = 0; i < nextElements.size(); ++i)
                        if (!nextElements.get(i).attr("title").isEmpty()) {
                            assessments.add(nextElements.get(i).attr("title"));
                        }
                    for (int i = 0; i < nextLinks.size(); i += 2)
                        links.add(nextLinks.get(i).attr("href"));
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                Utils.showUnexpectedError(SubjectsActivity.this);
                SubjectsActivity.this.finish();
            }
            findViewById(R.id.loading_indicator).setVisibility(View.GONE);
            if (statusCode != 200) {
                TextView emptyView = findViewById(R.id.empty_view);
                emptyView.setVisibility(View.VISIBLE);
                TextView wifiwarning = findViewById(R.id.wifiwarning);
                wifiwarning.setVisibility(View.VISIBLE);
            } else {
                TextView emptyView = findViewById(R.id.empty_view);
                emptyView.setVisibility(View.GONE);
                TextView wifiwarning = findViewById(R.id.wifiwarning);
                wifiwarning.setVisibility(View.GONE);
                ListView listView = findViewById(R.id.list);
                PaperAdapter subsAdapter = new PaperAdapter(SubjectsActivity.this, assessments, "subjects");
                subsAdapter.setCustomListener(new PaperAdapter.customListener() {
                    @Override
                    public void onItemClickListener(int i) {
                        final ArrayList<String> qPaperOptions = new ArrayList<>();
                        qPaperOptions.add("Open");
                        qPaperOptions.add("Download");
                        final int p = i;
                        AlertDialog.Builder qPaperBuilder = new AlertDialog.Builder(SubjectsActivity.this);
                        ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(SubjectsActivity.this, android.R.layout.simple_list_item_1, qPaperOptions);
                        qPaperBuilder.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                if (pos == 0) {
                                    String link = links.get(p).substring(0, links.get(p).indexOf("?"));
                                    new OpenTask(SubjectsActivity.this, externLink + link, 1);
                                } else if (pos == 1) {
                                    if (ContextCompat.checkSelfPermission(SubjectsActivity.this,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {

                                        ActivityCompat.requestPermissions(SubjectsActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                1);
                                    } else {
                                        if (Utils.isConnected(SubjectsActivity.this)) {
                                            new DownloadTask(SubjectsActivity.this, externLink + links.get(p), 1);
                                        } else {
                                            Utils.showToast(SubjectsActivity.this, "Device not connected to Internet.");
                                        }


                                    }
                                }
                            }
                        });
                        qPaperBuilder.show();

                    }
                });
                listView.setAdapter(subsAdapter);
                listView.setVisibility(View.VISIBLE);
            }
        }
    }
}
