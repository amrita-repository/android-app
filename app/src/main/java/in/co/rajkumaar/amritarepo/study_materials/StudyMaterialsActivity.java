/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.study_materials;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class StudyMaterialsActivity extends BaseActivity {

    ProgressDialog dialog;
    WebView myWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_materials);
        try {
            Bundle b = getIntent().getExtras();
            String dept = b.getString("department");
            String webViewLink = getLink(dept);
            if (webViewLink == null) {
                Utils.showToast(StudyMaterialsActivity.this, "Invalid URL");
                finish();
                return;
            }
            this.setTitle(dept);
            myWebView = findViewById(R.id.webView);
            myWebView.getSettings().setJavaScriptEnabled(true);
            dialog = new ProgressDialog(StudyMaterialsActivity.this);
            if (b.getBoolean("zoom", false)) {
                myWebView.getSettings().setSupportZoom(true);
                myWebView.getSettings().setBuiltInZoomControls(true);
            }
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
                @Override
                public void onPageFinished(WebView view, String url) {
                    dismissProgressDialog();
                }
            });
            myWebView.loadUrl(webViewLink);

        } catch (Exception e) {
            e.printStackTrace();
            finish();
            Toast.makeText(StudyMaterialsActivity.this, "Unexpected error. Please try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private String getLink(String dept) {
        switch (dept) {
            case "Computer Science Engineering":
                return "https://sites.google.com/view/amrita-cbe-cse";
            default:
                return null;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.study_materials, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.reload:
                myWebView.reload();
                break;
            case R.id.info:
                AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
                infoDialog.setTitle("Help - Study Materials");
                infoDialog.setMessage(this.getString(R.string.study_materials_info));
                infoDialog.setPositiveButton("Send mail", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                        /* Fill it with Data */
                        emailIntent.setType("plain/text");
                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"rajkumaar2304@icloud.com"});
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Adding study materials in Amrita Repository");

                        /* Send it off to the Activity-Chooser */
                        startActivity(Intent.createChooser(emailIntent, "Send mail..."));

                    }
                });
                infoDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                infoDialog.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
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
            Log.i("LINK", myWebView.getUrl());
            dialog.setMessage("Loading..");
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack())
            myWebView.goBack();
        else
            super.onBackPressed();
    }

    public void openInBrowser(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(myWebView.getUrl()));
        startActivity(i);

    }
}
