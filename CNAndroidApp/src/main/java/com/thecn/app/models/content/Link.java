package com.thecn.app.models.content;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for link construct in server json.
 * Display url is what should be displayed to the user, while
 * View url may redirect through the server for analytics.
 */
public class Link implements Serializable {

    @SerializedName("display_url")
    private String displayUrl;

    @SerializedName("view_url")
    private String viewUrl;

    public String getDisplayUrl() {
        return displayUrl;
    }

    public void setDisplayUrl(String displayUrl) {
        this.displayUrl = displayUrl;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }
}
