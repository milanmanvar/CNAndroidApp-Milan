package com.thecn.app.models.profile;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for time zone construct in server json
 */
public class TimeZone implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("zone")
    private String zone;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        return zone;
    }
}
