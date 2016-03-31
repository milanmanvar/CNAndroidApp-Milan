package com.thecn.app.models.content;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for website construct in server's json
 */
public class Website implements Serializable {
    @SerializedName("link")
    private String link;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
