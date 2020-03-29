/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class SearchResultsActivity extends BaseActivity implements ResultsAdapter.customListener {

    private JSONArray searchResults;
    private String action, username;
    private ListView listView;
    private OPACClient opacClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        listView = findViewById(R.id.list);

        opacClient = new OPACClient(this);

        Bundle data = getIntent().getExtras();
        username = data.getString("username");
        action = data.getString("action");
        try {
            searchResults = new JSONArray(data.getString("data"));
            fireUpList();
        } catch (Exception e) {
            finish();
            Utils.showUnexpectedError(this);
            e.printStackTrace();
        }
    }

    private void fireUpList() throws JSONException {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < searchResults.length(); ++i) {
            items.add(searchResults.getJSONObject(i).getString("title"));
        }
        ResultsAdapter resultsAdapter = new ResultsAdapter(this, items);
        resultsAdapter.setCustomListener(this);
        listView.setAdapter(resultsAdapter);
    }

    @Override
    public void onItemClickListener(int position) {
        final ProgressDialog dialog = new ProgressDialog(this);
        try {
            dialog.setCancelable(false);
            dialog.setMessage("Please wait");
            dialog.show();
            opacClient.getBookDetails(action, username,
                    searchResults.getJSONObject(position).getInt("id") , new BookDetailResponse() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    startActivity(new Intent(getApplicationContext(),BookDetailActivity.class)
                            .putExtra("details",jsonObject.toString()));
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Exception exception) {
                    dialog.dismiss();
                    exception.printStackTrace();
                    Utils.showUnexpectedError(getApplicationContext());
                }
            });
        } catch (Exception e) {
            dialog.dismiss();
            Utils.showUnexpectedError(getApplicationContext());
            e.printStackTrace();
        }
    }
}
