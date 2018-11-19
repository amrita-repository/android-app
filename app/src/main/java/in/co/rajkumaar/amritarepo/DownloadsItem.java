package in.co.rajkumaar.amritarepo;

import android.widget.CheckBox;

import java.io.File;

public class DownloadsItem {

    private File title;
    private String size;
    private boolean checkBox;

    DownloadsItem(File title,String size,boolean checkBox){
        this.title=title;
        this.checkBox=checkBox;
        this.size=size;
    }

    File getTitle() {
        return title;
    }

    boolean getCheckBox() {
        return checkBox;
    }

    public String getSize() {
        return size;
    }

    public void setCheckBox(boolean t){
        checkBox=t;
    }
}
