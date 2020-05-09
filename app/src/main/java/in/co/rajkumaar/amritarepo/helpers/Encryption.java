/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;


public class Encryption {
    public static SharedPreferences getEncPrefs(Context context, String prefName) {
        SharedPreferences pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                pref = EncryptedSharedPreferences.create(
                        prefName,
                        masterKeyAlias,
                        context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        }
        return pref;
    }
}
