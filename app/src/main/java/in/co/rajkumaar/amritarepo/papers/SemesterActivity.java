/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.papers;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

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
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.ClearCache;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class SemesterActivity extends BaseActivity {

    private String semUrl;
    private String externLink;
    private int statusCode;
    private List<String> sems = new ArrayList<>();
    private List<String> links = new ArrayList<>();

    @SuppressLint("PrivateResource")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        new ClearCache().clear(this);
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
        private String proxy = BuildConfig.PROXY_URL;
        private Document document = null;
        private Elements elements;

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
                Utils.showUnexpectedError(SemesterActivity.this);
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
                    PaperAdapter semsAdapter = new PaperAdapter(SemesterActivity.this, sems, "sem");
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
