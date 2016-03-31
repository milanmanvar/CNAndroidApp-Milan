package com.thecn.app.models.course;

import com.google.gson.annotations.SerializedName;
import com.thecn.app.models.user.User;

import java.io.Serializable;

/**
 * Model for score user construct in server json
 * Includes a user and additional information related to a user's score.
 */
public class ScoreUser implements Serializable {

    @SerializedName("has_flag")
    private boolean hasFlag;

    @SerializedName("id")
    private String id;

    @SerializedName("model")
    private User model;

    @SerializedName("score")
    private UserScore userScore;

    @SerializedName("user_position")
    private String userPosition;

    @SerializedName("user_type")
    private String userType;

    public ScoreUser(String id) {
        this.id = id;
    }

    public boolean isHasFlag() {
        return hasFlag;
    }

    public void setHasFlag(boolean hasFlag) {
        this.hasFlag = hasFlag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getModel() {
        return model;
    }

    public void setModel(User model) {
        this.model = model;
    }

    public UserScore getUserScore() {
        return userScore;
    }

    public void setUserScore(UserScore userScore) {
        this.userScore = userScore;
    }

    public String getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(String userPosition) {
        this.userPosition = userPosition;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
