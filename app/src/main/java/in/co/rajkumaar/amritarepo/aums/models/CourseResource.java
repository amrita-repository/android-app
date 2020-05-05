/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.models;

public class CourseResource {
    private String resourceUrl;
    private String resourceFileName;
    private String type;

    public CourseResource(String resourceFileName, String resourceUrl, String type) {
        this.resourceFileName = resourceFileName;
        this.resourceUrl = resourceUrl;
        this.type = type;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public String getResourceFileName() {
        return resourceFileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}