/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.timetable;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.activities.WebViewActivity;
import in.co.rajkumaar.amritarepo.helpers.ClearCache;
import in.co.rajkumaar.amritarepo.helpers.DownloadTask;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class AcademicTimetableActivity extends BaseActivity {

    public String TIMETABLE_URL;
    public Spinner year;
    public Spinner course;
    public Spinner branch;
    public Spinner sem;
    public Spinner batch;
    private List<String> courses = new ArrayList<>();
    private List<String> branches = new ArrayList<>();
    private List<String> batches = new ArrayList<>();
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private RequestQueue requestQueue;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(AcademicTimetableActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(AcademicTimetableActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);

        }
        setContentView(R.layout.activity_timetable);

        pref = getSharedPreferences("academic_timetable", MODE_PRIVATE);
        editor = pref.edit();
        new ClearCache().clear(this);
        year = findViewById(R.id.acad_year);
        course = findViewById(R.id.acad_course);
        branch = findViewById(R.id.acad_branch);
        sem = findViewById(R.id.acad_sem);
        batch = findViewById(R.id.acad_batch);
        requestQueue = Volley.newRequestQueue(this);

        loadLists();
        loadFromPref();
        course.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Gson gson = new Gson();
                List<String> dummyDisplay = new ArrayList<>();
                dummyDisplay.add("[Choose branch]");
                String dummyDisplayJson = gson.toJson(dummyDisplay);
                pref.edit().putString("[Choose course]", dummyDisplayJson).apply();
                buildBranchesSpinner(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadFromPref() {
        try {
            buildBranchesSpinner(pref.getInt("course", 0));
            course.setSelection(pref.getInt("course", 0));
            year.setSelection(pref.getInt("year", 0));
            sem.setSelection(pref.getInt("sem", 0));
            batch.setSelection(pref.getInt("batch", 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePref() {
        editor.putInt("year", year.getSelectedItemPosition());
        editor.putInt("course", course.getSelectedItemPosition());
        editor.putInt("branch", branch.getSelectedItemPosition());
        editor.putInt("sem", sem.getSelectedItemPosition());
        editor.putInt("batch", batch.getSelectedItemPosition());
        editor.apply();
    }

    private void buildTimetableUrl(boolean isImage) {
        TIMETABLE_URL += year.getSelectedItem() + "/";
        TIMETABLE_URL += course.getSelectedItem() + "/";
        TIMETABLE_URL += branch.getSelectedItem() + "/";
        TIMETABLE_URL += (String)
                course.getSelectedItem() +
                branch.getSelectedItem() +
                batch.getSelectedItem() +
                sem.getSelectedItem() +
                (isImage ? ".jpg" : ".pdf");
    }

    public void viewTimetable(View view) {
        if (batch.getSelectedItemPosition() > 0 && branch.getSelectedItemPosition() > 0 && course.getSelectedItemPosition() > 0 && sem.getSelectedItemPosition() > 0 && year.getSelectedItemPosition() > 0) {
            savePref();
            if (Utils.isConnected(this)) {
                TIMETABLE_URL = "https://intranet.cb.amrita.edu/TimeTable/PDF/";
                buildTimetableUrl(true);
                Intent intent = new Intent(AcademicTimetableActivity.this, WebViewActivity.class);
                intent.putExtra("webview", TIMETABLE_URL);
                intent.putExtra("zoom", true);
                intent.putExtra("title", branch.getSelectedItem() + " " + batch.getSelectedItem() + " - Semester " + sem.getSelectedItem());
                startActivity(intent);
            } else {
                Snackbar.make(view, "Device not connected to Internet", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(view, "Please select all the choices", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void downloadTimetable(View view) {
        if (batch.getSelectedItemPosition() > 0 && branch.getSelectedItemPosition() > 0 && course.getSelectedItemPosition() > 0 && sem.getSelectedItemPosition() > 0 && year.getSelectedItemPosition() > 0) {
            savePref();
            if (ContextCompat.checkSelfPermission(AcademicTimetableActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(AcademicTimetableActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            } else {
                if (Utils.isConnected(this)) {
                    String protocol = "https://";
                    String intranet = getString(R.string.intranet);
                    String timetable = getString(R.string.timetable);
                    TIMETABLE_URL = protocol + intranet + timetable;
                    CharSequence[] items = {"Image", "PDF"};
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setTitle("Download as");
                    dialogBuilder.setItems(items, (dialog, which) -> {
                                try {
                                    buildTimetableUrl(items[which].equals("Image"));
                                    new DownloadTask(AcademicTimetableActivity.this, TIMETABLE_URL);
                                } catch (UnsupportedEncodingException | URISyntaxException e) {
                                    Utils.showUnexpectedError(AcademicTimetableActivity.this);
                                    e.printStackTrace();
                                }
                            }
                    );
                    dialogBuilder.create().show();

                } else {
                    Snackbar.make(view, "Device not connected to Internet.", Snackbar.LENGTH_SHORT).show();
                }
            }
        } else {
            Snackbar.make(view, "Please select all the choices", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void buildBranchesSpinner(int courseID) {
        List<String> branchesTemp = new ArrayList<>();
        branchesTemp.add("[Choose branch]");
        branches = new ArrayList<>();
        branches.add("[Choose branch]");
        setBranchSpinner();
        if (!pref.contains(courses.get(courseID))) {
            getBranches(courseID, branchesTemp, true);
        } else {
            branches = new ArrayList<>();
            Gson gson = new Gson();
            String json = pref.getString(courses.get(courseID), null);
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            branches.addAll(gson.fromJson(json, listType));
            setBranchSpinner();
            getBranches(courseID, branchesTemp, false);
        }
    }


    private void loadLists() {
        List<String> coursesTemp = new ArrayList<>();
        courses = new ArrayList<>();
        courses.add("[Choose course]");
        setCourseSpinner();
        if (!pref.contains("courses")) {
            getCourse(coursesTemp, true);
        } else {
            Gson gson = new Gson();
            String json = pref.getString("courses", null);
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            courses.addAll(gson.fromJson(json, listType));
            setCourseSpinner();
            getCourse(coursesTemp, false);
        }


        List<String> sems = new ArrayList<>();
        sems.add("[Choose semester]");
        int i = 1;
        while (i <= 10) {
            sems.add(String.valueOf(i++));
        }
        ArrayAdapter<String> semAdapter = new ArrayAdapter<>(this, R.layout.spinner_item1, sems);
        semAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sem.setAdapter(semAdapter);

        batches.add("[Choose batch]");
        batches.add("A");
        batches.add("B");
        batches.add("C");
        batches.add("D");
        batches.add("E");
        batches.add("F");
        ArrayAdapter<String> batchAdapter = new ArrayAdapter<>(this, R.layout.spinner_item1, batches);
        batchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batch.setAdapter(batchAdapter);


        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, R.layout.spinner_item1, Utils.getAcademicYears());
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year.setAdapter(yearAdapter);

    }

    private void setCourseSpinner() {
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(AcademicTimetableActivity.this, R.layout.spinner_item1, courses);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        course.setAdapter(courseAdapter);
    }

    private void setBranchSpinner() {
        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this, R.layout.spinner_item1, branches);
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        branch.setAdapter(branchAdapter);
        if (course.getSelectedItemPosition() == pref.getInt("course", 0)) {
            branch.setSelection(pref.getInt("branch", 0));
        }
    }

    private void getCourse(final List<String> coursesTemp, final boolean needUpdateCourse) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.course_name_url),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document html = Jsoup.parse(response);
                        Element row = html.getElementById("drop_1");
                        Elements columns = row.getElementsByTag("option");
                        if (columns.get(0).text().equals("Course")) {
                            for (int j = 1; j < columns.size(); j++) {
                                String parsedCourse = columns.get(j).attr("value");
                                coursesTemp.add(parsedCourse);
                            }
                        }
                        Gson gson = new Gson();
                        String coursesJson = gson.toJson(coursesTemp);
                        pref.edit().putString("courses", coursesJson).apply();
                        if (needUpdateCourse) {
                            courses.addAll(coursesTemp);
                            setCourseSpinner();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.showToast(AcademicTimetableActivity.this, "An unexpected error occurred. Please try again later");
            }
        });
        requestQueue.add(stringRequest);
    }

    private void getBranches(final int position, final List<String> branchesTemp, final boolean needUpdateBranch) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.branch_name_url) + courses.get(position),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document html = Jsoup.parse(response);
                        Element row = html.getElementById("drop_2");
                        Elements columns = row.getElementsByTag("option");
                        for (int j = 1; j < columns.size(); j++) {
                            String parsedCourse = columns.get(j).attr("value");
                            branchesTemp.add(parsedCourse);
                        }
                        Gson gson = new Gson();
                        String branchJson = gson.toJson(branchesTemp);
                        pref.edit().putString(courses.get(position), branchJson).apply();
                        if (needUpdateBranch) {
                            branches = new ArrayList<>();
                            branches.addAll(branchesTemp);
                            setBranchSpinner();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.showToast(AcademicTimetableActivity.this, "An unexpected error occurred. Please try again later");
            }
        });
        requestQueue.add(stringRequest);
    }

}

