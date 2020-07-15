/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class WebViewActivity extends BaseActivity {


    ProgressDialog dialog;
    String webViewLink;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        try {
            Bundle b = getIntent().getExtras();
            webViewLink = b.getString("webview");
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

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        receivedError(failingUrl);
                    }
                }

                @TargetApi(23)
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    receivedError(request.getUrl().toString());
                }

                @TargetApi(23)
                @Override
                public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                    receivedError(request.getUrl().toString());
                }

                private void receivedError(String url) {
                    if (url.equals(webViewLink)) {
                        Utils.showToast(WebViewActivity.this, getString(R.string.not_uploaded_yet));
                        finish();
                    }
                }
            });
            myWebView.loadUrl(webViewLink);

        } catch (Exception e) {
            Utils.showUnexpectedError(WebViewActivity.this);
            e.printStackTrace();
            finish();
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

