package com.thecn.app.stores;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.tools.network.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Used to make network calls for {@link com.thecn.app.models.conexus.Conexus}s from server.
 * Also has methods for converting json data from server to Java models.
 */
public class ConexusStore extends BaseStore{

    private static final String TAG = ConexusStore.class.getSimpleName();

    /**
     * Get data from json object
     * @param response response from server
     * @return new Conexus model
     */
    public static Conexus getData(JSONObject response) {
        try {
            return fromJSON(response.getJSONObject("data"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get data from json object
     * @param response response from server
     * @return list of Conexus models
     */
    public static ArrayList<Conexus> getListData(JSONObject response) {
        try {
            return fromJSON(response.getJSONArray("data"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get data from json object
     * @param json response from server
     * @return new Conexus model
     */
    public static Conexus fromJSON(JSONObject json) {

        Gson gson = GlobalGson.getGson();
        try {
            Conexus conexus = gson.fromJson(json.toString(), Conexus.class);
            return conexus;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get data from json array
     * @param jsonArray data from server
     * @return list of Conexus models
     */
    public static ArrayList<Conexus> fromJSON(JSONArray jsonArray) {
        ArrayList<Conexus> conexuses = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Conexus conexus = fromJSON(jsonArray.getJSONObject(i));
                if (conexus != null) {
                    conexuses.add(conexus);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (Exception e) {
                //do nothing
            }
        }

        return conexuses.size() > 0 ? conexuses : null;
    }

    /**
     * Get conexus data by id.
     * @param conexusID id of conexus
     * @param callback code to run on return
     */
    public static void getConexusById(String conexusID, final ResponseCallback callback) {
        String query = "/conexus/" + conexusID + "?with_conexus_country=1"
                + "&with_conexus_moderators=1&with_conexus_user_score=1&with_conexus_content_count=1"
                + "&with_conexus_user_content_count=1&with_conexus_count=1";

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get conexus by its number
     * @param conexusNumber conexus number
     * @param callback code to run on return
     */
    public static void getConexusByNumber(String conexusNumber, final ResponseCallback callback) {
        String query = "/conexus_number/" + conexusNumber + "?with_conexus_country=1"
                + "&with_conexus_moderators=1&with_conexus_user_score=1&with_conexus_content_count=1"
                + "&with_conexus_user_content_count=1&with_conexus_count=1";

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get conexus's roster
     * @param conexus conexus to get roster from
     * @param limit limit of users to get in one call
     * @param offset offset in list of users
     * @param callback code to run on return
     */
    public static void getConexusRoster(Conexus conexus, int limit, int offset, final ResponseCallback callback) {
        String query = "/conexus_user/?conexus_id=" + conexus.getId() + "&limit=" + limit + "&offset=" + offset
                + "&with_user_country=1&with_user_profile=1&with_user_score=1&with_conexus_user_model=1" +
                "&with_conexus_user_count=1&with_conexus_user_score=1";

        api(query, Request.Method.GET, callback);
    }

    /**
     * Join a conexus.
     * @param conexusID id of conexus to join
     * @param userID user that wants to join conexus
     * @param inviteEmailID id of invitation email
     * @param callback code to run on return
     */
    public static void joinConexus(String conexusID, String userID, String inviteEmailID, final ResponseCallback callback) {
        String query = "/conexus_user/" + conexusID + "?user_id=" + userID + "&invite_email_id=" + inviteEmailID;

        api(query, Request.Method.PUT, callback);
    }
}
