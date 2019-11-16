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
import android.os.Environment;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.widgets.ImageWidget;

public class ImagesFragment extends Fragment {
    final String dirPath = Environment.getExternalStorageDirectory() + "/AmritaRepo";
    SwipeRefreshLayout swipeRefreshLayout;
    ListView listView;
    File dir = new File(dirPath);
    File[] files;
    ArrayAdapter<String> fileAdapter;
    private List<String> fileList = new ArrayList<String>();

    public ImagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.word_list, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_downloads);
        swipeRefreshLayout.setColorScheme(R.color.colorAccent);
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

    public void retrieveFiles() {
        files = dir.listFiles();
        fileList.clear();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (name.toLowerCase().contains(".jpg"))
                    fileList.add(file.getName());
            }
        }
    }


    public void listFiles(final View rootView) {
        if (!fileList.isEmpty()) {
            LinearLayout empty = rootView.findViewById(R.id.dempty_view);
            empty.setVisibility(View.GONE);
            fileAdapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_list_item, fileList);
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

                    final File pdfFile = new File(dirPath + "/" + fileList.get(i));
                    final String renamingFileName = fileList.get(i);
                    if (pdfFile.exists()) {
                        final ArrayList<String> qPaperOptions = new ArrayList<>();
                        qPaperOptions.add("Open");
                        qPaperOptions.add("Delete");
                        qPaperOptions.add("Rename");
                        qPaperOptions.add("Set as widget");
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()); //Read Update
                        ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, qPaperOptions);
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
                                    }
                                }
                            }

                        });

                        alertDialog.show();
                    } else {
                        Toast.makeText(getContext(), "Error Opening File", Toast.LENGTH_SHORT).show();
                    }
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

}
