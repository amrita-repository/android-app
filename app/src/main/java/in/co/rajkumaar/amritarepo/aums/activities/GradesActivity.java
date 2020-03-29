/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.aums.helpers.UserData;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class GradesActivity extends BaseActivity {

    ListView list;
    TextView actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);

        actionBar = findViewById(R.id.section_header);
        list = findViewById(R.id.list);
        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);

        UserData.refIndex = 1;

        getGrades(UserData.client, getIntent().getStringExtra("sem"));
    }


    void getGrades(final AsyncHttpClient client, final String sem) {
        RequestParams params = new RequestParams();
        params.put("htmlPageTopContainer_selectStep", sem);
        params.put("Page_refIndex_hidden", UserData.refIndex++);
        params.put("htmlPageTopContainer_hiddentblGrades", "");
        params.put("htmlPageTopContainer_status", "");
        params.put("htmlPageTopContainer_action", "UMS-EVAL_STUDPERFORMSURVEY_CHANGESEM_SCREEN");
        params.put("htmlPageTopContainer_notify", "");

        client.post(UserData.domain + "/aums/Jsp/StudentGrade/StudentPerformanceWithSurvey.jsp?action=UMS-EVAL_STUDPERFORMSURVEY_INIT_SCREEN&isMenu=true&pagePostSerialID=0", params, new AsyncHttpResponseHandler() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                {

                    ArrayList<CourseData> courseGradeDataList = new ArrayList<>();
                    String sgpa = null;
                    Document doc = Jsoup.parse(new String(responseBody));

                    try {
                        Element PublishedState = doc.select("input[name=htmlPageTopContainer_status]").first();
                        if (PublishedState.attr("value").equals("Result Not Published.")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(GradesActivity.this, "Grades unavailable for this semester!", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                        } else {
                            Element table = doc.select("table[width=75%] > tbody").first();
                            Elements rows = table.select("tr:gt(0)");

                            for (Element row : rows) {
                                Elements dataHolders = row.select("td > span");

                                CourseData courseData = new CourseData();

                                if (dataHolders.size() > 2) {
                                    courseData.setCode(dataHolders.get(1).text());
                                    courseData.setTitle(dataHolders.get(2).text());
                                    courseData.setType(dataHolders.get(4).text());
                                    courseData.setGrade(dataHolders.get(5).text());
                                    courseGradeDataList.add(courseData);
                                } else {
                                    try {
                                        sgpa = dataHolders.get(1).text();
                                        if (sgpa == null || sgpa.trim().equals("null")) {
                                            Toast.makeText(GradesActivity.this, "Grades unavailable for this semester!", Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    } catch (Exception e) {
                                        sgpa = "N/A";
                                    }
                                }
                            }

                            try {
                                actionBar.setText("This semester's GPA : " + sgpa);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            GradesAdapter gradesAdapter = new GradesAdapter(GradesActivity.this, courseGradeDataList);
                            list.setAdapter(gradesAdapter);
                            list.setVisibility(View.VISIBLE);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Toast.makeText(GradesActivity.this, getString(R.string.site_change), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showUnexpectedError(GradesActivity.this);
                finish();
            }
        });
    }


    class CourseData {
        private String code, title, type, grade;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }


    class GradesAdapter extends ArrayAdapter<CourseData> {
        GradesAdapter(Context context, ArrayList<CourseData> HomeItems) {
            super(context, 0, HomeItems);
        }


        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View listItemView = convertView;
            if (listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.grades_item, parent, false);
            }


            final CourseData current = getItem(position);


            TextView title = listItemView.findViewById(R.id.title);
            TextView code_type = listItemView.findViewById(R.id.code_type);
            TextView grade = listItemView.findViewById(R.id.grade);
            ImageView color = listItemView.findViewById(R.id.circle);

            if (current.getGrade().contains("F") || current.getGrade().contains("I"))
                color.setBackgroundColor(getResources().getColor(R.color.danger));
            else if (current.getGrade().equals("C") || current.getGrade().equals("P"))
                color.setBackgroundColor(getResources().getColor(R.color.orange));
            else
                color.setBackgroundColor(getResources().getColor(R.color.green));

            code_type.setText(String.format("%s - %s", current.getCode(), current.getType()));
            title.setText(current.getTitle());
            grade.setText(current.getGrade());


            return listItemView;

        }
    }


}
