package in.co.rajkumaar.amritarepo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DocumentsFragment extends Fragment {


    public DocumentsFragment() {
        // Required empty public constructor
    }

    SwipeRefreshLayout swipeRefreshLayout;
    final String dirPath= Environment.getExternalStorageDirectory() + "/AmritaRepo";
    File dir = new File(dirPath);
    File[] files;
    ListView listView;
    private List<String> fileList = new ArrayList<String>();
    ArrayAdapter<String> fileAdapter;
    View rootView;

    public void reproduce(View rootView){
        retrieveFiles();
        displayList(rootView);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.word_list, container, false);

        swipeRefreshLayout=rootView.findViewById(R.id.swipe_downloads);
        swipeRefreshLayout.setColorScheme(R.color.colorAccent);
        listView=rootView.findViewById(R.id.dlist);
        listView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
        retrieveFiles();
        displayList(rootView);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reproduce(rootView);
            }
        });
        return rootView;
    }

    public void retrieveFiles(){
        files = dir.listFiles();
        fileList.clear();
        if(files!=null) {
            for (File file : files) {
                String name = file.getName();
                if (name.contains(".pdf") || name.contains(".xls") || name.contains(".xlsx"))
                    fileList.add(file.getName());
            }
        }
    }

    public void displayList(final View rootView){
        if(!fileList.isEmpty()){
            fileAdapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_list_item, fileList);
            final ListView downloads = listView;

            downloads.setAdapter(fileAdapter);

            downloads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final File pdfFile = new File(dirPath + "/" + fileList.get(i));
                    if (pdfFile.exists()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri data = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", pdfFile);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(data, getMime(pdfFile.toString()));
                        if (intent.resolveActivity(getContext().getPackageManager()) != null)
                            startActivity(intent);

                    }



                }
            });

            downloads.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                    final File pdfFile = new File(dirPath + "/" + fileList.get(i));
                    final String renamingFileName=fileList.get(i);
                    if (pdfFile.exists()) {
                        final ArrayList<String> qPaperOptions=new ArrayList<>();
                        qPaperOptions.add("Open");
                        qPaperOptions.add("Delete");
                        qPaperOptions.add("Rename");
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()); //Read Update
                        ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, qPaperOptions);
                        alertDialog.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                if (pos == 0) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri data = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", pdfFile);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    intent.setDataAndType(data, getMime(pdfFile.toString()));
                                    if (intent.resolveActivity(getContext().getPackageManager()) != null)
                                        startActivity(Intent.createChooser(intent, "Open the file"));
                                } else if (pos == 1) {
                                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                    alertDialog.setMessage("Are you sure you want to delete the file? ");
                                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            pdfFile.delete();
                                            Toast.makeText(getActivity(),renamingFileName+" Deleted",Toast.LENGTH_SHORT).show();
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

                                }
                                else if(pos==2)
                                {
                                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                    alertDialog.setMessage("Rename file : \n"+renamingFileName);

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
                                                pdfFile.renameTo(new File(dirPath + "/" + textBox.getText() + ".pdf"));
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
                                }
                            }

                        });

                        alertDialog.show();
                    } else {
                        Toast.makeText(getContext(), "Error Opening File", Toast.LENGTH_SHORT).show();
                    }
                    return true;}
            });}
        else {
            if(fileAdapter!=null)
            fileAdapter.clear();
            TextView empty=rootView.findViewById(R.id.dempty_view);
            empty.setVisibility(View.VISIBLE);
            ImageView emptyimage=rootView.findViewById(R.id.dempty_imageview);
            emptyimage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        reproduce(rootView);
        super.onResume();
    }

    String getMime(String url){
        if (url.contains(".doc") || url.contains(".docx")) {
            // Word document
            return "application/msword";
        } else if(url.contains(".pdf")) {
            // PDF file
            return "application/pdf";
        } else if(url.contains(".xls") || url.contains(".xlsx")) {
            // Excel file
            return "application/vnd.ms-excel";
        } else if(url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png")) {
            // JPG file
            return "image/jpeg";
        }else{
            return "application/pdf";
        }
    }


}

