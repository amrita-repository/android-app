package in.co.rajkumaar.amritarepo.aumsV2.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.aums.helpers.HomeItemAdapter;
import in.co.rajkumaar.amritarepo.aums.models.HomeItem;
import in.co.rajkumaar.amritarepo.aumsV2.helpers.GlobalData;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class HomeActivity extends AppCompatActivity {

    SharedPreferences preferences;
    private boolean doubleBackToExitPressedOnce = false;
    private TextView name, username;
    private TextView email;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        getSupportActionBar().setSubtitle("Lite Version");
        preferences = getSharedPreferences("aums-lite", MODE_PRIVATE);
        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        listView = findViewById(R.id.list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name.setText(preferences.getString("name", "N/A"));
        username.setText(preferences.getString("username", "N/A"));
        email.setText(preferences.getString("email", "N/A"));

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
