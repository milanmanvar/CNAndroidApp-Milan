package com.thecn.app.models.profile;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for country construct in server json
 */
public class Country implements Serializable {

    @SerializedName("flag_url")
    private String flagURL;

    @SerializedName("id")
    private String id;

    //long name
    @SerializedName("name")
    private String name;

    @SerializedName("short_name")
    private String shortName;

    @Override
    public String toString() {
        return name;
    }

    public String getFlagURL() {
        return flagURL;
    }

    public void setFlagURL(String flagURL) {
        this.flagURL = flagURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
