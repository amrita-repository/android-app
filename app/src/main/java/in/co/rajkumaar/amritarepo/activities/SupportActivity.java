/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.activities;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;

import java.util.ArrayList;

import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.helpers.BillingItem;
import in.co.rajkumaar.amritarepo.helpers.Utils;

import static android.view.View.GONE;

public class SupportActivity extends BaseActivity {

    private static final String GOOGLE_TEZ_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
    Spinner options;
    CheckBox anonymous;
    EditText email;
    TextView upi_id;
    private ActivityCheckout mCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        options = findViewById(R.id.options);
        email = findViewById(R.id.email);
        anonymous = findViewById(R.id.anonymous);
        upi_id = findViewById(R.id.upi_id);
        ArrayList<String> donations = new ArrayList<>();
        donations.add("Rs 10");
        donations.add("Rs 50");
        donations.add("Rs 100");
        donations.add("Rs 200");
        donations.add("Rs 500");
        ArrayAdapter<String> optionsAdapter = new ArrayAdapter<>(this, R.layout.spinner_item1, donations);
        optionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        options.setAdapter(optionsAdapter);
        final Billing billing = BillingItem.get(this).getBilling();
        mCheckout = Checkout.forActivity(this, billing);
        mCheckout.start();
        mCheckout.loadInventory(Inventory.Request.create().loadAllPurchases(), new InventoryCallback());
        findViewById(R.id.buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donateMoney();
            }
        });

        upi_id.setText(String.format("UPI ID : %s", getString(R.string.upi_id)));
        anonymous.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.email_container).setVisibility(GONE);
                } else {
                    findViewById(R.id.email_container).setVisibility(View.VISIBLE);
                }
            }
        });
        findViewById(R.id.kofi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri =
                            new Uri.Builder()
                                    .scheme("upi")
                                    .authority("pay")
                                    .appendQueryParameter("pa", getString(R.string.upi_id))
                                    .appendQueryParameter("pn", "Rajkumar S")
                                    .appendQueryParameter("tn", "Donation to Amrita Repository")
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
            }
        });
    }

    private void donateMoney() {
        if (!anonymous.isChecked() && email.getText().toString().isEmpty()) {
            Utils.showToast(getBaseContext(), "Please fill in your email (or) choose anonymous if you don't wish to!");
            return;
        }
        String donation = "donation";
        switch (options.getSelectedItemPosition()) {
            case 0:
                donation = "donation";
                break;
            case 1:
                donation = "donation50";
                break;
            case 2:
                donation = "donation100";
                break;
            case 3:
                donation = "donation200";
                break;
            case 4:
                donation = "donation500";
                break;
        }
        mCheckout.startPurchaseFlow(ProductTypes.IN_APP, donation, null, new PurchaseListener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCheckout.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mCheckout.stop();
        super.onDestroy();
    }


    private void showCelebs() {
        final Dialog thanksGiving = new Dialog(SupportActivity.this);
        thanksGiving.setContentView(R.layout.thanks_dialog);
        TextView textView = thanksGiving.findViewById(R.id.update_text);
        textView.setText(Html.fromHtml("Thanks for donating to Amrita Repository! <br>People like you keep me motivated to do much for the society. &hearts;"));
        thanksGiving.show();
        thanksGiving.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
    }

    public void copyUPI(View view) {
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Source Text", "rajkumaar2304@oksbi");
        clipboardManager.setPrimaryClip(clipData);
        Utils.showToast(this, "UPI ID copied to clipboard");
    }

    private class PurchaseListener extends EmptyRequestListener<Purchase> {
        @Override
        public void onSuccess(@NonNull Purchase purchase) {
            showCelebs();
            if (!anonymous.isChecked()) {
                Bundle params = new Bundle();
                params.putString("Email", email.getText().toString());
                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(SupportActivity.this);
                mFirebaseAnalytics.logEvent("Donations", params);
            }
        }


        @Override
        public void onError(int response, @NonNull Exception e) {
            Utils.showToast(getBaseContext(), e.getLocalizedMessage());
            e.printStackTrace();
            super.onError(response, e);
        }
    }

    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(@NonNull Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.IN_APP);
            if (!product.supported) {
                finish();
                Utils.showToast(getBaseContext(), "Sorry. It is not supported for your device yet.");
            }
        }
    }
}
