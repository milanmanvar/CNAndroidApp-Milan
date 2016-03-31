package com.thecn.app.stores;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.content.Attachment;
import com.thecn.app.tools.network.GlobalGson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Used to make network calls for {@link com.thecn.app.models.content.Attachment}s to server.
 * Also has methods for converting json data from server to Java models.
 */
public class AttachmentStore extends BaseStore {

    /**
     * Construct Attachment model from json
     * @param response json response
     * @return attachment
     */
    public static Attachment getData(JSONObject response) {
        try {
            Gson gson = GlobalGson.getGson();
            return gson.fromJson(
                    response.getJSONObject("data").toString(),
                    Attachment.class
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get an attachment from server
     * @param contentID id of attachment
     * @param callback code to execute on return
     */
    public static void getAttachment(String contentID, ResponseCallback callback) {
        String query = "/attachment/" + contentID;

        api(query, Request.Method.GET, callback);
    }
}
