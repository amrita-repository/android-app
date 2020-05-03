/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
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

public class CoursesActivity extends BaseActivity {

    private ListView list;
    private ArrayList<CourseData> courseList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);
        list = findViewById(R.id.courses_list);
        courseList = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);


        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);

        getCourses(UserData.client);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Utils.isConnected(CoursesActivity.this)) {
                    CourseData courseData = (CourseData) list.getItemAtPosition(position);
                    Intent intent = new Intent(CoursesActivity.this, CourseResourcesActivity.class);
                    intent.putExtra("courseID", courseData.getId());
                    intent.putExtra("courseName", courseData.getCourseName());
                    startActivity(intent);
                } else {
                    Snackbar.make(view, "Device not connected to Internet.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getCourses(final AsyncHttpClient client){
        RequestParams params = new RequestParams();
        params.put("action", "UMS-EVAL_CLASSHEADER_SCREEN_INIT");
        client.get(UserData.domain + "/aums/Jsp/DefineComponent/ClassHeader.jsp?", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                try {
                    Element tr = doc.select("body > form > table > tbody > tr").first();
                    Elements dispCells = tr.select("td[width=\"21%\"]");
                    dispCells.remove(0);
                    Elements options = tr.select("td > select > option");
                    for (Element mainCell : dispCells) {
                        String fullName = mainCell.select("a").first().text().trim();
                        String fullUrl = mainCell.select("a").first().attr("href").trim();

                        String[] fullNameArray = fullName.split("\\.");
                        String[] fullUrlArray = fullUrl.split("/");

                        CourseData courseData = new CourseData();
                        courseData.setCourseCode(fullNameArray[fullNameArray.length - 1]);
                        courseData.setId(fullUrlArray[fullUrlArray.length - 1]);

                        if(courseData.getCourseCode() != null) {
                            if(courseData.getCourseCode().trim().length() > 3 ) {
                                courseList.add(courseData);
                            }
                        }
                    }

                    for (Element option : options) {
                        if (!option.attr("value").equals("0")) {
                            String fullName = option.text().trim();
                            String[] fullNameArray = fullName.split("\\.");
                            CourseData courseData = new CourseData();
                            courseData.setCourseCode(fullNameArray[fullNameArray.length - 1]);
                            courseData.setId(option.attr("value").trim());

                            if(courseData.getCourseCode() != null) {
                                if(courseData.getCourseCode().trim().length() > 3 ) {
                                    courseList.add(courseData);
                                }
                            }
                        }
                    }
                    final int[] responded  = {0};
                    for (final CourseData courseData : courseList) {
                        client.get(UserData.domain + "/access/site/"+ courseData.getId() +"/", new AsyncHttpResponseHandler(){

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                Document doc = Jsoup.parse(new String(responseBody));
                                Element div = doc.select("body > div").first();
                                String detailCourseName = div.text().trim();
                                String simpleCourseName = detailCourseName.replaceFirst(".+?(?=:)", "").split("_")[0].split(":")[1];
                                courseData.setCourseName(simpleCourseName);
                                responded[0]++;
                                if (responded[0] == courseList.size()) {
                                    CourseDataAdapter courseDataAdapter = new CourseDataAdapter(CoursesActivity.this, courseList);
                                    list.setAdapter(courseDataAdapter);

                                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                                    list.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                responded[0]++;
                            }
                        });
                    }
                }catch (Exception e){
                    Toast.makeText(CoursesActivity.this, getString(R.string.site_change), Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showUnexpectedError(CoursesActivity.this);
                finish();
            }
        });
    }

    public class CourseData {

        private String id;
        private String courseCode;
        private String courseName;

        public CourseData() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public void setCourseCode(String courseCode) {
            this.courseCode = courseCode;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }
    }

    public class CourseDataAdapter extends ArrayAdapter<CourseData> {
        private final Random random;
        private final Context context;

        public CourseDataAdapter(Context context, ArrayList<CourseData> Courses) {
            super(context, 0, Courses);
            this.context = context;
            random = new Random();
        }
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View listItemView = convertView;
            if (listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.course_item, parent, false);
            }
            final CourseData current = getItem(position);

            int[] mMaterial_Colors = getContext().getResources().getIntArray(R.array.colors);
            TextView courseName = listItemView.findViewById(R.id.title);
            TextView courseCode = listItemView.findViewById(R.id.code);
            ImageView imageView = listItemView.findViewById(R.id.image);

            courseName.setText(current.getCourseName());
            courseCode.setText(current.getCourseCode());

            imageView.setImageDrawable(new IconDrawable(context, FontAwesomeIcons.fa_folder_open)
                    .color(mMaterial_Colors[random.nextInt(mMaterial_Colors.length)]));

            return listItemView;
        }
    }
}