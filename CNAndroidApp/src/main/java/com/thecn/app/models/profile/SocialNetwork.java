package com.thecn.app.models.profile;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for social network construct in server json
 */
public class SocialNetwork implements Serializable {
    @SerializedName("name")
    private String name;

    @SerializedName("link")
    private String link;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
