package com.thecn.app.stores;

import com.android.volley.Request;
import com.thecn.app.AppSession;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for authorizing with the server (logging in or out)
 * Also used to add or remove Google Cloud Messaging ids.
 * Contains methods for converting json to Java models.
 */
public class AuthStore extends BaseStore {

    /**
     * Sets token into {@link com.thecn.app.AppSession} singleton.
     * @param response data containing token
     * @return true if successful
     */
    public static boolean setToken(JSONObject response) {
        String token = getToken(response);
        if (token == null) return false;
        AppSession.getInstance().setToken(token);
        return true;
    }

    /**
     * Get token from json
     * @param response data containing token
     * @return token or null if failure
     */
    public static String getToken(JSONObject response) {
        String token;
        try {
            token = response.getJSONObject("data").getString("token");
        } catch (Exception e) {
            return null;
        }

        return token;
    }

    /**
     * Log in to the server, obtaining token in response if successful
     * @param username user name
     * @param password password
     * @param callback code to execute on return
     */
    public static void login(String username, String password, final ResponseCallback callback) {

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("username", username);
        headers.put("password", password);

        api("/auth?action=login", Request.Method.GET, new HashMap<String, String>(), headers, callback);

    }

    /**
     * Log out of server.
     * @param callback code to execute on return
     */
    public static void logout(final ResponseCallback callback) {

        api("/auth?action=logout", Request.Method.GET, callback);

    }

    /**
     * Add google cloud messaging device id to this user, so that "send to sync" messages
     * will be sent to this device from Google Cloud Messaging.
     * @param id gcm id to add
     * @param callback code to execute on return
     */
    public static void addGCMID(String id, ResponseCallback callback) {
        api("/user_gcm_reg/?registration_key=" + id, Request.Method.POST, callback);
    }

    /**
     * Remove google cloud messaging device id from user, so that messages are not sent from GCM to this device.
     * @param id id to remove
     * @param callback code to execute on return
     */
    public static void removeGCMID(String id, ResponseCallback callback) {
        api("/user_gcm_reg/?registration_key=" + id, Request.Method.DELETE, callback);
    }
}
