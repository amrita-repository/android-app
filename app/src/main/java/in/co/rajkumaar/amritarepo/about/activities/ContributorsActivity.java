/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.about.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.about.adapters.ContributorsAdapter;
import in.co.rajkumaar.amritarepo.about.models.Contributor;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class ContributorsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contributors);
        final GridView contributorsGrid = findViewById(R.id.gridView);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, getString(R.string.all_contributors_link), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ArrayList<Contributor> contributors;
                            JSONArray contributorsArray = response.getJSONArray("contributors");
                            contributors = new ArrayList<>();
                            for (int i = 0; i < contributorsArray.length(); ++i) {
                                JSONObject item = contributorsArray.getJSONObject(i);
                                contributors.add(new Contributor(item.getString("name"), item.getString("avatar_url"), item.getString("login"), item.getString("profile")));
                            }
                            ContributorsAdapter contributorsAdapter = new ContributorsAdapter(ContributorsActivity.this, contributors);
                            contributorsGrid.setAdapter(contributorsAdapter);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        } catch (JSONException e) {
                            Utils.showUnexpectedError(ContributorsActivity.this);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utils.showUnexpectedError(ContributorsActivity.this);
                        finish();
                        new Exception(error).printStackTrace();
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }
}
