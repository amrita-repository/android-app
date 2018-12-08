package in.co.rajkumaar.amritarepo.aums;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;

import static android.view.View.GONE;

public class HomeActivity extends AppCompatActivity {

    TextView name,username,cgpa;
    ImageView pic;
    ListView list;
    String semester;
    AsyncHttpClient client;
    private Map<String, String> semesterMapping;
    ProgressBar image_progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if(!UserData.loggedin)
        {
            finish();
            startActivity(new Intent(this,LoginActivity.class));
        }
        semesterMapping = new HashMap<>();
        name=findViewById(R.id.name);
        username=findViewById(R.id.username);
        cgpa=findViewById(R.id.cgpa);
        pic=findViewById(R.id.userImage);
        list=findViewById(R.id.list);
        image_progress=findViewById(R.id.image_progress);

        client = UserData.client;
        setData();
        ArrayList<HomeItem> items = new ArrayList<>();

        items.add(new HomeItem("Attendance Status",R.drawable.attendance));
        items.add(new HomeItem("Grades",R.drawable.grades));
        items.add(new HomeItem("Marks",R.drawable.marks));

        loadSemesterMapping();

        HomeItemAdapter homeItemAdapter = new HomeItemAdapter(this,items);

        list.setAdapter(homeItemAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                semesterPicker(position);

            }
        });
        getPhoto(UserData.client);

    }


    void getPhoto(final AsyncHttpClient client){

        RequestParams params = new RequestParams();
        params.add("action","UMS-SRMHR_SHOW_PERSON_PHOTO");
        params.add("personId",UserData.uuid);

        client.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        client.get(UserData.domain + "/aums/FileUploadServlet", params, new FileAsyncHttpResponseHandler(HomeActivity.this) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                image_progress.setVisibility(GONE);
                pic.setVisibility(View.VISIBLE);
                pic.setImageResource(R.drawable.user);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                image_progress.setVisibility(GONE);
                pic.setVisibility(View.VISIBLE);
                Picasso.get().load(file).into(pic);
            }
        });
    }



    private void semesterPicker(final int position) {
        final String[] items = {"1", "2", "Vacation 1", "3", "4", "Vacation 2", "5", "6", "Vacation 3", "7", "8", "Vacation 4", "9", "10", "Vacation 5", "11", "12", "Vacation 6", "13", "14", "15"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Select a Semester");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int pos) {
                semester = semesterMapping.get(items[pos]);
                if(Utils.isConnected(HomeActivity.this)){
                 switch (position){
                     case 0 : startActivity(new Intent(HomeActivity.this,AttendanceActivity.class).putExtra("sem",semester));
                                break;
                     case 1 : startActivity(new Intent(HomeActivity.this,GradesActivity.class).putExtra("sem",semester));
                                break;
                     case 2:
                         startActivity(new Intent(HomeActivity.this,MarksActivity.class).putExtra("sem",semester));
                         break;
                 }
                }
                else{
                    Toast.makeText(HomeActivity.this,"Please connect to internet",Toast.LENGTH_LONG).show();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    void setData(){
        name.setText(UserData.name);
        username.setText(UserData.username);
        cgpa.setText("Current CGPA : "+UserData.CGPA);
        Picasso.get().load(UserData.image).into(pic);
    }


    private void loadSemesterMapping() {
        semesterMapping.clear();
        semesterMapping.put("1", "7");
        semesterMapping.put("2", "8");
        semesterMapping.put("Vacation 1", "231");
        semesterMapping.put("3", "9");
        semesterMapping.put("4", "10");
        semesterMapping.put("Vacation 2", "232");
        semesterMapping.put("5", "11");
        semesterMapping.put("6", "12");
        semesterMapping.put("Vacation 3", "233");
        semesterMapping.put("7", "13");
        semesterMapping.put("8", "14");
        semesterMapping.put("Vacation 4", "234");
        semesterMapping.put("9", "72");
        semesterMapping.put("10", "73");
        semesterMapping.put("Vacation 5", "243");
        semesterMapping.put("11", "138");
        semesterMapping.put("12", "139");
        semesterMapping.put("Vacation 6", "244");
        semesterMapping.put("13", "177");
        semesterMapping.put("14", "190");
        semesterMapping.put("15", "219");
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        showSnackbar("Press back again to logout of AUMS");
        //Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                doubleBackToExitPressedOnce=false;
            }
        }, 2000);

    }
    private void showSnackbar(String message) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar
                .make(parentLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.homeAsUp) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
