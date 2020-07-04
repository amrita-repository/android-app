/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.curriculum;

import android.Manifest;
import android.app.ProgressDialog;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);

        try {
            webViewLink = getString(R.string.curriculum_url);
            this.setTitle("Curriculum");
            myWebView = findViewById(R.id.webView);
            dialog = new ProgressDialog(CurriculumActivity.this);
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
            loadHtmlContent();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            Utils.showUnexpectedError(CurriculumActivity.this);
        }
    }

    private void loadHtmlContent() {
        StringBuilder finalHtml = new StringBuilder();
        finalHtml.append(
                "<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
                        "    <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\"\n" +
                        "          integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">\n" +
                        "    <title>Curriculum - Amrita</title>\n" +
                        "    <style>\n" +
                        "        * { background-color: ")
                .append(currentTheme.equals(Utils.THEME_DARK) ? "#262f35" : "#ffffff").append(
                "        ; }\n" +
                        "\n" +
                        "        body { color : \n")
                .append(currentTheme.equals(Utils.THEME_DARK) ? "#ffffff" : "#262f35").append(
                "        ; }\n" +
                        "\n" +
                        "        a {\n" +
                        "            color: #03a9f4;\n" +
                        "        }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "\n" +
                        "<body>\n" +
                        "<div class=\"container mt-2\">");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, webViewLink,
                response -> {
                    try {
                        Document htmlDoc = Jsoup.parse(response);
                        String content = htmlDoc.select("#block-system-main").html();
                        finalHtml.append(content);
                        finalHtml.append("</div>\n" +
                                "</body>\n" +
                                "\n" +
                                "</html>");
                        myWebView.loadDataWithBaseURL("https://www.amrita.edu", finalHtml.toString(), "text/html", "UTF-8", "");
                        System.out.println(finalHtml.toString());
                        dismissProgressDialog();
                    } catch (Exception e) {
                        Utils.showUnexpectedError(CurriculumActivity.this);
                        e.printStackTrace();
                        finish();
                    }
                }, error -> {
            Utils.showUnexpectedError(CurriculumActivity.this);
            error.printStackTrace();
            finish();
        });
        requestQueue.add(stringRequest);
    }

    private void getRealURLAndDisplayMenu(String courseURL) {
        showProgressDialog();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, courseURL,
                response -> {
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
                }, error -> {
            error.printStackTrace();
            dismissProgressDialog();
            Utils.showUnexpectedError(CurriculumActivity.this);
            finish();
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
        optionsBuilder.setAdapter(optionsAdapter, (dialogInterface, pos) -> {
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
                            new DownloadTask(CurriculumActivity.this, Utils.getUrlWithoutParameters(url));
                        } catch (URISyntaxException | UnsupportedEncodingException e) {
                            Utils.showUnexpectedError(CurriculumActivity.this);
                            e.printStackTrace();
                        }
                    } else {
                        Utils.showToast(CurriculumActivity.this, "Device not connected to Internet.");
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
