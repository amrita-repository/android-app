/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.downloads.models;

public class DownloadsItem {

    private String filePath;
    private String size;
    private boolean checkBox;

    public DownloadsItem(String filePath, String size, boolean checkBox) {
        this.filePath = filePath;
        this.checkBox = checkBox;
        this.size = size;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(boolean t) {
        checkBox = t;
    }

    public String getSize() {
        return size;
    }
}
