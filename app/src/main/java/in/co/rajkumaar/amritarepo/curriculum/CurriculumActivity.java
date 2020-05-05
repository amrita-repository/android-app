/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.curriculum;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URISyntaxException;
import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.DownloadTask;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class CurriculumActivity extends BaseActivity {

    ProgressDialog dialog;
    WebView myWebView;
    String webViewLink;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);

        try {
            webViewLink = getString(R.string.curriculum_url);
            this.setTitle("Curriculum");
            myWebView = findViewById(R.id.webView);
            dialog = new ProgressDialog(CurriculumActivity.this);
            myWebView.getSettings().setJavaScriptEnabled(true);
            myWebView.getSettings().setLoadWithOverviewMode(true);
            myWebView.getSettings().setUseWideViewPort(true);
            showProgressDialog();
            myWebView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith(getString(R.string.amrita_file_prefix))) {
                        showProgressDialog();
                        getRealURLAndDisplayMenu(url);
                    }
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    dismissProgressDialog();
                    super.onPageFinished(view, url);
                }
            });
            myWebView.loadUrl(webViewLink);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            Utils.showUnexpectedError(CurriculumActivity.this);
        }
    }

    private void getRealURLAndDisplayMenu(String courseURL) {
        showProgressDialog();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, courseURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Document document = Jsoup.parse(response);
                            String fileLink = document.select(".pdf-reader-download-link").first().select("a").attr("href");
                            displayOptions(fileLink);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Utils.showUnexpectedError(CurriculumActivity.this);
                            finish();
                        } finally {
                            dismissProgressDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                dismissProgressDialog();
                Utils.showUnexpectedError(CurriculumActivity.this);
                finish();
            }
        });
        requestQueue.add(stringRequest);
    }

    private void displayOptions(final String url) {
        dismissProgressDialog();
        final ArrayList<String> options = new ArrayList<>();
        options.add("View");
        options.add("Download");
        AlertDialog.Builder optionsBuilder = new AlertDialog.Builder(CurriculumActivity.this);
        ArrayAdapter<String> optionsAdapter = new ArrayAdapter<>(CurriculumActivity.this, android.R.layout.simple_list_item_1, options);
        optionsBuilder.setAdapter(optionsAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                if (pos == 0) {
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));
                } else if (pos == 1) {
                    if (ContextCompat.checkSelfPermission(CurriculumActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(CurriculumActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                1);
                    } else {
                        if (Utils.isConnected(CurriculumActivity.this)) {
                            try {
                                new DownloadTask(CurriculumActivity.this, Utils.getUrlWithoutParameters(url), 0);
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Utils.showToast(CurriculumActivity.this, "Device not connected to Internet.");
                        }


                    }
                }
            }
        });
        optionsBuilder.show();
    }

    public void dismissProgressDialog() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showProgressDialog() {
        try {
            dialog.setMessage("Loading..");
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
