/*
 * MIT License
 *
 * Copyright (c) 2018  RAJKUMAR S
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package in.co.rajkumaar.amritarepo.timings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class PublicTransportsActivity extends AppCompatActivity {

    ProgressDialog dialog;
    private ListView list_View;
    private ArrayList<DataItem> items_fill;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_bus_timings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        list_View = findViewById(R.id.timings_list);
        Bundle extras = getIntent().getExtras();
        final String type = extras.getString("type");

        preferences = getSharedPreferences("public-transport", MODE_PRIVATE);
        getSupportActionBar().setTitle(type);

        try {
            if (!preferences.contains("trains-from-cbe")) {
                fetchData(type);
            } else {
                loadData(type);
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("timings").child("public");
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String version = dataSnapshot.child("version").getValue(String.class);
                        Log.d("VERSION FROM FBDB", version);
                        if (!version.equals(preferences.getString("version", null))) {
                            preferences.edit().putString("version", version).apply();
                            Log.d("VERSION AFTER EDIT PREF", preferences.getString("version", null));
                            fetchData(type);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w("FBDB", "Failed to read value.", error.toException());
                        Utils.showUnexpectedError(PublicTransportsActivity.this);
                        finish();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Utils.showUnexpectedError(PublicTransportsActivity.this);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void fetchData(final String type) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog = new ProgressDialog(PublicTransportsActivity.this);
                    dialog.setCancelable(false);
                    dialog.setMessage("Please wait while data is fetched & cached");
                    dialog.show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("timings").child("public");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Trains from CBE
                DataSnapshot tempShot = dataSnapshot.child("trains").child("from").child("cbe");
                List<DataItem> timings = new ArrayList<>();
                for (DataSnapshot postSnapshot : tempShot.getChildren()) {
                    timings.add(postSnapshot.getValue(DataItem.class));
                }
                if (timings == null) {
                    System.out.println("No timings");
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("trains-from-cbe", json).apply();
                }

                //Trains from PKD
                timings.clear();
                tempShot = dataSnapshot.child("trains").child("from").child("pkd");
                for (DataSnapshot postSnapshot : tempShot.getChildren()) {
                    timings.add(postSnapshot.getValue(DataItem.class));
                }
                if (timings == null) {
                    System.out.println("No timings");
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("trains-from-pkd", json).apply();
                }


                //Trains to CBE
                timings.clear();
                tempShot = dataSnapshot.child("trains").child("to").child("cbe");
                for (DataSnapshot postSnapshot : tempShot.getChildren()) {
                    timings.add(postSnapshot.getValue(DataItem.class));
                }
                if (timings == null) {
                    System.out.println("No timings");
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("trains-to-cbe", json).apply();
                }


                //Trains to PKD
                timings.clear();
                tempShot = dataSnapshot.child("trains").child("to").child("pkd");
                for (DataSnapshot postSnapshot : tempShot.getChildren()) {
                    timings.add(postSnapshot.getValue(DataItem.class));
                }
                if (timings == null) {
                    System.out.println("No timings");
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("trains-to-pkd", json).apply();
                }


                //Bus from CBE
                timings.clear();
                tempShot = dataSnapshot.child("bus").child("from").child("cbe");
                for (DataSnapshot postSnapshot : tempShot.getChildren()) {
                    timings.add(postSnapshot.getValue(DataItem.class));
                }
                if (timings == null) {
                    System.out.println("No timings");
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("bus-from-cbe", json).apply();
                }

                //Bus to CBE
                timings.clear();
                tempShot = dataSnapshot.child("bus").child("to").child("cbe");
                for (DataSnapshot postSnapshot : tempShot.getChildren()) {
                    timings.add(postSnapshot.getValue(DataItem.class));
                }
                if (timings == null) {
                    System.out.println("No timings");
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("bus-to-cbe", json).apply();
                }

                Log.d("INFO", "Loading after fetch");
                try {
                    loadData(type);
                } catch (JSONException e) {
                    Utils.showUnexpectedError(PublicTransportsActivity.this);
                    finish();
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("FBDB", "Failed to read value.", error.toException());
                Utils.showUnexpectedError(PublicTransportsActivity.this);
                finish();
            }
        });
    }

    private void loadData(String type) throws JSONException {
        items_fill = new ArrayList<>();

        if (type != null && type.equals("Trains from Coimbatore")) {

            String json = preferences.getString("trains-from-cbe", null);
            if (json != null) {
                JSONArray timings = new JSONArray(json);
                System.out.println(timings.toString());
                for (int i = 0; i < timings.length(); ++i) {
                    JSONObject item = timings.getJSONObject(i);
                    items_fill.add(new DataItem(
                            item.getString("name"),
                            item.getString("days"),
                            item.getString("dep"),
                            "cbe",
                            "etmd",
                            "train"
                    ));
                }
            }
        }

        if (type != null && type.equals("Trains from Palghat")) {
            String json = preferences.getString("trains-from-pkd", null);
            if (json != null) {
                JSONArray timings = new JSONArray(json);
                System.out.println(timings.toString());
                for (int i = 0; i < timings.length(); ++i) {
                    JSONObject item = timings.getJSONObject(i);
                    items_fill.add(new DataItem(
                            item.getString("name"),
                            item.getString("days"),
                            item.getString("dep"),
                            "pkd",
                            "etmd",
                            "train"
                    ));
                }
            }
        }

        if (type != null && type.equals("Trains to Coimbatore")) {
            String json = preferences.getString("trains-to-cbe", null);
            if (json != null) {
                JSONArray timings = new JSONArray(json);
                System.out.println(timings.toString());
                for (int i = 0; i < timings.length(); ++i) {
                    JSONObject item = timings.getJSONObject(i);
                    items_fill.add(new DataItem(
                            item.getString("name"),
                            item.getString("days"),
                            item.getString("dep"),
                            "etmd",
                            "cbe",
                            "train"
                    ));
                }
            }
        }

        if (type != null && type.equals("Trains to Palghat")) {
            String json = preferences.getString("trains-to-pkd", null);
            if (json != null) {
                JSONArray timings = new JSONArray(json);
                System.out.println(timings.toString());
                for (int i = 0; i < timings.length(); ++i) {
                    JSONObject item = timings.getJSONObject(i);
                    items_fill.add(new DataItem(
                            item.getString("name"),
                            item.getString("days"),
                            item.getString("dep"),
                            "etmd",
                            "pkd",
                            "train"
                    ));
                }
            }
        }

        if (type != null && type.equals("Buses from Coimbatore")) {
            String json = preferences.getString("bus-from-cbe", null);
            if (json != null) {
                JSONArray timings = new JSONArray(json);
                System.out.println(timings.toString());
                for (int i = 0; i < timings.length(); ++i) {
                    JSONObject item = timings.getJSONObject(i);
                    items_fill.add(new DataItem(
                            item.getString("name"),
                            item.getString("days"),
                            item.getString("dep"),
                            "cbe",
                            "etmd",
                            "bus"
                    ));
                }
            }
        }

        if (type != null && type.equals("Buses to Coimbatore")) {
            String json = preferences.getString("bus-to-cbe", null);
            if (json != null) {
                JSONArray timings = new JSONArray(json);
                System.out.println(timings.toString());
                for (int i = 0; i < timings.length(); ++i) {
                    JSONObject item = timings.getJSONObject(i);
                    items_fill.add(new DataItem(
                            item.getString("name"),
                            item.getString("days"),
                            item.getString("dep"),
                            "etmd",
                            "cbe",
                            "bus"
                    ));
                }
            }
        }

        if (items_fill != null) {
            Log.d("REFRESHING ADAPTER", "HERE");
            ArrayAdapter<DataItem> dataItemArrayAdapter = new ArrayAdapter<DataItem>(getBaseContext(), R.layout.timing_item, items_fill) {
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(R.layout.timing_item, null);
                    }
                    DataItem item = getItem(position);
                    ((TextView) convertView.findViewById(R.id.name)).setText(item.name);
                    String font_color = "<font color='#00b200'>";

                    if (item.from.equals("cbe"))
                        ((TextView) convertView.findViewById(R.id.departure)).setText(Html.fromHtml("Departs from Coimbatore at " + font_color + item.dep + "</font>"));
                    if (item.from.equals("etmd"))
                        ((TextView) convertView.findViewById(R.id.departure)).setText(Html.fromHtml("Departs from Ettimadai at " + font_color + item.dep + "</font>"));
                    if (item.from.equals("pkd"))
                        ((TextView) convertView.findViewById(R.id.departure)).setText(Html.fromHtml("Departs from Palghat at " + font_color + item.dep + "</font>"));


                    if (item.days.equals("All Days"))
                        ((TextView) convertView.findViewById(R.id.days)).setText(Html.fromHtml("Runs on All Days"));
                    else
                        ((TextView) convertView.findViewById(R.id.days)).setText(Html.fromHtml("Runs on All Days " + item.days));

                    return convertView;
                }
            };
            list_View.setAdapter(dataItemArrayAdapter);
            list_View.setTextFilterEnabled(true);
            list_View.setItemsCanFocus(false);
            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.timings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.info) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(PublicTransportsActivity.this);
            alertDialog.setMessage(Html.fromHtml("Users are advised to have backup plans before making any decision " +
                    "based on the information provided here. Amrita Repository will not be responsible for any liability."));
            alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();
        }else if(id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

