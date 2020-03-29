/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.timings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class PublicTransportsActivity extends BaseActivity {

    private String type;
    private int flag;
    private ProgressDialog dialog;
    private ListView listView;
    private ArrayList<DataItem> items;
    private SharedPreferences preferences;
    private TextView nextTrainBus;
    private TextView countdownTimer;
    private ImageView trainBusImage;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_bus_timings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        countdownTimer = findViewById(R.id.countdownTime);
        nextTrainBus = findViewById(R.id.nextBusTrainTime);
        listView = findViewById(R.id.timings_list);
        Bundle extras = getIntent().getExtras();
        type = extras.getString("type");
        trainBusImage = findViewById(R.id.busTrainLogo);
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

        items = new ArrayList<>();

        if (type != null && type.equals("Trains from Coimbatore")) {
            trainBusImage.setImageResource(R.drawable.trainimage);
            flag = 0;
            String json = preferences.getString("trains-from-cbe", null);
            if (json != null) {
                JSONArray timings = new JSONArray(json);
                System.out.println(timings.toString());
                try {
                    Calendar currentTime = Calendar.getInstance();
                    for (int i = 0; i < timings.length(); ++i) {
                        JSONObject item = timings.getJSONObject(i);
                        Date busTimeDate = dateFormat.parse(item.getString("dep"));
                        Calendar trainTime = setTrainTime(busTimeDate);
                        items.add(new DataItem(
                                item.getString("name"),
                                item.getString("days"),
                                item.getString("dep"),
                                "cbe",
                                "etmd",
                                "train"
                        ));
                        if (item.getString("days").toLowerCase().contains(dayFormat.format(trainTime.getTime()).toLowerCase())) {
                            continue;
                        } else if (trainTime.after(currentTime) && flag == 0) {
                            calcTimeDiff(item.getString("dep"), currentTime, trainTime);
                        }
                    }
                    if (flag == 0) {
                        setNoTrain();
                    }
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (type != null && type.equals("Trains from Palghat")) {
            flag = 0;
            trainBusImage.setImageResource(R.drawable.trainimage);
            String json = preferences.getString("trains-from-pkd", null);
            if (json != null) {
                JSONArray timings = new JSONArray(json);
                System.out.println(timings.toString());
                try {
                    Calendar currentTime = Calendar.getInstance();
                    for (int i = 0; i < timings.length(); ++i) {
                        JSONObject item = timings.getJSONObject(i);
                        Date busTimeDate = dateFormat.parse(item.getString("dep"));
                        Calendar trainTime = setTrainTime(busTimeDate);
                        items.add(new DataItem(
                                item.getString("name"),
                                item.getString("days"),
                                item.getString("dep"),
                                "pkd",
                                "etmd",
                                "train"
                        ));
                        if (item.getString("days").toLowerCase().contains(dayFormat.format(trainTime.getTime()).toLowerCase())) {
                            continue;
                        } else if (trainTime.after(currentTime) && flag == 0) {
                            calcTimeDiff(item.getString("dep"), currentTime, trainTime);
                        }
                    }
                    if (flag == 0) {
                        setNoTrain();
                    }
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (type != null && type.equals("Trains to Coimbatore")) {
            flag = 0;
            trainBusImage.setImageResource(R.drawable.trainimage);
            String json = preferences.getString("trains-to-cbe", null);
            if (json != null) {
                JSONArray timings = new JSONArray(json);
                System.out.println(timings.toString());
                try {
                    Calendar currentTime = Calendar.getInstance();

                    for (int i = 0; i < timings.length(); ++i) {
                        JSONObject item = timings.getJSONObject(i);
                        Date busTimeDate = dateFormat.parse(item.getString("dep"));
                        Calendar trainTime = setTrainTime(busTimeDate);

                        items.add(new DataItem(
                                item.getString("name"),
                                item.getString("days"),
                                item.getString("dep"),
                                "etmd",
                                "cbe",
                                "train"
                        ));
                        if (item.getString("days").toLowerCase().contains(dayFormat.format(trainTime.getTime()).toLowerCase())) {
                            continue;
                        } else if (trainTime.after(currentTime) && flag == 0) {
                            calcTimeDiff(item.getString("dep"), currentTime, trainTime);
                        }
                    }
                    if (flag == 0) {
                        setNoTrain();
                    }
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (type != null && type.equals("Trains to Palghat")) {

            trainBusImage.setImageResource(R.drawable.trainimage);
            flag = 0;
            String json = preferences.getString("trains-to-pkd", null);
            if (json != null) {
                JSONArray timings = new JSONArray(json);
                System.out.println(timings.toString());
                try {
                    Calendar currentTime = Calendar.getInstance();

                    for (int i = 0; i < timings.length(); ++i) {
                        JSONObject item = timings.getJSONObject(i);
                        Date busTimeDate = dateFormat.parse(item.getString("dep"));
                        Calendar trainTime = setTrainTime(busTimeDate);

                        items.add(new DataItem(
                                item.getString("name"),
                                item.getString("days"),
                                item.getString("dep"),
                                "etmd",
                                "pkd",
                                "train"
                        ));
                        if (item.getString("days").toLowerCase().contains(dayFormat.format(trainTime.getTime()).toLowerCase())) {
                            continue;
                        } else if (trainTime.after(currentTime) && flag == 0) {
                            calcTimeDiff(item.getString("dep"), currentTime, trainTime);
                        }
                    }
                    if (flag == 0) {
                        setNoTrain();
                    }
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (type != null && type.equals("Buses from Coimbatore")) {
            trainBusImage.setImageResource(R.drawable.busimage);
            flag = 0;
            String json = preferences.getString("bus-from-cbe", null);
            if (json != null) {
                JSONArray timings = new JSONArray(json);
                System.out.println(timings.toString());
                try {
                    Calendar currentTime = Calendar.getInstance();

                    for (int i = 0; i < timings.length(); ++i) {
                        JSONObject item = timings.getJSONObject(i);
                        Date busTimeDate = dateFormat.parse(item.getString("dep"));
                        Calendar busTime = setTrainTime(busTimeDate);
                        items.add(new DataItem(
                                item.getString("name"),
                                item.getString("days"),
                                item.getString("dep"),
                                "cbe",
                                "etmd",
                                "bus"
                        ));
                        if (busTime.after(currentTime) && flag == 0) {
                            nextTrainBus.setText(String.format("%s %s", getString(R.string.nextBusText), item.getString("dep")));
                            flag = 1;
                            Date startTime = currentTime.getTime();
                            Date endTime = busTime.getTime();
                            long timediff = endTime.getTime() - startTime.getTime();
                            countdown(timediff);
                        }
                    }
                    if (flag == 0) {
                        nextTrainBus.setText(R.string.noBusText);
                        countdownTimer.setVisibility(View.GONE);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (type != null && type.equals("Buses to Coimbatore")) {
            trainBusImage.setImageResource(R.drawable.busimage);
            flag = 0;
            String json = preferences.getString("bus-to-cbe", null);
            if (json != null) {
                JSONArray timings = new JSONArray(json);
                System.out.println(timings.toString());
                try {
                    Calendar currentTime = Calendar.getInstance();

                    for (int i = 0; i < timings.length(); ++i) {
                        JSONObject item = timings.getJSONObject(i);
                        Date busTimeDate = dateFormat.parse(item.getString("dep"));
                        Calendar busTime = setTrainTime(busTimeDate);
                        items.add(new DataItem(
                                item.getString("name"),
                                item.getString("days"),
                                item.getString("dep"),
                                "etmd",
                                "cbe",
                                "bus"
                        ));
                        if (busTime.after(currentTime) && flag == 0) {
                            nextTrainBus.setText(String.format("%s %s", getString(R.string.nextBusText), item.getString("dep")));
                            flag = 1;
                            Date startTime = currentTime.getTime();
                            Date endTime = busTime.getTime();
                            long timediff = endTime.getTime() - startTime.getTime();
                            countdown(timediff);
                        }
                    }
                    if (flag == 0) {
                        nextTrainBus.setText(R.string.noBusText);
                        countdownTimer.setVisibility(View.GONE);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (items != null) {
            Log.d("REFRESHING ADAPTER", "HERE");
            ArrayAdapter<DataItem> dataItemArrayAdapter = new ArrayAdapter<DataItem>(getBaseContext(), R.layout.timing_item, items) {
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(R.layout.timing_item, parent, false);
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
                        ((TextView) convertView.findViewById(R.id.days)).setText(Html.fromHtml("Runs on " + item.days));

                    return convertView;
                }
            };
            listView.setAdapter(dataItemArrayAdapter);
            listView.setTextFilterEnabled(true);
            listView.setItemsCanFocus(false);
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
        } else if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setNoTrain() {
        nextTrainBus.setText(R.string.noTrainsText);
        countdownTimer.setVisibility(View.GONE);
    }

    private Calendar setTrainTime(Date time) {
        DateTime dateTime = new DateTime(time);
        Calendar trainTime = Calendar.getInstance();
        trainTime.setTime(new Date());
        trainTime.set(Calendar.HOUR_OF_DAY, dateTime.getHourOfDay());
        trainTime.set(Calendar.MINUTE, dateTime.getMinuteOfHour());
        return trainTime;
    }

    private void calcTimeDiff(String time, Calendar currentTime, Calendar trainTime) {
        nextTrainBus.setText(String.format("%s %s", getString(R.string.nextTrainText), time));
        flag = 1;
        Date startTime = currentTime.getTime();
        Date endTime = trainTime.getTime();
        long timeDiff = endTime.getTime() - startTime.getTime();
        countdown(timeDiff);
    }

    private void countdown(long timeDiff) {
        new CountDownTimer(timeDiff, 1000) {

            @SuppressLint("DefaultLocale")
            public void onTick(long millisUntilFinished) {

                if (TimeUnit.MILLISECONDS.toHours(millisUntilFinished) == 0 && (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished))) == 0) {
                    countdownTimer.setText(String.format(" %d Sec Left",
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                } else if (TimeUnit.MILLISECONDS.toHours(millisUntilFinished) == 0) {
                    countdownTimer.setText(String.format(" %d Min : %d Sec Left",
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                } else {
                    countdownTimer.setText(String.format("%d Hr : %d Min : %d Sec Left",
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                }
            }

            public void onFinish() {
                countdownTimer.setText(R.string.departed);
                try {
                    loadData(type);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
}

