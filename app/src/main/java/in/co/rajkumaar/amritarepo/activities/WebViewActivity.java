/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import in.co.rajkumaar.amritarepo.R;

public class WebViewActivity extends AppCompatActivity {


    ProgressDialog dialog;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        try {
            Bundle b = getIntent().getExtras();
            String webviewlink = b.getString("webview");
            this.setTitle(b.getString("title"));
            WebView mywebview = findViewById(R.id.webView);
            mywebview.getSettings().setJavaScriptEnabled(true);
            dialog = new ProgressDialog(WebViewActivity.this);
            if (b.getBoolean("zoom")) {
                mywebview.getSettings().setSupportZoom(true);
                mywebview.getSettings().setBuiltInZoomControls(true);
            }
            mywebview.getSettings().setLoadWithOverviewMode(true);
            mywebview.getSettings().setUseWideViewPort(true);
            showProgressDialog();
            mywebview.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    dismissProgressDialog();
                }
            });
            mywebview.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    dismissProgressDialog();
                }
            });
            mywebview.loadUrl(webviewlink);

        } catch (Exception e) {
            e.printStackTrace();
            finish();
            Toast.makeText(WebViewActivity.this, "Unexpected error. Please try again later", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    public void dismissProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void showProgressDialog() {

        dialog.setMessage("Loading..");
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}

