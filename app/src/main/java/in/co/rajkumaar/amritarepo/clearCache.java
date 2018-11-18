package in.co.rajkumaar.amritarepo;

import android.os.Environment;

import java.io.File;

public class clearCache {
    public void clear()
    {
            try {
                final String dirPath = Environment.getExternalStorageDirectory() + "/.AmritaRepoCache";
                File dir = new File(dirPath);
                final String dirPathOld = Environment.getExternalStorageDirectory() + "/AmritaRepoCache";
                File dirOld = new File(dirPathOld);
                if (dirOld.exists())
                    dirOld.delete();

                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

    }
}
