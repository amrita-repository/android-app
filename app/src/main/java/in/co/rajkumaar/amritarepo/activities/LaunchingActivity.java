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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.about.AboutActivity;
import in.co.rajkumaar.amritarepo.aums.activities.LoginActivity;
import in.co.rajkumaar.amritarepo.curriculum.CurriculumActivity;
import in.co.rajkumaar.amritarepo.downloads.DownloadsActivity;
import in.co.rajkumaar.amritarepo.examschedule.ExamCategoryActivity;
import in.co.rajkumaar.amritarepo.faq.ExamsFAQActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;
import in.co.rajkumaar.amritarepo.helpers.clearCache;
import in.co.rajkumaar.amritarepo.papers.SemesterActivity;
import in.co.rajkumaar.amritarepo.timetable.AcademicTimetableActivity;
import in.co.rajkumaar.amritarepo.timetable.FacultyTimetableActivity;
import in.co.rajkumaar.amritarepo.timings.ShuttleBusTimingsActivity;
import in.co.rajkumaar.amritarepo.timings.TimingsActivity;
import in.co.rajkumaar.amritarepo.wifistatus.WifiStatusActivity;

public class LaunchingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static boolean active = false;
    boolean doubleBackToExitPressedOnce = false;
    private FirebaseAnalytics mFirebaseAnalytics;
    ProgressDialog warmUpDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launching);
        warmUpDialog = new ProgressDialog(this);
        warmUpDialog.setMessage("Warming up. Please wait..");
        warmUpDialog.setCancelable(false);
        if(!BuildConfig.DEBUG)
        warmUpDialog.show();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        if (ContextCompat.checkSelfPermission(LaunchingActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LaunchingActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            new clearCache().clear();
            if (pref.getBoolean("first", true)) {
                AboutActivity.showDisclaimer(this);
                SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                editor.putBoolean("first", false);
                editor.apply();
            }
        }
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Utils.showSmallAd(this, (LinearLayout) findViewById(R.id.banner_container));
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_home));
        navigationView.setNavigationItemSelectedListener(this);


        TextView versionName = navigationView.getHeaderView(0).findViewById(R.id.versioncode);
        versionName.setText("Version ".concat(BuildConfig.VERSION_NAME));

        navigationView.getMenu().getItem(0).setChecked(true);

        powerUpOnClickListeners();
    }

    @Override
    protected void onPostResume() {
        if (Utils.isConnected(LaunchingActivity.this) && !BuildConfig.DEBUG)
            checkUpdate();
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        new clearCache().clear();
        super.onDestroy();
    }

    /**
     * Compares existing version with latest and prompts for update
     */
    public void checkUpdate() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("version");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String latest = dataSnapshot.getValue(String.class);
                    if (active) {
                        try{
                            warmUpDialog.dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        Log.e("Latest : " + latest, " Having :" + BuildConfig.VERSION_NAME);
                        if (!latest.equals(BuildConfig.VERSION_NAME)) {
                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(LaunchingActivity.this);
                            alertDialog.setCancelable(false);
                            alertDialog.setMessage("An update is available for Amrita Repository.");
                            alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                                    if (intent.resolveActivity(getPackageManager()) != null)
                                        startActivity(intent);
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                            if (active)
                                alertDialog.show();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                try{
                    warmUpDialog.dismiss();
                    Crashlytics.log(databaseError.toException().getMessage());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.nav_home);
            } else {
                super.onBackPressed();
            }
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Utils.showSnackBar(this, "Please click BACK again to exit");
        //Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                doubleBackToExitPressedOnce = false;
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
                    "mailto", "rajkumaar2304@gmail.com", null));
            it.putExtra(Intent.EXTRA_SUBJECT, "Regarding Bug in Amrita Repository App");
            it.putExtra(Intent.EXTRA_EMAIL, new String[]{"rajkumaar2304@gmail.com"});
            if (it.resolveActivity(getPackageManager()) != null)
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

        } else if (id == R.id.nav_faq) {
            if (Utils.isConnected(LaunchingActivity.this))
                startActivity(new Intent(LaunchingActivity.this, ExamsFAQActivity.class));
            else
                Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
        } else if (id == R.id.nav_share) {
            drawer.closeDrawer(GravityCompat.START);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Find all question papers under one roof. Download Amrita Repository, the must-have app for exam preparation " +
                            " : https://play.google.com/store/apps/details?id=" + getPackageName());
            sendIntent.setType("text/plain");
            if (sendIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(sendIntent);
            } else {
                Toast.makeText(this, "Error.", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.nav_about) {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.settings) {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_review) {
            drawer.closeDrawer(GravityCompat.START);
            if (Utils.isConnected(this)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Error Opening Play Store.", Toast.LENGTH_SHORT).show();
                }
            } else
                Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
        }
        return true;
    }


    /**
     * Power up the on click listeners of items in home grid
     */
    private void powerUpOnClickListeners() {
        findViewById(R.id.qpapers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences pref = LaunchingActivity.this.getSharedPreferences("user", Context.MODE_PRIVATE);
                if (pref.getBoolean("remember_program", false) && pref.getInt("pos", -1) >= 0) {
                    intentSemActivity(pref.getInt("pos", 0), pref.getString("program", null));
                } else {
                    final AlertDialog.Builder programs_builder = new AlertDialog.Builder(LaunchingActivity.this);
                    programs_builder.setCancelable(true);
                    programs_builder.setTitle("Choose your program");
                    final String[] categories = {"B.Tech", "BA Communication", "MA Communication", "Integrated MSc & MA", "MCA", "MSW", "M.Tech"};
                    final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(LaunchingActivity.this, android.R.layout.simple_list_item_1, categories);
                    programs_builder.setItems(categories, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final int position = which;
                            if (Utils.isConnected(LaunchingActivity.this)) {
                                if (pref.getBoolean("prompt", true)) {
                                    final SharedPreferences.Editor ed = pref.edit();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LaunchingActivity.this);
                                    builder.setMessage("Do you want me to remember your academic program ? You can change it later in settings anytime.");
                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ed.putBoolean("remember_program", true);
                                            ed.putInt("pos", position);
                                            ed.putString("program", dataAdapter.getItem(position));
                                            intentSemActivity(position, dataAdapter.getItem(position));
                                            ed.apply();
                                        }
                                    });
                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ed.putBoolean("remember_program", false);
                                            ed.putInt("pos", -1);
                                            ed.putString("program", null);
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
                startActivity(new Intent(LaunchingActivity.this, AcademicTimetableActivity.class));
            }
        });

        findViewById(R.id.faculty_timetable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LaunchingActivity.this, FacultyTimetableActivity.class));
            }
        });
        findViewById(R.id.exam_schedule).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isConnected(LaunchingActivity.this))
                    startActivity(new Intent(LaunchingActivity.this, ExamCategoryActivity.class));
                else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
            }
        });
        findViewById(R.id.aums).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] items = {"AUMS - v1","AUMS - Lite"};
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LaunchingActivity.this);
                dialogBuilder.setTitle("Confused ? Try v1 and if it doesn't work for you, choose Lite.");
                dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(Utils.isConnected(LaunchingActivity.this)) {
                                    switch (which) {
                                        case 0:
                                            startActivity(new Intent(LaunchingActivity.this, LoginActivity.class));
                                            break;
                                        case 1:
                                            SharedPreferences pref = getSharedPreferences("aums-lite", Context.MODE_PRIVATE);
                                            if (pref.getBoolean("logged-in", false)) {
                                                startActivity(new Intent(LaunchingActivity.this, in.co.rajkumaar.amritarepo.aumsV2.activities.HomeActivity.class));
                                            } else {
                                                startActivity(new Intent(LaunchingActivity.this, in.co.rajkumaar.amritarepo.aumsV2.activities.LoginActivity.class));
                                            }
                                            break;
                                    }
                                }else{
                                    Utils.showInternetError(LaunchingActivity.this);
                                }
                            }
                        }
                );
                dialogBuilder.create().show();
            }
        });

        findViewById(R.id.timings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] items = {"Campus Shuttle Buses","Public Transport"};
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LaunchingActivity.this);
                dialogBuilder.setTitle("View timings of ?");
                dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        if(item==0) {
                            final CharSequence[] items = {"Buses from AB1", "Buses from AB3"};
                            AlertDialog.Builder dialogBuilderInner = new AlertDialog.Builder(LaunchingActivity.this);
                            dialogBuilderInner.setTitle("View timings of ?");
                            dialogBuilderInner.setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    Intent trainBusOpen = new Intent(LaunchingActivity.this, ShuttleBusTimingsActivity.class);
                                    trainBusOpen.putExtra("type", items[item]);
                                    startActivity(trainBusOpen);
                                }
                            });
                            AlertDialog dialogInner = dialogBuilderInner.create();
                            dialogInner.show();
                        }else{
                            final CharSequence[] items = {"Trains from Coimbatore", "Trains from Palghat", "Trains to Coimbatore", "Trains to Palghat", "Buses from Coimbatore", "Buses to Coimbatore"};
                            AlertDialog.Builder dialogBuilderInner = new AlertDialog.Builder(LaunchingActivity.this);
                            dialogBuilderInner.setTitle("View timings of ?");
                            dialogBuilderInner.setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    Intent trainBusOpen = new Intent(LaunchingActivity.this, TimingsActivity.class);
                                    trainBusOpen.putExtra("type", items[item]);
                                    startActivity(trainBusOpen);
                                }
                            });
                            AlertDialog dialogInner = dialogBuilderInner.create();
                            dialogInner.show();
                        }
                    }
                });
                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
        });
        findViewById(R.id.curriculum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isConnected(LaunchingActivity.this)) {
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
                } else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
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
                if (Utils.isConnected(LaunchingActivity.this))
                    startActivity(new Intent(LaunchingActivity.this, WifiStatusActivity.class));
                else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
            }
        });
    }

    /**
     * Sends an intent to semester activity with the selected program
     *
     * @param position
     * @param title
     */
    private void intentSemActivity(int position, String title) {
        Bundle params = new Bundle();
        params.putString("Department", title);
        Log.e("Dept", title);
        mFirebaseAnalytics.logEvent("EventDept", params);

        Intent intent = new Intent(LaunchingActivity.this, SemesterActivity.class);
        intent.putExtra("course", position);
        intent.putExtra("pageTitle", title);
        startActivity(intent);
    }


    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
}

