package com.thecn.app.stores;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.notification.UserNewMessage;
import com.thecn.app.tools.network.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Used to make network calls for {@link com.thecn.app.models.notification.UserNewMessage}s to server.
 * Also has methods for converting json data from server to Java models.
 */
public class NewMessageStore extends BaseStore {

    /**
     * Get UserNewMessage model from json response
     * @param response data from server
     * @return new UserNewMessage model or null if error
     */
    public static UserNewMessage getData(JSONObject response) {
        try {
            return fromJSON(response.getJSONObject("data").getJSONArray("user_new_message"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get UserNewMessage model from json array
     * @param jsonArray data from server
     * @return new user new message model or null if error
     */
    public static UserNewMessage fromJSON(JSONArray jsonArray) {
        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(jsonArray.toString(), UserNewMessage.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Make network request for user new message (count of the user's new notifications)
     * @param callback code to run on network return
     */
    public static void getNewMessages(final ResponseCallback callback) {

        api("/v2/user_new_message", Request.Method.GET, callback);
    }
}
