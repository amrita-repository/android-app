/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.about.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.about.adapters.ContributorsAdapter;
import in.co.rajkumaar.amritarepo.about.models.Contributor;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class ContributorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contributors);
        final GridView contributorsGrid = findViewById(R.id.gridView);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(getString(R.string.all_contributors_link), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    ArrayList<Contributor> contributors;
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    JSONArray contributorsArray = jsonObject.getJSONArray("contributors");
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

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showUnexpectedError(ContributorsActivity.this);
                new Exception(error).printStackTrace();
            }
        });
    }
}
