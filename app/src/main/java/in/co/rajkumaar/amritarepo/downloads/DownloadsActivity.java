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

package in.co.rajkumaar.amritarepo.downloads;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.downloads.adapters.CategoryAdapter;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.clearCache;

public class DownloadsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_downloads);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        if (ContextCompat.checkSelfPermission(DownloadsActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DownloadsActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        this.recreate();}

        new clearCache().clear();
        // Create an adapter that knows which fragment should be shown on each page
        CategoryAdapter adapter = new CategoryAdapter(this,getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);



        // Find the tab layout that shows the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

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
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.delete_multiple:
                String dirPath= Environment.getExternalStorageDirectory() + "/AmritaRepo";
                File dir = new File(dirPath);
                File[] files;
                files = dir.listFiles();
                if(files!=null && files.length>0) {
                    startActivity(new Intent(this,DeleteFilesActivity.class));
                }
                break;
            case R.id.delete_all:
            {
                dirPath=Environment.getExternalStorageDirectory() + "/AmritaRepo";
                dir=new File(dirPath);
                final File[] filesList = dir.listFiles();
                if(filesList!=null && filesList.length>0) {
                    final ArrayList<String> qPaperOptions = new ArrayList<>();
                    qPaperOptions.add("Documents");
                    qPaperOptions.add("Images");
                    qPaperOptions.add("All Files");
                    AlertDialog.Builder contents = new AlertDialog.Builder(this); //Read Update
                    ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, qPaperOptions);
                    contents.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(DownloadsActivity.this);
                        @Override
                        public void onClick(DialogInterface dialogInterface, int pos) {
                            switch (pos) {
                                case 0:
                                {
                                    alertDialog.setMessage("Are you sure you want to delete all documents? ");
                                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            boolean flag=false;
                                            for (File file : filesList) {
                                                String name=file.getName();
                                                if (name.contains(".pdf") || name.contains(".xls") || name.contains(".xlsx")) {
                                                    flag=true;
                                                    file.delete();
                                                }
                                            }
                                            if(flag) {
                                                Toast.makeText(DownloadsActivity.this, "All documents deleted", Toast.LENGTH_SHORT).show();
                                                finish();
                                                startActivity(new Intent(DownloadsActivity.this, DownloadsActivity.class));
                                            }
                                        }
                                    });
                                    break;
                                }
                                case 1:
                                {
                                    alertDialog.setMessage("Are you sure you want to delete all images? ");
                                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            boolean flag=false;
                                            for (File file : filesList) {
                                                String name = file.getName();
                                                if (name.contains(".jpg")) {
                                                    file.delete();
                                                    flag=true;
                                                }
                                            }
                                            if(flag) {
                                                Toast.makeText(DownloadsActivity.this, "All images deleted", Toast.LENGTH_SHORT).show();
                                                finish();
                                                startActivity(new Intent(DownloadsActivity.this, DownloadsActivity.class));
                                            }
                                        }
                                    });
                                    break;
                                }
                                case 2:
                                {
                                    alertDialog.setMessage("Are you sure you want to delete all files? ");
                                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            boolean flag=false;
                                            for (File file : filesList) {
                                                flag=true;
                                                file.delete();
                                            }
                                            if(flag) {
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
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        super.onBackPressed();
    }
}
