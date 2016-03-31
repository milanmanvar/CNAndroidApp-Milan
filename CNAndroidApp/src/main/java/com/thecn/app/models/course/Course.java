package com.thecn.app.models.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model for course construct in server json
 */
public class Course implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("course_id")
    private String courseNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("about")
    private String about;

    @SerializedName("display_time")
    private String displayTime;

    @SerializedName("logo_url")
    private String logoURL;

    @SerializedName("type")
    private String type;

    @SerializedName("user_position")
    private String userPosition;

    @SerializedName("is_start")
    private boolean isStart;

    @SerializedName("is_end")
    private boolean isEnd;

    @SerializedName("tasks")
    private ArrayList<com.thecn.app.models.course.Task> tasks;

    @SerializedName("school")
    private com.thecn.app.models.course.CourseSchool school;

    @SerializedName("instructor_users")
    private com.thecn.app.models.course.InstructorUsers instructorUsers;

    @SerializedName("score")
    private Score score;

    @SerializedName("score_expected_today")
    private int expectedScore;

    @SerializedName("score_setting")
    private com.thecn.app.models.course.ScoreSetting scoreSetting;

    @SerializedName("user_score")
    private UserScore userScore;

    @SerializedName("most_course_score_users")
    private ArrayList<com.thecn.app.models.course.ScoreUser> mostScoreUsers;

    @SerializedName("least_course_score_users")
    private ArrayList<com.thecn.app.models.course.ScoreUser> leastScoreUsers;

    @SerializedName("count")
    private Count count;

    //@SerializedName("hidden_elements")
    //private GradebookHiddenData hiddenElements;

    public Course(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(String userPosition) {
        this.userPosition = userPosition;
    }

    public boolean getIsStart() {
        return isStart;
    }

    public void setIsStart(boolean isStart) {
        this.isStart = isStart;
    }

    public boolean getIsEnd() {
        return isEnd;
    }

    public void setIsEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public ArrayList<com.thecn.app.models.course.Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<com.thecn.app.models.course.Task> tasks) {
        this.tasks = tasks;
    }

    public com.thecn.app.models.course.CourseSchool getSchool() {
        return school;
    }

    public void setSchool(com.thecn.app.models.course.CourseSchool school) {
        this.school = school;
    }


    public com.thecn.app.models.course.InstructorUsers getInstructorUsers() {
        return instructorUsers;
    }

    public void setInstructorUsers(com.thecn.app.models.course.InstructorUsers instructorUsers) {
        this.instructorUsers = instructorUsers;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public int getExpectedScore() {
        return expectedScore;
    }

    public void setExpectedScore(int expectedScore) {
        this.expectedScore = expectedScore;
    }

    public com.thecn.app.models.course.ScoreSetting getScoreSetting() {
        return scoreSetting;
    }

    public void setScoreSetting(com.thecn.app.models.course.ScoreSetting scoreSetting) {
        this.scoreSetting = scoreSetting;
    }

    public UserScore getUserScore() {
        return userScore;
    }

    public void setUserScore(UserScore userScore) {
        this.userScore = userScore;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean isStart) {
        this.isStart = isStart;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public ArrayList<com.thecn.app.models.course.ScoreUser> getMostScoreUsers() {
        return mostScoreUsers;
    }

    public void setMostScoreUsers(ArrayList<com.thecn.app.models.course.ScoreUser> mostScoreUsers) {
        this.mostScoreUsers = mostScoreUsers;
    }

    public ArrayList<com.thecn.app.models.course.ScoreUser> getLeastScoreUsers() {
        return leastScoreUsers;
    }

    public void setLeastScoreUsers(ArrayList<com.thecn.app.models.course.ScoreUser> leastScoreUsers) {
        this.leastScoreUsers = leastScoreUsers;
    }

    public Count getCount() {
        return count;
    }

    public void setCount(Count count) {
        this.count = count;
    }

    /**
     * Get ids of all courses in list
     * @param list list of courses to get ids from
     * @return string array of ids
     */
    public static String[] getIds(ArrayList<Course> list) {
        if (list == null) return null;

        String[] ids = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            Course course = list.get(i);
            if (course != null) {
                ids[i] = course.getId();
            }
        }

        return ids;
    }

//    public GradebookHiddenData getHiddenElements() {
//        return hiddenElements;
//    }
//
//    public void setHiddenElements(GradebookHiddenData hiddenElements) {
//        this.hiddenElements = hiddenElements;
//    }
}
