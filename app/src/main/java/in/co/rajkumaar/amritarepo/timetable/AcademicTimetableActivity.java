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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.WebViewActivity;
import in.co.rajkumaar.amritarepo.helpers.DownloadTask;
import in.co.rajkumaar.amritarepo.helpers.Utils;
import in.co.rajkumaar.amritarepo.helpers.clearCache;
import okhttp3.internal.Util;

public class AcademicTimetableActivity extends AppCompatActivity {

    public String TIMETABLE_URL;
    public Spinner year, course, branch, sem, batch;
    List<String> years = new ArrayList<>();
    List<String> courses = new ArrayList<>();
    List<String> branches = new ArrayList<>();
    List<String> sems = new ArrayList<>();
    List<String> batches = new ArrayList<>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;

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
        Utils.showSmallAd(this, (LinearLayout) findViewById(R.id.banner_container));
        pref = getSharedPreferences("student_timetable", MODE_PRIVATE);
        editor = pref.edit();
        new clearCache().clear();


        year = findViewById(R.id.acad_year);
        course = findViewById(R.id.acad_course);
        branch = findViewById(R.id.acad_branch);
        sem = findViewById(R.id.acad_sem);
        batch = findViewById(R.id.acad_batch);

        loadLists();
        loadFromPref();
        course.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);
        dialog.show();
        if (batch.getSelectedItemPosition() > 0 && branch.getSelectedItemPosition() > 0 && course.getSelectedItemPosition() > 0 && sem.getSelectedItemPosition() > 0 && year.getSelectedItemPosition() > 0) {
            savePref();
            if (isNetworkAvailable()) {
                TIMETABLE_URL = "https://intranet.cb.amrita.edu/TimeTable/PDF/";
                buildTimetableUrl();
                AsyncHttpClient client = new AsyncHttpClient();
                client.get(TIMETABLE_URL, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Intent intent = new Intent(AcademicTimetableActivity.this, WebViewActivity.class);
                        intent.putExtra("webview", TIMETABLE_URL);
                        intent.putExtra("zoom", true);
                        intent.putExtra("title", branch.getSelectedItem() + " " + batch.getSelectedItem() + " - Semester " + sem.getSelectedItem());
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Utils.showSnackBar(AcademicTimetableActivity.this,"The requested timetable has not yet been uploaded. Please check back later.");
                    }

                    @Override
                    public void onFinish() {
                        try{
                            dialog.dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        super.onFinish();
                    }
                });
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
                    final ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setMessage("Please wait");
                    dialog.setCancelable(false);
                    dialog.show();
                    String protocol = "https://";
                    String intranet = getString(R.string.intranet);
                    String timetable = getString(R.string.timetable);
                    TIMETABLE_URL = protocol + intranet + timetable;
                    buildTimetableUrl();
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.get(TIMETABLE_URL, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            new DownloadTask(AcademicTimetableActivity.this, TIMETABLE_URL, 0);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Utils.showSnackBar(AcademicTimetableActivity.this,"The requested timetable has not yet been uploaded. Please check back later.");
                        }

                        @Override
                        public void onFinish() {
                            try{
                                dialog.dismiss();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            super.onFinish();
                        }
                    });
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
        branches.add("[Choose Branch]");
        switch (courseID) {
            case 1:
                branches.add("AEE");
                branches.add("CHE");
                branches.add("CIE");
                branches.add("CVI");
                branches.add("CSE");
                branches.add("ECE");
                branches.add("EEE");
                branches.add("EIE");
                branches.add("MEE");
                break;
            case 2:
                branches.add("MAC");
                branches.add("ENG");
                break;
            case 3:
                branches.add("CHE");
                branches.add("MAT");
                branches.add("PHY");
                break;
            case 4:
                branches.add("ATE");
                branches.add("ATL");
                branches.add("BME");
                branches.add("CEN");
                branches.add("CHE");
                branches.add("CIE");
                branches.add("CSE");
                branches.add("CSP");
                branches.add("CVI");
                branches.add("CYS");
                branches.add("EBS");
                branches.add("EDN");
                branches.add("MFG");
                branches.add("MSE");
                branches.add("PWE");
                branches.add("RET");
                branches.add("RSW");
                branches.add("SCE");
                branches.add("VLD");
                break;
            case 5:
                branches.add("CMN");
                branches.add("MAC");
                branches.add("ENG");
                break;
            case 6:
                branches.add("MBA");
                break;
            case 7:
                branches.add("MCA");
                break;
            case 8:
                branches.add("MSW");
                break;
            case 9:
                branches.add("JLM");
                break;
        }
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, R.layout.spinner_item1, branches);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        branch.setAdapter(courseAdapter);
        if(course.getSelectedItemPosition() == pref.getInt("course", 0)){
            branch.setSelection(pref.getInt("branch",0));
        }
    }

    private void loadLists() {

        courses = new ArrayList<>();
        courses.add("[Choose course]");
        courses.add("BTech");
        courses.add("BA");
        courses.add("IMSc");
        courses.add("MTech");
        courses.add("MA");
        courses.add("MBA");
        courses.add("MCA");
        courses.add("MSW");
        courses.add("PGD");
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<String>(AcademicTimetableActivity.this, R.layout.spinner_item1, courses);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        course.setAdapter(courseAdapter);

        sems = new ArrayList<>();
        sems.add("[Choose semester]");
        int i = 1;
        while (i <= 10) {
            sems.add(String.valueOf(i++));
        }
        ArrayAdapter<String> semAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item1, sems);
        semAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sem.setAdapter(semAdapter);

        batches.add("[Choose batch]");
        batches.add("A");
        batches.add("B");
        batches.add("C");
        batches.add("D");
        batches.add("E");
        batches.add("F");
        ArrayAdapter<String> batchAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item1, batches);
        batchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batch.setAdapter(batchAdapter);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item1, Utils.getAcademicYears());
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year.setAdapter(yearAdapter);

    }

}
