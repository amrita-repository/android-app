/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.curriculum;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.net.URISyntaxException;
import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.DownloadTask;
import in.co.rajkumaar.amritarepo.helpers.Utils;

import static in.co.rajkumaar.amritarepo.helpers.Utils.THEME_LIGHT;

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
            webViewLink = getString(R.string.dev_domain) + "/utils/curriculum.php?theme=" + getSharedPreferences("theming", MODE_PRIVATE).getString("theme", THEME_LIGHT);
            Log.e("LINK", webViewLink);
            if (webViewLink == null) {
                Utils.showToast(CurriculumActivity.this, "Invalid URL");
                finish();
                return;
            }
            this.setTitle("Curriculum");
            myWebView = findViewById(R.id.webView);
            myWebView.getSettings().setJavaScriptEnabled(true);
            dialog = new ProgressDialog(CurriculumActivity.this);
            myWebView.getSettings().setLoadWithOverviewMode(true);
            myWebView.getSettings().setUseWideViewPort(true);
            showProgressDialog();
            myWebView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    showProgressDialog();
                    if (progress == 100)
                        dismissProgressDialog();
                }
            });
            myWebView.setWebViewClient(new WebViewClient() {
                @Nullable
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, final String url) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (Utils.getUrlWithoutParameters(url).contains(".pdf")) {
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
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    return super.shouldInterceptRequest(view, url);
                }

                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    try {
                        return !Utils.getUrlWithoutParameters(url).contains(".pdf");
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        return true;
                    }
                }
            });
            myWebView.loadUrl(webViewLink);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            Toast.makeText(CurriculumActivity.this, "Unexpected error. Please try again later", Toast.LENGTH_SHORT).show();
        }
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
