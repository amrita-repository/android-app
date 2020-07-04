/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.faq;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class ExamsFAQActivity extends BaseActivity {
    private ProgressDialog dialog;
    private WebView faqWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        try {
            this.setTitle("Frequently Asked Questions");
            dialog = new ProgressDialog(ExamsFAQActivity.this);
            faqWebView = findViewById(R.id.webView);
            getFAQ();
            faqWebView.getSettings().setLoadWithOverviewMode(true);
            faqWebView.getSettings().setUseWideViewPort(true);
            showProgressDialog();
            faqWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    dismissProgressDialog();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            finish();
            Toast.makeText(ExamsFAQActivity.this, "Unexpected error. Please try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private void getFAQ() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(getString(R.string.faq_exams_url), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Document htmlDoc;
                StringBuilder finalHtml = new StringBuilder();
                finalHtml.append(
                        "<!DOCTYPE html>\n" +
                                "<html>\n" +
                                "<head>\n" +
                                "\t<title>\n" +
                                "\t\tExams FAQ from Intranet\n" +
                                "\t</title>\n" +
                                "\t <meta charset=\"utf-8\">\n" +
                                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                                "    <link href=\"https://fonts.googleapis.com/css?family=Lato\" rel=\"stylesheet\">\n" +
                                "    <style type=\"text/css\">\n" +
                                "    \tbody{\n" +
                                "    \t\tbackground: ")
                        .append(currentTheme.equals(Utils.THEME_DARK) ? "#1f262a" : "#ffffff")
                        .append(";\n\t\tfont-family: 'Lato', sans-serif;color:")
                        .append(currentTheme.equals(Utils.THEME_DARK) ? "#ffffff" : "#1f262a")
                        .append(";\n" + " \t}\n" +
                                "    </style>\n" +
                                "</head>\n" + "<body>");
                htmlDoc = Jsoup.parse(new String(bytes));
                String content = htmlDoc.select("#node-607").outerHtml();
                finalHtml.append(content);
                finalHtml.append("</body></html>");
                faqWebView.loadDataWithBaseURL(getString(R.string.intranet_url), finalHtml.toString(), "text/html", "UTF-8", "");
                dismissProgressDialog();
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Utils.showToast(ExamsFAQActivity.this, "An unexpected error occurred. Please try again later");
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
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
}

