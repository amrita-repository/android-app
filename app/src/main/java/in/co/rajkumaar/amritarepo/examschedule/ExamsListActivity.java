/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.examschedule;

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
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Random;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.DownloadTask;
import in.co.rajkumaar.amritarepo.helpers.OpenTask;
import in.co.rajkumaar.amritarepo.helpers.Utils;
import in.co.rajkumaar.amritarepo.papers.PaperAdapter;

public class ExamsListActivity extends BaseActivity {

    private String url_exams;

    private ArrayList<String> texts;
    private ArrayList<String> links;
    private int block;
    private PaperAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exams_under_each_dept);

        url_exams = getResources().getString(R.string.url_exams);
        listView = findViewById(R.id.list);
        block = getIntent().getExtras().getInt("block");
        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);

        texts = new ArrayList<>();
        links = new ArrayList<>();

        new GetExams().execute();
    }


    @SuppressLint("StaticFieldLeak")
    class GetExams extends AsyncTask<Void, Void, Void> {
        private Document document = null;
        private Elements ul_lists;

        @Override
        protected void onPreExecute() {
            texts.clear();
            links.clear();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                document = Jsoup.connect(url_exams).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            try {
                ul_lists = document.select("div.field-items").select("ul");
                for (int j = 0; j < ul_lists.get(block).select("li").size(); ++j) {

                    String text = ul_lists.get(block).select("li").get(j).select("li").text();
                    texts.add(text.trim());
                    links.add(ul_lists.get(block).select("li").get(j).select("a[href]").attr("href"));
                }
                adapter = new PaperAdapter(ExamsListActivity.this, texts, "examlist");

                adapter.setCustomListener(new PaperAdapter.customListener() {
                    @Override
                    public void onItemClickListener(final int i) {
                        final ArrayList<String> qPaperOptions = new ArrayList<>();
                        qPaperOptions.add("Open");
                        qPaperOptions.add("Download");
                        AlertDialog.Builder qPaperBuilder = new AlertDialog.Builder(ExamsListActivity.this);
                        ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(ExamsListActivity.this, android.R.layout.simple_list_item_1, qPaperOptions);
                        qPaperBuilder.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                if (pos == 0) {
                                    if (ContextCompat.checkSelfPermission(ExamsListActivity.this,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {

                                        ActivityCompat.requestPermissions(ExamsListActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                1);
                                    } else {
                                        if (Utils.isConnected(ExamsListActivity.this)) {
                                            new OpenTask(ExamsListActivity.this, "https://intranet.cb.amrita.edu" + links.get(i), 2);
                                        } else {
                                            Utils.showInternetError(ExamsListActivity.this);
                                        }


                                    }
                                } else if (pos == 1) {
                                    if (ContextCompat.checkSelfPermission(ExamsListActivity.this,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {

                                        ActivityCompat.requestPermissions(ExamsListActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                1);
                                    } else {
                                        if (Utils.isConnected(ExamsListActivity.this)) {
                                            new DownloadTask(ExamsListActivity.this, "https://intranet.cb.amrita.edu" + links.get(i), 2);
                                        } else {
                                            Utils.showInternetError(ExamsListActivity.this);
                                        }


                                    }
                                }
                            }
                        });
                        qPaperBuilder.show();


                    }
                });
                listView.setAdapter(adapter);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ExamsListActivity.this, "Unexpected error. Please try again later", Toast.LENGTH_SHORT).show();
                finish();
            }

            findViewById(R.id.loading_indicator).setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }
}
