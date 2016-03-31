package com.thecn.app.stores;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.thecn.app.models.profile.Country;
import com.thecn.app.models.profile.Language;
import com.thecn.app.models.profile.Location;
import com.thecn.app.models.profile.TimeZone;
import com.thecn.app.models.user.User;
import com.thecn.app.tools.network.GlobalGson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Used to obtain data from the server that is search relevant (countries, time zones, etc., something that can be searched).
 * Also makes search requests for users by keyword and cn number.
 */
public class SearchStore extends BaseStore {

    public static final String TAG = SearchStore.class.getSimpleName();

    /**
     * Construct list of Country models from json
     * @param response data from server
     * @return list of Country models or null on error
     */
    public static ArrayList<Country> getCountryData(JSONObject response) {
        try {
            ArrayList<Country> list = new ArrayList<Country>();
            JSONArray jsonArray = response.getJSONArray("data");
            Gson gson = GlobalGson.getGson();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                list.add(gson.fromJson(object.toString(), Country.class));
            }

            return list;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get list of TimeZone models from json
     * @param response data from server
     * @return list of TimeZone models or null on error
     */
    public static ArrayList<TimeZone> getTimeZoneData(JSONObject response) {
        try {
            ArrayList<TimeZone> list = new ArrayList<TimeZone>();
            JSONArray jsonArray = response.getJSONArray("data");
            Gson gson = GlobalGson.getGson();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                list.add(gson.fromJson(object.toString(), TimeZone.class));
            }

            return list;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct list of Language models from json
     * @param response data from server
     * @return list of Language models or null on error
     */
    public static ArrayList<Language> getLanguageData(JSONObject response) {
        try {
            ArrayList<Language> list = new ArrayList<>();
            JSONArray jsonArray = response.getJSONArray("data");
            Gson gson = GlobalGson.getGson();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                list.add(gson.fromJson(object.toString(), Language.class));
            }

            return list;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct list of users from json response.
     * Used instead of {@link com.thecn.app.stores.UserStore#getListData(org.json.JSONObject)}
     * because structure of json returned is different
     * @param response data from server
     * @return new list of Users or null on error
     */
    public static ArrayList<User> getUserListData(JSONObject response) {

        ArrayList<User> users = new ArrayList<User>();

        try {
            JSONArray jArray = response.getJSONArray("data");

            for (int i = 0; i < jArray.length(); i++) {
                try {
                    JSONObject jObject = jArray.getJSONObject(i);
                    String type = jObject.getString("type");

                    if (type != null && type.equals("user")) {
                        JSONObject userJObject = jObject.getJSONObject("model");
                        User user = UserStore.fromJSON(userJObject);
                        if (user != null) users.add(user);
                        else StoreUtil.logNullAtIndex(TAG, i);
                    }
                } catch (JSONException e) {
                    //whoops
                } catch (NullPointerException e) {
                    //whoops
                }
            }

        } catch (JSONException e) {
            //whoops
        } catch (NullPointerException e) {
            //whoops
        }

        return users;
    }

    /**
     * Query for a list of users that might correspond to given keyword
     * @param keyword keyword used in search
     * @param limit limit of users to get from server
     * @param callback code to run on server response
     */
    public static void userSearchByKeyword(String keyword, int limit, final ResponseCallback callback) {
        String query = "/search_user/?keyword=" + keyword + "&limit=" + limit + "&with_user_country=1";

        api(query, Request.Method.GET, callback);
    }

    /**
     * Query for list of countries in database.
     * Uses {@link com.thecn.app.AppSession} auth token
     * @param callback code to run on server response
     */
    public static void getCountriesList(ResponseCallback callback) {
        getCountriesList(null, callback);
    }

    /**
     * Query for list of countries in database
     * @param token auth token to use in query
     * @param callback code to run on server response
     */
    public static void getCountriesList(String token, ResponseCallback callback) {
        String query = "/country/?limit=999";
        APIParams params = new APIParams(query, Request.Method.GET, callback);

        if (token != null) {
            params.headers = getHeadersFromToken(token);
        }

        api(params);
    }

    /**
     * Query for list of time zones in database
     * Uses {@link com.thecn.app.AppSession} auth token
     * @param callback code to run on server response
     */
    public static void getTimeZoneList(ResponseCallback callback) {
        getTimeZoneList(null, callback);
    }

    /**
     * Query for list of time zones in database.
     * @param token auth token to use in query
     * @param callback code to run on server response
     */
    public static void getTimeZoneList(String token, ResponseCallback callback) {
        String query = "/time_zone/?limit=999";
        getTimeZoneList(token, query, callback);
    }

    /**
     * Query for list of time zones in database that exist in a given country
     * @param token auth token to use in query
     * @param countryID id of country whose time zones to get
     * @param callback code to run on server response
     */
    public static void getTimeZoneListByCountry(String token, String countryID, ResponseCallback callback) {
        String query = "/time_zone/?limit=999&country_id=" + countryID;
        getTimeZoneList(token, query, callback);
    }

    /**
     * Query for a list of time zones.  Most general method for getting time zones.
     * @param token auth token to use in query.  If null, use {@link com.thecn.app.AppSession} token
     * @param query url of network call
     * @param callback code to run on server response
     */
    private static void getTimeZoneList(String token, String query, ResponseCallback callback) {
        APIParams params = new APIParams(query, Request.Method.GET, callback);

        if (token != null) {
            params.headers = getHeadersFromToken(token);
        }

        api(params);
    }

    /**
     * Get list of languages from the server's database
     * @param callback code to run on server response
     */
    public static void getLanguageList(ResponseCallback callback) {
        String query = "/language/?limit=999";
        api(query, Request.Method.GET, callback);
    }
}
