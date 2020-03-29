/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.downloads;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.downloads.adapters.DownloadsItemAdapter;
import in.co.rajkumaar.amritarepo.downloads.models.DownloadsItem;

public class DeleteFilesActivity extends BaseActivity {

    private File dir;
    private ListView listView;
    private ArrayAdapter<DownloadsItem> fileAdapter;
    private int count;
    private ArrayList<DownloadsItem> fileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_files);
        String dirPath = getExternalFilesDir(null) + "/AmritaRepo";
        dir = new File(dirPath);
        fileList = new ArrayList<DownloadsItem>();
        listView = findViewById(R.id.list);
        retrieveFiles();
        listFiles();

        Button delete = findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = 0;
                for (int i = 0; i < fileList.size(); ++i) {
                    if (fileList.get(i).getCheckBox()) {
                        count++;
                    }
                }
                if (count > 0) {
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(DeleteFilesActivity.this);
                    alertDialog.setMessage("Are you sure you want to delete these " + count + " files? ");
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            if (!fileList.isEmpty()) {
                                for (int i = 0; i < fileList.size(); ++i) {
                                    if (fileList.get(i).getCheckBox()) {
                                        fileList.get(i).getTitle().delete();
                                    }
                                }
                                Toast.makeText(DeleteFilesActivity.this, count + " files deleted", Toast.LENGTH_SHORT).show();
                                retrieveFiles();
                                listFiles();
                            }
                        }
                    });
                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    alertDialog.show();
                } else {
                    Toast.makeText(DeleteFilesActivity.this, "You have not selected any file.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void retrieveFiles() {
        File[] files = dir.listFiles();
        fileList.clear();
        if (files != null) {
            for (File file : files) {
                fileList.add(new DownloadsItem(file, (file.length() / 1024) + " kb", false));
            }
        }
    }

    private void listFiles() {
        if (!fileList.isEmpty()) {
            fileAdapter = new DownloadsItemAdapter(this, fileList);
            final ListView downloads = listView;
            downloads.setAdapter(fileAdapter);
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
