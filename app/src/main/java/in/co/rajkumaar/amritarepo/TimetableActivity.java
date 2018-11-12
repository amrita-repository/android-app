package in.co.rajkumaar.amritarepo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class TimetableActivity extends AppCompatActivity {

    public String TIMETABLE_URL;
    public int userYear,userCourse,userBranch,userSem,userBatch;
    public Spinner yearId,courseId,branchId,semId,batchId;
    public Button viewButtonId,downButtonId;
    List<String> years=new ArrayList<>();
    List<String> courses=new ArrayList<>();
    List<String> branches=new ArrayList<>();
    List<String> sems=new ArrayList<>();
    List<String> batches=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(TimetableActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(TimetableActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

        }
        setContentView(R.layout.activity_timetable);
        new clearCache().clear();


        MobileAds.initialize(this, getResources().getString(R.string.banner_id));
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);


        final Spinner year=findViewById(R.id.acad_year);
        yearId=year;
        final Spinner course=findViewById(R.id.acad_course);
        courseId=course;
        final Spinner branch=findViewById(R.id.acad_branch);
        branchId=branch;
        final Spinner sem=findViewById(R.id.acad_sem);
        semId=sem;
        final Spinner batch=findViewById(R.id.acad_batch);
        batchId=batch;
        final Button viewButton=findViewById(R.id.timeTableViewButton);
        viewButtonId=viewButton;

        final Button downloadButton=findViewById(R.id.timeTableDownloadButton);
        downButtonId=downloadButton;


        course.setVisibility(View.GONE);
        branch.setVisibility(View.GONE);
        sem.setVisibility(View.GONE);
        batch.setVisibility(View.GONE);
        viewButton.setVisibility(View.GONE);
        downloadButton.setVisibility(View.GONE);

        if(courses.isEmpty()) {
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
        }
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<String>(TimetableActivity.this, R.layout.spinner_item1, courses);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseId.setAdapter(courseAdapter);


        if (sems.isEmpty())
        {
            sems.add("[Choose semester]");
            for(int i=1;i<=10;++i)
            {
                sems.add(String.valueOf(i));
            }

        }
        ArrayAdapter<String> semAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item1, sems);
        semAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        semId.setAdapter(semAdapter);

        if(batches.isEmpty())
        {
            batches.add("[Choose batch]");
            batches.add("A");
            batches.add("B");
            batches.add("C");
            batches.add("D");
            batches.add("E");
            batches.add("F");
        }
        ArrayAdapter<String> batchAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item1, batches);
        batchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batchId.setAdapter(batchAdapter);


        years.add("[Choose year]");
        years.add("2015-16");
        years.add("2016-17");
        years.add("2017-18");
        years.add("2018-19");
        years.add("2019-20");
        years.add("2020-21");
        years.add("2021-22");
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item1, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if(!yearAdapter.isEmpty()){

            year.setAdapter(yearAdapter);
            year.setSelection(0);}
        year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                userYear = year.getSelectedItemPosition();
                if (userYear > 0) {
                    courseId.setVisibility(View.VISIBLE);

                    courseId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                            userCourse = courseId.getSelectedItemPosition();
                            buildBranchesSpinner();

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userBatch>0 && userBranch>0 && userCourse>0 && userSem>0 && userYear>0){
                TIMETABLE_URL="https://intranet.cb.amrita.edu/TimeTable/PDF/";
                buildTimetableUrl();
                Intent intent=new Intent(TimetableActivity.this,WebViewActivity.class);
                intent.putExtra("webview",TIMETABLE_URL);
                intent.putExtra("zoom",true);
                intent.putExtra("title",branches.get(userBranch)+" "+batches.get(userBatch)+" - Semester "+String.valueOf(sems.get(userSem)));
                if(isNetworkAvailable())
                {
                    startActivity(intent);
                }
                else{
                    Snackbar.make(view,"Device not connected to Internet",Snackbar.LENGTH_SHORT).show();
                }}
                else
                {
                    Snackbar.make(view,"Please select all the choices",Snackbar.LENGTH_SHORT).show();
                }

            }
        });
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userBatch>0 && userBranch>0 && userCourse>0 && userSem>0 && userYear>0){
                if (ContextCompat.checkSelfPermission(TimetableActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(TimetableActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
                else{
                    if(isNetworkAvailable())
                    {
                        String protocol="https://";
                        String intranet=getString(R.string.intranet);
                        String timetable=getString(R.string.timetable);
                            TIMETABLE_URL=protocol+intranet+timetable;
                            buildTimetableUrl();
                            new DownloadTask(TimetableActivity.this,TIMETABLE_URL,0);
                    }
                    else{
                        Snackbar.make(view,"Device not connected to Internet.",Snackbar.LENGTH_SHORT).show();
                    }


                }
            } else
                {
                    Snackbar.make(view,"Please select all the choices",Snackbar.LENGTH_SHORT).show();
                }
            }

        });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void buildTimetableUrl(){
        switch (userYear)
        {
            case 1:TIMETABLE_URL+="2015_16"+"/"; break;
            case 2:TIMETABLE_URL+="2016_17"+"/"; break;
            case 3:TIMETABLE_URL+="2017_18"+"/"; break;
            case 4:TIMETABLE_URL+="2018_19"+"/"; break;
            case 5:TIMETABLE_URL+="2019_20"+"/"; break;
            case 6:TIMETABLE_URL+="2020_21"+"/"; break;
            case 7:TIMETABLE_URL+="2021_22"+"/"; break;
        }
        TIMETABLE_URL+=courses.get(userCourse)+"/";
        TIMETABLE_URL+=branches.get(userBranch)+"/";
        TIMETABLE_URL+=courses.get(userCourse)+branches.get(userBranch)+batches.get(userBatch)+String.valueOf(sems.get(userSem))+".jpg";

    }

    public void buildBranchesSpinner()
    {
        if (userCourse > 0) {
            branchId.setVisibility(View.VISIBLE);
            if(!branches.isEmpty()) {
                branches.clear();

            }

                branches.add("[Choose Branch]");
                switch (userCourse) {
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
            ArrayAdapter<String> courseAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item1, branches);
            courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            branchId.setAdapter(courseAdapter);
            branchId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    userBranch = branchId.getSelectedItemPosition();
                    if(userBranch>0)
                        buildSemSpinner();

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            }

    }

    public void buildSemSpinner()
    {
        semId.setVisibility(View.VISIBLE);

        semId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                userSem = semId.getSelectedItemPosition();
                if(userSem>0)
                    buildBatchSpinner();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void buildBatchSpinner(){
        batchId.setVisibility(View.VISIBLE);

        batchId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                userBatch = batchId.getSelectedItemPosition();
                if(userBatch>0){
                    viewButtonId.setVisibility(View.VISIBLE);
                    downButtonId.setVisibility(View.VISIBLE);}


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
