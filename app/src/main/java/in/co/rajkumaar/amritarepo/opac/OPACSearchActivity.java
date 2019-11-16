/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class OPACSearchActivity extends AppCompatActivity {

    private OPACClient opacClient;
    private String username;
    private ProgressDialog dialog;
    private TreeMap<String, Integer> docTypes;
    private TreeMap<String, Integer> fields;
    private Spinner docTypeSpinner, fieldSpinner;
    private EditText searchKeyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opacsearch);
        opacClient = new OPACClient();
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
                public void onSuccess(String user_name, Map<String, Integer> docTypes, Map<String, Integer> fields) {
                    populateData(user_name, docTypes, fields);
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

    private void populateData(String username, Map<String, Integer> docTypes, Map<String, Integer> fields) {
        this.username = username;
        this.docTypes = new TreeMap<>(docTypes);
        this.fields = new TreeMap<>(fields);


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
        if (Utils.isConnected(this)) {
            opacClient.searchResults(username, docTypes.get(docTypeSpinner.getSelectedItem()), fields.get(fieldSpinner.getSelectedItem()), searchKeyword.getText().toString(), new SearchResponse() {
                @Override
                public void onSuccess(Map<String, Integer> results) {
                    System.out.println(results.toString());
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Exception exception) {
                    dialog.dismiss();
                    Utils.showUnexpectedError(OPACSearchActivity.this);
                }
            });
//            startActivity(new Intent(this,SearchResultsActivity.class)
//                    .putExtra("username",username)
//                    .putExtra("search",searchKeyword.getText().toString())
//                    .putExtra("field",fields.get((String)fieldSpinner.getSelectedItem()))
//                    .putExtra("docType",docTypes.get((String) docTypeSpinner.getSelectedItem()));
        } else {
            Utils.showInternetError(this);
        }
    }
}
