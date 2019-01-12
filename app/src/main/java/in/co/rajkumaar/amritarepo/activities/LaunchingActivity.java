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

package in.co.rajkumaar.amritarepo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.about.AboutActivity;
import in.co.rajkumaar.amritarepo.aums.activities.LoginActivity;
import in.co.rajkumaar.amritarepo.curriculum.CurriculumActivity;
import in.co.rajkumaar.amritarepo.downloads.DownloadsActivity;
import in.co.rajkumaar.amritarepo.examschedule.ExamCategoryActivity;
import in.co.rajkumaar.amritarepo.helpers.clearCache;
import in.co.rajkumaar.amritarepo.papers.SemesterActivity;
import in.co.rajkumaar.amritarepo.timetable.AcademicTimetableActivity;
import in.co.rajkumaar.amritarepo.timetable.FacultyTimetableActivity;
import in.co.rajkumaar.amritarepo.timings.TimingsActivity;
import in.co.rajkumaar.amritarepo.wifistatus.WifiStatusActivity;

public class LaunchingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    static boolean active = false;



    private FirebaseAnalytics mFirebaseAnalytics;
    Document doc;
    int version=0,realVersion=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE) ;
        if (ContextCompat.checkSelfPermission(LaunchingActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LaunchingActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
        else {
            new clearCache().clear();
            if(pref.getBoolean("first",true)){
                AboutActivity.showDisclaimer(this);
                SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                editor.putBoolean("first",false);
                editor.apply();
            }
        }
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        setContentView(R.layout.activity_launching);
        AdView mAdView;
        MobileAds.initialize(this, getResources().getString(R.string.banner_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
        if(isNetworkAvailable())
            new checkVersion().execute();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_home));
        navigationView.setNavigationItemSelectedListener(this);


        TextView versionName=navigationView.getHeaderView(0).findViewById(R.id.versioncode);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versionName.setText("VERSION "+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        navigationView.getMenu().getItem(0).setChecked(true);

        powerUpOnClickListeners();

       /* Button button=findViewById(R.id.sembutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle params = new Bundle();
                params.putString("Department", spinner.getSelectedItem().toString());
                Log.e("Dept",spinner.getSelectedItem().toString());
                mFirebaseAnalytics.logEvent("EventDept", params);
                int pos=spinner.getSelectedItemPosition();

                SharedPreferences pref = LaunchingActivity.this.getSharedPreferences("user",Context.MODE_PRIVATE) ;
                SharedPreferences.Editor ed = pref.edit();
                ed.putInt("pos",pos);
                ed.apply();

                if(isNetworkAvailable()){
                        if(pos>0)
                        {
                            Intent intent=new Intent(LaunchingActivity.this,SemesterActivity.class);
                            intent.putExtra("course",pos);
                            intent.putExtra("pageTitle",categories.get(pos));
                            startActivity(intent);
                        }
                        else {
                            Snackbar.make(view, "You haven't selected any course.", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        }
                }
                else{
                    Snackbar.make(view,"Device not connected to Internet.",Snackbar.LENGTH_SHORT).show();
                }

            }
        });
*/
    }

    @Override
    protected void onDestroy() {
        new clearCache().clear();
        super.onDestroy();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void showSnackbar(String message) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar
                .make(parentLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
    public void checkUpdate(){

        if(active) {
            if (realVersion > version) {
                Log.e("Real : " + realVersion, " HAving :" + version);
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(LaunchingActivity.this);
                alertDialog.setMessage("An update is available for Amrita Repository.");
                alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                        if (intent.resolveActivity(getPackageManager()) != null)
                            startActivity(intent);
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                if (active)
                    alertDialog.show();
            }
        }

    }
    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.nav_home);
            } else {
            super.onBackPressed();}
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        showSnackbar("Please click BACK again to exit");
        //Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                doubleBackToExitPressedOnce=false;
            }
        }, 2000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent it = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","rajkumaar2304@gmail.com", null));
            it.putExtra(Intent.EXTRA_SUBJECT, "Regarding Bug in Amrita Repository App");
            it.putExtra(Intent.EXTRA_EMAIL, new String[] {"rajkumaar2304@gmail.com"});
            if(it.resolveActivity(getPackageManager())!=null)
            startActivity(it);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (id == R.id.nav_downloads) {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, DownloadsActivity.class));

        }

        else if(id == R.id.nav_faq){
            if(isNetworkAvailable())
                startActivity(new Intent(LaunchingActivity.this, WebViewActivity.class).putExtra("webview","https://dev.rajkumaar.co.in/utils/faq.php")
                        .putExtra("title","Frequently Asked Questions")
                        .putExtra("zoom",false)
                );
            else
                showSnackbar("Device not connected to internet");
        }
         else if (id == R.id.nav_share) {
            drawer.closeDrawer(GravityCompat.START);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Find all question papers under one roof. Download Amrita Repository, the must-have app for exam preparation " +
                            " : https://play.google.com/store/apps/details?id="+getPackageName());
            sendIntent.setType("text/plain");
            if(sendIntent.resolveActivity(getPackageManager())!=null)
            {
                startActivity(sendIntent);
            }
            else{
                Toast.makeText(this,"Error.",Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.nav_about) {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this,AboutActivity.class));
        }
        else if(id==R.id.nav_review){
            drawer.closeDrawer(GravityCompat.START);
            if(isNetworkAvailable()){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName()));
                if(intent.resolveActivity(getPackageManager())!=null)
                {
                    startActivity(intent);
                }
                else{
                    Toast.makeText(this,"Error Opening Play Store.",Toast.LENGTH_SHORT).show();
                }
            }
            else
                showSnackbar("Device not connected to internet");
        }
        return true;
    }


    /**
     * Power up the on click listeners of items in home grid
     */
    private void powerUpOnClickListeners(){
        findViewById(R.id.qpapers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences pref = LaunchingActivity.this.getSharedPreferences("user",Context.MODE_PRIVATE);
                if(pref.getBoolean("remember_program",false) && pref.getInt("pos",-1) >= 0){
                    intentSemActivity(pref.getInt("pos",0), pref.getString("program",null));
                }else {
                    final AlertDialog.Builder programs_builder = new AlertDialog.Builder(LaunchingActivity.this);
                    programs_builder.setCancelable(true);
                    programs_builder.setTitle("Choose your program");
                    final String [] categories = {"B.Tech","BA Communication","MA Communication","Integrated MSc & MA","MCA","MSW","M.Tech"};
                    final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(LaunchingActivity.this, android.R.layout.simple_list_item_1, categories);
                    programs_builder.setItems(categories, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final int position = which;
                            if (isNetworkAvailable()) {
                                if (pref.getBoolean("prompt", true)) {
                                    final SharedPreferences.Editor ed = pref.edit();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LaunchingActivity.this);
                                    builder.setMessage("Do you want me to remember your academic program ? ");
                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ed.putBoolean("remember_program", true);
                                            ed.putInt("pos", position);
                                            ed.putString("program",dataAdapter.getItem(position));
                                            intentSemActivity(position, dataAdapter.getItem(position));
                                            ed.apply();
                                        }
                                    });
                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ed.putBoolean("remember_program", false);
                                            ed.putInt("pos", -1);
                                            ed.putString("program",null);
                                            ed.apply();
                                            intentSemActivity(position, dataAdapter.getItem(position));
                                        }
                                    });
                                    builder.setNeutralButton("Don\'t show again", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ed.putBoolean("prompt", false);
                                            ed.apply();
                                            intentSemActivity(position, dataAdapter.getItem(position));
                                        }
                                    });
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                } else {
                                    intentSemActivity(position, dataAdapter.getItem(position));
                                }

                            } else {
                               Toast.makeText(LaunchingActivity.this, "Device not connected to Internet.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    AlertDialog alertDialog = programs_builder.create();
                    alertDialog.show();
                }

            }
        });
        findViewById(R.id.student_timetable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LaunchingActivity.this,AcademicTimetableActivity.class));
            }
        });

        findViewById(R.id.faculty_timetable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LaunchingActivity.this,FacultyTimetableActivity.class));
            }
        });
        findViewById(R.id.exam_schedule).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable())
                    startActivity(new Intent(LaunchingActivity.this,ExamCategoryActivity.class));
                else
                    showSnackbar("Device not connected to internet");
            }
        });
        findViewById(R.id.aums).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LaunchingActivity.this,LoginActivity.class));
            }
        });

        findViewById(R.id.timings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] items = {"Trains from Coimbatore", "Trains from Palghat", "Trains to Coimbatore", "Trains to Palghat", "Buses from Coimbatore", "Buses to Coimbatore"};
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LaunchingActivity.this);
                dialogBuilder.setTitle("View timings of ?");
                dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Intent trainBusOpen = new Intent(LaunchingActivity.this, TimingsActivity.class);
                        trainBusOpen.putExtra("type", items[item]);
                        startActivity(trainBusOpen);
                    }
                });
                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
        });
        findViewById(R.id.curriculum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable()) {
                    final CharSequence[] depts = {"Computer Science Engineering", "Electronics & Communication Engineering", "Aerospace Engineering", "Civil Engineering", "Chemical Engineering", "Electrical & Electronics Engineering", "Electronics & Instrumentation Engineering", "Mechanical Engineering"};
                    AlertDialog.Builder departmentDialogBuilder = new AlertDialog.Builder(LaunchingActivity.this);
                    departmentDialogBuilder.setTitle("Select your Department");
                    departmentDialogBuilder.setItems(depts, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            Intent curriculum_open = new Intent(LaunchingActivity.this, CurriculumActivity.class);
                            curriculum_open.putExtra("department", depts[item]);
                            startActivity(curriculum_open);
                        }
                    });
                    AlertDialog departmentDialog = departmentDialogBuilder.create();
                    departmentDialog.show();
                }else
                    showSnackbar("Device not connected to internet");
            }
        });
        findViewById(R.id.downloads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LaunchingActivity.this, DownloadsActivity.class));
            }
        });
        findViewById(R.id.wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable())
                    startActivity(new Intent(LaunchingActivity.this, WifiStatusActivity.class));
                else
                    showSnackbar("Device not connected to internet");
            }
        });
    }

    /**
     * Sends an intent to semester activity with the selected program
     * @param position
     * @param title
     */
    private void intentSemActivity(int position,String title){
        Bundle params = new Bundle();
        params.putString("Department", title);
        Log.e("Dept",title);
        mFirebaseAnalytics.logEvent("EventDept", params);

        Intent intent=new Intent(LaunchingActivity.this,SemesterActivity.class);
        intent.putExtra("course",position);
        intent.putExtra("pageTitle",title);
        startActivity(intent);
    }


    /**
     * Compares app version with the one in rajkumaar.co.in and prompts to update
     */
    @SuppressLint("StaticFieldLeak")
    private class checkVersion extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                int statusCode=Jsoup.connect("http://rajkumaar.co.in/repoversion.html").execute().statusCode();
                if(statusCode==200) {
                    doc = Jsoup.connect("http://rajkumaar.co.in/repoversion.html").execute().parse();
                    realVersion = Integer.parseInt(doc.title());
                }
            }catch (Exception e) {
                e.printStackTrace();
                try {
                    int statusCode = Jsoup.connect("https://rajkumaar.co.in/repoversion.html").execute().statusCode();
                    if (statusCode == 200) {
                        doc = Jsoup.connect("https://rajkumaar.co.in/repoversion.html").execute().parse();
                        realVersion = Integer.parseInt(doc.title());
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            return null;
    }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pInfo.versionCode;
                checkUpdate();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            super.onPostExecute(aVoid);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        active=false;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
}

