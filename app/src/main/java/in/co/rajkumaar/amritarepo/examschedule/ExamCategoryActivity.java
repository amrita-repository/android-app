/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.examschedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;
import in.co.rajkumaar.amritarepo.papers.PaperAdapter;

public class ExamCategoryActivity extends BaseActivity {

    private String urlExams;
    private ListView listView;
    private PaperAdapter scheduleBlockArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_schedule);

        urlExams = getResources().getString(R.string.url_exams);
        listView = findViewById(R.id.list);
        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);

        retrieveSchedules();
    }

    private void retrieveSchedules() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        final Type listOfExamItemType = new TypeToken<ArrayList<ExamItem>>() {
        }.getType();
        final Gson gson = new Gson();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlExams,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Document document = Jsoup.parse(response);
                            Element firstTitle = document.select("div.field-items").select("p").first();
                            Elements siblings = firstTitle.siblingElements();
                            final HashMap<Integer, ArrayList<ExamItem>> examItems = new HashMap<>();
                            ArrayList<ExamItem> examItemArrayList = new ArrayList<>();
                            ArrayList<String> categories = new ArrayList<>();
                            categories.add(firstTitle.text());
                            int countIndex = 0;
                            for (int i = 0; i < siblings.size(); i++) {
                                Element sibling = siblings.get(i);
                                if (!"p".equals(sibling.tagName())) {
                                    if ("ul".equals(sibling.tagName())) {
                                        Elements listItems = sibling.select("li");
                                        for (Element item : listItems) {
                                            examItemArrayList.add(
                                                    new ExamItem(item.text().trim(), item.select("a[href]").attr("href"))
                                            );
                                        }
                                    }
                                } else {
                                    examItems.put(countIndex, examItemArrayList);
                                    examItemArrayList = new ArrayList<>();
                                    if (sibling.select("a>span").attr("style").equals("color:#D3D3D3")) {
                                        break;
                                    }
                                    categories.add(sibling.select("a>strong").text());
                                    countIndex++;
                                }
                            }
                            if (!examItemArrayList.isEmpty()) {
                                examItems.put(countIndex, examItemArrayList);
                            }

                            scheduleBlockArrayAdapter = new PaperAdapter(ExamCategoryActivity.this, categories, "examcategory");
                            scheduleBlockArrayAdapter.setCustomListener(new PaperAdapter.customListener() {
                                @Override
                                public void onItemClickListener(int i) {
                                    if (Utils.isConnected(ExamCategoryActivity.this)) {
                                        startActivity(
                                                new Intent(ExamCategoryActivity.this, ExamsListActivity.class).putExtra("exams", gson.toJson(examItems.get(i), listOfExamItemType)));
                                    } else {
                                        Utils.showInternetError(ExamCategoryActivity.this);
                                    }
                                }
                            });
                            listView.setAdapter(scheduleBlockArrayAdapter);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ExamCategoryActivity.this, "Unexpected error. Please try again later", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        findViewById(R.id.loading_indicator).setVisibility(View.GONE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(ExamCategoryActivity.this, "Unexpected error. Please try again later", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        requestQueue.add(stringRequest);
    }

    public static class ExamItem {
        private String title;
        private String link;

        private ExamItem(String title, String link) {
            this.title = title;
            this.link = link;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
