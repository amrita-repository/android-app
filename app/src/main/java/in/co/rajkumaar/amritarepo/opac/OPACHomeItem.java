/*
 * Copyright (c) 2019 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.opac;

import com.joanzapata.iconify.fonts.FontAwesomeIcons;

public class OPACHomeItem {
    private final String color;
    private final String name;
    private FontAwesomeIcons image;

    OPACHomeItem(String color, String name, FontAwesomeIcons imageID) {
        this.color = color;
        this.name = name;
        this.image = imageID;
    }

    public String getName() {
        return name;
    }

    public FontAwesomeIcons getImage() {
        return image;
    }

    public void setImage(FontAwesomeIcons image) {
        this.image = image;
    }

    public String getColor() {
        return color;
    }
}
