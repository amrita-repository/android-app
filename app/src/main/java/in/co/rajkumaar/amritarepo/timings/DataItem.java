/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.timings;

public class DataItem {

    public String name = "";
    public String days = "";
    public String dep = "";
    public String from = "";
    public String to = "";
    public String type = "";

    public DataItem() {

    }

    public DataItem(String name, String days, String departure, String from, String to, String type) {
        this.name = name;
        this.days = days;
        this.dep = departure;
        this.from = from;
        this.to = to;
        this.type = type;
    }
}