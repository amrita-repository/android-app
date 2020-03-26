/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.ArrayList;
import java.util.List;

import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.about.activities.AboutActivity;
import in.co.rajkumaar.amritarepo.aums.activities.LoginActivity;
import in.co.rajkumaar.amritarepo.curriculum.CurriculumActivity;
import in.co.rajkumaar.amritarepo.downloads.DownloadsActivity;
import in.co.rajkumaar.amritarepo.downloads.FTPActivity;
import in.co.rajkumaar.amritarepo.examschedule.ExamCategoryActivity;
import in.co.rajkumaar.amritarepo.faq.ExamsFAQActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;
import in.co.rajkumaar.amritarepo.helpers.clearCache;
import in.co.rajkumaar.amritarepo.news.NewsActivity;
import in.co.rajkumaar.amritarepo.opac.OPACHomeActivity;
import in.co.rajkumaar.amritarepo.papers.SemesterActivity;
import in.co.rajkumaar.amritarepo.study_materials.StudyMaterialsActivity;
import in.co.rajkumaar.amritarepo.timetable.AcademicTimetableActivity;
import in.co.rajkumaar.amritarepo.timetable.FacultyTimetableActivity;
import in.co.rajkumaar.amritarepo.timings.TimingsHomeActivity;
import in.co.rajkumaar.amritarepo.wifistatus.WifiStatusActivity;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.ParticleSystem;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class LaunchingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static boolean active = false;
    boolean doubleBackToExitPressedOnce = false;
    private FirebaseAnalytics mFirebaseAnalytics;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launching);
        FirebaseApp.initializeApp(this);
        Iconify.with(new FontAwesomeModule());

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        if (ContextCompat.checkSelfPermission(LaunchingActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LaunchingActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            new clearCache().clear(this);
            if (pref.getBoolean("first", true)) {
                AboutActivity.showDisclaimer(this);
                SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                editor.putBoolean("first", false);
                editor.apply();
            }
        }
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

        if (pref.getInt("visit", 0) >= 3 && pref.getBoolean("ftp-dialog", true)) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.ftp_dialog);
            dialog.findViewById(R.id.okay).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), FTPActivity.class));
                }
            });
            dialog.show();
            pref.edit().putBoolean("ftp-dialog", false).apply();
        } else if (pref.getInt("visit", 0) <= 7) {
            pref.edit().putInt("visit", pref.getInt("visit", 0) + 1).apply();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        toolbar.setSubtitle(Html.fromHtml("Crafted with &hearts; by Rajkumar"));
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://rajkumaar.co.in";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_home));
        navigationView.setNavigationItemSelectedListener(this);
        TextView versionName = navigationView.getHeaderView(0).findViewById(R.id.versioncode);
        versionName.setText("Version ".concat(BuildConfig.VERSION_NAME));
        navigationView.getMenu().getItem(0).setChecked(true);

        initialize();
        if (!getSharedPreferences("confetti", MODE_PRIVATE).getBoolean("shown", false)) {
            showCelebs();
            getSharedPreferences("confetti", MODE_PRIVATE).edit().putBoolean("shown", true).apply();
        } else {
            if (!pref.getBoolean("ftp-dialog", false) && !pref.getBoolean("first", true) && !pref.getBoolean("feedback-done", true)) {
                String feedbackText = "Hello there, your opinion matters.<br>Could you please spend a minute to leave feedback on your experience? <br>I appreciate your help! &hearts;";
                try {
                    final AlertDialog.Builder feedbackDialog = new AlertDialog.Builder(LaunchingActivity.this);
                    feedbackDialog.setMessage(Html.fromHtml(feedbackText));
                    feedbackDialog.setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://rajkumaar.co.in/repo-feedback"));
                            pref.edit().putBoolean("feedback-done", true).apply();
                            if (intent.resolveActivity(getPackageManager()) != null)
                                startActivity(intent);
                        }
                    });
                    feedbackDialog.setNegativeButton("Never", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            pref.edit().putBoolean("feedback-done", true).apply();
                            dialogInterface.dismiss();
                        }
                    });
                    feedbackDialog.setNeutralButton("Remind me later", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    feedbackDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showCelebs() {
        ParticleSystem konfettiView = ((KonfettiView) findViewById(R.id.container)).build();
        konfettiView.addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                .setDirection(359.0, 0.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(3000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(new Size(8, 2f))
                .setPosition(-50f, findViewById(R.id.container).getWidth() + 5000f, -50f, -50f)
                .streamFor(400, 5000L);

        final Dialog thanksGiving = new Dialog(LaunchingActivity.this);
        thanksGiving.setContentView(R.layout.thanks_dialog);
        thanksGiving.setCancelable(false);
        TextView textView = thanksGiving.findViewById(R.id.update_text);
        textView.setText(Html.fromHtml("Amrita Repository <br>celebrates 7000+ downloads in Play Store. <br>Thanks for being an active user. &hearts;<br><br>I’m convinced that the only thing that kept me going was that I loved what I did. You’ve got to find what you love.<br>- Steve Jobs"));
        thanksGiving.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    thanksGiving.setCancelable(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 3000);
    }

    private void initialize() {
        ((ListView) findViewById(R.id.items_list)).setAdapter(new HomeItemAdapter(this));
    }

    private void powerUpOnClickListener(HomeItemAdapter.Item item) {
        switch (item.getName()) {
            case "Question Papers":
                final SharedPreferences pref = LaunchingActivity.this.getSharedPreferences("user", Context.MODE_PRIVATE);
                if (pref.getBoolean("remember_program", false) && pref.getInt("pos", -1) >= 0) {
                    intentSemActivity(pref.getInt("pos", 0), pref.getString("program", null));
                } else {
                    final AlertDialog.Builder programs_builder = new AlertDialog.Builder(LaunchingActivity.this);
                    programs_builder.setCancelable(true);
                    programs_builder.setTitle("Choose your program");
                    final String[] categories = {"B.Tech", "BA Communication", "MA Communication", "Integrated MSc & MA", "MCA", "MSW", "M.Tech"};
                    final ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(LaunchingActivity.this, android.R.layout.simple_list_item_1, categories);
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
                break;
            case "Academic Timetable":
                if (Utils.isConnected(LaunchingActivity.this)) {
                    startActivity(new Intent(LaunchingActivity.this, AcademicTimetableActivity.class));
                } else {
                    Utils.showInternetError(this);
                }
                break;

            case "Faculty Timetable":
                if (Utils.isConnected(LaunchingActivity.this)) {
                    startActivity(new Intent(LaunchingActivity.this, FacultyTimetableActivity.class));
                } else {
                    Utils.showInternetError(this);
                }
                break;
            case "Exam Schedule":
                if (Utils.isConnected(LaunchingActivity.this))
                    startActivity(new Intent(LaunchingActivity.this, ExamCategoryActivity.class));
                else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "AUMS":
                CharSequence[] items = {"AUMS - v1", "AUMS - Lite (Easier to login)"};
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LaunchingActivity.this);
                dialogBuilder.setTitle("Try v1 and if it doesn't work for you, choose Lite.");
                dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Utils.isConnected(LaunchingActivity.this)) {
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
                                } else {
                                    Utils.showInternetError(LaunchingActivity.this);
                                }
                            }
                        }
                );
                dialogBuilder.create().show();
                break;
            case "Timings":
                startActivity(new Intent(this, TimingsHomeActivity.class));
                break;
            case "Curriculum":
                if (Utils.isConnected(LaunchingActivity.this)) {
                    Intent curriculum_open = new Intent(LaunchingActivity.this, CurriculumActivity.class);
                    startActivity(curriculum_open);
                } else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "Downloads":
                startActivity(new Intent(LaunchingActivity.this, DownloadsActivity.class));
                break;
            case "Study Materials":
                if (Utils.isConnected(LaunchingActivity.this)) {
                    final CharSequence[] depts = {"Computer Science Engineering"};
                    AlertDialog.Builder departmentDialogBuilder = new AlertDialog.Builder(LaunchingActivity.this);
                    departmentDialogBuilder.setTitle("Select your Department");
                    departmentDialogBuilder.setItems(depts, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            Intent curriculum_open = new Intent(LaunchingActivity.this, StudyMaterialsActivity.class);
                            curriculum_open.putExtra("department", depts[item]);
                            startActivity(curriculum_open);
                        }
                    });
                    AlertDialog departmentDialog = departmentDialogBuilder.create();
                    departmentDialog.show();
                } else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "Central Library":
                if (Utils.isConnected(LaunchingActivity.this)) {
                    startActivity(new Intent(LaunchingActivity.this, OPACHomeActivity.class));
                } else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "News":
                if (Utils.isConnected(LaunchingActivity.this))
                    startActivity(new Intent(LaunchingActivity.this, NewsActivity.class));
                else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "WiFi Status":
                if (Utils.isConnected(LaunchingActivity.this))
                    startActivity(new Intent(LaunchingActivity.this, WifiStatusActivity.class));
                else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "FAQ - Exams":
                if (Utils.isConnected(LaunchingActivity.this))
                    startActivity(new Intent(LaunchingActivity.this, ExamsFAQActivity.class));
                else
                    Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
                break;
            case "CMS":
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("in.co.rajkumaar.amritacms");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=in.co.rajkumaar.amritacms"));
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(intent);
                }
                break;
            case "About":
                startActivity(new Intent(getBaseContext(), AboutActivity.class));
                break;
            case "Support":
                startActivity(new Intent(getBaseContext(), SupportActivity.class));
                break;
        }
    }

    @Override
    protected void onPostResume() {
        if (Utils.isConnected(LaunchingActivity.this) && !BuildConfig.DEBUG) {
            checkUpdate();
        }
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        new clearCache().clear(this);
        super.onDestroy();
    }

    /**
     * Compares existing version with latest and prompts for update
     */
    public void checkUpdate() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference versionInDB = firebaseDatabase.getReference("update");
        versionInDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long latestVersion = dataSnapshot.child("version").getValue(Long.class);
                String whatsNewText = dataSnapshot.child("whatsnew").getValue(String.class);
                if (latestVersion > BuildConfig.VERSION_CODE) {
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(LaunchingActivity.this);
                    alertDialog.setMessage(Html.fromHtml(
                            "An update is available for Amrita Repository.<br><br><strong><font color='#AA0000'>What's New ?</font></strong><br>"
                                    + whatsNewText));
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
                            dialogInterface.dismiss();
                        }
                    });
                    try {
                        if (active)
                            alertDialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                try {
                    Crashlytics.log(databaseError.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                NavigationView navigationView = findViewById(R.id.nav_view);
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

        if (id == R.id.celebs) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.action_download) {
            startActivity(new Intent(this, DownloadsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (id == R.id.nav_downloads) {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, DownloadsActivity.class));
        } else if (id == R.id.nav_bot) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://t.me/amrepobot"));
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(intent);
        } else if (id == R.id.nav_feedback) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://rajkumaar.co.in/repo-feedback"));
            getSharedPreferences("user", MODE_PRIVATE).edit().putBoolean("feedback-done", true).apply();
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(intent);
        } else if (id == R.id.nav_share) {
            drawer.closeDrawer(GravityCompat.START);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Find all features an Amritian needs under one roof. Download Amrita Repository, the must-have app!\n" +
                            "https://bit.ly/amritarepo");
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
        } else if (id == R.id.nav_donate) {
            startActivity(new Intent(getBaseContext(), SupportActivity.class));
        } else if (id == R.id.nav_bugreport) {
            Intent it = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "rajkumaar2304@icloud.com", null));
            it.putExtra(Intent.EXTRA_SUBJECT, "Regarding Bug in Amrita Repository App");
            it.putExtra(Intent.EXTRA_EMAIL, new String[]{"rajkumaar2304@icloud.com"});
            if (it.resolveActivity(getPackageManager()) != null)
                startActivity(it);
        }
        return true;
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

        if (Utils.isConnected(this)) {

            Intent intent = new Intent(LaunchingActivity.this, SemesterActivity.class);
            intent.putExtra("course", position);
            intent.putExtra("pageTitle", title);
            startActivity(intent);

        } else {
            Utils.showSnackBar(LaunchingActivity.this, "Device not connected to internet");
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
        active = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    public void openSupport(View view) {
        startActivity(new Intent(this, SupportActivity.class));
    }

    class HomeItemAdapter extends BaseAdapter {

        private List<Item> items = new ArrayList<>();
        private LayoutInflater inflater;
        private Context context;

        HomeItemAdapter(Context context) {

            this.context = context;
            inflater = LayoutInflater.from(context);
            items.clear();
            items.add(new Item("#FF201B", "Question Papers", FontAwesomeIcons.fa_paper_plane));
            items.add(new Item("#009688", "AUMS", FontAwesomeIcons.fa_graduation_cap));
            items.add(new Item("#a4123f", "CMS", FontAwesomeIcons.fa_university));
            items.add(new Item("#ffc107", "Academic Timetable", FontAwesomeIcons.fa_calendar));
            items.add(new Item("#e91e63", "Faculty Timetable", FontAwesomeIcons.fa_users));
            items.add(new Item("#03a9f4", "Central Library", FontAwesomeIcons.fa_laptop));
            items.add(new Item("#3f51b5", "Curriculum", FontAwesomeIcons.fa_paperclip));
            items.add(new Item("#fe5352", "Exam Schedule", FontAwesomeIcons.fa_pencil));
            items.add(new Item("#ffffff", "Timings", FontAwesomeIcons.fa_clock_o));
            items.add(new Item("#9c27b0", "News", FontAwesomeIcons.fa_newspaper_o));
            items.add(new Item("#03a9f4", "Study Materials", FontAwesomeIcons.fa_book));
            items.add(new Item("#259b24", "Downloads", FontAwesomeIcons.fa_download));
//            items.add(new Item("#03a9f4", "WiFi Status", FontAwesomeIcons.fa_wifi));
//            items.add(new Item("#116466", "FAQ - Exams", FontAwesomeIcons.fa_question_circle));
            items.add(new Item("#f13c20", "Support", FontAwesomeIcons.fa_dollar));
            items.add(new Item("#259b24", "About", FontAwesomeIcons.fa_info_circle));
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = view;
            ImageView picture;
            TextView name;

            if (v == null) {
                v = inflater.inflate(R.layout.item_main_grid, viewGroup, false);
                v.setTag(R.id.landing_item_holder, v.findViewById(R.id.landing_item_holder));
                v.setTag(R.id.landing_picture, v.findViewById(R.id.landing_picture));
                v.setTag(R.id.landing_text, v.findViewById(R.id.landing_text));
            }
            picture = (ImageView) v.getTag(R.id.landing_picture);
            name = (TextView) v.getTag(R.id.landing_text);

            final Item item = (Item) getItem(i);
            picture.setImageDrawable(new IconDrawable(context, item.image).color(Color.parseColor(item.color)));
            name.setText(item.name);
            //holder.setBackgroundColor(Color.parseColor(item.color));
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    powerUpOnClickListener(item);
                }
            });
            return v;
        }

        private class Item {

            private final String color;
            private final String name;
            private FontAwesomeIcons image;

            Item(String color, String name, FontAwesomeIcons imageID) {
                this.color = color;
                this.name = name;
                this.image = imageID;
            }

            public String getName() {
                return name;
            }
        }
    }
}

