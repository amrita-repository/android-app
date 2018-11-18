package in.co.rajkumaar.amritarepo;

import android.os.Environment;

class CheckForSDCard {
    //Check If SD Card is present or not method
    boolean isSDCardPresent() {
        return (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
    }
}