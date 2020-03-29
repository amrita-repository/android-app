/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aumsV2.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.aumsV2.helpers.GlobalData;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class AttendanceActivity extends BaseActivity {

    ListView list;
    String sem;
    private AsyncHttpClient client = GlobalData.getClient();
    private SharedPreferences preferences;
    private ArrayList<CourseData> attendanceData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        preferences = getSharedPreferences("aums-lite", MODE_PRIVATE);
        list = findViewById(R.id.list);

        sem = getIntent().getStringExtra("sem");
        getAttendance(sem);
    }

    void getAttendance(final String sem) {
        attendanceData = new ArrayList<>();
        client.addHeader("Authorization", GlobalData.auth);
        client.addHeader("token", preferences.getString("token", ""));
        client.setTimeout(5000);
        client.get("https://amritavidya.amrita.edu:8444/DataServices/rest/attRes?rollno=" + preferences.getString("username", "") + "&sem=" + sem, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(bytes));
                    JSONArray subjects = jsonObject.getJSONArray("Values");
                    Log.e("SEM", sem);
                    Log.e("SUBS", jsonObject.toString());
                    for (int j = 0; j < subjects.length(); ++j) {
                        JSONObject current = subjects.getJSONObject(j);
                        CourseData courseData = new CourseData();
                        courseData.setCode(current.getString("CourseCode"));
                        courseData.setTitle(current.getString("CourseName"));
                        courseData.setTotal(String.valueOf((int) Double.parseDouble(current.getString("ClassTotal"))));
                        courseData.setAttended(current.getString("ClassPresent"));
                        courseData.setPercentage(current.getString("TotalPercentage"));
                        attendanceData.add(courseData);
                    }
                    preferences.edit().putString("token", jsonObject.getString("Token")).apply();
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
                } catch (JSONException e) {
                    Utils.showUnexpectedError(AttendanceActivity.this);
                    GlobalData.resetUser(AttendanceActivity.this);
                    finish();
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Utils.showUnexpectedError(AttendanceActivity.this);
                GlobalData.resetUser(AttendanceActivity.this);
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

    class CourseData {
        private String code, title, total, attended, percentage;

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

        String getTotal() {
            return total;
        }

        void setTotal(String total) {
            this.total = total;
        }

        int getBunkingCount() {
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
