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

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class ShuttleBusTimingsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<DataItem> items;
    private SharedPreferences preferences;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuttle_bus_timings);
        Utils.showBigAd(this, (com.google.android.gms.ads.AdView) findViewById(R.id.banner_container));

        preferences = getSharedPreferences("timings",MODE_PRIVATE);
        listView = findViewById(R.id.timings_list);
        Bundle extras = getIntent().getExtras();
        type = extras.getString("type");
        getSupportActionBar().setTitle(type);

        if(!preferences.contains("ab1")){
            fetchData();
        }else{
            loadData(type);
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("timings").child("shuttle");
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String version = dataSnapshot.child("version").getValue(String.class);
                    if(!version.equals(preferences.getString("version", null))){
                        preferences.edit().putString("version",version).apply();
                        fetchData();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("FBDB", "Failed to read value.", error.toException());
                    Utils.showUnexpectedError(ShuttleBusTimingsActivity.this);
                    finish();
                }
            });
        }
    }

    private void fetchData(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("timings").child("shuttle");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                List timings = dataSnapshot.child("ab1").getValue(t);
                if( timings == null ) {
                    System.out.println("No timings");
                }
                else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("ab1",json).apply();
                }
                timings = dataSnapshot.child("ab3").getValue(t);
                if( timings == null ) {
                    System.out.println("No timings");
                }
                else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("ab3",json).apply();
                }
                Log.d("INFO","Loading after fetch");
                loadData(type);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("FBDB", "Failed to read value.", error.toException());
                Utils.showUnexpectedError(ShuttleBusTimingsActivity.this);
                finish();
            }
        });
    }

    private void loadData(String type) {
        items = new ArrayList<>();
        Gson gson = new Gson();
        if (type != null && type.equals("Buses from AB1")) {
            String json = preferences.getString("ab1", null);
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            ArrayList<String> timings = gson.fromJson(json, listType);
            for (String item:timings) {
                items.add(new DataItem(
                        item, "ab1"
                ));
            }
        } else if (type != null && type.equals("Buses from AB3")) {
            String json = preferences.getString("ab3", null);
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            ArrayList<String> timings = gson.fromJson(json, listType);
            for (String item:timings) {
                items.add(new DataItem(
                        item, "ab3"
                ));
            }
        }
        if(items!=null){
            ArrayAdapter<DataItem> dataItemArrayAdapter = new ArrayAdapter<DataItem>(getBaseContext(), R.layout.timing_item, items) {
                @SuppressLint("InflateParams")
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(R.layout.shuttle_timing_item, null);
                    }
                    DataItem item = getItem(position);
                    String font_color = "<font color='#ff8800'>";

                    if (item.from.equals("ab1"))
                        ((TextView) convertView.findViewById(R.id.departure)).setText(Html.fromHtml("Departs from AB1 at " + font_color + item.departure + "</font>"));
                    if (item.from.equals("ab3"))
                        ((TextView) convertView.findViewById(R.id.departure)).setText(Html.fromHtml("Departs from AB3 at " + font_color + item.departure + "</font>"));
                    return convertView;
                }
            };
            listView.setAdapter(dataItemArrayAdapter);
            listView.setTextFilterEnabled(true);
            listView.setItemsCanFocus(false);
        }
    }



    private class DataItem {
        String departure;
        String from;

        DataItem(String departure, String from) {
            this.departure = departure;
            this.from = from;
        }
    }


}

