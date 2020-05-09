/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.downloads.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.Stack;

import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.downloads.DeleteFilesActivity;
import in.co.rajkumaar.amritarepo.downloads.adapters.DocumentsItemAdapter;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class DocumentsFragment extends Fragment {


    private String dirPath;
    private SwipeRefreshLayout swipeRefreshLayout;
    private File dir;
    private ListView listView;
    private DocumentsItemAdapter fileAdapter;
    private View rootView;

    private Stack<ArrayList<String>> fileListStack;
    private Stack<String> curFolder;
    private File current;


    private void reproduce() {
        fileListStack.pop();
        retrieveFiles(current);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.word_list, container, false);
        dirPath = getContext().getExternalFilesDir(null) + "/AmritaRepo";
        dir = new File(dirPath);

        fileListStack = new Stack<>();
        curFolder = new Stack<>();
        curFolder.add("");
        current = dir;

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
        retrieveFiles(dir);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reproduce();
            }
        });
        return rootView;
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
                if (!(Element.toLowerCase().contains(".jpg") || Element.toLowerCase().contains(".jpeg") || Element.toLowerCase().contains(".png"))) {
                    fileList.add(Element);
                }
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
                LinearLayout empty = rootView.findViewById(R.id.dempty_view);
                empty.setVisibility(View.VISIBLE);
            } else {
                LinearLayout empty = rootView.findViewById(R.id.dempty_view);
                empty.setVisibility(View.GONE);
                fileAdapter = new DocumentsItemAdapter(getActivity(), fileList);
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
                            if (Element.isDirectory()) {
                                if (Element.list().length == 0) {
                                    Utils.showToast(getActivity(), "This directory is empty!");
                                } else {
                                    curFolder.push(curFolder.peek() + fileList.get(i) + "/");
                                    retrieveFiles(Element);
                                }
                            } else {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                Uri data = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", Element);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.setData(data);
                                Intent fileChooserIntent = Intent.createChooser(intent, "Open " + Element.getName() + " with:");
                                if (intent.resolveActivity(getContext().getPackageManager()) != null)
                                    startActivity(fileChooserIntent);
                                else
                                    Utils.showToast(getActivity(), "Sorry, there's no appropriate app in the device to open this file.");
                            }
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
            LinearLayout empty = rootView.findViewById(R.id.dempty_view);
            empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        reproduce();
        super.onResume();
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (String child : fileOrDirectory.list())
                deleteRecursive(new File(fileOrDirectory, child));
        }
        fileOrDirectory.delete();
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
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()); //Read Update
            ArrayAdapter<String> optionsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, fileOptions);
            alertDialog.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int pos) {
                    if (pos == 0) {
                        if (file.isDirectory()) {
                            if (file.list().length == 0) {
                                Utils.showToast(getActivity(), "This directory is empty!");
                            } else {
                                curFolder.push(curFolder.peek() + renamingFileName + "/");
                                retrieveFiles(file);
                            }
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri data = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", file);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setData(data);
                            Intent fileChooserIntent = Intent.createChooser(intent, "Open " + file.getName() + " with:");
                            if (intent.resolveActivity(getContext().getPackageManager()) != null)
                                startActivity(fileChooserIntent);
                            else
                                Utils.showToast(getActivity(), "Sorry, there's no appropriate app in the device to open this file.");
                        }
                    } else if (pos == 1) {
                        String deleteMsg = "Are you sure you want to delete the file? ";
                        if (file.isDirectory()) {
                            deleteMsg = "Are you sure you want to delete the folder? ";
                        }
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                        alertDialog.setMessage(deleteMsg);
                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteRecursive(file);
                                Toast.makeText(getActivity(), renamingFileName + " Deleted", Toast.LENGTH_SHORT).show();
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

                    } else if (pos == 2) {
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                        if (file.isDirectory()) {
                            alertDialog.setMessage("Rename folder : \n" + renamingFileName);
                        } else {
                            alertDialog.setMessage("Rename file : \n" + renamingFileName.substring(0, renamingFileName.lastIndexOf('.')));
                        }
                        LinearLayout layout = new LinearLayout(getActivity());
                        layout.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(40, 0, 50, 0);
                        final EditText textBox = new EditText(getActivity());
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
                    } else if (pos == 3) {
                        startActivity(new Intent(getContext(), DeleteFilesActivity.class));
                    }
                }

            });

            alertDialog.show();
        } else {
            Toast.makeText(getContext(), "Error Opening File", Toast.LENGTH_SHORT).show();
        }
    }
}

