package com.thecn.app.models.profile;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for language construct in server json
 */
public class Language implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

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

    public String toString() {
        return name;
    }
}
