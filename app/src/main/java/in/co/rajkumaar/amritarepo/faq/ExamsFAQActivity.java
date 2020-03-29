package in.co.rajkumaar.amritarepo.faq;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class ExamsFAQActivity extends AppCompatActivity {
    private ProgressDialog dialog;
    private WebView mywebview;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        try {
            this.setTitle("Frequently Asked Questions");
            dialog = new ProgressDialog(ExamsFAQActivity.this);
            mywebview = findViewById(R.id.webView);
            getFAQ();
            mywebview.getSettings().setJavaScriptEnabled(true);
            mywebview.setDrawingCacheBackgroundColor(getResources().getColor(R.color.colorBackground));
            mywebview.setBackgroundColor(getResources().getColor(R.color.colorBackground));
            mywebview.getSettings().setLoadWithOverviewMode(true);
            mywebview.getSettings().setUseWideViewPort(true);
            showProgressDialog();
            mywebview.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return true;
                }

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
        client.get("https://intranet.cb.amrita.edu/?q=exam", new AsyncHttpResponseHandler() {
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
                        "    \t\tbackground: #1f262a;\n" +
                        "\t\tfont-family: 'Lato', sans-serif;color:#fff;\n" +
                        "    \t}\n" +
                        "    </style>\n" +
                        "</head>\n" +
                                "<body>");
                htmlDoc = Jsoup.parse(new String(bytes));
                String content = htmlDoc.select("section#post-content").html();
                finalHtml.append(content);
                finalHtml.append("</body></html>");
                mywebview.loadData(finalHtml.toString(), "text/html; charset=utf-8", "UTF-8");
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

