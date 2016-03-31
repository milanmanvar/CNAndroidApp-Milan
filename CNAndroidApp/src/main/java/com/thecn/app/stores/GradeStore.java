package com.thecn.app.stores;

import android.util.Log;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.thecn.app.models.course.Course;
import com.thecn.app.tools.network.GlobalGson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Used to make network calls for {@link Course}s to server.
 * Also has methods for converting json data from server to Java models.
 */
public class GradeStore extends BaseStore {

    public static final String TAG = GradeStore.class.getSimpleName();

    private static final String COMMON_COURSE_PARAMS = "with_course_school=1&with_course_country=1"
            + "&with_course_tasks=1&with_course_country=1&with_course_instructor_users=1"
            + "&with_course_user_score=1&with_course_score=1&with_course_score_expected_today=1"
            + "&with_course_score_setting=1&with_course_most_course_score_users=1"
            + "&with_course_user_model=1&with_course_count=1&with_course_least_course_score_users=1";

    /**
     * Construct course model from json
     *
     * @param response data from server
     * @return course model or null if error
     */
    public static Course getData(JSONObject response) {
        Log.e("Course response:", "" + response);
        try {
            return fromJSON(response.getJSONObject("data"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct list of Course models from json
     *
     * @param response data from server
     * @return list of course models or null if error
     */
    public static ArrayList<Course> getListData(JSONObject response) {
        try {
            return fromJSON(response.getJSONArray("data"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get course model from json using Gson
     *
     * @param json data from server
     * @return course model or null if error
     */
    public static Course fromJSON(JSONObject json) {

        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(json.toString(), Course.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get list of course models from json
     *
     * @param jsonArray data from server
     * @return list of course models or null if error
     */
    public static ArrayList<Course> fromJSON(JSONArray jsonArray) {
        ArrayList<Course> courses = new ArrayList<Course>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Course course = fromJSON(jsonArray.getJSONObject(i));
                if (course != null) {
                    courses.add(course);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (Exception e) {
                //do nothing
            }
        }

        return courses.size() > 0 ? courses : null;
    }

    public static void getAllGradebookData(String courseId, final ResponseCallback callback) {
        api("/user_course_gradebook_item/?course_id=" + courseId + "&with_user_course_gradebook_item_category=1&with_user_course_gradebook_item_grade_info=1&with_course_gradebook_item_average_grade=1&with_user_course_gradebook_item_comments=1&with_course_gradebook_category_average_grade_percentage=1&with_user_course_gradebook_item_dropbox_items=1&with_course_gradebook_item_grade_publish_setting=1&limit=999", Request.Method.GET, callback);
    }

    public static void getGradebookInfo(String courseId, final ResponseCallback callback) {
        api("/user_course_gradebook_grade_info/" + courseId, Request.Method.GET, callback);
    }
}
