/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aumsV2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.aums.helpers.HomeItemAdapter;
import in.co.rajkumaar.amritarepo.aums.models.HomeItem;
import in.co.rajkumaar.amritarepo.aumsV2.helpers.GlobalData;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class HomeActivity extends BaseActivity {

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        getSupportActionBar().setSubtitle("Lite Version");
        SharedPreferences preferences = getSharedPreferences("aums-lite", MODE_PRIVATE);
        TextView name = findViewById(R.id.name);
        TextView user_name = findViewById(R.id.username);
        TextView e_mail = findViewById(R.id.email);
        ListView listView = findViewById(R.id.list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name.setText(preferences.getString("name", "N/A"));
        user_name.setText(preferences.getString("username", "N/A"));
        e_mail.setText(preferences.getString("email", "N/A"));

        final ArrayList<HomeItem> items = new ArrayList<>();
        items.add(new HomeItem("Attendance Status", R.drawable.attendance));
        items.add(new HomeItem("Grades", R.drawable.grades));
        HomeItemAdapter homeItemAdapter = new HomeItemAdapter(this, items);
        listView.setAdapter(homeItemAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Utils.isConnected(HomeActivity.this)) {
                    switch (items.get(position).getName()) {
                        case "Attendance Status":
                            startActivity(new Intent(HomeActivity.this, AttendanceSemestersActivity.class));
                            break;
                        case "Grades":
                            startActivity(new Intent(HomeActivity.this, GradesSemestersActivity.class));
                            break;
                    }
                } else {
                    Utils.showInternetError(HomeActivity.this);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.logout:
                onLogout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void onLogout() {
        if (doubleBackToExitPressedOnce) {
            logout();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Utils.showSnackBar(this, "Press back again to logout of AUMS");
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void logout() {
        GlobalData.resetUser(this);
        Utils.showToast(this, "Successfully logged out");
        finish();
    }
}
