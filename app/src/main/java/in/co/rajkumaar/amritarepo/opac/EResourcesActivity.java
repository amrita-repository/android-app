/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class EResourcesActivity extends BaseActivity {

    private Map<String, String> links = new HashMap<>();
    private ArrayList<String> items = new ArrayList<>();
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eresources);
        listView = findViewById(R.id.list);
        if (getIntent().hasExtra("useful-sites")) {
            getSupportActionBar().setTitle("Useful Websites");
        }
        fetchResources((getIntent().hasExtra("useful-sites")));
    }

    void fetchResources(final boolean b) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.central_library),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Document doc = Jsoup.parse(response);
                            Element table = doc.select("table").get(b ? 1 : 0);
                            Elements anchors = table.select("a");
                            for (Element a : anchors) {
                                links.put(a.text(), a.attr("href"));
                                items.add(a.text());
                            }
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
                        } catch (Exception e) {
                            Utils.showUnexpectedError(EResourcesActivity.this);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.showUnexpectedError(EResourcesActivity.this);
            }
        });
        requestQueue.add(stringRequest);
    }
}
