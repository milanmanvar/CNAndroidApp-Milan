package com.thecn.app.models.conexus;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model of Conexus object, found in server's json
 */
public class Conexus implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("conexus_id")
    private String conexusNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("about")
    private String about;

    @SerializedName("display_time")
    private String displayTime;

    @SerializedName("logo_url")
    private String logoURL;

    @SerializedName("user_position")
    private String userPosition;

    @SerializedName("user_score")
    private UserScore userScore;

    public Conexus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConexusNumber() {
        return conexusNumber;
    }

    public void setConexusNumber(String conexusNumber) {
        this.conexusNumber = conexusNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    public String getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(String userPosition) {
        this.userPosition = userPosition;
    }

    public UserScore getUserScore() {
        return userScore;
    }

    public void setUserScore(UserScore userScore) {
        this.userScore = userScore;
    }

    /**
     * Gets all conexus ids of conexuses in the list.
     * @param list conexuses to get ids from
     * @return array of all conexus ids
     */
    public static String[] getIds(ArrayList<Conexus> list) {
        if (list == null) return null;

        String[] ids = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            Conexus conexus = list.get(i);
            if (conexus != null) {
                ids[i] = conexus.getId();
            }
        }

        return ids;
    }
}
