/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.helpers;

import android.content.Context;

import java.io.File;

public class ClearCache {
    public void clear(Context context) {
        try {
            final String dirPath = context.getExternalFilesDir(null) + "/.AmritaRepoCache";
            File dir = new File(dirPath);
            final String dirPathOld = context.getExternalFilesDir(null) + "/AmritaRepoCache";
            File dirOld = new File(dirPathOld);
            if (dirOld.exists())
                dirOld.delete();

            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
