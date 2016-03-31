package com.thecn.app.models.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for count construct in server json.
 * Represents count of users in a course.
 */
public class Count implements Serializable {
    @SerializedName("all")
    int allMemberCount;

    @SerializedName("instructor")
    int instructorCount;

    @SerializedName("student")
    int studentCount;

    public int getAllMemberCount() {
        return allMemberCount;
    }

    public void setAllMemberCount(int allMemberCount) {
        this.allMemberCount = allMemberCount;
    }

    public int getInstructorCount() {
        return instructorCount;
    }

    public void setInstructorCount(int instructorCount) {
        this.instructorCount = instructorCount;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }
}
