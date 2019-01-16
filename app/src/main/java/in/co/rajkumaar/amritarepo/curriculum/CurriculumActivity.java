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

package in.co.rajkumaar.amritarepo.curriculum;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class CurriculumActivity extends AppCompatActivity {

    ArrayList<ListView> listViews;
    LinearLayout linearLayout;
    ProgressDialog progressDialog;
    int statuscode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);
        linearLayout=findViewById(R.id.container);

        android.support.v7.app.ActionBar actionBar=getSupportActionBar();
        listViews=new ArrayList<>();
        listViews.add((ListView)findViewById(R.id.sem1));
        listViews.add((ListView)findViewById(R.id.sem2));
        listViews.add((ListView)findViewById(R.id.sem3));
        listViews.add((ListView)findViewById(R.id.sem4));
        listViews.add((ListView)findViewById(R.id.sem5));
        listViews.add((ListView)findViewById(R.id.sem6));
        listViews.add((ListView)findViewById(R.id.sem7));
        listViews.add((ListView)findViewById(R.id.sem8));

        Utils.displayAd(this,(AdView)findViewById(R.id.adView));

        String dept=getIntent().getStringExtra("department");
        try{
            assert actionBar != null;
            actionBar.setSubtitle(dept);
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        progressDialog.show();

        getData(getUrl(dept));
    }


    void getData(final String dept){
        AsyncHttpClient client=new AsyncHttpClient();
        client.get("https://dev.rajkumaar.co.in/utils/btech.php?q="+dept, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                statuscode=statusCode;
                try {
                    JSONArray result = new JSONArray(new String(responseBody));
                    Log.e("JSON SIZE",result.length()+"");
                    for(int i=0;i<result.length();++i)
                    {
                        ArrayList<String> contents=new ArrayList<>();
                        JSONArray items=result.getJSONArray(i);
                        int j;
                        if(dept.equals("eee"))
                            j=0;
                        else
                            j=1;
                        for(;j<items.length();++j){
                            if(items.getString(j).isEmpty())
                                break;
                            String temp=items.getString(j).trim();
                            temp=temp.split("\n")[0];
                            contents.add(Html.fromHtml("&#8226;")+" "+temp);
                        }
                        ArrayAdapter<String> adapter=new ArrayAdapter<>(CurriculumActivity.this,R.layout.curriculum_item,contents);
                        listViews.get(i).setAdapter(adapter);
                        setListViewHeightBasedOnChildren(listViews.get(i));
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }

            @Override
            public void onFinish() {
                try {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    if (statuscode == 200)
                        linearLayout.setVisibility(View.VISIBLE);
                    else {
                        finish();
                        Toast.makeText(CurriculumActivity.this, "Unexpected error occurred.Please try again later", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    finish();
                    Toast.makeText(CurriculumActivity.this, "Unexpected error occurred.Please try again later", Toast.LENGTH_SHORT).show();
                }

                super.onFinish();
            }
        });
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    String getUrl(String dept){
        switch (dept){
            case "Aerospace Engineering":
                return "ae";
            case "Civil Engineering":
                return "cvi";
            case "Chemical Engineering":
                return "che";
            case "Computer Science Engineering":
                return "cse";
            case "Electrical & Electronics Engineering":
                return "eee";
            case "Electronics & Communication Engineering":
                return "ece";
            case "Mechanical Engineering":
                return "me";
            case "Electronics & Instrumentation Engineering":
                return "eie";
            default:
                return "cse";
        }

    }


}
