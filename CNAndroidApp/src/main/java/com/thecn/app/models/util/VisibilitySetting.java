package com.thecn.app.models.util;

import com.thecn.app.R;

import java.util.ArrayList;

/**
 * Class used to represent visibility setting.
 * Used to represent json string from server as one of 8 integers.
 * Use these integer ids to quickly get a display string, get an icon associated with the setting,
 * compare settings, or get the json string back again.
 */
public class VisibilitySetting {
    public static final int PUBLIC = 0;
    public static final int ONLY_ME = 1;
    public static final int MY_COLLEAGUE = 2;
    public static final int MY_FOLLOWER = 3;
    public static final int SIMILAR_COURSES = 4;
    public static final int ALL_MY_COURSES = 5;
    public static final int ALL_MY_CONEXUS = 6;
    public static final int CLASSMATE = 7;
    public static final int INSTRUCTOR = 8;

    public static final String PUBLIC_DISPLAY = "Public";
    public static final String ONLY_ME_DISPLAY = "Only Me";
    public static final String MY_COLLEAGUE_DISPLAY = "My Colleagues";
    public static final String MY_FOLLOWER_DISPLAY = "My Followers";
    public static final String SIMILAR_COURSES_DISPLAY = "Similar to my Courses";
    public static final String ALL_MY_COURSES_DISPLAY = "My Courses";
    public static final String ALL_MY_CONEXUS_DISPLAY = "My Conexus";
    public static final String CLASSMATE_DISPLAY = "Classmates";
    public static final String INSTRUCTOR_DISPLAY = "Instructor";

    public static final String PUBLIC_JSON = "public";
    public static final String ONLY_ME_JSON = "only_me";
    public static final String MY_COLLEAGUE_JSON = "my_colleague";
    public static final String MY_FOLLOWER_JSON = "my_follower";
    public static final String SIMILAR_COURSES_JSON = "similar_courses";
    public static final String ALL_MY_COURSES_JSON = "all_my_courses";
    public static final String ALL_MY_CONEXUS_JSON = "all_my_conexus";
    public static final String CLASSMATE_JSON = "is_course_student";
    public static final String INSTRUCTOR_JSON = "is_course_instructor";

    private int mVisibility;

    /**
     * Construct new instance using integer identifier
     * @param visibility identifier
     */
    public VisibilitySetting(int visibility) {
        if (0 <= visibility && visibility <= 8) {
            mVisibility = visibility;
        } else throw new IllegalStateException("Visibility is represented by an int from 0 to 8");
    }

    public int getType() {
        return mVisibility;
    }

    /**
     * For the integer given, get the display string for this setting.
     * @return
     */
    @Override
    public String toString() {
        switch (mVisibility) {
            case PUBLIC:
                return PUBLIC_DISPLAY;
            case ONLY_ME:
                return ONLY_ME_DISPLAY;
            case MY_COLLEAGUE:
                return MY_COLLEAGUE_DISPLAY;
            case MY_FOLLOWER:
                return MY_FOLLOWER_DISPLAY;
            case SIMILAR_COURSES:
                return SIMILAR_COURSES_DISPLAY;
            case ALL_MY_COURSES:
                return ALL_MY_COURSES_DISPLAY;
            case ALL_MY_CONEXUS:
                return ALL_MY_CONEXUS_DISPLAY;
            case CLASSMATE:
                return CLASSMATE_DISPLAY;
            case INSTRUCTOR:
                return INSTRUCTOR_DISPLAY;
            default:
                return null;
        }
    }

    /**
     * Construct new visibility setting from a json string.
     * @param json json string that specifies visibility setting
     * @return new instance of this class
     */
    public static VisibilitySetting fromJsonString(String json) {
        if (json == null || json.isEmpty()) return new VisibilitySetting(PUBLIC);

        switch (json) {
            case ONLY_ME_JSON:
                return new VisibilitySetting(ONLY_ME);
            case INSTRUCTOR_JSON:
                return new VisibilitySetting(INSTRUCTOR);
            case CLASSMATE_JSON:
                return new VisibilitySetting(CLASSMATE);
            default:
                return new VisibilitySetting(PUBLIC);
        }
    }

    /**
     * Get representation as json string for sending to CN server.
     * @return json string
     */
    public String toJsonString() {
        switch (mVisibility) {
            case PUBLIC:
                return PUBLIC_JSON;
            case ONLY_ME:
                return ONLY_ME_JSON;
            case MY_COLLEAGUE:
                return MY_COLLEAGUE_JSON;
            case MY_FOLLOWER:
                return MY_FOLLOWER_JSON;
            case SIMILAR_COURSES:
                return SIMILAR_COURSES_JSON;
            case ALL_MY_COURSES:
                return ALL_MY_COURSES_JSON;
            case ALL_MY_CONEXUS:
                return ALL_MY_CONEXUS_JSON;
            case CLASSMATE:
                return CLASSMATE_JSON;
            case INSTRUCTOR:
                return INSTRUCTOR_JSON;
            default:
                return null;
        }
    }

    /**
     * Get icon id associated with this visibility setting
     * @return id of appropriate icon
     */
    public int getIconResource() {
        switch (mVisibility) {
            case VisibilitySetting.ONLY_ME:
                return R.drawable.ic_lock;
            case VisibilitySetting.INSTRUCTOR:
                return R.drawable.ic_letter_i;
            case VisibilitySetting.CLASSMATE:
                return R.drawable.ic_letter_c;
            default:
                return R.drawable.ic_globe_gray;
        }
    }

    /**
     * Get list of visibility settings
     * @return list of visibility settings
     */
    public static ArrayList<VisibilitySetting> getList() {
        ArrayList<VisibilitySetting> list = new ArrayList<VisibilitySetting>();
        list.add(new VisibilitySetting(PUBLIC));
        list.add(new VisibilitySetting(CLASSMATE));
        list.add(new VisibilitySetting(INSTRUCTOR));
        list.add(new VisibilitySetting(ONLY_ME));
        return list;
    }
}
