/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import in.co.rajkumaar.amritarepo.R;

public class WebViewActivity extends BaseActivity {


    ProgressDialog dialog;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        try {
            Bundle b = getIntent().getExtras();
            String webViewLink = b.getString("webview");
            this.setTitle(b.getString("title"));
            WebView myWebView = findViewById(R.id.webView);
            myWebView.getSettings().setJavaScriptEnabled(true);
            dialog = new ProgressDialog(WebViewActivity.this);
            if (b.getBoolean("zoom")) {
                myWebView.getSettings().setSupportZoom(true);
                myWebView.getSettings().setBuiltInZoomControls(true);
            }
            myWebView.getSettings().setLoadWithOverviewMode(true);
            myWebView.getSettings().setUseWideViewPort(true);
            showProgressDialog();
            myWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    dismissProgressDialog();
                }
            });
            myWebView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    dismissProgressDialog();
                }
            });
            myWebView.loadUrl(webViewLink);

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

