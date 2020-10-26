/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.Utils;

public class SupportActivity extends BaseActivity {

    private static final String GOOGLE_TEZ_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        findViewById(R.id.gpay).setOnClickListener(v -> {
            try {
                Uri uri =
                        new Uri.Builder()
                                .scheme("upi")
                                .authority("pay")
                                .appendQueryParameter("pa", getString(R.string.upi_id))
                                .appendQueryParameter("pn", "Rajkumar S")
                                .appendQueryParameter("tn", "Amrita Repository")
                                .appendQueryParameter("cu", "INR")
                                .build();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                intent.setPackage(GOOGLE_TEZ_PACKAGE_NAME);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.showToast(SupportActivity.this, "Please install Google Pay first.");
            }
        });

        findViewById(R.id.paypal).setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.paypal_url)));
            startActivity(browserIntent);
        });
    }
}
