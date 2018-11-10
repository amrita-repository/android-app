package in.co.rajkumaar.amritarepo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class ExamSchedule extends AppCompatActivity {

    ProgressBar progressBar;
    String url_exams;
    ArrayList<String> headings,texts,links;
    ListView listView;
    ArrayAdapter<String> scheduleBlockArrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_schedule);
        progressBar=findViewById(R.id.progressBar);
        url_exams=getResources().getString(R.string.url_exams);
        headings=new ArrayList<>();
        texts=new ArrayList<>();
        links=new ArrayList<>();
        listView=findViewById(R.id.list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(ExamSchedule.this,ExamsUnderEachDept.class).putExtra("block",position));
            }
        });


        new retrieveSchedule().execute();
    }



    class retrieveSchedule extends AsyncTask<Void,Void,Void>{
        Document document=null;
        Elements titles,ul_lists;
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
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



            try {
                titles = document.select("div.field-items").select("p");
                ul_lists=document.select("div.field-items").select("ul");
                Log.e("ELEMENTS ", String.valueOf(titles.size()));
                for(int i=0;i<titles.size();++i){
                    String spanstyle=titles.get(i).select("span[style]").attr("style");
                        String head=titles.get(i).select("p").text().trim();
                        if(head.length()>0)
                        headings.add(head);
                } //Log.e("TEXT SIZE",String.valueOf(scheduleBlocks.get(1).getTexts().size()));

                scheduleBlockArrayAdapter=new ArrayAdapter<>(ExamSchedule.this,R.layout.custom_list_item,headings);
                listView.setAdapter(scheduleBlockArrayAdapter);


            }catch (Exception e){
                e.printStackTrace();
            }
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }
}
