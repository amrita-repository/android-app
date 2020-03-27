/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.downloads.fragments;


import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.downloads.DeleteFilesActivity;
import in.co.rajkumaar.amritarepo.widgets.ImageWidget;

public class ImagesFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private File dir;
    private String dirPath;
    private ArrayAdapter<String> fileAdapter;
    private List<String> fileList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.word_list, container, false);
        dirPath = getContext().getExternalFilesDir(null) + "/AmritaRepo";
        dir = new File(dirPath);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_downloads);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        listView = rootView.findViewById(R.id.dlist);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
        reproduce(rootView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reproduce(rootView);
            }
        });
        return rootView;
    }

    public void reproduce(View rootView) {
        retrieveFiles();
        listFiles(rootView);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void retrieveFiles() {
        File[] files = dir.listFiles();
        fileList.clear();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (name.toLowerCase().contains(".jpg"))
                    fileList.add(file.getName());
            }
        }
    }


    private void listFiles(final View rootView) {
        if (!fileList.isEmpty()) {
            LinearLayout empty = rootView.findViewById(R.id.dempty_view);
            empty.setVisibility(View.GONE);
            fileAdapter = new ArrayAdapter<>(getActivity(), R.layout.custom_list_item, fileList);
            final ListView downloads = listView;

            downloads.setAdapter(fileAdapter);
            downloads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    File pdfFile = new File(dirPath + "/" + fileList.get(i));
                    if (pdfFile.exists()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri data = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", pdfFile);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(data, "image/*");
                        if (intent.resolveActivity(getContext().getPackageManager()) != null)
                            startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Error Opening File", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            downloads.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    activateLongClick(i, rootView);
                    return true;
                }
            });
        } else {
            if (fileAdapter != null)
                fileAdapter.clear();
            LinearLayout empty = rootView.findViewById(R.id.dempty_view);
            empty.setVisibility(View.VISIBLE);
        }
    }

    private void activateLongClick(int i, final View rootView) {
        final File pdfFile = new File(dirPath + "/" + fileList.get(i));
        final String renamingFileName = fileList.get(i);
        if (pdfFile.exists()) {
            final ArrayList<String> options = new ArrayList<>();
            options.add("Open");
            options.add("Delete");
            options.add("Rename");
            options.add("Set as widget");
            options.add("Delete multiple files");
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()); //Read Update
            ArrayAdapter<String> optionsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, options);
            alertDialog.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int pos) {
                    switch (pos) {
                        case 0:
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri data = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", pdfFile);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setDataAndType(data, "image/jpeg");
                            if (intent.resolveActivity(getContext().getPackageManager()) != null)
                                startActivity(Intent.createChooser(intent, "Open the file"));
                            break;
                        case 1: {
                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                            alertDialog.setMessage("Are you sure you want to delete the file? ");
                            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    pdfFile.delete();
                                    Toast.makeText(getActivity(), renamingFileName + " Deleted", Toast.LENGTH_SHORT).show();
                                    reproduce(rootView);
                                }
                            });
                            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                            alertDialog.show();
                            break;
                        }
                        case 2: {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                            alertDialog.setTitle("Rename file");
                            LinearLayout layout = new LinearLayout(getActivity());
                            layout.setOrientation(LinearLayout.VERTICAL);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(40, 0, 50, 0);

                            final EditText textBox = new EditText(getActivity());
                            textBox.setText(renamingFileName.split("\\.")[0]);
                            layout.addView(textBox, params);

                            alertDialog.setView(layout);

                            alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    pdfFile.renameTo(new File(dirPath + "/" + textBox.getText() + ".jpg"));
                                    reproduce(rootView);
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                            alertDialog.show();
                            break;
                        }
                        case 3: {
                            try {
                                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                SharedPreferences preferences = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
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
                                Intent intentWidget = new Intent(getContext(), ImageWidget.class);
                                intentWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                                int[] ids = AppWidgetManager.getInstance(getActivity()).getAppWidgetIds(new ComponentName(getActivity(), ImageWidget.class));
                                intentWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                                getActivity().sendBroadcast(intentWidget);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case 4: {
                            startActivity(new Intent(getContext(), DeleteFilesActivity.class));
                            break;
                        }
                    }
                }

            });

            alertDialog.show();
        } else {
            Toast.makeText(getContext(), "Error Opening File", Toast.LENGTH_SHORT).show();
        }
    }

}
