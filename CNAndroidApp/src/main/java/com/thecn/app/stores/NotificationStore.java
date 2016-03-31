package com.thecn.app.stores;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.notification.Notification;
import com.thecn.app.tools.network.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Used to make network calls for {@link com.thecn.app.models.notification.Notification}s to server.
 * Also has methods for converting json data from server to Java models.
 */
public class NotificationStore extends BaseStore {

    public static final String TAG = NotificationStore.class.getSimpleName();

    /**
     * Get list of Notification models from json
     * @param response data from server
     * @return list of notifications or null if error
     */
    public static ArrayList<Notification> getListData(JSONObject response) {
        try {
            return fromJSON(response.getJSONArray("data"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get Notification model from json.
     * @param jsonObject data from server
     * @return new notification model or null if error
     */
    public static Notification fromJSON(JSONObject jsonObject) {
        Gson gson = GlobalGson.getGson();

        try {
            return gson.fromJson(jsonObject.toString(), Notification.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get list of notifications from json
     * @param jsonArray data from server
     * @return list of notifications or null if error
     */
    public static ArrayList<Notification> fromJSON(JSONArray jsonArray) {
        ArrayList<Notification> notifications = new ArrayList<Notification>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Notification notification = fromJSON(jsonArray.getJSONObject(i));
                if (notification != null) {
                    notifications.add(notification);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (Exception e) {
                //do nothing
            }
        }

        return notifications.size() > 0 ? notifications : null;
    }

    /**
     * Get list of general notifications from server
     * @param limit limit on notifications to get
     * @param offset offset in notification server list
     * @param callback code to run on network response
     */
    public static void getNotifications(int limit, int offset, final ResponseCallback callback) {
        String query = "/user_notification/?with_user_notification_extra_data=1" +
                "&limit=" + limit + "&offset=" + offset +
                "&user_notification_list_skip_types[]=add_follow";

        api(query, Request.Method.GET, callback);
    }

    /**
     * get list of follower notifications from server
     * @param limit limit on notifications to get
     * @param offset offset in notification server list
     * @param callback code to run on network response
     */
    public static void getFollowNotifications(int limit, int offset, final ResponseCallback callback) {
        String query = "/user_notification/?with_user_notification_extra_data=1" +
                "&limit=" + limit + "&offset=" + offset +
                "&user_notification_list_only_types[]=add_follow";

        api(query, Request.Method.GET, callback);
    }

    /**
     * Tell the server that the user has read this notification
     * @param id id of notification to mark
     */
    public static void markNotificationRead(String id) {
        String query = "/user_notification/" + id + "?mark=read";

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
}
