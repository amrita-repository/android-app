/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.aums.helpers.UserData;
import in.co.rajkumaar.amritarepo.helpers.CheckForSDCard;
import in.co.rajkumaar.amritarepo.helpers.Utils;


public class AttendanceActivity extends BaseActivity {

    private String domain;
    private ListView list;
    private Map<String, String> courses;
    private ArrayList<CourseData> attendanceData;
    private String sem;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        list = findViewById(R.id.list);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        courses = new HashMap<>();
        UserData.refIndex = 1;
        domain = UserData.domain;
        sem = getIntent().getStringExtra("sem");
        getAttendance(UserData.client, sem);
        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (ContextCompat.checkSelfPermission(AttendanceActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(AttendanceActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                } else {
                    if (Utils.isConnected(AttendanceActivity.this)) {
                        progressDialog.show();
                        if(courses.size() > 0)
                            getSubjectAttendance(UserData.client,attendanceData.get(position).getCode());
                        else
                            loadCourseMapping(UserData.client,sem,true,attendanceData.get(position).getCode());
                    } else {
                        Snackbar.make(view, "Device not connected to Internet.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void getSubjectAttendance(final AsyncHttpClient client, final String code) {
        client.get(domain + "/aums/Jsp/Attendance/AttendanceReportStudent.jsp?action=UMS-ATD_INIT_ATDREPORTSTUD_SCREEN&isMenu=true", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                RequestParams params = new RequestParams();
                params.put("htmlPageTopContainer_txtrollnumber", "");
                params.put("Page_refIndex_hidden", UserData.refIndex++);
                params.put("htmlPageTopContainer_selectSem", sem);
                params.put("htmlPageTopContainer_selectCourse", courses.get(code));
                params.put("htmlPageTopContainer_selectType", "1");
                params.put("htmlPageTopContainer_hiddentSummary", "");
                params.put("htmlPageTopContainer_status", "");
                params.put("htmlPageTopContainer_action", "UMS-ATD_SHOW_ATDREPORTSTUD_SCREEN");
                params.put("htmlPageTopContainer_notify", "");
                params.put("htmlPageTopContainer_hidrollNo", "Student");
                client.post(domain + "/aums/Jsp/Attendance/AttendanceReportStudent.jsp", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        new SaveAUMSReport().execute(responseBody);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Utils.showUnexpectedError(AttendanceActivity.this);
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showUnexpectedError(AttendanceActivity.this);
            }
        });
    }


    private void loadCourseMapping(final AsyncHttpClient client, final String sem, final boolean download, final String code) {
        RequestParams params = new RequestParams();
        params.put("Page_refIndex_hidden", UserData.refIndex++);
        params.put("htmlPageTopContainer_selectSem", sem);
        params.put("htmlPageTopContainer_selectCourse", "0");
        params.put("htmlPageTopContainer_selectType", "1");
        params.put("htmlPageTopContainer_txtrollnumber", "");
        params.put("htmlPageTopContainer_status", "");
        params.put("htmlPageTopContainer_action", "UMS-ATD_CHANGESEM_ATDREPORTSTUD_SCREEN");
        params.put("htmlPageTopContainer_notify", "");
        params.put("htmlPageTopContainer_hidrollNo", "Student");
        client.addHeader("Referer", domain + "/aums/Jsp/Attendance/AttendanceReportStudent.jsp?action=UMS-ATD_INIT_ATDREPORTSTUD_SCREEN&isMenu=true&pagePostSerialID=1");
        client.post(domain + "/aums/Jsp/Attendance/AttendanceReportStudent.jsp?action=UMS-ATD_INIT_ATDREPORTSTUD_SCREEN&isMenu=true&pagePostSerialID=0", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                try {
                    Element subjectSpinner = doc.select("select[name=htmlPageTopContainer_selectCourse]").get(0);
                    Elements items = subjectSpinner.select("option");
                    for (Element item : items) {
                        courses.put(item.text().split(":")[0].trim(), item.attr("value"));
                    }
                    if(courses.size() > 0 && download){
                        getSubjectAttendance(client,code);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }

    private void getAttendance(final AsyncHttpClient client, final String sem) {
        loadCourseMapping(client,sem,false,null);
        RequestParams params = new RequestParams();
        params.put("action", "UMS-ATD_INIT_ATDREPORTSTUD_SCREEN");
        params.put("isMenu", "true");
        client.get(domain + "/aums/Jsp/Attendance/AttendanceReportStudent.jsp", params, new AsyncHttpResponseHandler() {
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
                client.addHeader("Referer", domain + "/aums/Jsp/Attendance/AttendanceReportStudent.jsp?task=off");
                client.post(domain + "/aums/Jsp/Attendance/AttendanceReportStudent.jsp?action=UMS-ATD_INIT_ATDREPORTSTUD_SCREEN&isMenu=true&pagePostSerialID=0", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Document doc = Jsoup.parse(new String(responseBody));
                        try {
                            Element table = doc.select("table[width=75%] > tbody").first();
                            Elements rows = table.select("tr:gt(0)");
                            if (rows.toString().equals("")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showToast(AttendanceActivity.this, "Attendance data unavailable for this semester!");
                                        finish();
                                    }
                                });
                            } else {
                                rows = table.select("tr");
                                attendanceData = new ArrayList<>();
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
                                AttendanceAdapter attendanceAdapter = new AttendanceAdapter(AttendanceActivity.this, attendanceData);
                                list.setAdapter(attendanceAdapter);
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                list.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (getSharedPreferences("aums", MODE_PRIVATE).getBoolean("disclaimer", true)) {
                                            new AlertDialog.Builder(AttendanceActivity.this)
                                                    .setTitle("Disclaimer")
                                                    .setCancelable(false)
                                                    .setMessage(Html.fromHtml("Amrita Repository is not responsible if your attendance is not updated.<br><br>Follow the instructions given here <strong><font color=#AA0000>at your own risk.</font></strong>"))
                                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .setNegativeButton("Don\'t show again", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            getSharedPreferences("aums", MODE_PRIVATE).edit().putBoolean("disclaimer", false).apply();
                                                        }
                                                    })
                                                    .create()
                                                    .show();
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Toast.makeText(AttendanceActivity.this, getString(R.string.site_change), Toast.LENGTH_LONG).show();
                            finish();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(AttendanceActivity.this, getString(R.string.server_error), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(AttendanceActivity.this, getString(R.string.server_error), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.timings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.info) {
            final android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(AttendanceActivity.this);
            alertDialog.setMessage(Html.fromHtml("Amrita Repository is not responsible if your attendance is not updated.<br><br>Follow the instructions given here <strong><font color=#AA0000>at your own risk.</font></strong>"));
            alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    class SaveAUMSReport extends AsyncTask<byte[], String, String> {

        @Override
        protected String doInBackground(byte[]... file) {
            if (!new CheckForSDCard().isSDCardPresent()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AttendanceActivity.this, "Oops!! There is no storage.", Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                File report = new File(getExternalFilesDir(null), ".AmritaRepoCache");

                if (!report.exists()) {
                    report.mkdir();
                    Log.e("AUMS Report", "Directory Created.");
                }

                report = new File(report,"AUMSReport.pdf");

                if (report.exists()) {
                    report.delete();
                }
                try {
                    FileOutputStream fos = new FileOutputStream(report.getPath());
                    fos.write(file[0]);
                    fos.close();
                    Log.e("AUMS PDF", "Saved");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri data = FileProvider.getUriForFile(AttendanceActivity.this, BuildConfig.APPLICATION_ID + ".provider", report);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(data, "application/pdf");
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(intent);
                    else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showToast(AttendanceActivity.this, "Sorry, there's no appropriate app in the device to open this file.");
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                } catch (java.io.IOException e) {
                    Log.e("AUMS", "Exception in AUMS Attendance", e);
                    Utils.showUnexpectedError(AttendanceActivity.this);
                }
            }
            return (null);
        }
    }

    class CourseData {
        private String code;
        private String title;
        private String total;
        private String attended;
        private String percentage;

        public String getAttended() {
            return attended;
        }

        public void setAttended(String attended) {
            this.attended = attended;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getPercentage() {
            return percentage;
        }

        public void setPercentage(String percentage) {
            this.percentage = percentage;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        private String getTotal() {
            return total;
        }

        void setTotal(String total) {
            this.total = total;
        }

        private int getBunkingCount() {
            return Double.parseDouble(getPercentage()) > 75.0 ?
                    (int) Math.floor(Math.abs((Double.parseDouble(this.attended) / 76) * 100 - Double.parseDouble(total)))
                    : (int) Math.ceil(Math.abs(((0.75 * (Double.parseDouble(getTotal()))) - Double.parseDouble(getAttended())) / 0.25));
        }
    }


    class AttendanceAdapter extends ArrayAdapter<CourseData> {
        AttendanceAdapter(Context context, ArrayList<CourseData> HomeItems) {
            super(context, 0, HomeItems);
        }


        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View listItemView = convertView;
            if (listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.attendance_item, parent, false);
            }

            String[] idioms = {
                    "you will land in a big soup !",
                    "you will be in a crisis !",
                    "you will be in a jam !",
                    "you will be in a fix !",
                    "you will be in a bad situation !",
                    "you will be in a tight spot !",
                    "you will be in trouble !",
                    "you will be in a pickle !",
                    "you will be in a lurch !",
            };


            final CourseData current = getItem(position);


            TextView title = listItemView.findViewById(R.id.title);
            TextView attended = listItemView.findViewById(R.id.attended);
            TextView percentage = listItemView.findViewById(R.id.percentage);
            ImageView color = listItemView.findViewById(R.id.circle);
            TextView comments = listItemView.findViewById(R.id.comments);

            assert current != null;
            int percent = (int) Math.round(Double.parseDouble(current.getPercentage()));
            if (percent <= 75)
                color.setBackgroundColor(getResources().getColor(R.color.danger));
            else if (percent > 75 && percent < 85)
                color.setBackgroundColor(getResources().getColor(R.color.orange));
            else
                color.setBackgroundColor(getResources().getColor(R.color.green));

            percentage.setText(Math.round(Double.parseDouble(current.getPercentage())) + "%");
            title.setText(current.getTitle());
            attended.setText(Html.fromHtml("You attended <b>" + Math.round(Double.parseDouble(current.getAttended())) + "</b> of <b>" + current.getTotal() + "</b> classes"));
            comments.setVisibility(View.VISIBLE);
            if (percent > 94) {
                comments.setVisibility(View.GONE);
            } else if (percent < 95 && percent > 75 && current.getBunkingCount() > 0) {
                comments.setText(
                        (current.getBunkingCount() > 1)
                                ? "You miss " + current.getBunkingCount() + " more classes and " + idioms[new Random().nextInt((idioms.length))]
                                : "You miss " + current.getBunkingCount() + " more class and " + idioms[new Random().nextInt((idioms.length))]
                );
            } else if (percent == 75 || current.getBunkingCount() == 0) {
                comments.setText("Your situation is like the cat on the wall. Start going to class and be on safer side!");
            } else if (percent < 75) {
                comments.setText(
                        (current.getBunkingCount() > 1)
                                ? "Oh-No ! You need to attend at least " + current.getBunkingCount() + " classes to make it 75% !"
                                : "Oh-No ! You need to attend at least " + current.getBunkingCount() + " class to make it 75% !"
                );
            } else {
                comments.setVisibility(View.GONE);
            }


            return listItemView;

        }
    }


}
