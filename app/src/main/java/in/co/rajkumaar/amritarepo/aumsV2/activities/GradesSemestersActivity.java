/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aumsV2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

public class GradesSemestersActivity extends BaseActivity {

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

        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);
        preferences = getSharedPreferences("aums-lite", MODE_PRIVATE);
        listView = findViewById(R.id.list);

        if (GlobalData.getGradeSemesters() == null) {
            getSemesterMapping();
        } else {
            loadSemesterMapping();
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (Utils.isConnected(getBaseContext()))
                    startActivity(new Intent(GradesSemestersActivity.this, GradesActivity.class).putExtra("sem", String.valueOf(semesterObjects.get(i).getId())));
                else
                    Utils.showInternetError(getBaseContext());
            }
        });
    }

    private void loadSemesterMapping() {
        semesterObjects = GlobalData.getGradeSemesters();
        sems = new ArrayList<>();
        for (Semester current : semesterObjects) {
            sems.add("Semester " + current.getSemester() + " (" + current.getPeriod() + ")");
        }
        semsAdapter = new ArrayAdapter<>(GradesSemestersActivity.this, R.layout.white_textview, sems);
        GlobalData.setGradeSemesters(semesterObjects);
        listView.setAdapter(semsAdapter);
        listView.setEmptyView(findViewById(R.id.emptyView));
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    private void getSemesterMapping() {
        semesterObjects = new ArrayList<>();
        sems = new ArrayList<>();
        client.addHeader("Authorization", GlobalData.auth);
        client.addHeader("token", preferences.getString("token", ""));
        client.setTimeout(3000000);
        client.get("https://amritavidya.amrita.edu:8444/DataServices/rest/semRes?rollno=" + preferences.getString("username", ""), new AsyncHttpResponseHandler() {
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
                    semsAdapter = new ArrayAdapter<>(GradesSemestersActivity.this, R.layout.white_textview, sems);
                    GlobalData.setGradeSemesters(semesterObjects);
                    listView.setAdapter(semsAdapter);
                    listView.setEmptyView(findViewById(R.id.emptyView));
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                } catch (JSONException e) {
                    Utils.showUnexpectedError(GradesSemestersActivity.this);
                    finish();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Log.e("ERROR", throwable.getLocalizedMessage());
                GlobalData.resetUser(GradesSemestersActivity.this);
                Utils.showUnexpectedError(GradesSemestersActivity.this);
                finish();
            }
        });
    }

}
