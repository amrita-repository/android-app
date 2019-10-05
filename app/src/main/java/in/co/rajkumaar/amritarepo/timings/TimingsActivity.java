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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class TimingsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<DataItem> items;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_bus_timings);
        Utils.showSmallAd(this, (com.google.android.gms.ads.AdView) findViewById(R.id.banner_container));

        listView = findViewById(R.id.timings_list);
        Bundle extras = getIntent().getExtras();
        final String type = extras.getString("type");

        preferences = getSharedPreferences("public",MODE_PRIVATE);
        getSupportActionBar().setTitle(type);

        try {
            if(!preferences.contains("from-cbe")){
                fetchData(type);
            }else{
                loadData(type);
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("timings").child("public");
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String version = dataSnapshot.child("version").getValue(String.class);
                        if(!version.equals(preferences.getString("version", null))){
                            preferences.edit().putString("version",version).apply();
                            fetchData(type);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w("FBDB", "Failed to read value.", error.toException());
                        Utils.showUnexpectedError(TimingsActivity.this);
                        finish();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Utils.showUnexpectedError(TimingsActivity.this);
            finish();
        }
    }

    private void fetchData(final String type) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("timings").child("public");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<DataItem>> t = new GenericTypeIndicator<List<DataItem>>() {};

                //Trains from CBE
                DataSnapshot tempShot = dataSnapshot.child("trains").child("from").child("cbe");
                List<DataItem> timings = new ArrayList<>();
                for (DataSnapshot postSnapshot: tempShot.getChildren()) {
                    timings.add(postSnapshot.getValue(DataItem.class));
                }
                if( timings == null ) {
                    System.out.println("No timings");
                }
                else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("trains-from-cbe",json).apply();
                }

                //Trains from PKD
                timings = new ArrayList<DataItem>();
                tempShot = dataSnapshot.child("trains").child("from").child("pkd");
                for (DataSnapshot postSnapshot: tempShot.getChildren()) {
                    timings.add(postSnapshot.getValue(DataItem.class));
                }
                if( timings == null ) {
                    System.out.println("No timings");
                }
                else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("trains-from-pkd",json).apply();
                }


                //Trains to CBE
                timings.clear();
                tempShot=dataSnapshot.child("trains").child("to").child("cbe");
                for (DataSnapshot postSnapshot: tempShot.getChildren()) {
                    timings.add(postSnapshot.getValue(DataItem.class));
                }
                if( timings == null ) {
                    System.out.println("No timings");
                }
                else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("trains-to-cbe",json).apply();
                }


                //Trains to PKD
                timings.clear();
                tempShot = dataSnapshot.child("trains").child("to").child("pkd");
                for (DataSnapshot postSnapshot: tempShot.getChildren()) {
                    timings.add(postSnapshot.getValue(DataItem.class));
                }
                if( timings == null ) {
                    System.out.println("No timings");
                }
                else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("trains-to-pkd",json).apply();
                }


                //Bus from CBE
                timings.clear();
                tempShot= dataSnapshot.child("bus").child("from").child("cbe");
                for (DataSnapshot postSnapshot: tempShot.getChildren()) {
                    timings.add(postSnapshot.getValue(DataItem.class));
                }
                if( timings == null ) {
                    System.out.println("No timings");
                }
                else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("bus-from-cbe",json).apply();
                }

                //Bus to CBE
                timings.clear();
                tempShot= dataSnapshot.child("bus").child("to").child("cbe");
                for (DataSnapshot postSnapshot: tempShot.getChildren()) {
                    timings.add(postSnapshot.getValue(DataItem.class));
                }
                if( timings == null ) {
                    System.out.println("No timings");
                }
                else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("bus-to-cbe",json).apply();
                }


                Log.d("INFO","Loading after fetch");
                try {
                    loadData(type);
                } catch (JSONException e) {
                    Utils.showUnexpectedError(TimingsActivity.this);
                    finish();
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("FBDB", "Failed to read value.", error.toException());
                Utils.showUnexpectedError(TimingsActivity.this);
                finish();
            }
        });
    }

    private void loadData(String type) throws JSONException {
        items = new ArrayList<>();
        Gson gson = new Gson();

        if (type != null && type.equals("Trains from Coimbatore")) {

            String json = preferences.getString("trains-from-cbe", null);
//            Type listType = new TypeToken<ArrayList<JSONObject>>() {}.getType();
//            ArrayList<JSONObject> timings = gson.fromJson(json, listType);
            JSONArray timings = new JSONArray(json);
            System.out.println(timings.toString());
            for(int i=0;i<timings.length();++i){
                JSONObject item = timings.getJSONObject(i);
                items.add(new DataItem(
                        item.getString("name"),
                        item.getString("days"),
                        item.getString("dep"),
                        "cbe",
                        "etmd",
                        "train"
                ));
            }
        }

        if (type != null && type.equals("Trains from Palghat")) {
            String json = preferences.getString("trains-from-pkd", null);
            Type listType = new TypeToken<ArrayList<JSONObject>>() {}.getType();
            ArrayList<JSONObject> timings = gson.fromJson(json, listType);
            for (JSONObject item:timings) {
                items.add(new DataItem(
                        item.getString("name"),
                        item.getString("days"),
                        item.getString("dep"),
                        "pkd",
                        "etmd",
                        "train"
                ));
            }
        }

        if (type != null && type.equals("Trains to Coimbatore")) {
            String json = preferences.getString("trains-to-cbe", null);
            Type listType = new TypeToken<ArrayList<JSONObject>>() {}.getType();
            ArrayList<JSONObject> timings = gson.fromJson(json, listType);
            for (JSONObject item:timings) {
                items.add(new DataItem(
                        item.getString("name"),
                        item.getString("days"),
                        item.getString("dep"),
                        "etmd",
                        "cbe",
                        "train"
                ));
            }
        }

        if (type != null && type.equals("Trains to Palghat")) {
            String json = preferences.getString("trains-to-pkd", null);
            Type listType = new TypeToken<ArrayList<JSONObject>>() {}.getType();
            ArrayList<JSONObject> timings = gson.fromJson(json, listType);
            for (JSONObject item:timings) {
                items.add(new DataItem(
                        item.getString("name"),
                        item.getString("days"),
                        item.getString("dep"),
                        "etmd",
                        "pkd",
                        "train"
                ));
            }
        }

        if (type != null && type.equals("Buses from Coimbatore")) {
            String json = preferences.getString("bus-from-cbe", null);
            Type listType = new TypeToken<ArrayList<JSONObject>>() {}.getType();
            ArrayList<JSONObject> timings = gson.fromJson(json, listType);
            for (JSONObject item:timings) {
                items.add(new DataItem(
                        item.getString("name"),
                        item.getString("days"),
                        item.getString("dep"),
                        "cbe",
                        "etmd",
                        "bus"
                ));
            }
            /*items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "06:15 hours",
                    "07:15 hours",
                    "cbe",
                    "etmd",
                    "bus"
            ));
            items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "09:30 hours",
                    "10:30 hours",
                    "cbe",
                    "etmd",
                    "bus"
            ));
            items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "11:30 hours",
                    "12:30 hours",
                    "cbe",
                    "etmd",
                    "bus"
            ));
            items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "13:40 hours",
                    "14:40 hours",
                    "cbe",
                    "etmd",
                    "bus"
            ));
            items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "15:55 hours",
                    "16:55 hours",
                    "cbe",
                    "etmd",
                    "bus"
            ));
            items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "18:15 hours",
                    "19:15 hours",
                    "cbe",
                    "etmd",
                    "bus"
            ));
            items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "20:30 hours",
                    "21:30 hours",
                    "cbe",
                    "etmd",
                    "bus"
            ));*/
        }

        if (type != null && type.equals("Buses to Coimbatore")) {
            String json = preferences.getString("bus-to-cbe", null);
            Type listType = new TypeToken<ArrayList<JSONObject>>() {}.getType();
            ArrayList<JSONObject> timings = gson.fromJson(json, listType);
            for (JSONObject item:timings) {
                items.add(new DataItem(
                        item.getString("name"),
                        item.getString("days"),
                        item.getString("dep"),
                        "etmd",
                        "cbe",
                        "bus"
                ));
            }
            /*items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "08:30 hours",
                    "09:30 hours",
                    "etmd",
                    "cbe",
                    "bus"
            ));
            items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "10:30 hours",
                    "11:30 hours",
                    "etmd",
                    "cbe",
                    "bus"
            ));
            items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "12:25 hours",
                    "13:25 hours",
                    "etmd",
                    "cbe",
                    "bus"
            ));
            items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "14:45 hours",
                    "15:45 hours",
                    "etmd",
                    "cbe",
                    "bus"
            ));
            items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "17:00 hours",
                    "18:00 hours",
                    "etmd",
                    "cbe",
                    "bus"
            ));
            items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "19:20 hours",
                    "20:20 hours",
                    "etmd",
                    "cbe",
                    "bus"
            ));
            items.add(new DataItem(
                    "Local Govt. Transport Bus 'A3'",
                    "All Days",
                    "21:40 hours",
                    "22:40 hours",
                    "etmd",
                    "cbe",
                    "bus"
            ));*/
        }

        if(items!=null){
            ArrayAdapter<DataItem> dataItemArrayAdapter = new ArrayAdapter<DataItem>(getBaseContext(), R.layout.timing_item, items) {
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(R.layout.timing_item, null);
                    }
                    DataItem item = getItem(position);
                    ((TextView) convertView.findViewById(R.id.name)).setText(item.name);
                    String font_color = "<font color='#b71c1c'>";

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
            listView.setAdapter(dataItemArrayAdapter);
            listView.setTextFilterEnabled(true);
            listView.setItemsCanFocus(false);
        }
    }
}

