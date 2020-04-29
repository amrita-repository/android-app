/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.util.Strings;
import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.apache.ftpserver.ftplet.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.aums.helpers.HomeItemAdapter;
import in.co.rajkumaar.amritarepo.aums.helpers.UserData;
import in.co.rajkumaar.amritarepo.aums.models.HomeItem;
import in.co.rajkumaar.amritarepo.helpers.Utils;

import static android.view.View.GONE;

public class CourseResourcesActivity extends BaseActivity {

    private String courseId, courseName, courseCode;
    private ListView list;
    private ArrayList<CourseResource> courseResourceList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courseresources);

        list = findViewById(R.id.courses_list);
        courseResourceList = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);


        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);

        Bundle bundle = getIntent().getExtras();
        courseId = bundle.get("courseID").toString();
        courseName = bundle.get("courseName").toString();
        courseCode = bundle.get("courseCode").toString();

        getCourseResources(UserData.client);

    }
    private void getCourseResources(final AsyncHttpClient client){
        System.out.println(UserData.domain + "/access/content/group/"+ courseId+"/");
        client.get(UserData.domain + "/access/content/group/" + courseId + "/", new AsyncHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                System.out.println(doc.toString());
                /**Elements tRows = doc.select("body > div > table > tbody > tr");
                String lastRes = tRows.last().select("td:nth-child(1) > a").first().text().trim();
                final ArrayList<CourseResource> courseResourceList = new ArrayList<>();

                for (Element row : tRows) {
                    String resName = row.select("td:nth-child(1) > a").first().text().trim();
                    courseResourceList.add(new CourseResource(courseId, resName));
                    if(resName.equals(lastRes)){
                        courseResAdapter CourseResAdapter = new courseResAdapter(CourseResourcesActivity.this, courseResourceList);
                        list.setAdapter(CourseResAdapter);
                        System.out.println("REACHED");
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);
                    }
                }*/
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void trialgetCourseResources(final AsyncHttpClient client) {
        System.out.println("https://amritavidya1.amrita.edu:8444/portal/site/" + courseId);
        client.get("https://amritavidya1.amrita.edu:8444/portal/tool/12884493-68dd-4803-88c5-ee2cd21f4b65?panel=Main", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                System.out.println(doc.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }
    public class CourseResource {

        private String courseId;
        private String resourceFileName;
        private double bytesSize;

        public CourseResource() {
        }

        public CourseResource(String courseId, String resourceFileName) {
            this.courseId = courseId;
            this.resourceFileName = resourceFileName;
        }

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public String getResourceFileName() {
            return resourceFileName;
        }

        public void setResourceFileName(String resourceFileName) {
            this.resourceFileName = resourceFileName;
        }

        public double getBytesSize() {
            return bytesSize;
        }

        public void setBytesSize(double bytesSize) {
            this.bytesSize = bytesSize;
        }
    }
    public class courseResAdapter extends ArrayAdapter<CourseResource> {

        public courseResAdapter(Context context, ArrayList<CourseResource> Resources) {
            super(context, 0, Resources);
        }
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View listItemView = convertView;
            if (listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.home_item, parent, false);
            }


            final CourseResource current = getItem(position);


            TextView title = listItemView.findViewById(R.id.title);
            ImageView imageView = listItemView.findViewById(R.id.image);

            title.setText(current.getResourceFileName());
            imageView.setImageResource(R.drawable.resfolder);


            return listItemView;

        }

    }

}
