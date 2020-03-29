/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class OPACAccountActivity extends BaseActivity {

    OPACClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opacaccount);

        final TextView name = findViewById(R.id.name);
        final TextView number = findViewById(R.id.noOfCheckouts);
        final TextView lastChecked = findViewById(R.id.lastChecked);
        final TextView fine = findViewById(R.id.fine);
        final WebView webView = findViewById(R.id.webView);
        client = new OPACClient(this);
        client.getProfile(getIntent().getStringExtra("id"), getIntent().getStringExtra("password"), new BookDetailResponse() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    Bundle params = new Bundle();
                    params.putString("LibraryUser", getIntent().getStringExtra("id")+"["+jsonObject.getString("name")+"]");
                    FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(OPACAccountActivity.this);
                    mFirebaseAnalytics.logEvent("LibraryLogin", params);
                    name.setText(String.format("Henlo, %s \uD83E\uDD29 ", jsonObject.getString("name")));
                    number.setText(Html.fromHtml("Currently you have "+(!jsonObject.getString("checkouts").isEmpty()
                            ? "<font color='#FF201B'>"+jsonObject.getString("checkouts")+"</font> checkouts \uD83D\uDCDA"
                            : "<font color='#FF201B'>0</font> checkouts")));
                    lastChecked.setText(Html.fromHtml(
                            String.format("Your last check out at library was on <font color='#FF201B'>%s</font>",
                                    jsonObject.getString("last_checked").isEmpty() ? "N/A" : jsonObject.getString("last_checked"))));
                    if(jsonObject.getString("fine_due").isEmpty())
                        fine.setVisibility(View.GONE);
                    else{
                        fine.setText(Html.fromHtml("Your fine due is Rs ."+jsonObject.getString("fine_due")));
                    }

                    if(jsonObject.getJSONArray("items").length() < 1)
                        webView.setVisibility(View.GONE);
                    else {
                        StringBuilder data = new StringBuilder();
                        data.append("<h3>Your checkouts along with due date</h3><ul>");
                        for (int i = 0; i < jsonObject.getJSONArray("items").length(); ++i) {
                            data.append("<li>").append(jsonObject.getJSONArray("items").getString(i)).append("</li>");
                        }
                        data.append("</ul>");
                        webView.loadData(data.toString(), "text/html", "UTF-8");
                    }
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                } catch (JSONException e) {
                    Utils.showUnexpectedError(OPACAccountActivity.this);
                    finish();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception exception) {
                Utils.showUnexpectedError(OPACAccountActivity.this);
                finish();
            }
        });
    }

}
