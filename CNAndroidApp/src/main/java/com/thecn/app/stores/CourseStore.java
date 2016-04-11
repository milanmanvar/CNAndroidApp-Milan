package com.thecn.app.stores;

import android.util.Log;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.course.SubTask;
import com.thecn.app.models.course.Task;
import com.thecn.app.tools.network.GlobalGson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Used to make network calls for {@link com.thecn.app.models.course.Course}s to server.
 * Also has methods for converting json data from server to Java models.
 */
public class CourseStore extends BaseStore {

    public static final String TAG = CourseStore.class.getSimpleName();

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
//        Log.e("Course response:", "" + response);
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
            Log.e("Course data:", "" + response);
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

    /**
     * Construct Task model from json
     *
     * @param response data from server
     * @return new Task model or null if error
     */
    public static Task getTaskData(JSONObject response) {
        try {
            boolean success = response.getBoolean("result");

            if (success) {
                return taskFromJSON(response.getJSONObject("data"));
            }
        } catch (Exception e) {
            //proceed...
        }

        return null;
    }

    /**
     * Construct Task model from json
     *
     * @param response data from server
     * @return new Task model or null if error
     */
    public static SubTask getSubTaskData(JSONObject response) {
        try {
            boolean success = response.getBoolean("result");

            if (success) {
                return subTaskFromJSON(response.getJSONObject("data"));
            }
        } catch (Exception e) {
            //proceed...
        }

        return null;
    }

    /**
     * Construct list of Task models from json
     *
     * @param response data from server
     * @return new list of Task models or null if error
     */
    public static ArrayList<Task> getTaskListData(JSONObject response) {
        try {
            return tasksFromJSON(response.getJSONArray("data"));
        } catch (Exception e) {
            return null;
        }
    }

    public static ArrayList<SubTask> getSubTaskListData(JSONObject response) {
        try {
            return subTasksFromJSON(response.getJSONArray("data"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get task model from json
     *
     * @param json data from server
     * @return new Task model or null if error
     */
    public static Task taskFromJSON(JSONObject json) {
        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(json.toString(), Task.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static SubTask subTaskFromJSON(JSONObject json) {
        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(json.toString(), SubTask.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get list of Task models from json
     *
     * @param jsonArray data from server
     * @return list of Task models or null if error
     */
    public static ArrayList<Task> tasksFromJSON(JSONArray jsonArray) {
        ArrayList<Task> tasks = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Task task = taskFromJSON(jsonArray.getJSONObject(i));
                if (task != null) {
                    tasks.add(task);
                } else {
                    StoreUtil.logNullAtIndex(TAG + " Task", i);
                }
            } catch (Exception e) {
                //do nothing
            }
        }

        return tasks.size() > 0 ? tasks : null;
    }

    public static ArrayList<SubTask> subTasksFromJSON(JSONArray jsonArray) {
        ArrayList<SubTask> tasks = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                SubTask task = subTaskFromJSON(jsonArray.getJSONObject(i));
                if (task != null) {
                    tasks.add(task);
                } else {
                    StoreUtil.logNullAtIndex(TAG + " Task", i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return tasks;
    }

    /**
     * Get course from server by id
     *
     * @param courseId id of course
     * @param callback code to run on return
     */
    public static void getCourseById(String courseId, final ResponseCallback callback) {
        String query = "/course/" + courseId + "?" + COMMON_COURSE_PARAMS;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get course from server by number
     *
     * @param courseNumber number of course
     * @param callback     code to run on return
     */
    public static void getCourseByNumber(String courseNumber, final ResponseCallback callback) {
        String query = "/course_number/" + courseNumber + "?" + COMMON_COURSE_PARAMS;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get task data from server by id
     *
     * @param id       id of task
     * @param callback code to run on return
     */
    public static void getTaskDetails(String id, final ResponseCallback callback) {
        String query = "/task_details/" + id + "?with_content_current_user_is_observer=1" +
                "&with_content_comment_attachments=1&with_content_comment_pictures=1&with_content_subtask";

        api(query, Request.Method.GET, callback);
    }

    public static void getSubTaskDetails(String id, String courseId, final ResponseCallback callback) {
        String query = "/task?parent_id=" + id + "&course_id=" + courseId;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get tasks of a course from server
     *
     * @param course   the course whose tasks to get
     * @param callback code to run on return
     */
    public static void getCourseTasks(Course course, final ResponseCallback callback) {
        api("/task/?course_id=" + course.getId(), Request.Method.GET, callback);
    }

    /**
     * Get roster of users that are members of this course
     *
     * @param course   course to get roster from
     * @param limit    limit of users to get in one call
     * @param offset   offset in roster list
     * @param callback code to run on return
     */
    public static void getCourseRoster(Course course, int limit, int offset, final ResponseCallback callback) {
        String query = "/course_user/?course_id=" + course.getId() + "&limit=" + limit + "&offset=" + offset
                + "&with_user_country=1&with_user_profile=1&with_user_score=0&with_course_user_model=1" +
                "&with_course_user_count=1&with_course_user_score=1";

        ///course_user/?course_id=%@&with_user_country=1&with_user_profile=0&with_user_score=0&with_course_user_model=1&with_course_user_count=1&with_course_user_score=1&limit=%i&offset=%i", courseId, limit, offset];

        api(query, Request.Method.GET, callback);
    }

    /**
     * Join a course user was invited to
     *
     * @param courseID      id of course to join
     * @param userID        id of user that will join course
     * @param inviteEmailID id of invitation to course
     * @param callback      code to run on return
     */
    public static void joinCourse(String courseID, String userID, String inviteEmailID, final ResponseCallback callback) {
        String query = "/course_user/" + courseID + "?user_id=" + userID + "&invite_email_id=" + inviteEmailID;

        api(query, Request.Method.PUT, callback);
    }

    public static void getCourseContentPost(String taskId, final ResponseCallback callback) {
        api("/course_content_post/" + taskId + "?with_course_content_post_user=1", Request.Method.GET, callback);
    }

    public static void getCourseContentPage(String taskId, final ResponseCallback callback) {
        api("/course_content_page/" + taskId, Request.Method.GET, callback);
    }
}
