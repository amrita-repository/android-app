/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Map;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class SearchResultsActivity extends AppCompatActivity {

    private int docType, field;
    private String search, username;
    private OPACClient opacClient;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Bundle data = getIntent().getExtras();
        docType = data.getInt("docType");
        search = data.getString("search");
        field = data.getInt("field");
        username = data.getString("username");
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        opacClient = new OPACClient();
        init();
    }

    private void init() {
        dialog.setMessage("Loading results");
        dialog.show();
        opacClient.searchResults(username, docType, field, search, new SearchResponse() {
            @Override
            public void onSuccess(Map<String, Integer> results) {
                System.out.println(results.toString());
                dialog.dismiss();
            }

            @Override
            public void onFailure(Exception exception) {
                dialog.dismiss();
                Utils.showUnexpectedError(SearchResultsActivity.this);
            }
        });
    }
}
