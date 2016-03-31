package com.thecn.app.models.content;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for content count json object.
 * Counts the number of times an action has been performed on content.
 */
public class ContentCount implements Serializable {

    @SerializedName("good")
    private int likes;

    @SerializedName("comment")
    private int reflections;

    @SerializedName("remember")
    private int remembers;

    @SerializedName("repost")
    private int reposts;

    @SerializedName("view")
    private int views;

    @SerializedName("best")
    private int bestCount;

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getReflections() {
        return reflections;
    }

    public void setReflections(int reflections) {
        this.reflections = reflections;
    }

    public int getRemembers() {
        return remembers;
    }

    public void setRemembers(int remembers) {
        this.remembers = remembers;
    }

    public int getReposts() {
        return reposts;
    }

    public void setReposts(int reposts) {
        this.reposts = reposts;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getBestCount() {
        return bestCount;
    }

    public void setBestCount(int bestCount) {
        this.bestCount = bestCount;
    }
}