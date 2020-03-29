/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class OPACSearchActivity extends BaseActivity {

    private OPACClient opacClient;
    private ProgressDialog dialog;
    private HashMap<String, Integer> docTypes;
    private HashMap<String, Integer> fields;
    private Spinner docTypeSpinner, fieldSpinner;
    private EditText searchKeyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opacsearch);
        opacClient = new OPACClient(this);
        dialog = new ProgressDialog(this);
        docTypeSpinner = findViewById(R.id.docType);
        fieldSpinner = findViewById(R.id.field);
        searchKeyword = findViewById(R.id.searchKeyword);

        init();
    }

    private void init() {
        try {
            dialog.setCancelable(false);
            dialog.setMessage("Please wait");
            dialog.show();
            opacClient.init(new InitResponse() {
                @Override
                public void onSuccess(Map<String, Integer> docTypes, Map<String, Integer> fields) {
                    populateData(docTypes, fields);
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Exception exception) {
                    Log.e("ERROR WHILE INIT", exception.getLocalizedMessage());
                    dialog.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showUnexpectedError(this);
        }
    }

    private void populateData(Map<String, Integer> docTypes, Map<String, Integer> fields) {
        this.docTypes = Utils.sortByValue(docTypes);
        this.fields = Utils.sortByValue(fields);

        ArrayList<String> docTypesArray = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : this.docTypes.entrySet()) {
            docTypesArray.add(entry.getKey());
        }
        ArrayAdapter<String> docTypeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item1, docTypesArray);
        docTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        docTypeSpinner.setAdapter(docTypeAdapter);

        ArrayList<String> fieldsArray = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : this.fields.entrySet()) {
            fieldsArray.add(entry.getKey());
        }
        ArrayAdapter<String> fieldsAdapter = new ArrayAdapter<>(this, R.layout.spinner_item1, fieldsArray);
        fieldsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fieldSpinner.setAdapter(fieldsAdapter);

    }

    public void searchOPAC(View view) {
        if (Utils.isConnected(this) && !searchKeyword.getText().toString().isEmpty()) {
            Utils.hideKeyboard(this);
            dialog.setCancelable(false);
            dialog.setMessage("Please wait");
            dialog.show();
            opacClient.searchResults(docTypes.get(docTypeSpinner.getSelectedItem().toString()),
                    fields.get(fieldSpinner.getSelectedItem().toString()), searchKeyword.getText().toString(), new SearchResponse() {
                        @Override
                        public void onSuccess(JSONArray data, String action, String username) {
                            startActivity(new Intent(getApplicationContext(), SearchResultsActivity.class)
                                    .putExtra("data", data.toString())
                                    .putExtra("action", action)
                                    .putExtra("username", username));
                            dialog.dismiss();
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            dialog.dismiss();
                            exception.printStackTrace();
                            Utils.showToast(getApplicationContext(),"Please try with a different search phrase");
                        }
                    });
        } else {
            Utils.showInternetError(this);
        }
    }

    public void clear(View view) {
        searchKeyword.setText("");
    }
}
