/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aumsV2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import in.co.rajkumaar.amritarepo.aumsV2.models.Semester;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class AttendanceSemestersActivity extends BaseActivity {

    private ListView listView;
    private ArrayAdapter<String> semsAdapter;
    private ArrayList<String> sems;
    private ArrayList<Semester> semesterObjects;
    private AsyncHttpClient client = GlobalData.getClient();
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_semesters);

        preferences = getSharedPreferences("aums-lite", MODE_PRIVATE);
        listView = findViewById(R.id.list);
        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);
        if (GlobalData.getAttendanceSemesters() == null) {
            getSemesterMapping();
        } else {
            loadSemesterMapping();
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (Utils.isConnected(AttendanceSemestersActivity.this))
                    startActivity(new Intent(AttendanceSemestersActivity.this, AttendanceActivity.class).putExtra("sem", String.valueOf(semesterObjects.get(i).getId())));
                else
                    Utils.showInternetError(getBaseContext());
            }
        });
    }

    private void loadSemesterMapping() {
        semesterObjects = GlobalData.getAttendanceSemesters();
        sems = new ArrayList<>();
        for (Semester current : semesterObjects) {
            sems.add("Semester " + current.getSemester() + " (" + current.getPeriod() + ")");
        }
        semsAdapter = new ArrayAdapter<>(AttendanceSemestersActivity.this, R.layout.white_textview, sems);
        GlobalData.setAttendanceSemesters(semesterObjects);
        listView.setAdapter(semsAdapter);
        listView.setEmptyView(findViewById(R.id.emptyView));
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    private void getSemesterMapping() {
        semesterObjects = new ArrayList<>();
        sems = new ArrayList<>();
        client.addHeader("Authorization", GlobalData.auth);
        client.addHeader("token", preferences.getString("token", ""));
        client.get("https://amritavidya.amrita.edu:8444/DataServices/rest/semAtdRes?rollno=" + preferences.getString("username", ""), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(bytes));
                    JSONArray jsonArray = jsonObject.getJSONArray("Semester");
                    for (int j = 0; j < jsonArray.length(); ++j) {
                        JSONObject current = jsonArray.getJSONObject(j);
                        semesterObjects.add(new Semester(current.getInt("Id"), current.getString("Semester"), current.getString("Period")));
                        sems.add("Semester " + current.getString("Semester") + " (" + current.getString("Period") + ")");
                    }
                    preferences.edit().putString("token", jsonObject.getString("Token")).apply();
                    semsAdapter = new ArrayAdapter<>(AttendanceSemestersActivity.this, R.layout.white_textview, sems);
                    GlobalData.setAttendanceSemesters(semesterObjects);
                    listView.setAdapter(semsAdapter);
                    listView.setEmptyView(findViewById(R.id.emptyView));
                    findViewById(R.id.progressBar).setVisibility(View.GONE);

                } catch (JSONException e) {
                    Utils.showUnexpectedError(AttendanceSemestersActivity.this);
                    finish();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                GlobalData.resetUser(AttendanceSemestersActivity.this);
                Utils.showUnexpectedError(AttendanceSemestersActivity.this);
                finish();
            }
        });
    }


}
