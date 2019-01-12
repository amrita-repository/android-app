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

package in.co.rajkumaar.amritarepo.aums.activities;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.aums.helpers.UserData;

public class GradesActivity extends AppCompatActivity {

    ListView list;
    android.support.v7.app.ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);
        actionBar=getSupportActionBar();
        list=findViewById(R.id.list);


        UserData.refIndex =1;

        getGrades(UserData.client,getIntent().getStringExtra("sem"));
    }




    void getGrades(final AsyncHttpClient client,final String sem){
        RequestParams params = new RequestParams();
        params.put("htmlPageTopContainer_selectStep", sem);
        params.put("Page_refIndex_hidden", UserData.refIndex++);
        params.put("htmlPageTopContainer_hiddentblGrades", "");
        params.put("htmlPageTopContainer_status", "");
        params.put("htmlPageTopContainer_action", "UMS-EVAL_STUDPERFORMSURVEY_CHANGESEM_SCREEN");
        params.put("htmlPageTopContainer_notify", "");

        client.post(UserData.domain+"/aums/Jsp/StudentGrade/StudentPerformanceWithSurvey.jsp?action=UMS-EVAL_STUDPERFORMSURVEY_INIT_SCREEN&isMenu=true&pagePostSerialID=0", params, new AsyncHttpResponseHandler() {
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
                                    Toast.makeText(GradesActivity.this,"Grades unavailable for this semester!",Toast.LENGTH_LONG).show();
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
                                            Toast.makeText(GradesActivity.this,"Grades unavailable for this semester!",Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    } catch (Exception e) {
                                        sgpa = "N/A";
                                    }
                                }
                            }

                            try{
                                actionBar.setTitle("This semester's GPA : "+sgpa);
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }
                            GradesAdapter gradesAdapter = new GradesAdapter(GradesActivity.this,courseGradeDataList);
                            list.setAdapter(gradesAdapter);
                            list.setVisibility(View.VISIBLE);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Toast.makeText(GradesActivity.this,getString(R.string.site_change),Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(GradesActivity.this,"An error occurred while connecting to server",Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }


    class CourseData {
        private String code,title,type,grade;

        public void setTitle(String title) {
            this.title = title;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public String getCode() {
            return code;
        }

        public String getGrade() {
            return grade;
        }

        public String getType() {
            return type;
        }
    }


    class GradesAdapter extends ArrayAdapter<CourseData> {
        GradesAdapter(Context context, ArrayList<CourseData> HomeItems) {
            super(context, 0,HomeItems);
        }


        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View listItemView = convertView;
            if(listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.grades_item, parent, false);
            }


            final CourseData current=getItem(position);


            TextView title = listItemView.findViewById(R.id.title);
            TextView code_type = listItemView.findViewById(R.id.code_type);
            TextView grade = listItemView.findViewById(R.id.grade);
            ImageView color = listItemView.findViewById(R.id.circle);

            if(current.getGrade().contains("F"))
                color.setBackgroundColor(getResources().getColor(R.color.danger));
            else
                color.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

            code_type.setText(current.getCode() + " - " + current.getType());
            title.setText(current.getTitle());
            grade.setText(current.getGrade());



            return listItemView;

        }
    }


}
