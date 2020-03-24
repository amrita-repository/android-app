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

package in.co.rajkumaar.amritarepo.timetable;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.WebViewActivity;
import in.co.rajkumaar.amritarepo.helpers.DownloadTask;
import in.co.rajkumaar.amritarepo.helpers.Utils;
import in.co.rajkumaar.amritarepo.helpers.clearCache;

public class AcademicTimetableActivity extends AppCompatActivity {

    public String TIMETABLE_URL;
    public Spinner year, course, branch, sem, batch;
    List<String> courses = new ArrayList<>();
    List<String> branches = new ArrayList<>();
    List<String> sems = new ArrayList<>();
    List<String> batches = new ArrayList<>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;

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

        pref = getSharedPreferences("student_timetable", MODE_PRIVATE);
        editor = pref.edit();
        new clearCache().clear(this);
        year = findViewById(R.id.acad_year);
        course = findViewById(R.id.acad_course);
        branch = findViewById(R.id.acad_branch);
        sem = findViewById(R.id.acad_sem);
        batch = findViewById(R.id.acad_batch);


        loadLists();
        //loadFromPref();
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
        buildBranchesSpinner(pref.getInt("course", 0));
        year.setSelection(pref.getInt("year", 0));
        course.setSelection(pref.getInt("course", 0));
        sem.setSelection(pref.getInt("sem", 0));
        batch.setSelection(pref.getInt("batch", 0));
    }

    private void savePref() {
        editor.putInt("year", year.getSelectedItemPosition());
        editor.putInt("course", course.getSelectedItemPosition());
        editor.putInt("branch", branch.getSelectedItemPosition());
        editor.putInt("sem", sem.getSelectedItemPosition());
        editor.putInt("batch", batch.getSelectedItemPosition());
        editor.apply();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void buildTimetableUrl() {
        TIMETABLE_URL += year.getSelectedItem() + "/";
        TIMETABLE_URL += course.getSelectedItem() + "/";
        TIMETABLE_URL += branch.getSelectedItem() + "/";
        TIMETABLE_URL += (String) course.getSelectedItem() + branch.getSelectedItem() + batch.getSelectedItem() + sem.getSelectedItem() + ".jpg";

    }

    public void viewTimetable(View view) {
        if (batch.getSelectedItemPosition() > 0 && branch.getSelectedItemPosition() > 0 && course.getSelectedItemPosition() > 0 && sem.getSelectedItemPosition() > 0 && year.getSelectedItemPosition() > 0) {
            savePref();
            if (isNetworkAvailable()) {
                TIMETABLE_URL = "https://intranet.cb.amrita.edu/TimeTable/PDF/";
                buildTimetableUrl();
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
                if (isNetworkAvailable()) {
                    String protocol = "https://";
                    String intranet = getString(R.string.intranet);
                    String timetable = getString(R.string.timetable);
                    TIMETABLE_URL = protocol + intranet + timetable;
                    buildTimetableUrl();
                    new DownloadTask(AcademicTimetableActivity.this, TIMETABLE_URL, 0);
                } else {
                    Snackbar.make(view, "Device not connected to Internet.", Snackbar.LENGTH_SHORT).show();
                }
            }
        } else {
            Snackbar.make(view, "Please select all the choices", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void buildBranchesSpinner(int courseID) {
        branches = new ArrayList<>();
        branches.add("[Choose branch]");
        if (!pref.contains(courses.get(courseID))) {
            getBranches(courseID);
        } else {
            branches = new ArrayList<>();
            Gson gson = new Gson();
            String json = pref.getString(courses.get(courseID), null);
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            ArrayList<String> branch = gson.fromJson(json, listType);
            branches.addAll(branch);
        }
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, R.layout.spinner_item1, branches);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        branch.setAdapter(courseAdapter);
        if (course.getSelectedItemPosition() == pref.getInt("course", 0)) {
            branch.setSelection(pref.getInt("branch", 0));
        }
    }

    private void loadLists() {
        courses.add("[Choose course]");
        if (!pref.contains("courses")) {
            getCourse();
        } else {
            courses = new ArrayList<>();
            Gson gson = new Gson();
            String json = pref.getString("courses", null);
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            ArrayList<String> timings = gson.fromJson(json, listType);
            courses.addAll(timings);
        }
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(AcademicTimetableActivity.this, R.layout.spinner_item1, courses);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        course.setAdapter(courseAdapter);

        sems = new ArrayList<>();
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

    private void getCourse() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(getString(R.string.course_name_url), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Document html = Jsoup.parse(new String(bytes));
                Elements rows = html.select("center > table>tbody>tr").select("td>table>tbody>tr>td>form>select");
                for (Element row : rows) {
                    Elements columns = row.getElementsByTag("option");
                    if (columns.get(0).text().equals("Course")) {
                        for (int j = 1; j < columns.size(); j++) {
                            String parsedCourse = columns.get(j).attr("value");
                            courses.add(parsedCourse);
                        }
                    }
                }
                Gson gson = new Gson();
                String coursesjson = gson.toJson(courses);
                pref.edit().putString("courses", coursesjson).apply();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showToast(AcademicTimetableActivity.this, "An unexpected error occurred. Please try again later");
                finish();
            }
        });
    }

    private void getBranches(final int position) {
        AsyncHttpClient client = new AsyncHttpClient();
        String html = getString(R.string.branch_name_url) + courses.get(position);
        client.get(html, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Document html = Jsoup.parse(new String(bytes));
                Elements rows = html.select("body>select");

                for (Element row : rows) {
                    Elements columns = row.getElementsByTag("option");
                    for (int j = 1; j < columns.size(); j++) {
                        String parsedCourse = columns.get(j).text().replace(".", "");
                        branches.add(parsedCourse);
                    }
                }
                Gson gson = new Gson();
                String branchJson = gson.toJson(branches);
                pref.edit().putString(courses.get(position), branchJson).apply();
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showToast(AcademicTimetableActivity.this, "An unexpected error occurred. Please try again later");
                finish();
            }
        });
    }

}

