package com.thecn.app.models.course;

import java.io.Serializable;

/**
 * Created by utsav.k on 04-02-2016.
 */
public class CourseTaskLinkDetail implements Serializable {

    private String id,courseId,text,title;
    private double cTime;

    public double getcTime() {
        return cTime;
    }

    public void setcTime(double cTime) {
        this.cTime = cTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
