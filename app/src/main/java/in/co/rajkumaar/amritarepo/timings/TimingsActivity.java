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

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;

public class TimingsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<DataItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_bus_timings);

        listView = findViewById(R.id.timings_list);
        Bundle extras = getIntent().getExtras();
        String type = extras.getString("type");

        getSupportActionBar().setTitle(type);

        AdView mAdView;
        MobileAds.initialize(this, getResources().getString(R.string.banner_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        loadData(type);

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
                ((TextView) convertView.findViewById(R.id.departure)).setText(Html.fromHtml("Departs from Coimbatore at " +font_color + item.departure + "</font>"));
            if (item.from.equals("etmd"))
                ((TextView) convertView.findViewById(R.id.departure)).setText(Html.fromHtml("Departs from Ettimadai at " +font_color+ item.departure + "</font>"));
            if (item.from.equals("pkd"))
                ((TextView) convertView.findViewById(R.id.departure)).setText(Html.fromHtml("Departs from Palghat at " +font_color+ item.departure + "</font>"));

            if (item.to.equals("cbe"))
                ((TextView) convertView.findViewById(R.id.arrival)).setText(Html.fromHtml("Reaches Coimbatore at "+font_color + item.arrival + "</font>"));
            if (item.to.equals("etmd"))
                ((TextView) convertView.findViewById(R.id.arrival)).setText(Html.fromHtml("Reaches Ettimadai at "+font_color + item.arrival + "</font>"));
            if (item.to.equals("pkd"))
                ((TextView) convertView.findViewById(R.id.arrival)).setText(Html.fromHtml("Reaches Palghat at "+font_color + item.arrival + "</font>"));

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



    class DataItem {
        String name;
        String days;
        String departure;
        String arrival;
        String from;
        String to;
        String type;

        DataItem(String name, String days, String departure, String arrival, String from, String to, String type) {
            this.name = name;
            this.days = days;
            this.departure = departure;
            this.arrival = arrival;
            this.from = from;
            this.to = to;
            this.type = type;
        }
    }


    private void loadData(String type) {
        items = new ArrayList<>();


        if (type != null && type.equals("Trains from Coimbatore")) {
            items.add(new DataItem(
                    "56323 Coimbatore Mangalore Fast Passenger",
                    "All Days",
                    "07:30 hours",
                    "08:00 hours",
                    "cbe",
                    "etmd",
                    "train"
            ));
            items.add(new DataItem(
                    "66605 Coimbatore Shoranur Passenger",
                    "Except Sundays",
                    "09:45 hours",
                    "10:14 hours",
                    "cbe",
                    "etmd",
                    "train"
            ));
            items.add(new DataItem(
                    "66609 Erode Palakkad MEMU",
                    "Except Thursdays",
                    "10:30 hours",
                    "11:04 hours",
                    "cbe",
                    "etmd",
                    "train"
            ));
            items.add(new DataItem(
                    "56651 Coimbatore Kannur Fast Passenger",
                    "All Days",
                    "14:10 hours",
                    "14:36 hours",
                    "cbe",
                    "etmd",
                    "train"
            ));
            items.add(new DataItem(
                    "56605 Coimbatore Thrissur Passenger ",
                    "All Days",
                    "16:40 hours",
                    "17:07 hours",
                    "cbe",
                    "etmd",
                    "train"
            ));
            items.add(new DataItem(
                    "66607 Coimbatore Palakkad Town Passenger",
                    "Except Sundays",
                    "18:10 hours",
                    "18:37 hours",
                    "cbe",
                    "etmd",
                    "train"
            ));
            items.add(new DataItem(
                    "56713 Trichy - Palakkad Town Fast Passenger ",
                    "All Days",
                    "18:30 hours",
                    "19:03 hours",
                    "cbe",
                    "etmd",
                    "train"
            ));
        }

        if (type != null && type.equals("Trains from Palghat")) {
            items.add(new DataItem(
                    "56712 Palghat Town Tiruchchirapalli Fast Passenger",
                    "All Days",
                    "06:43 hours",
                    "07:24 hours",
                    "pkd",
                    "etmd",
                    "train"
            ));
            items.add(new DataItem(
                    "66606 Palghat Town Coimbatore Passenger",
                    "Except Sundays",
                    "07:33 hours",
                    "08:14 hours",
                    "pkd",
                    "etmd",
                    "train"
            ));
            items.add(new DataItem(
                    "56604 Shoranur Coimbatore Passenger",
                    "All Days",
                    "09:10 hours",
                    "09:54 hours",
                    "pkd",
                    "etmd",
                    "train"
            ));
            items.add(new DataItem(
                    "56650 Kannur Coimbatore Fast Passenger",
                    "All Days",
                    "11:25 hours",
                    "12:14 hours",
                    "pkd",
                    "etmd",
                    "train"
            ));
            items.add(new DataItem(
                    "66608 Palakkad Erode MEMU",
                    "Except Thursdays",
                    "14:45 hours",
                    "15:31 hours",
                    "pkd",
                    "etmd",
                    "train"
            ));
            items.add(new DataItem(
                    "66604 Shoranur Coimbatore Passenger",
                    "Except Sundays",
                    "15:55 hours",
                    "16:39 hours",
                    "pkd",
                    "etmd",
                    "train"
            ));
            items.add(new DataItem(
                    "56324 Mangalore - Coimbatore Fast Passenger",
                    "All Days",
                    "17:50 hours",
                    "18:39 hours",
                    "pkd",
                    "etmd",
                    "train"
            ));
        }

        if (type != null && type.equals("Trains to Coimbatore")) {
            items.add(new DataItem(
                    "56712 Palghat Town Tiruchchirapalli Fast Passenger",
                    "All Days",
                    "07:25 hours",
                    "08:05 hours",
                    "etmd",
                    "cbe",
                    "train"
            ));
            items.add(new DataItem(
                    "66606 Palghat Town Coimbatore Passenger",
                    "Except Sundays",
                    "08:15 hours",
                    "09:00 hours",
                    "etmd",
                    "cbe",
                    "train"
            ));
            items.add(new DataItem(
                    "56604 Shoranur Coimbatore Passenger",
                    "All Days",
                    "09:55 hours",
                    "10:50 hours",
                    "etmd",
                    "cbe",
                    "train"
            ));
            items.add(new DataItem(
                    "56650 Kannur Coimbatore Fast Passenger",
                    "All Days",
                    "12:15 hours",
                    "13:30 hours",
                    "etmd",
                    "cbe",
                    "train"
            ));
            items.add(new DataItem(
                    "66608 Palakkad Erode MEMU",
                    "Except Thursdays",
                    "15:32 hours",
                    "16:05 hours",
                    "etmd",
                    "cbe",
                    "train"
            ));
            items.add(new DataItem(
                    "66604 Shoranur Coimbatore Passenger",
                    "Except Sundays",
                    "16:40 hours",
                    "17:40 hours",
                    "etmd",
                    "cbe",
                    "train"
            ));
            items.add(new DataItem(
                    "56324 Mangalore - Coimbatore Fast Passenger",
                    "All Days",
                    "18:40 hours",
                    "19:35 hours",
                    "etmd",
                    "cbe",
                    "train"
            ));
        }

        if (type != null && type.equals("Trains to Palghat")) {
            items.add(new DataItem(
                    "56323 Coimbatore Mangalore Fast Passenger",
                    "All Days",
                    "07:58 hours",
                    "08:55  hours",
                    "etmd",
                    "pkd",
                    "train"
            ));
            items.add(new DataItem(
                    "66605 Coimbatore Shoranur Passenger",
                    "Except Sundays",
                    "10:15 hours",
                    "11:00 hours",
                    "etmd",
                    "pkd",
                    "train"
            ));
            items.add(new DataItem(
                    "66609 Erode Palakkad MEMU",
                    "Except Thursdays",
                    "11:05 hours",
                    "11:50  hours",
                    "etmd",
                    "pkd",
                    "train"
            ));
            items.add(new DataItem(
                    "56651 Coimbatore Kannur Fast Passenger",
                    "All Days",
                    "14:37 hours",
                    "15:15 hours",
                    "etmd",
                    "pkd",
                    "train"
            ));
            items.add(new DataItem(
                    "56605 Coimbatore Thrissur Passenger",
                    "All Days",
                    "17:08 hours",
                    "17:50 hours",
                    "etmd",
                    "pkd",
                    "train"
            ));
            items.add(new DataItem(
                    "66607 Coimbatore Palakkad Town Passenger",
                    "Except Sundays",
                    "18:38 hours",
                    "19:23 hours",
                    "etmd",
                    "pkd",
                    "train"
            ));
            items.add(new DataItem(
                    "56847 Trichy - Palakkad Town Fast Passenger",
                    "All Days",
                    "19:04 hours",
                    "19:55 hours",
                    "etmd",
                    "pkd",
                    "train"
            ));
        }

        if (type != null && type.equals("Buses from Coimbatore")) {
            items.add(new DataItem(
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
            ));
        }

        if (type != null && type.equals("Buses to Coimbatore")) {
            items.add(new DataItem(
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
            ));
        }
    }


}

