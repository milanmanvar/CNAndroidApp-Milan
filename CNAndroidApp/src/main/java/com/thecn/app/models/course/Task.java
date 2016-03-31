package com.thecn.app.models.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Task implements Serializable {
    @SerializedName("description")
    private String description;

    @SerializedName("display_start_time")
    private String startTime;

    @SerializedName("display_end_time")
    private String endTime;

    @SerializedName("title")
    private String title;

    private int sequence;

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
    /*
    @SerializedName("display_text")
    private String displayText;
    */

    @SerializedName("text")
    private String displayText;

    @SerializedName("id")
    private String id;

    @SerializedName("default_display")
    private boolean defaultDiplay;

    public boolean isDefaultDiplay() {
        return defaultDiplay;
    }

    public void setDefaultDiplay(boolean defaultDiplay) {
        this.defaultDiplay = defaultDiplay;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private transient CharSequence formattedTitle;

    private transient CharSequence formattedContent;
    private transient CharSequence unformattedContent;
    private ArrayList<SubTask> subTasks;

    public ArrayList<SubTask> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(ArrayList<SubTask> subTasks) {
        this.subTasks = subTasks;
    }

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
    private boolean isSelectable;

    public boolean isSelectable() {
        return isSelectable;
    }

    public void setIsSelectable(boolean isSelectable) {
        this.isSelectable = isSelectable;
    }
}