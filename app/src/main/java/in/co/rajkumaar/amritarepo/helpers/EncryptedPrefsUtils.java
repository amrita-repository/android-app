/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import at.favre.lib.armadillo.Armadillo;


public class EncryptedPrefsUtils {
    public static SharedPreferences get(Context context, String prefName) {
        SharedPreferences pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Armadillo.create(context, prefName)
                        .encryptionFingerprint(context)
                        .enableKitKatSupport(true)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return pref;
    }
}
