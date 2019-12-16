/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class EResourcesActivity extends AppCompatActivity {

    Map<String,String> links = new HashMap<>();
    ArrayList<String> items = new ArrayList<>();
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eresources);
        listView = findViewById(R.id.list);
        if(getIntent().hasExtra("useful-sites")){
            getSupportActionBar().setTitle("Useful Websites");
        }
        fetchResources((getIntent().hasExtra("useful-sites")));
    }

    void fetchResources(final boolean b){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(getString(R.string.central_library), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    Document doc = Jsoup.parse(new String(responseBody));
                    Element table = doc.select("table").get(b ? 1 : 0);
                    Elements anchors = table.select("a");
                    for (Element a : anchors) {
                        links.put(a.text(), a.attr("href"));
                        items.add(a.text());
                    }
                    System.out.println(links.toString());
                    ArrayAdapter arrayAdapter = new ArrayAdapter<>(EResourcesActivity.this, R.layout.eresources_item, R.id.text1, items);
                    listView.setAdapter(arrayAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(links.get(items.get(position))));
                            startActivity(intent);
                        }
                    });
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }catch (Exception e){
                    Utils.showUnexpectedError(EResourcesActivity.this);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showUnexpectedError(EResourcesActivity.this);
            }
        });
    }
}
