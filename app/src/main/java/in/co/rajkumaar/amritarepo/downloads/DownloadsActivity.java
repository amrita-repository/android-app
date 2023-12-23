/*
 * Copyright (c) 2023 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.downloads;

import static in.co.rajkumaar.amritarepo.helpers.Utils.isExtension;

import android.Manifest;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.downloads.adapters.DocumentsItemAdapter;
import in.co.rajkumaar.amritarepo.helpers.ClearCache;
import in.co.rajkumaar.amritarepo.helpers.Utils;
import in.co.rajkumaar.amritarepo.widgets.ImageWidget;

public class DownloadsActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private File dir;
    private ListView listView;
    private DocumentsItemAdapter fileAdapter;

    private ArrayList<String> fileList;
    private Stack<String> curFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);

        if (ContextCompat.checkSelfPermission(DownloadsActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DownloadsActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            this.recreate();
        }

        String dirPath = getExternalFilesDir(null) + "/AmritaRepo";
        dir = new File(dirPath);

        fileList = new ArrayList<>();
        curFolder = new Stack<>();
        curFolder.push(dirPath);

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
        retrieveFiles();
        displayList();
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reproduce();
            }
        });

        new ClearCache().clear(this);

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
        retrieveFiles();
        displayList();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void retrieveFiles() {
        File[] files = dir.listFiles();
        fileList.clear();
        if (curFolder.size() > 1) {
            fileList.add("Go Back");
        }
        if (files != null) {
            for (File file : files) {
                fileList.add(file.getAbsolutePath());
            }
        }
    }

    private void displayList() {
        if (!fileList.isEmpty()) {
            LinearLayout empty = findViewById(R.id.dempty_view);
            empty.setVisibility(View.GONE);
            fileAdapter = new DocumentsItemAdapter(DownloadsActivity.this, fileList);
            final ListView downloads = listView;

            downloads.setAdapter(fileAdapter);
            downloads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (fileList.get(i).equalsIgnoreCase("Go Back")) {
                        curFolder.pop();
                        dir = new File(curFolder.peek());
                        retrieveFiles();
                        displayList();
                        return;
                    }
                    final File Element = new File(fileList.get(i));
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
        } else {
            if (fileAdapter != null)
                fileAdapter.clear();
            LinearLayout empty = findViewById(R.id.dempty_view);
            empty.setVisibility(View.VISIBLE);
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (String child : Objects.requireNonNull(fileOrDirectory.list()))
                deleteRecursive(new File(fileOrDirectory, child));
        }
        fileOrDirectory.delete();
    }

    private void openFile(File file) {
        if (file.isDirectory()) {
            if (Objects.requireNonNull(file.list()).length == 0) {
                Utils.showToast(DownloadsActivity.this, "This directory is empty!");
            } else {
                curFolder.push(file.getAbsolutePath());
                this.dir = new File(curFolder.peek());
                retrieveFiles();
                displayList();
            }
        } else {
            Utils.openFileIntent(DownloadsActivity.this, file);
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
                    file.renameTo(new File(file.getParent() + "/" + textBox.getText()));
                } else {
                    String extension = renamingFileName.substring(renamingFileName.lastIndexOf('.'));
                    file.renameTo(new File(file.getParent() + "/" + textBox.getText() + extension));
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

    private void setWidget(String renamingFileName) {
        try {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(DownloadsActivity.this);
            SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
            String path = preferences.getString("path", null);
            alertDialog.setMessage(
                    (path == null)
                            ? renamingFileName + " has been set as your widget image. Go to your homescreen and long press to add widgets and choose Amrita Repository widget!"
                            : renamingFileName + " has been set as your widget image.");
            preferences.edit().putString("path", renamingFileName).apply();
            alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();
            Intent intentWidget = new Intent(DownloadsActivity.this, ImageWidget.class);
            intentWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = AppWidgetManager.getInstance(DownloadsActivity.this).getAppWidgetIds(new ComponentName(DownloadsActivity.this, ImageWidget.class));
            intentWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intentWidget);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void activateLongClick(int i) {
        final File file = new File(fileList.get(i));
        final String renamingFileName = file.getName();
        if (file.exists()) {
            final ArrayList<String> fileOptions = new ArrayList<>();
            fileOptions.add("Open");
            fileOptions.add("Delete");
            fileOptions.add("Rename");
            if (!file.isDirectory()) {
                fileOptions.add("Share");
            }

            String currentType = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            if (isExtension(Utils.image, currentType)) {
                fileOptions.add("Set as widget");
            }

            fileOptions.add("Delete multiple files");

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(DownloadsActivity.this); //Read Update
            ArrayAdapter<String> optionsAdapter = new ArrayAdapter<>(DownloadsActivity.this, android.R.layout.simple_list_item_1, fileOptions);
            alertDialog.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int pos) {
                    switch (fileOptions.get(pos)) {
                        case "Open":
                            openFile(file);
                            break;
                        case "Delete":
                            deleteFileOption(file);
                            break;
                        case "Rename":
                            renameFile(file, renamingFileName);
                            break;
                        case "Set as widget":
                            setWidget(renamingFileName);
                            break;
                        case "Delete multiple files":
                            startActivity(new Intent(DownloadsActivity.this, DeleteFilesActivity.class));
                            break;
                        case "Share":
                            Utils.shareFileIntent(DownloadsActivity.this, file);
                            break;
                        default:
                            Utils.showUnexpectedError(DownloadsActivity.this);
                            break;
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
                                                                    recreate();
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
                                                                    recreate();
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
                                                                    recreate();
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
        if (!(isExtension(Utils.image, doc.substring(doc.lastIndexOf('.') + 1)) || doc.equalsIgnoreCase("AmritaRepo"))) {
            fileOrDirectory.delete();
        }
    }

    private void deleteRecursiveImages(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (String child : fileOrDirectory.list())
                deleteRecursiveImages(new File(fileOrDirectory, child));
        }
        String doc = fileOrDirectory.getName().toLowerCase();
        if (isExtension(Utils.image, doc.substring(doc.lastIndexOf('.') + 1))) {
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
