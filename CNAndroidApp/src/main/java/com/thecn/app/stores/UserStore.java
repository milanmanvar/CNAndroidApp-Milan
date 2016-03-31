package com.thecn.app.stores;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.thecn.app.AppSession;
import com.thecn.app.models.user.Score;
import com.thecn.app.models.user.User;
import com.thecn.app.tools.network.GlobalGson;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to make network calls for {@link com.thecn.app.models.user.User}s to server.
 * Has methods for converting json data from server to Java models.
 * Has methods for following, creating, and updating users as well.
 */
public class UserStore extends BaseStore {

    public static final String COMMON_USER_PARAMS = "?with_user_profile=1&with_user_score=1&" +
            "with_user_count=1&with_user_country=1&with_user_relations=1";
    private static final String TAG = UserStore.class.getSimpleName();

    /**
     * Construct User model from json
     *
     * @param response data from server
     * @return new User model or null if error
     */
    public static User getData(JSONObject response) {
        try {
            JSONObject jsonObject = response.getJSONObject("data");
            return fromJSON(jsonObject);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct list of user models from json
     *
     * @param response data from server
     * @return new list of users or null on error
     */
    public static ArrayList<User> getListData(JSONObject response) {
        try {
            JSONArray jsonArray = response.getJSONArray("data");
            return fromJSON(jsonArray);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct list of users (roster) from json.
     * Cannot use {@link #getListData(org.json.JSONObject)} because
     * structure of json returned from server is different.
     *
     * @param response data from server
     * @return new list of users or null on error
     */
    public static ArrayList<User> getRosterData(JSONObject response) {
        try {
            JSONArray jsonArray = response.getJSONArray("data");
            ArrayList<User> users = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    User user = fromJSON(jsonArray.getJSONObject(i).getJSONObject("model"));
                    Score score = fromJSONScore(jsonArray.getJSONObject(i).getJSONObject("score"));
                    user.setScore(score);
                    if (user != null) {
                        users.add(user);
                    } else {
                        StoreUtil.logNullAtIndex(TAG, i);
                    }
                } catch (Exception e) {
                    Log.d(TAG + ": ERROR", e.getMessage());
                }
            }

            return users.size() > 0 ? users : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct user model from json
     *
     * @param jsonObject data from server
     * @return new User model or null on error
     */
    public static User fromJSON(JSONObject jsonObject) {
        Log.e("Roster user object:", "" + jsonObject.toString());
        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(jsonObject.toString(), User.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct user model from json
     *
     * @param jsonObject data from server
     * @return new User model or null on error
     */
    public static Score fromJSONScore(JSONObject jsonObject) {
        Log.e("Roster user object:", "" + jsonObject.toString());
        Gson gson = GlobalGson.getGson();
        try {
            return gson.fromJson(jsonObject.toString(), Score.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct list of user models from json
     *
     * @param jsonArray data from server
     * @return new list of user models or null on error
     */
    public static ArrayList<User> fromJSON(JSONArray jsonArray) {
        ArrayList<User> users = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                User user = fromJSON(jsonArray.getJSONObject(i));
                if (user != null) {
                    users.add(user);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (Exception e) {
                //do nothing
            }
        }

        return users.size() > 0 ? users : null;
    }

    /**
     * Construct a json object to send to server.
     * This object represents a new user to create (this user wants to create an account).
     *
     * @param firstName first name of user
     * @param lastName  last name of user
     * @param email     primary email of user
     * @param password  password for user's new account
     * @return json representation of new user
     */
    public static JSONObject getNewUserJSON(String firstName, String lastName, String email, String password) {
        HashMap<String, String> map = new HashMap<>();
        map.put("first_name", firstName.toLowerCase());
        map.put("last_name", lastName.toLowerCase());
        map.put("email", email);
        map.put("password", password);
        String fullName = WordUtils.capitalizeFully(firstName) + " " + WordUtils.capitalizeFully(lastName);
        map.put("fullname", fullName);

        Gson gson = GlobalGson.getGson();
        try {
            return new JSONObject(gson.toJson(map));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the model that represents the currently logged in user.
     *
     * @param callback code to run on network response
     */
    public static void getMe(final ResponseCallback callback) {

        api("/me/?with_user_count=1&with_user_country=1&with_user_visible_settings=1", Request.Method.GET, callback);
    }

    /**
     * Get a user by id from server
     *
     * @param userId   id of user to get
     * @param callback code to run on network response
     */
    public static void getUserById(String userId, final ResponseCallback callback) {
        getUserById(userId, COMMON_USER_PARAMS, callback);
    }

    /**
     * Get user by id from server, with custom params
     *
     * @param userId   id of user to get
     * @param params   custom query params
     * @param callback code to run on network response
     */
    public static void getUserById(String userId, String params, final ResponseCallback callback) {
        api("/user/" + userId + params, Request.Method.GET, callback);
    }

    /**
     * Get user by id, pass in token (e.g., when user is verifying and a different token is being used)
     *
     * @param userId   id of user to get
     * @param token    auth token
     * @param callback code to run on network response
     */
    public static void getUserByIdPassToken(String userId, String token, final ResponseCallback callback) {
        APIParams params = new APIParams("/user/" + userId + COMMON_USER_PARAMS, Request.Method.GET, callback);
        params.headers = new HashMap<>();
        params.headers.put("token", token);

        api(params);
    }

    /**
     * Get user by their cn number
     *
     * @param cnNumber cn number of user to get
     * @param callback code to run on network response
     */
    public static void getUserByCNNumber(String cnNumber, final ResponseCallback callback) {

        getUserByCNNumber(cnNumber,
                "?with_user_profile=1&with_user_score=1&with_user_count=1&with_user_country=1&with_user_relations=1",
                callback);
    }

    /**
     * Get user by their cn number, with custom query params
     *
     * @param cnNumber cn number of user to get
     * @param params   custom query params
     * @param callback code to run on network response
     */
    public static void getUserByCNNumber(String cnNumber, String params, final ResponseCallback callback) {
        api("/cn_number/" + cnNumber + params, Request.Method.GET, callback);
    }

    /**
     * Get all the courses of the currently logged in user
     *
     * @param callback code to run on network response
     */
    public static void getAllUserCourses(final ResponseCallback callback) {

        api("/user_course?limit=999", Request.Method.GET, callback);
    }

    /**
     * Get all the conexuses of the currently logged in user.
     *
     * @param callback code to run on network response
     */
    public static void getAllUserConexuses(final ResponseCallback callback) {

        api("/user_conexus?limit=999", Request.Method.GET, callback);
    }

    /**
     * Get list of users that given user is currently following
     *
     * @param userID   id of user for which to get list of users that he/she is following
     * @param limit    limit on size of list returned from server
     * @param offset   offset within entire list on server
     * @param callback code to run on network response
     */
    public static void getUserFollowing(String userID, int limit, int offset, ResponseCallback callback) {
        String query = "/user_following/?with_user_relations=1&user_id=" + userID + "&limit=" + limit + "&offset=" + offset;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get list of users that are following given user
     *
     * @param userID   id of user for which to get list of users that are following him/her
     * @param limit    limit on size of list returned from server
     * @param offset   offset within entire list on server
     * @param callback code to run on network response
     */
    public static void getUserFollowers(String userID, int limit, int offset, ResponseCallback callback) {
        String query = "/user_follower/?with_user_relations=1&user_id=" + userID + "&limit=" + limit + "&offset=" + offset;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Tell server that the logged in user wants to follow a given user
     *
     * @param userId   id of user to follow
     * @param callback code to run on network response
     */
    public static void followUser(String userId, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("follow_user_id", userId);

        try {
            Gson gson = GlobalGson.getGson();
            JSONObject json = new JSONObject(gson.toJson(params));
            api("/user_following/", Request.Method.POST, json, callback);
        } catch (JSONException e) {
            // something went wrong
        }
    }

    /**
     * Tell server that logged in user wants to stop following given user
     *
     * @param userId   id of user to stop following
     * @param callback code to run on network response
     */
    public static void stopFollowingUser(String userId, ResponseCallback callback) {
        api("/user_following/" + userId, Request.Method.DELETE, callback);
    }

    /**
     * Create a new user using the JSONObject payload
     *
     * @param userJSON payload used to create user
     * @param callback code to run on network response
     */
    public static void createNewUser(JSONObject userJSON, ResponseCallback callback) {
        api("/user/", Request.Method.POST, userJSON, callback);
    }

    /**
     * Set a new image as the logged in user's profile picture
     *
     * @param attachmentID id of new image to use
     * @param callback     code to run on network response
     */
    public static void changeProfilePicture(String attachmentID, ResponseCallback callback) {
        String token = AppSession.getInstance().getLoginOrVerificationToken();
        if (token == null) return;

        String query = "/user_avatar/?as_current_user_avatar=1&type=attachment&value=" + attachmentID;
        APIParams params = new APIParams(query, Request.Method.POST, callback);
        params.headers = new HashMap<>();
        params.headers.put("token", token);

        api(params);
    }

    /**
     * Get given user's profile picture (avatar) from server
     *
     * @param id       id of user for which to get profile picture
     * @param callback code to run on network response
     */
    public static void getUserAvatar(String id, ResponseCallback callback) {
        api("/user_avatar/" + id, Request.Method.GET, callback);
    }

    /**
     * Get given user's profile picture (avatar) from server
     * If token null, use token in {@link com.thecn.app.AppSession}
     *
     * @param id       id of user for which to get profile picture
     * @param token    auth token
     * @param callback code to run on network response
     */
    public static void getUserAvatar(String id, String token, ResponseCallback callback) {
        APIParams params = new APIParams("/user_avatar/" + id, Request.Method.GET, callback);
        params.headers = BaseStore.getHeadersFromToken(token);

        api(params);
    }

    /**
     * Set a different picture as the logged in user's banner picture (Shown at top of profile).
     *
     * @param bannerSrcID id of source image used to crop actual banner image
     * @param bannerID    actual banner image id
     * @param callback    code to run on network response
     */
    public static void changeBanner(String bannerSrcID, String bannerID, ResponseCallback callback) {
        Map<String, String> profileMap = new HashMap<>();
        profileMap.put("theme_home_banner", bannerID);
        profileMap.put("theme_home_banner_origin", bannerSrcID);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("profile", profileMap);

        Gson gson = GlobalGson.getGson();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(gson.toJson(userMap));
        } catch (Exception e) {
            callback.onError(new VolleyError("A parsing error occurred."));
            return;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("token", AppSession.getInstance().getToken());

        api("/me/?with_user_profile=1", Request.Method.POST, jsonObject, headers, callback);
    }

    /**
     * Post verification of a recently registered user using a code returned from the request
     * sent by pressing a button in a verification email.
     *
     * @param code     verification code
     * @param token    auth token
     * @param callback code to run on network response
     */
    public static void postUserVerification(String code, String token, ResponseCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("create_from", "");

        Gson gson = GlobalGson.getGson();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(gson.toJson(params));
        } catch (Exception e) {
            callback.onError(new VolleyError("A parsing error occurred."));
            return;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("token", token);

        api("/user_verification/", Request.Method.POST, jsonObject, headers, callback);
    }
}
