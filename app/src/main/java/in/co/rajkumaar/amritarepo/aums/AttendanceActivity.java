package in.co.rajkumaar.amritarepo.aums;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ivbaranov.mli.MaterialLetterIcon;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;

public class AttendanceActivity extends AppCompatActivity {

    String domain;
    ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        list=findViewById(R.id.list);


        domain = UserData.domain;
        String sem = getIntent().getStringExtra("sem");
        getAttendance(UserData.client,sem);
    }

    void getAttendance(final AsyncHttpClient client, final String sem){
        RequestParams params = new RequestParams();
        params.put("action", "UMS-ATD_INIT_ATDREPORTSTUD_SCREEN");
        params.put("isMenu", "true");
        client.get(domain+"/aums/Jsp/Attendance/AttendanceReportStudent.jsp", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        RequestParams params = new RequestParams();
                        params.put("htmlPageTopContainer_selectSem", sem);
                        params.put("Page_refIndex_hidden", UserData.refIndex++);
                        params.put("htmlPageTopContainer_selectCourse", "0");
                        params.put("htmlPageTopContainer_selectType", "1");
                        params.put("htmlPageTopContainer_hiddentSummary", "");
                        params.put("htmlPageTopContainer_status", "");
                        params.put("htmlPageTopContainer_action", "UMS-ATD_SHOW_ATDSUMMARY_SCREEN");
                        params.put("htmlPageTopContainer_notify", "");
                        client.addHeader("Referer",domain + "/aums/Jsp/Attendance/AttendanceReportStudent.jsp?task=off");
                        client.post(domain+"/aums/Jsp/Attendance/AttendanceReportStudent.jsp?action=UMS-ATD_INIT_ATDREPORTSTUD_SCREEN&isMenu=true&pagePostSerialID=0", params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                Document doc = Jsoup.parse(new String(responseBody));
                                //Toast.makeText(AttendanceActivity.this,new String(responseBody),Toast.LENGTH_LONG).show();
                                try {
                                    Element table = doc.select("table[width=75%] > tbody").first();
                                    Elements rows = table.select("tr:gt(0)");
                                    if (rows.toString().equals("")) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(AttendanceActivity.this,"Attendance data unavailable for this semester!",Toast.LENGTH_LONG).show();
                                                finish();
                                            }
                                        });
                                    } else {
                                        rows = table.select("tr");
                                        ArrayList<CourseData> attendanceData = new ArrayList<>();
                                        int index = 0;
                                        for (Element row : rows) {
                                            index++;
                                            if ((index & 1) == 0) {
                                                Elements dataHolders = row.select("td > span");
                                                CourseData courseData = new CourseData();
                                                courseData.setCode(dataHolders.get(0).text());
                                                courseData.setTitle(dataHolders.get(1).text());
                                                courseData.setTotal(dataHolders.get(5).text());
                                                courseData.setAttended(dataHolders.get(6).text());
                                                courseData.setPercentage(dataHolders.get(7).text());
                                                attendanceData.add(courseData);
                                            }
                                        }
                                        AttendanceAdapter attendanceAdapter = new AttendanceAdapter(AttendanceActivity.this,attendanceData);
                                        list.setAdapter(attendanceAdapter);
                                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                                        list.setVisibility(View.VISIBLE);

                                    }
                                } catch (Exception e) {
                                    Toast.makeText(AttendanceActivity.this,getString(R.string.site_change),Toast.LENGTH_LONG).show();
                                    finish();
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(AttendanceActivity.this,"An error occurred while connecting to server",Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(AttendanceActivity.this,"An error occurred while connecting to server",Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }


    class CourseData {
        private String code,title,total,attended,percentage;

        public void setAttended(String attended) {
            this.attended = attended;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public void setPercentage(String percentage) {
            this.percentage = percentage;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getAttended() {
            return attended;
        }

        public String getCode() {
            return code;
        }

        public String getPercentage() {
            return percentage;
        }

        public String getTitle() {
            return title;
        }

        public String getTotal() {
            return total;
        }
    }


    class AttendanceAdapter extends ArrayAdapter<CourseData>{
        AttendanceAdapter(Context context, ArrayList<CourseData> HomeItems) {
            super(context, 0,HomeItems);
        }


        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View listItemView = convertView;
            if(listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.attendance_item, parent, false);
            }


            final CourseData current=getItem(position);


            TextView title = listItemView.findViewById(R.id.title);
            TextView attended = listItemView.findViewById(R.id.attended);
            TextView percentage = listItemView.findViewById(R.id.percentage);

            percentage.setText(Math.round(Double.parseDouble(current.getPercentage()))+"%");
            title.setText(current.getTitle());
            attended.setText(Html.fromHtml("You attended <b>" + Math.round(Double.parseDouble(current.getAttended())) + "</b> of <b>" + current.getTotal() + "</b> classes"));



            return listItemView;

        }
    }


}
