/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class BookDetailActivity extends BaseActivity {

    LinearLayout container;
    JSONObject details;
    TextView protip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        container = findViewById(R.id.content);
        protip = findViewById(R.id.protip);
        protip.setText(Html.fromHtml("<b><font color='#FF201B'>Pro Tip</font></b><br>Note the <u>Class No</u> field if you would like to<br>find the book in the library"));
        try {
            details = new JSONObject(getIntent().getStringExtra("details"));
            for (Iterator<String> it = this.details.keys(); it.hasNext(); ) {
                String entry = it.next();
                addItem(entry,details.getString(entry));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
            Utils.showUnexpectedError(this);
        }


    }

    private void addItem(String title,String body){
        LinearLayout item = new LinearLayout(this);
        item.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(18);
        titleView.setTextColor(getResources().getColor(android.R.color.white));
        titleView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,2));
        titleView.setTypeface(null, Typeface.BOLD);

        TextView bodyView = new TextView(this);
        bodyView.setText(body);
        bodyView.setTextSize(18);
        bodyView.setTextColor(getResources().getColor(android.R.color.white));
        bodyView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,5));

        if(title.trim().equals("Class No")){
            protip.setVisibility(View.VISIBLE);
            titleView.setTextColor(getResources().getColor(R.color.colorAccent));
            bodyView.setTextColor(getResources().getColor(R.color.colorAccent));
        }


        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1));
        divider.setBackgroundColor(getResources().getColor(R.color.light_grey));

        item.addView(titleView);
        item.addView(bodyView);

        container.addView(item);
//        container.addView(divider);
    }
}
