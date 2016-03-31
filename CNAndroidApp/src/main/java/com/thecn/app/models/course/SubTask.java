package com.thecn.app.models.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SubTask implements Serializable {
    @SerializedName("description")
    private String description;

    @SerializedName("display_start_time")
    private String startTime;

    @SerializedName("display_end_time")
    private String endTime;

    @SerializedName("title")
    private String title;

    private int subTaskSequence;

    public int getSubTaskSequence() {
        return subTaskSequence;
    }

    public void setSubTaskSequence(int subTaskSequence) {
        this.subTaskSequence = subTaskSequence;
    }
    /*
    @SerializedName("display_text")
    private String displayText;
    */

    @Override
    public String toString() {
        return "SubTask{" +
                "description='" + description + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", title='" + title + '\'' +
                ", displayText='" + displayText + '\'' +
                ", id='" + id + '\'' +
                ", formattedTitle=" + formattedTitle +
                ", formattedContent=" + formattedContent +
                ", unformattedContent=" + unformattedContent +
                ", loadingState=" + loadingState +
                '}';
    }

    @SerializedName("text")
    private String displayText;

    @SerializedName("id")
    private String id;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private transient CharSequence formattedTitle;

    private transient CharSequence formattedContent;
    private transient CharSequence unformattedContent;

    public static enum LoadingState {
        NOT_SET, LOADING, DONE_LOADING
    }

    private transient LoadingState loadingState = LoadingState.NOT_SET;

    public LoadingState getLoadingState() {
        return loadingState;
    }

    public void setLoadingState(LoadingState loadingTask) {
        this.loadingState = loadingTask;
    }

    public CharSequence getFormattedTitle() {
        return formattedTitle;
    }

    public CharSequence getUnformattedContent() {
        return unformattedContent;
    }

    public void setUnformattedContent(CharSequence unformattedContent) {
        this.unformattedContent = unformattedContent;
    }

    public void setFormattedTitle(CharSequence formattedTitle) {
        this.formattedTitle = formattedTitle;
    }

    public CharSequence getFormattedContent() {
        return formattedContent;
    }

    public void setFormattedContent(CharSequence formattedContent) {
        this.formattedContent = formattedContent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}