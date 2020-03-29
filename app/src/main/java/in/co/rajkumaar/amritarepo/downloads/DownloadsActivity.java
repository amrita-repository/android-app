/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.downloads;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.downloads.adapters.CategoryAdapter;
import in.co.rajkumaar.amritarepo.helpers.ClearCache;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class DownloadsActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_downloads);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = findViewById(R.id.viewpager);

        if (ContextCompat.checkSelfPermission(DownloadsActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DownloadsActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            this.recreate();
        }


        new ClearCache().clear(this);
        // Create an adapter that knows which fragment should be shown on each page
        CategoryAdapter adapter = new CategoryAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        if (getSharedPreferences("user", MODE_PRIVATE).getBoolean("ftp-dialog", true)) {
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
            getSharedPreferences("user", MODE_PRIVATE).edit().putBoolean("ftp-dialog", false).apply();
        }

        if (getIntent().getBooleanExtra("widget", false)) {
            viewPager.setCurrentItem(1);
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Long click on any image here and set as widget image");
            alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();
        }


        // Find the tab layout that shows the tabs
        TabLayout tabLayout = findViewById(R.id.tabs);

        // Connect the tab layout with the view pager. This will
        //   1. Update the tab layout when the view pager is swiped
        //   2. Update the view pager when a tab is selected
        //   3. Set the tab layout's tab names with the view pager's adapter's titles
        //      by calling onPageTitle()
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.downloads_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.ftp:
                startActivity(new Intent(this, FTPActivity.class));
                break;
            case R.id.trash:
                CharSequence[] items = {"Delete multiple files", "Delete all files"};
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DownloadsActivity.this);
                dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        String dirPath = getExternalFilesDir(null) + "/AmritaRepo";
                                        File dir = new File(dirPath);
                                        File[] files;
                                        files = dir.listFiles();
                                        if (files != null && files.length > 0) {
                                            startActivity(new Intent(DownloadsActivity.this, DeleteFilesActivity.class));
                                        }
                                        break;
                                    case 1: {
                                        dirPath = getExternalFilesDir(null) + "/AmritaRepo";
                                        dir = new File(dirPath);
                                        final File[] filesList = dir.listFiles();
                                        if (filesList != null && filesList.length > 0) {
                                            final ArrayList<String> qPaperOptions = new ArrayList<>();
                                            qPaperOptions.add("Documents");
                                            qPaperOptions.add("Images");
                                            qPaperOptions.add("All Files");
                                            AlertDialog.Builder contents = new AlertDialog.Builder(DownloadsActivity.this); //Read Update
                                            ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(DownloadsActivity.this, android.R.layout.simple_list_item_1, qPaperOptions);
                                            contents.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
                                                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(DownloadsActivity.this);

                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int pos) {
                                                    switch (pos) {
                                                        case 0: {
                                                            alertDialog.setMessage("Are you sure you want to delete all documents? ");
                                                            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    boolean flag = false;
                                                                    for (File file : filesList) {
                                                                        String name = file.getName();
                                                                        if (name.contains(".pdf") || name.contains(".xls") || name.contains(".xlsx")) {
                                                                            flag = true;
                                                                            file.delete();
                                                                        }
                                                                    }
                                                                    if (flag) {
                                                                       Utils.showToast(DownloadsActivity.this, "All documents deleted");
                                                                        finish();
                                                                        startActivity(new Intent(DownloadsActivity.this, DownloadsActivity.class));
                                                                    }
                                                                }
                                                            });
                                                            break;
                                                        }
                                                        case 1: {
                                                            alertDialog.setMessage("Are you sure you want to delete all images? ");
                                                            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    boolean flag = false;
                                                                    for (File file : filesList) {
                                                                        String name = file.getName();
                                                                        if (name.contains(".jpg")) {
                                                                            file.delete();
                                                                            flag = true;
                                                                        }
                                                                    }
                                                                    if (flag) {
                                                                        Toast.makeText(DownloadsActivity.this, "All images deleted", Toast.LENGTH_SHORT).show();
                                                                        finish();
                                                                        startActivity(new Intent(DownloadsActivity.this, DownloadsActivity.class));
                                                                    }
                                                                }
                                                            });
                                                            break;
                                                        }
                                                        case 2: {
                                                            alertDialog.setMessage("Are you sure you want to delete all files? ");
                                                            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    boolean flag = false;
                                                                    for (File file : filesList) {
                                                                        flag = true;
                                                                        file.delete();
                                                                    }
                                                                    if (flag) {
                                                                        Toast.makeText(DownloadsActivity.this, "All files deleted", Toast.LENGTH_SHORT).show();
                                                                        finish();
                                                                        startActivity(new Intent(DownloadsActivity.this, DownloadsActivity.class));
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.cancel();
                                                        }
                                                    });
                                                    alertDialog.show();
                                                }
                                            });
                                            contents.show();
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                );
                dialogBuilder.create().show();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        super.onBackPressed();
    }
}
