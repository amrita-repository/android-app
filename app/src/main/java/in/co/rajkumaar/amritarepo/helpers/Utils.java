/*
 * MIT License
 *
 * Copyright (c) 2018  RAJKUMAR S
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package in.co.rajkumaar.amritarepo.helpers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

import in.co.rajkumaar.amritarepo.R;

public class Utils {

    public static boolean isConnected(Context context) {
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void showSnackBar(Context context, String message) {
        View parentLayout = ((Activity) context).findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar
                .make(parentLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public static void hideKeyboard(Context context) {
        InputMethodManager inputManager = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager.isAcceptingText())
            inputManager.hideSoftInputFromWindow(Objects.requireNonNull(((Activity) context).getCurrentFocus()).getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showUnexpectedError(Context context) {
        showToast(context, "An unexpected error occurred. Please try again later.");
    }

    public static void showInternetError(Context context) {
        showSnackBar(context, "Connection unavailable. Please connect to internet");
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private static void showAd(final Context context, LinearLayout linearLayout, AdSize adSize) {
        AudienceNetworkAds.initialize(context);
        AdView adView = new AdView(context, context.getString(R.string.placement_id), adSize);
        linearLayout.addView(adView);
        adView.loadAd();
    }

    public static void showSmallAd(Context context, LinearLayout linearLayout) {
        showAd(context, linearLayout, AdSize.BANNER_HEIGHT_50);
    }

    public static void showBigAd(Context context, LinearLayout linearLayout) {
        showAd(context, linearLayout, AdSize.BANNER_HEIGHT_90);
    }

    public static ArrayList<String> getAcademicYears() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayList<String> years = new ArrayList<>();
        years.add("[Choose year]");
        years.add((currentYear - 4) + "_" + String.valueOf(currentYear - 3).substring(2, 4));
        years.add((currentYear - 3) + "_" + String.valueOf(currentYear - 2).substring(2, 4));
        years.add((currentYear - 2) + "_" + String.valueOf(currentYear - 1).substring(2, 4));
        years.add((currentYear - 1) + "_" + String.valueOf(currentYear).substring(2, 4));
        years.add((currentYear) + "_" + String.valueOf(currentYear + 1).substring(2, 4));
        years.add((currentYear + 1) + "_" + String.valueOf(currentYear + 2).substring(2, 4));
        return years;
    }

    public static void printAAID(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                if (Looper.myLooper() == Looper.getMainLooper())
                    throw new IllegalStateException("Cannot be called from the main thread");

                try {
                    PackageManager pm = context.getPackageManager();
                    pm.getPackageInfo("com.android.vending", 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                AdvertisingConnection connection = new AdvertisingConnection();
                Intent intent = new Intent("com.google.android.gms.ads.identifier.service.START");
                intent.setPackage("com.google.android.gms");
                if (context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
                    try {
                        AdvertisingInterface adInterface = new AdvertisingInterface(connection.getBinder());
                        AdInfo adInfo = new AdInfo(adInterface.getId(), adInterface.isLimitAdTrackingEnabled(true));
                        Log.v("ADS-ID", adInfo.getId());
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    } finally {
                        context.unbindService(connection);
                    }
                }
            }
        }).start();
    }

    public static final class AdInfo {
        private final String advertisingId;
        private final boolean limitAdTrackingEnabled;

        AdInfo(String advertisingId, boolean limitAdTrackingEnabled) {
            this.advertisingId = advertisingId;
            this.limitAdTrackingEnabled = limitAdTrackingEnabled;
        }

        public String getId() {
            return this.advertisingId;
        }

        public boolean isLimitAdTrackingEnabled() {
            return this.limitAdTrackingEnabled;
        }
    }

    private static final class AdvertisingConnection implements ServiceConnection {
        private final LinkedBlockingQueue<IBinder> queue = new LinkedBlockingQueue<IBinder>(1);
        boolean retrieved = false;

        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                this.queue.put(service);
            } catch (InterruptedException localInterruptedException) {
            }
        }

        public void onServiceDisconnected(ComponentName name) {
        }

        public IBinder getBinder() throws InterruptedException {
            if (this.retrieved) throw new IllegalStateException();
            this.retrieved = true;
            return (IBinder) this.queue.take();
        }
    }

    private static final class AdvertisingInterface implements IInterface {
        private IBinder binder;

        public AdvertisingInterface(IBinder pBinder) {
            binder = pBinder;
        }

        public IBinder asBinder() {
            return binder;
        }

        public String getId() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            String id;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                binder.transact(1, data, reply, 0);
                reply.readException();
                id = reply.readString();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return id;
        }

        public boolean isLimitAdTrackingEnabled(boolean paramBoolean) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            boolean limitAdTracking;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                data.writeInt(paramBoolean ? 1 : 0);
                binder.transact(2, data, reply, 0);
                reply.readException();
                limitAdTracking = 0 != reply.readInt();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return limitAdTracking;
        }
    }
}
