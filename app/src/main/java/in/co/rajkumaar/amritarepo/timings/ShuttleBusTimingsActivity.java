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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class ShuttleBusTimingsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<DataItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuttle_bus_timings);
        Utils.showBigAd(this, (LinearLayout) findViewById(R.id.banner_container));

        listView = findViewById(R.id.timings_list);
        Bundle extras = getIntent().getExtras();
        String type = extras.getString("type");

        getSupportActionBar().setTitle(type);
        loadData(type);

        ArrayAdapter<DataItem> dataItemArrayAdapter = new ArrayAdapter<DataItem>(getBaseContext(), R.layout.timing_item, items) {
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

    private void loadData(String type) {
        items = new ArrayList<>();
        if (type != null && type.equals("Buses from AB1")) {
            items.add(new DataItem(
                    "09:20 AM", "ab1"
            ));
            items.add(new DataItem(
                    "09:40 AM", "ab1"
            ));
            items.add(new DataItem(
                    "10:10 AM", "ab1"
            ));
            items.add(new DataItem(
                    "10:30 AM", "ab1"
            ));
            items.add(new DataItem(
                    "11:10 AM", "ab1"
            ));
            items.add(new DataItem(
                    "11:20 AM", "ab1"
            ));
            items.add(new DataItem(
                    "12:00 PM", "ab1"
            ));
            items.add(new DataItem(
                    "12:20 PM", "ab1"
            ));
            items.add(new DataItem(
                    "01:00 PM", "ab1"
            ));
            items.add(new DataItem(
                    "01:10 PM", "ab1"
            ));
            items.add(new DataItem(
                    "01:50 PM", "ab1"
            ));
            items.add(new DataItem(
                    "02:10 PM", "ab1"
            ));
            items.add(new DataItem(
                    "02:40 PM", "ab1"
            ));
            items.add(new DataItem(
                    "03:00 PM", "ab1"
            ));
            items.add(new DataItem(
                    "03:30 PM", "ab1"
            ));
            items.add(new DataItem(
                    "03:50 PM", "ab1"
            ));
        } else if (type != null && type.equals("Buses from AB3")) {
            items.add(new DataItem(
                    "09:20 AM", "ab3"
            ));
            items.add(new DataItem(
                    "09:40 AM", "ab3"
            ));
            items.add(new DataItem(
                    "10:10 AM", "ab3"
            ));
            items.add(new DataItem(
                    "10:30 AM", "ab3"
            ));
            items.add(new DataItem(
                    "11:10 AM", "ab3"
            ));
            items.add(new DataItem(
                    "11:20 AM", "ab3"
            ));
            items.add(new DataItem(
                    "12:00 PM", "ab3"
            ));
            items.add(new DataItem(
                    "12:20 PM", "ab3"
            ));
            items.add(new DataItem(
                    "01:00 PM", "ab3"
            ));
            items.add(new DataItem(
                    "01:10 PM", "ab3"
            ));
            items.add(new DataItem(
                    "01:50 PM", "ab3"
            ));
            items.add(new DataItem(
                    "02:10 PM", "ab3"
            ));
            items.add(new DataItem(
                    "02:40 PM", "ab3"
            ));
            items.add(new DataItem(
                    "03:00 PM", "ab3"
            ));
            items.add(new DataItem(
                    "03:30 PM", "ab3"
            ));
            items.add(new DataItem(
                    "03:50 PM", "ab3"
            ));
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

