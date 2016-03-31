package com.thecn.app.models.user;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for user position construct in server json
 */
public class UserPosition implements Serializable{

    @SerializedName("position")
    private String position;

    @SerializedName("school_name")
    private String schoolName;

    @SerializedName("type")
    private String type;

    @SerializedName("web_address")
    private String webAddress;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    public UserPosition(String position, String schoolName, String type, String webAddress) {
        this.position = position;
        this.schoolName = schoolName;
        this.type = type;
        this.webAddress = webAddress;
    }
}
