/*
 * Copyright (c) 2023 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

    private final Map<String, String> links = new HashMap<>();
    private final ArrayList<String> items = new ArrayList<>();
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
                response -> {
                    try {
                        Document doc = Jsoup.parse(response);
                        Element table = doc.select("table").get(b ? 1 : 0);
                        Elements anchors = table.select("a");
                        for (Element a : anchors) {
                            links.put(a.text(), a.attr("href"));
                            items.add(a.text());
                        }
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(EResourcesActivity.this, R.layout.eresources_item, R.id.text1, items);
                        listView.setAdapter(arrayAdapter);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(links.get(items.get(position))));
                            startActivity(intent);
                        });
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    } catch (Exception e) {
                        Utils.showUnexpectedError(EResourcesActivity.this);
                    }
                }, error -> Utils.showUnexpectedError(EResourcesActivity.this)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15");
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}
