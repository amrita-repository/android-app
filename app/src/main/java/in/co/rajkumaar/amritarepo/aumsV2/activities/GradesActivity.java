/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aumsV2.activities;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.aumsV2.helpers.GlobalData;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class GradesActivity extends BaseActivity {

    ListView list;
    String sem;
    private AsyncHttpClient client = GlobalData.getClient();
    private SharedPreferences preferences;
    private ArrayList<CourseData> gradesData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);
        preferences = getSharedPreferences("aums-lite", MODE_PRIVATE);
        list = findViewById(R.id.list);

        sem = getIntent().getStringExtra("sem");
        findViewById(R.id.section_header).setVisibility(View.GONE);
        getGrades(getIntent().getStringExtra("sem"));
    }


    void getGrades(final String sem) {
        gradesData = new ArrayList<>();
        client.addHeader("Authorization", GlobalData.auth);
        client.addHeader("token", preferences.getString("token", ""));
        client.get("https://amritavidya.amrita.edu:8444/DataServices/rest/andRes?rollno=" + preferences.getString("username", "") + "&sem=" + sem, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(bytes));
                    JSONArray subjects = jsonObject.getJSONArray("Subject");
                    Log.e("SEM", sem);
                    Log.e("SUBS", jsonObject.toString());
                    for (int j = 0; j < subjects.length(); ++j) {
                        JSONObject current = subjects.getJSONObject(j);
                        CourseData courseData = new CourseData();
                        courseData.setCode(current.getString("CourseCode"));
                        courseData.setTitle(current.getString("CourseName"));
                        courseData.setGrade(current.getString("Grade"));
                        gradesData.add(courseData);
                    }
                    GradesAdapter gradesAdapter = new GradesAdapter(GradesActivity.this, gradesData);
                    list.setAdapter(gradesAdapter);
                    list.setVisibility(View.VISIBLE);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                } catch (Exception e) {
                    Utils.showUnexpectedError(GradesActivity.this);
                    GlobalData.resetUser(GradesActivity.this);
                    finish();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Utils.showUnexpectedError(GradesActivity.this);
                GlobalData.resetUser(GradesActivity.this);
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

            code_type.setText(current.getCode());
            title.setText(current.getTitle());
            grade.setText(current.getGrade());


            return listItemView;

        }
    }


}
