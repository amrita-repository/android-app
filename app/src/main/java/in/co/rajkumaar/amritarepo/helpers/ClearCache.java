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

            deleteRecursiveFolders(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void deleteRecursiveFolders(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (String child : fileOrDirectory.list())
                deleteRecursiveFolders(new File(fileOrDirectory, child));
        }
        if (!(fileOrDirectory.getName().equals(".AmritaRepoCache")))
            fileOrDirectory.delete();
    }
}