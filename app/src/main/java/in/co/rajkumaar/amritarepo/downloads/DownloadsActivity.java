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
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.downloads.adapters.DocumentsItemAdapter;
import in.co.rajkumaar.amritarepo.helpers.ClearCache;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class DownloadsActivity extends BaseActivity {

    private String dirPath;
    private SwipeRefreshLayout swipeRefreshLayout;
    private File dir;
    private ListView listView;
    private DocumentsItemAdapter fileAdapter;

    private Stack<ArrayList<String>> fileListStack;
    private Stack<String> curFolder;
    private File current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_downloads);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (ContextCompat.checkSelfPermission(DownloadsActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DownloadsActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            this.recreate();
        }

        dirPath = getExternalFilesDir(null) + "/AmritaRepo";
        dir = new File(dirPath);

        fileListStack = new Stack<>();
        curFolder = new Stack<>();
        curFolder.add("");
        current = dir;

        swipeRefreshLayout = findViewById(R.id.swipe_downloads);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        listView = findViewById(R.id.dlist);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //Method must be implemented, intentional empty body
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
        retrieveFiles(dir);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reproduce();
            }
        });

        new ClearCache().clear(this);

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

    }

    private void reproduce() {
        fileListStack.pop();
        retrieveFiles(current);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void retrieveFiles(File dir) {
        current = dir;
        String[] dirElements = dir.list();
        ArrayList<String> fileList = new ArrayList<>();
        if (fileListStack.size() >= 1) {
            fileList.add("Go Back");
        }
        if (dirElements != null) {
            for (String Element : dirElements) {
                fileList.add(Element);
            }
        }
        fileListStack.add(fileList);
        displayList();
    }

    private void displayList() {
        if (!fileListStack.isEmpty()) {
            final ArrayList<String> fileList = fileListStack.peek();
            if (fileList.isEmpty()) {
                if (fileAdapter != null)
                    fileAdapter.clear();
                LinearLayout empty = findViewById(R.id.dempty_view);
                empty.setVisibility(View.VISIBLE);
            } else {
                LinearLayout empty = findViewById(R.id.dempty_view);
                empty.setVisibility(View.GONE);
                fileAdapter = new DocumentsItemAdapter(DownloadsActivity.this, fileList);
                final ListView downloads = listView;

                downloads.setAdapter(fileAdapter);

                downloads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if (fileList.get(i).equalsIgnoreCase("Go Back")) {
                            fileListStack.pop();
                            curFolder.pop();
                            if (curFolder.peek().equals("")) {
                                current = dir;
                            } else {
                                current = new File(dirPath + "/" + curFolder.peek());
                            }
                            displayList();
                        }
                        final File Element = new File(dirPath + "/" + curFolder.peek() + fileList.get(i));
                        if (Element.exists()) {
                            openFile(Element);
                        }
                    }
                });

                downloads.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                        activateLongClick(i);
                        return true;
                    }
                });
            }
        } else {
            if (fileAdapter != null)
                fileAdapter.clear();
            LinearLayout empty = findViewById(R.id.dempty_view);
            empty.setVisibility(View.VISIBLE);
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (String child : fileOrDirectory.list())
                deleteRecursive(new File(fileOrDirectory, child));
        }
        fileOrDirectory.delete();
    }

    private void openFile(File file) {
        if (file.isDirectory()) {
            if (file.list().length == 0) {
                Utils.showToast(DownloadsActivity.this, "This directory is empty!");
            } else {
                curFolder.push(curFolder.peek() + file.getName() + "/");
                retrieveFiles(file);
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data = FileProvider.getUriForFile(DownloadsActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setData(data);
            Intent fileChooserIntent = Intent.createChooser(intent, "Open " + file.getName() + " with:");
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(fileChooserIntent);
            else
                Utils.showToast(DownloadsActivity.this, "Sorry, there's no appropriate app in the device to open this file.");
        }
    }

    private void deleteFileOption(final File file) {
        String deleteMsg = "Are you sure you want to delete the file? ";
        if (file.isDirectory()) {
            deleteMsg = "Are you sure you want to delete the folder? ";
        }
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(DownloadsActivity.this);
        alertDialog.setMessage(deleteMsg);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteRecursive(file);
                Toast.makeText(DownloadsActivity.this, file.getName() + " Deleted", Toast.LENGTH_SHORT).show();
                reproduce();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertDialog.show();
    }

    private void renameFile(final File file, final String renamingFileName) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(DownloadsActivity.this);
        if (file.isDirectory()) {
            alertDialog.setMessage("Rename folder : \n" + renamingFileName);
        } else {
            alertDialog.setMessage("Rename file : \n" + renamingFileName.substring(0, renamingFileName.lastIndexOf('.')));
        }
        LinearLayout layout = new LinearLayout(DownloadsActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(40, 0, 50, 0);
        final EditText textBox = new EditText(DownloadsActivity.this);
        layout.addView(textBox, params);
        alertDialog.setView(layout);

        alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (file.isDirectory()) {
                    file.renameTo(new File(dirPath + "/" + curFolder.peek() + textBox.getText()));
                } else {
                    String extension = renamingFileName.substring(renamingFileName.lastIndexOf('.'));
                    file.renameTo(new File(dirPath + "/" + curFolder.peek() + textBox.getText() + extension));
                }
                reproduce();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertDialog.show();
    }

    private void activateLongClick(int i) {
        final ArrayList<String> fileList = fileListStack.peek();
        final File file = new File(dirPath + "/" + curFolder.peek() + fileList.get(i));
        final String renamingFileName = fileList.get(i);
        if (file.exists()) {
            final ArrayList<String> fileOptions = new ArrayList<>();
            fileOptions.add("Open");
            fileOptions.add("Delete");
            fileOptions.add("Rename");
            fileOptions.add("Delete multiple files");
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(DownloadsActivity.this); //Read Update
            ArrayAdapter<String> optionsAdapter = new ArrayAdapter<>(DownloadsActivity.this, android.R.layout.simple_list_item_1, fileOptions);
            alertDialog.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int pos) {
                    if (pos == 0) {
                        openFile(file);
                    } else if (pos == 1) {
                        deleteFileOption(file);

                    } else if (pos == 2) {
                        renameFile(file, renamingFileName);
                    } else if (pos == 3) {
                        startActivity(new Intent(DownloadsActivity.this, DeleteFilesActivity.class));
                    }
                }

            });

            alertDialog.show();
        } else {
            Toast.makeText(DownloadsActivity.this, "Error Opening File", Toast.LENGTH_SHORT).show();
        }
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
                                        final File direc = new File(dirPath);
                                        String[] files;
                                        files = direc.list();
                                        if (files != null && files.length > 0) {
                                            startActivity(new Intent(DownloadsActivity.this, DeleteFilesActivity.class));
                                        }
                                        break;
                                    case 1: {
                                        dirPath = getExternalFilesDir(null) + "/AmritaRepo";
                                        final File dir = new File(dirPath);
                                        final String[] filesList = dir.list();
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
                                                                    deleteRecursiveDocuments(dir);
                                                                    Utils.showToast(DownloadsActivity.this, "All documents deleted");
                                                                    finish();
                                                                    startActivity(new Intent(DownloadsActivity.this, DownloadsActivity.class));
                                                                }
                                                            });
                                                            break;
                                                        }
                                                        case 1: {
                                                            alertDialog.setMessage("Are you sure you want to delete all images? ");
                                                            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    deleteRecursiveImages(dir);
                                                                    Toast.makeText(DownloadsActivity.this, "All images deleted", Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                    startActivity(new Intent(DownloadsActivity.this, DownloadsActivity.class));
                                                                }
                                                            });
                                                            break;
                                                        }
                                                        case 2: {
                                                            alertDialog.setMessage("Are you sure you want to delete all files? ");
                                                            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    deleteRecursiveFiles(dir);
                                                                    Toast.makeText(DownloadsActivity.this, "All files deleted", Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                    startActivity(new Intent(DownloadsActivity.this, DownloadsActivity.class));
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

    private void deleteRecursiveDocuments(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (String child : fileOrDirectory.list())
                deleteRecursiveDocuments(new File(fileOrDirectory, child));
        }
        String doc = fileOrDirectory.getName().toLowerCase();
        if (!(doc.contains(".jpg") || doc.contains(".jpeg") || doc.contains(".png") || doc.equalsIgnoreCase("AmritaRepo"))) {
            fileOrDirectory.delete();
        }
    }

    private void deleteRecursiveImages(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (String child : fileOrDirectory.list())
                deleteRecursiveImages(new File(fileOrDirectory, child));
        }
        String doc = fileOrDirectory.getName().toLowerCase();
        if (doc.contains(".jpg") || doc.contains(".jpeg") || doc.contains(".png")) {
            fileOrDirectory.delete();
        }
    }

    private void deleteRecursiveFiles(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (String child : fileOrDirectory.list())
                deleteRecursiveFiles(new File(fileOrDirectory, child));
        }
        if (!fileOrDirectory.getName().equalsIgnoreCase("AmritaRepo")) {
            fileOrDirectory.delete();
        }
    }
}
