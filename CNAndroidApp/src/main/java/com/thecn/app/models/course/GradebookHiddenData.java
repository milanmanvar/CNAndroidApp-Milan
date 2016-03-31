package com.thecn.app.models.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model for "instructor users" construct in server's json.
 */
public class GradebookHiddenData implements Serializable {

    @SerializedName("course_sidebar_control_gradebook")
    private boolean courseSidebarControlGradebook;

    public boolean isCourseSidebarControlGradebook() {
        return courseSidebarControlGradebook;
    }

    public void setCourseSidebarControlGradebook(boolean courseSidebarControlGradebook) {
        this.courseSidebarControlGradebook = courseSidebarControlGradebook;
    }
}
