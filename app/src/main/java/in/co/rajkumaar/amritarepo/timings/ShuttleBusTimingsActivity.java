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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class ShuttleBusTimingsActivity extends AppCompatActivity {

    private String type;
    private ProgressDialog dialog;
    private GridView listView;
    private ArrayList<DataItem> items;
    private SharedPreferences preferences;
    private TextView nextBus;
    private TextView countdownTimer;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuttle_bus_timings);
        nextBus = findViewById(R.id.nextBusTime);
        countdownTimer = findViewById(R.id.countdownTime);
        preferences = getSharedPreferences("timings", MODE_PRIVATE);
        listView = findViewById(R.id.timings_list);
        Bundle extras = getIntent().getExtras();
        type = extras.getString("type");
        getSupportActionBar().setTitle(type);

        if (!preferences.contains("ab1")) {
            fetchData();
        } else {
            loadData(type);
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("timings").child("shuttle");
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String version = dataSnapshot.child("version").getValue(String.class);
                    if (!version.equals(preferences.getString("version", null))) {
                        preferences.edit().putString("version", version).apply();
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

    private void fetchData() {
        try {
            dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("Please wait while data is fetched & cached");
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("timings").child("shuttle");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                };
                List timings = dataSnapshot.child("ab1").getValue(t);
                if (timings == null) {
                    System.out.println("No timings");
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("ab1", json).apply();
                }
                timings = dataSnapshot.child("ab3").getValue(t);
                if (timings == null) {
                    System.out.println("No timings");
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(timings);
                    preferences.edit().putString("ab3", json).apply();
                }
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("INFO", "Loading after fetch");
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
        int flag;
        if (type != null && type.equals("Buses from AB1")) {
            String json = preferences.getString("ab1", null);
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            flag = 0;
            try {
                Calendar currentTime = Calendar.getInstance();
                ArrayList<String> timings = gson.fromJson(json, listType);
                for (String item : timings) {
                    Date busTimeDate = dateFormat.parse(item);
                    Calendar busTime = Calendar.getInstance();
                    busTime.setTime(new Date());
                    busTime.set(Calendar.HOUR_OF_DAY, busTimeDate.getHours());
                    busTime.set(Calendar.MINUTE, busTimeDate.getMinutes());
                    items.add(new DataItem(
                            item, "ab1"
                    ));

                    if (busTime.after(currentTime) && flag == 0) {
                        nextBus.setText(getString(R.string.nextBusText) + item);
                        flag = 1;
                        Date startTime = currentTime.getTime();
                        Date endTime = busTime.getTime();
                        long timediff = endTime.getTime() - startTime.getTime();
                        countdown(timediff);
                    }
                }
                if (flag == 0) {
                    nextBus.setText(R.string.noBusText);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else if (type != null && type.equals("Buses from AB3")) {
            String json = preferences.getString("ab3", null);
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            flag = 0;
            try {
                Calendar currentTime = Calendar.getInstance();
                ArrayList<String> timings = gson.fromJson(json, listType);
                for (String item : timings) {
                    Date busTimeDate = dateFormat.parse(item);
                    Calendar busTime = Calendar.getInstance();
                    busTime.setTime(new Date());
                    busTime.set(Calendar.HOUR_OF_DAY, busTimeDate.getHours());
                    busTime.set(Calendar.MINUTE, busTimeDate.getMinutes());


                    items.add(new DataItem(
                            item, "ab3"
                    ));
                    if (busTime.after(currentTime) && flag == 0) {
                        nextBus.setText(getString(R.string.nextBusText) + item);
                        flag = 1;
                        Date startTime = currentTime.getTime();
                        Date endTime = busTime.getTime();
                        long timediff = endTime.getTime() - startTime.getTime();
                        countdown(timediff);
                    }
                }
                if (flag == 0) {
                    nextBus.setText(R.string.noBusText);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (items != null) {
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
                        ((TextView) convertView.findViewById(R.id.departure)).setText(Html.fromHtml(font_color + item.departure + "</font>"));
                    if (item.from.equals("ab3"))
                        ((TextView) convertView.findViewById(R.id.departure)).setText(Html.fromHtml(font_color + item.departure + "</font>"));
                    return convertView;
                }
            };
            listView.setAdapter(dataItemArrayAdapter);
            listView.setTextFilterEnabled(true);
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
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShuttleBusTimingsActivity.this);
            alertDialog.setMessage(Html.fromHtml("Users are advised to have backup plans before making any decision " +
                    "based on the information provided here. Amrita Repository will not be responsible for any liability."));
            alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();
        } else if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private class DataItem {
        private String departure;
        private String from;

        DataItem(String departure, String from) {
            this.departure = departure;
            this.from = from;
        }
    }

    private void countdown(long timeDiff) {
        new CountDownTimer(timeDiff, 1000) {

            @SuppressLint("DefaultLocale")
            public void onTick(long millisUntilFinished) {
                if (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) == 0) {
                    countdownTimer.setText(String.format(" %d Sec Left",
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                } else {
                    countdownTimer.setText(String.format(" %d Min : %d Sec Left",
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                }
            }

            public void onFinish() {
                countdownTimer.setText("Departed");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData(type);
                    }
                }, 0);

            }
        }.start();
    }


}

