package com.thecn.app.models.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for instructor construct in server json.
 */
public class Instructor implements Serializable {
    @SerializedName("cn_number")
    private String cnNumber;

    @SerializedName("id")
    private String id;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("display_login_time")
    private String displayLoginTime;

    public String getCnNumber() {
        return cnNumber;
    }

    public void setCnNumber(String cnNumber) {
        this.cnNumber = cnNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayLoginTime() {
        return displayLoginTime;
    }

    public void setDisplayLoginTime(String displayLoginTime) {
        this.displayLoginTime = displayLoginTime;
    }
}
