package com.thecn.app.stores;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.content.Email;
import com.thecn.app.tools.network.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Used to make network calls for {@link com.thecn.app.models.content.Email}s to server.
 * Also has methods for converting json data from server to Java models.
 */
public class EmailStore extends BaseStore {

    public static final String TAG = EmailStore.class.getSimpleName();

    /**
     * Construct Email object from json response
     * @param response data from server
     * @return new Email model or null if error
     */
    public static Email getData(JSONObject response) {
        try {
            return fromJSON(response.getJSONObject("data"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct list of email objects from json response
     * @param response data from server
     * @return list of Email objects or null if error
     */
    public static ArrayList<Email> getListData(JSONObject response) {
        try {
            return fromJSON(response.getJSONArray("data"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct Email object from json
     * @param jsonObject data from server
     * @return new Email model or null if error
     */
    public static Email fromJSON(JSONObject jsonObject) {
        Gson gson = GlobalGson.getGson();

        try {
            String jsonString = jsonObject.toString();
            return gson.fromJson(jsonString, Email.class);
        } catch (Exception e) {
            //something went wrong
            return null;
        }
    }

    /**
     * Get list of email objects from json
     * @param jsonArray data from server
     * @return list of emails or null if error
     */
    public static ArrayList<Email> fromJSON(JSONArray jsonArray) {
        ArrayList<Email> emails = new ArrayList<Email>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Email email = fromJSON(jsonArray.getJSONObject(i));
                if (email != null) {
                    emails.add(email);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (Exception e) {
                //do nothing
            }
        }

        return emails.size() > 0 ? emails : null;
    }

    public static final Object SEND_EMAIL_TAG = new Object();

    /**
     * Send a newly constructed email to the server.
     * @param email email object in json form
     * @param callback code to run on return
     */
    public static void sendEmail(JSONObject email, final ResponseCallback callback) {
        sendEmail(email, "", callback);
    }

    /**
     * Send newly constructed email to the server, with query parameters.
     * @param email email object in json form
     * @param queryParams query parameters to add onto base query
     * @param callback code to run on return
     */
    public static void sendEmail(JSONObject email, String queryParams, final ResponseCallback callback) {
        String query = "/email/" + queryParams;

        BaseStore.APIParams params = new APIParams(query, Request.Method.POST, callback);
        params.jsonObject = email;
        params.tag = SEND_EMAIL_TAG;

        api(params);
    }

    /**
     * Get list of emails.  This is used for email notifications in {@link com.thecn.app.fragments.NotificationFragments.EmailNotificationFragment}
     * @param limit limit of emails to get
     * @param offset offset in list of emails
     * @param callback code to run on return
     */
    public static void getEmails(int limit, int offset, final ResponseCallback callback) {
        String query = "/email/?with_email_extra_data=1&with_email_sender=1" +
                "&email_list_order=most_new&limit=" + limit + "&offset=" + offset;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get an email by its id
     * @param id id of email to get
     * @param callback code to run on return
     */
    public static void getEmailById(String id, final ResponseCallback callback) {
        String query = "/email/" + id + "?with_email_sender=1&with_email_sub_emails=1" +
                "&with_email_extra_data=1&with_email_attachments=1&with_email_videos=1" +
                "&with_email_pictures=1";

        api(query, Request.Method.GET, callback);
    }

    /**
     * Tell the server that the user has read this email.
     * @param id id of email to be marked
     */
    public static void markEmailRead(String id) {
        String query = "/email/" + id + "?status=read";

        api(query, Request.Method.PUT, new ResponseCallback() {
            @Override
            public void onResponse(JSONObject response) {
                //don't need to do anything
            }

            @Override
            public void onError(VolleyError error) {
                //don't need to do anything
            }
        });
    }

    /**
     * Ask server to delete an email by id
     * @param id id of email to delete
     * @param callback code to run on return
     */
    public static void delete(String id, ResponseCallback callback) {

        if (callback == null) callback = new ResponseCallback() {
            @Override
            public void onResponse(JSONObject response) {
                //do nothing
            }

            @Override
            public void onError(VolleyError error) {
                //do nothing
            }
        };

        api("/email/" + id, Request.Method.DELETE, callback);
    }
}
