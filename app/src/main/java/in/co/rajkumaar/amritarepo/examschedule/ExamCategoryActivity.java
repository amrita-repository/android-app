/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.examschedule;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Random;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;
import in.co.rajkumaar.amritarepo.papers.PaperAdapter;

public class ExamCategoryActivity extends BaseActivity {

    private String urlExams;
    private ArrayList<String> headings;
    private ListView listView;
    private PaperAdapter scheduleBlockArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_schedule);

        urlExams = getResources().getString(R.string.url_exams);
        headings = new ArrayList<>();

        listView = findViewById(R.id.list);
        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);
        new RetrieveSchedule().execute();
    }


    @SuppressLint("StaticFieldLeak")
    private class RetrieveSchedule extends AsyncTask<Void, Void, Void> {
        private Document document = null;
        private Elements titles;
        private Elements ulLists;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                document = Jsoup.connect(urlExams).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {


            try {
                titles = document.select("div.field-items").select("p");
                ulLists = document.select("div.field-items").select("ul");
                Log.e("ELEMENTS ", String.valueOf(titles.size()));
                for (int i = 0; i < titles.size(); ++i) {
                    String spanstyle = titles.get(i).select("span[style]").attr("style");
                    String head = titles.get(i).select("p").text().trim();
                    if (head.length() > 0)
                        headings.add(head);
                } //Log.e("TEXT SIZE",String.valueOf(scheduleBlocks.get(1).getTexts().size()));


                scheduleBlockArrayAdapter = new PaperAdapter(ExamCategoryActivity.this, headings, "examcategory");

                scheduleBlockArrayAdapter.setCustomListener(new PaperAdapter.customListener() {
                    @Override
                    public void onItemClickListener(int i) {
                        if (Utils.isConnected(ExamCategoryActivity.this))
                            startActivity(new Intent(ExamCategoryActivity.this, ExamsListActivity.class).putExtra("block", i));
                        else
                            Utils.showInternetError(ExamCategoryActivity.this);
                    }
                });
                listView.setAdapter(scheduleBlockArrayAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ExamCategoryActivity.this, "Unexpected error. Please try again later", Toast.LENGTH_SHORT).show();
                finish();
            }
            findViewById(R.id.loading_indicator).setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }

}
