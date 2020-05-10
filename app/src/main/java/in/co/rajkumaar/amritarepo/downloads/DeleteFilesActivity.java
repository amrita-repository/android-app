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
import java.util.Objects;
import java.util.Stack;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.downloads.adapters.DownloadsItemAdapter;
import in.co.rajkumaar.amritarepo.downloads.models.DownloadsItem;

public class DeleteFilesActivity extends BaseActivity implements FolderHelper {

    private File dir;
    private ListView listView;
    private int count;
    private ArrayList<DownloadsItem> fileList;
    private Stack<String> curFolder = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_files);
        String dirPath = getExternalFilesDir(null) + "/AmritaRepo";
        dir = new File(dirPath);
        curFolder.push(dirPath);

        fileList = new ArrayList<>();
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
                                        File file = new File(fileList.get(i).getFilePath());
                                        deleteRecursive(file);
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
        if (curFolder.size() > 1) {
            fileList.add(new DownloadsItem(getString(R.string.go_back), "", false));
        }
        if (files != null) {
            for (File file : files) {
                fileList.add(new DownloadsItem(file.getAbsolutePath(), file.isDirectory() ? "Click to go inside the folder" : (file.length() / 1024) + " kb", false));
            }
        }
    }

    private void listFiles() {
        if (!fileList.isEmpty()) {
            ArrayAdapter<DownloadsItem> fileAdapter = new DownloadsItemAdapter(this, fileList, this);
            final ListView downloads = listView;
            downloads.setAdapter(fileAdapter);
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (String child : Objects.requireNonNull(fileOrDirectory.list()))
                deleteRecursive(new File(fileOrDirectory, child));
        }
        fileOrDirectory.delete();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void loadFilesFromDir(String dir) {
        if (getString(R.string.go_back).equalsIgnoreCase(dir)) {
            curFolder.pop();
        } else {
            curFolder.push(dir);
        }
        this.dir = new File(curFolder.peek());
        retrieveFiles();
        listFiles();
    }
}
