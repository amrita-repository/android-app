package in.co.rajkumaar.amritarepo.helpers;

import android.app.Activity;
import android.app.Application;
import androidx.annotation.NonNull;
import android.widget.Toast;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.PlayStoreListener;

import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;

public class BillingItem extends Application {


    private final Billing mBilling = new Billing(this, new Billing.DefaultConfiguration() {
        @NonNull
        @Override
        public String getPublicKey() {
            return BuildConfig.PLAY_LICENSE_KEY;
        }
    });

    public static BillingItem get(Activity activity) {
        return (BillingItem) activity.getApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBilling.addPlayStoreListener(new PlayStoreListener() {
            @Override
            public void onPurchasesChanged() {
                Toast.makeText(BillingItem.this, R.string.app_name, Toast.LENGTH_LONG).show();
            }
        });
    }

    @NonNull
    public Billing getBilling() {
        return mBilling;
    }
}
