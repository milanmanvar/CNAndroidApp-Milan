package com.thecn.app.models.profile;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for institution construct in server json.
 * This is used in a user's profile (a list of institutions they
 * are a part of is shown just under the profile picture.
 */
public class Institution implements Serializable {

    @SerializedName("comment")
    private String comment;

    @SerializedName("link")
    private String link;

    @SerializedName("logo")
    private String logoURL;

    @SerializedName("name")
    private String name;

    @SerializedName("time_end")
    private String timeEnd;

    @SerializedName("time_start")
    private String timeStart;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }
}
