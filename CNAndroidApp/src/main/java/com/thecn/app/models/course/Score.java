package com.thecn.app.models.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for score construct in server json.
 * Represents number of anar seeds in course, as well as average scores.
 */
public class Score implements Serializable {

    @SerializedName("total")
    private double total;

    @SerializedName("average")
    private int average;

    @SerializedName("average_student")
    private int studentAverage;

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getAverage() {
        return average;
    }

    public void setAverage(int average) {
        this.average = average;
    }

    public int getStudentAverage() {
        return studentAverage;
    }

    public void setStudentAverage(int studentAverage) {
        this.studentAverage = studentAverage;
    }
}
