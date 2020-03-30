/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.examschedule;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.DownloadTask;
import in.co.rajkumaar.amritarepo.helpers.OpenTask;
import in.co.rajkumaar.amritarepo.helpers.Utils;
import in.co.rajkumaar.amritarepo.papers.PaperAdapter;

public class ExamsListActivity extends BaseActivity {

    private ArrayList<String> texts;
    private ArrayList<String> links;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exams_under_each_dept);
        listView = findViewById(R.id.list);
        texts = new ArrayList<>();
        links = new ArrayList<>();
        ArrayList<ExamCategoryActivity.ExamItem> exams = new Gson().fromJson(getIntent().getStringExtra("exams")
                , new TypeToken<ArrayList<ExamCategoryActivity.ExamItem>>() {
                }.getType());
        for (ExamCategoryActivity.ExamItem item : exams) {
            texts.add(item.getTitle().replace("Download »", ""));
            links.add(item.getLink());
        }
        populateExams();
    }

    private void populateExams() {

        PaperAdapter adapter = new PaperAdapter(ExamsListActivity.this, texts, "examlist");
        adapter.setCustomListener(new PaperAdapter.customListener() {
            @Override
            public void onItemClickListener(final int i) {
                final ArrayList<String> options = new ArrayList<>();
                options.add("Open");
                options.add("Download");
                AlertDialog.Builder builder = new AlertDialog.Builder(ExamsListActivity.this);
                ArrayAdapter<String> optionsAdapter = new ArrayAdapter<>(ExamsListActivity.this, android.R.layout.simple_list_item_1, options);
                builder.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int pos) {
                        if (pos == 0) {
                            if (ContextCompat.checkSelfPermission(ExamsListActivity.this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(ExamsListActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        1);
                            } else {
                                if (Utils.isConnected(ExamsListActivity.this)) {
                                    new OpenTask(ExamsListActivity.this, getString(R.string.intranet_url) + links.get(i), 2);
                                } else {
                                    Utils.showInternetError(ExamsListActivity.this);
                                }


                            }
                        } else if (pos == 1) {
                            if (ContextCompat.checkSelfPermission(ExamsListActivity.this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(ExamsListActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        1);
                            } else {
                                if (Utils.isConnected(ExamsListActivity.this)) {
                                    new DownloadTask(ExamsListActivity.this, getString(R.string.intranet_url) + links.get(i), 2);
                                } else {
                                    Utils.showInternetError(ExamsListActivity.this);
                                }


                            }
                        }
                    }
                });
                builder.show();


            }
        });
        listView.setAdapter(adapter);
    }
}
