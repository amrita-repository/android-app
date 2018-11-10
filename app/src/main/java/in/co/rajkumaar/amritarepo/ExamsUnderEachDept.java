package in.co.rajkumaar.amritarepo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class ExamsUnderEachDept extends AppCompatActivity {

    ProgressBar progressBar;
    String url_exams;

    ArrayList<String> texts,links;
    int block;
    private ListView listView;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exams_under_each_dept);
        progressBar=findViewById(R.id.progressBar);
        url_exams=getResources().getString(R.string.url_exams);
        listView=findViewById(R.id.list);
        block=getIntent().getExtras().getInt("block");

        texts=new ArrayList<>();
        links=new ArrayList<>();

        new getExams().execute();
    }


    class getExams extends AsyncTask<Void,Void,Void>{
        Document document=null;
        Elements ul_lists;
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            texts.clear();
            links.clear();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                document = Jsoup.connect(url_exams).get();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            ul_lists=document.select("div.field-items").select("ul");
            for(int j=0;j<ul_lists.get(block).select("li").size();++j)
            {

                    String text=ul_lists.get(block).select("li").get(j).select("li").text();
                    String[] temp=text.split("-");
                    String comp=text.replace(temp[temp.length-1]," ");
                    int index = comp.lastIndexOf('-');
                    if(index != -1) {
                        comp = comp.substring(0,index);
                    }
                    texts.add(comp.trim());
                    //Log.e("TEXT", comp.trim());
                    adapter=new ArrayAdapter<>(ExamsUnderEachDept.this,R.layout.custom_list_item,texts);
                    listView.setAdapter(adapter);

                    links.add(ul_lists.get(block).select("li").get(j).select("a[href]").attr("href"));
                    //Log.e("LINK", ul_lists.get(i).select("li").get(j).select("a[href]").attr("href"));
            }
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final ArrayList<String> qPaperOptions=new ArrayList();
                    qPaperOptions.add("Open");
                    qPaperOptions.add("Download");
                    final View viewLocal=view;
                    AlertDialog.Builder qPaperBuilder=new AlertDialog.Builder(ExamsUnderEachDept.this);
                    ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(ExamsUnderEachDept.this, android.R.layout.simple_list_item_1, qPaperOptions);
                    qPaperBuilder.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int pos) {
                            if(pos==0) {
                                new OpenTask(ExamsUnderEachDept.this,"https://intranet.cb.amrita.edu"+links.get(position),2);
                            }
                            else if(pos==1)
                            {
                                if (ContextCompat.checkSelfPermission(ExamsUnderEachDept.this,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {

                                    ActivityCompat.requestPermissions(ExamsUnderEachDept.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            1);
                                }
                                else{
                                    if(isNetworkAvailable())
                                    {
                                        new DownloadTask(ExamsUnderEachDept.this,"https://intranet.cb.amrita.edu"+links.get(position),2);
                                    }
                                    else{
                                        Snackbar.make(viewLocal,"Device not connected to Internet.",Snackbar.LENGTH_SHORT).show();
                                    }


                                }
                            }
                        }
                    });
                    qPaperBuilder.show();


                }
            });

            progressBar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
