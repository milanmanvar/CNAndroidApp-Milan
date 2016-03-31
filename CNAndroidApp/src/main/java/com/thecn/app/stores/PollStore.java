package com.thecn.app.stores;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.content.PollItem;
import com.thecn.app.tools.network.GlobalGson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Used to make network calls for {@link com.thecn.app.models.content.PollItem}s
 * and other poll-related objects to server.
 * Also has methods for converting json data from server to Java models.
 */
public class PollStore {

    public static final String TAG = PollStore.class.getSimpleName();

    /**
     * Construct PollItem model from json
     * @param response data from server
     * @return new PollItem or null if error
     */
    public static PollItem getItemData(JSONObject response) {
        try {
            JSONObject jsonObject = response.getJSONObject("data");
            return itemFromJSON(jsonObject);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct PollItem model from json
     * @param jsonObject data from server
     * @return new PollItem or null if error
     */
    public static PollItem itemFromJSON(JSONObject jsonObject) {
        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(jsonObject.toString(), PollItem.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct list of submissions from json
     * @param response data from server
     * @return list of submissions or null if error
     */
    public static ArrayList<PollItem.Submission> getSubmissionListData(JSONObject response) {
        try {
            JSONArray jsonArray = response.getJSONArray("data");
            return submissionsFromJSON(jsonArray);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct submission from json
     * @param jsonObject data from server
     * @return submission or null if error
     */
    public static PollItem.Submission submissionFromJSON(JSONObject jsonObject) {
        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(jsonObject.toString(), PollItem.Submission.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * Construct list of submissions from json
     * @param jsonArray data from server
     * @return list of submissions or null if error
     */
    public static ArrayList<PollItem.Submission> submissionsFromJSON(JSONArray jsonArray) {
        ArrayList<PollItem.Submission> submissions = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                PollItem.Submission submission = submissionFromJSON(jsonArray.getJSONObject(i));
                if (submission != null) {
                    submissions.add(submission);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (Exception e) {
                // do nothing
            }
        }

        return submissions.size() > 0 ? submissions : null;
    }

    /**
     * Make network request for list of poll respondents
     * @param contentId id of poll
     * @param itemId id of poll item to get respondents of
     * @param limit limit on size of list returned
     * @param offset offset in server's list of responses
     * @param callback code to run on network return
     */
    public static void getPollRespondents(String contentId, String itemId, int limit, int offset, ResponseCallback callback) {
        String query = getPollSubmissionsQuery(contentId, itemId, limit, offset) + "&with_survey_submission_user=1";

        BaseStore.api(query, Request.Method.GET, callback);
    }

    /**
     * Make network request for list of poll submissions (short answer submissions)
     * @param contentId id of poll
     * @param itemId id of poll item to get submissions of
     * @param limit limit on size of list returned
     * @param offset offset in server's list of submissions
     * @param callback code to run on network return
     */
    public static void getPollSubmissions(String contentId, String itemId, int limit, int offset, ResponseCallback callback) {
        String query = getPollSubmissionsQuery(contentId, itemId, limit, offset);

        BaseStore.api(query, Request.Method.GET, callback);
    }

    /**
     * Construct query string for {@link #getPollSubmissions(String, String, int, int, ResponseCallback)}
     * @param contentId id of poll
     * @param itemId id of poll item to get submissions of
     * @param limit limit on size of list returned
     * @param offset offset in server's list of submissions
     * @return query string
     */
    private static String getPollSubmissionsQuery(String contentId, String itemId, int limit, int offset) {
        return "/survey_submission/?content_id=" + contentId + "&item_id=" + itemId + "&limit=" + limit + "&offset=" + offset
                + "&with_user_country=1&with_user_score=1";
    }
}
