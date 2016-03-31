package com.thecn.app.models.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model for "instructor users" construct in server's json.
 */
public class InstructorUsers implements Serializable {

    @SerializedName("instructors")
    private ArrayList<Instructor> instructors;

    public ArrayList<Instructor> getInstructors() {
        return instructors;
    }

    public void setInstructors(ArrayList<Instructor> instructors) {
        this.instructors = instructors;
    }

}
