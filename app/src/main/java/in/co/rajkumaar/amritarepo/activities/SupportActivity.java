package in.co.rajkumaar.amritarepo.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.ParticleSystem;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class SupportActivity extends AppCompatActivity {

    private ActivityCheckout mCheckout;
    Spinner options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        options = findViewById(R.id.options);
        ArrayList<String> donations = new ArrayList<>();
        donations.add("Rs 10");
        donations.add("Rs 50");
        donations.add("Rs 100");
        donations.add("Rs 500");
        ArrayAdapter<String> optionsAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item1, donations);
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
    }

    private void donateMoney() {
        String donation = "donation";
        switch (options.getSelectedItemPosition()){
            case 0 : donation = "donation"; break;
            case 1 : donation = "donation50"; break;
            case 2 : donation = "donation100"; break;
            case 3 : donation = "donation500"; break;
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

    private class PurchaseListener extends EmptyRequestListener<Purchase> {
        @Override
        public void onSuccess(@NonNull Purchase purchase) {
            showCelebs();
            Log.i("RES",purchase.data);
        }


        @Override
        public void onError(int response, @NonNull Exception e) {
            Utils.showToast(getBaseContext(),e.getMessage());
            super.onError(response, e);
        }
    }

    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(@NonNull Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.IN_APP);
            if (!product.supported) {
                finish();
                Utils.showToast(getBaseContext(),"Sorry. It is not supported for you yet.");
            }
        }
    }

    private void showCelebs(){
        final Dialog thanksGiving = new Dialog(SupportActivity.this);
        thanksGiving.setContentView(R.layout.thanks_dialog);
        TextView textView = thanksGiving.findViewById(R.id.update_text);
        textView.setText(Html.fromHtml("Thanks for donating to Amrita Repository! <br>People like you keep me motivated to do much for the society. &hearts;"));
        thanksGiving.show();
    }
}
