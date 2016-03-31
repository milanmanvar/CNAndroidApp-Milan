package com.thecn.app.stores;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.thecn.app.AppSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Utility methods used frequently with {@link com.thecn.app.stores.BaseStore}
 */
public class StoreUtil {

    public static final String TAG = StoreUtil.class.getSimpleName();

    /**
     * Get list of response errors from json (different from volley errors).
     * These errors are returned by the server when something about the data is not correct.
     * @param response data from server
     * @return list of string errors from server.
     */
    public static ArrayList<String> getResponseErrors(JSONObject response) {
        try {
            JSONArray jsonArray = response.getJSONArray("errors");
            ArrayList<String> retVal = new ArrayList<String>();

            for (int i = 0; i < jsonArray.length(); i ++) {
                try {
                    String error = jsonArray.getString(i);
                    if (error != null) {
                        retVal.add(error);
                    }
                } catch (JSONException e) {
                    //well that's weird
                }
            }

            return retVal.size() > 0 ? retVal : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Show only the very first response error returned from server.
     * @param response data from server
     */
    public static void showFirstResponseError(JSONObject response) {
        String error = getFirstResponseError(response);
        if (error != null) {
            AppSession.showLongToast(error);
        }
    }

    /**
     * Get the first response error returned from server
     * @param response data from server
     * @return string error
     */
    public static String getFirstResponseError(JSONObject response) {
        ArrayList<String> errors = StoreUtil.getResponseErrors(response);
        if (errors != null && errors.size() > 0) {
            return errors.get(0);
        }

        return null;
    }

    /**
     * Get the next offset attribute, usually returned when querying lists of information.
     * @param response data from server
     * @return int or -1 if there was an error
     */
    public static int getNextOffset(JSONObject response) {
        try {
            return response.getJSONObject("extra_data").getInt("next_offset");
        } catch (JSONException e) {
            return -1;
        }
    }

    /**
     * Show an error message for a given exception.
     * This could be a {@code VolleyError}.
     * Show specific messages for each kind of error.
     * Use {@link com.thecn.app.AppSession#showLongToast(String)}
     * to show error to user.
     * @param error error to show to user.
     */
    public static void showExceptionMessage(Exception error) {

        try {

            if (error == null) return;

            if (error instanceof VolleyError) {

                VolleyError volleyError = (VolleyError) error;

                Log.d(TAG, "Error Response Reason: " + volleyError.toString());

                if (volleyError instanceof NoConnectionError) {
                    Log.d(TAG, "Volley Error: NoConnectionError");
                    AppSession.showLongToast("No internet connection");
                } else if (volleyError instanceof ServerError) {
                    Log.d(TAG, "Volley Error: ServerError");
                    Log.d(TAG, "Volley Error: ServerError Code: " + volleyError.networkResponse.statusCode);
                    AppSession.showLongToast("A problem with the server occurred");
                } else if (volleyError instanceof AuthFailureError) {
                    Log.d(TAG, "Volley Error: AuthFailureError");
                    AppSession.showLongToast("Authentication Failure");
                } else if (volleyError instanceof ParseError) {
                    Log.d(TAG, "Volley Error: ParseError");
                    AppSession.showLongToast("A parsing error occurred");
                } else if (volleyError instanceof NetworkError) {
                    Log.d(TAG, "Volley Error: NetworkError");
                    AppSession.showLongToast("A problem with the network occurred");
                } else if (volleyError instanceof TimeoutError) {
                    Log.d(TAG, "Volley Error: TimeoutError");
                    AppSession.showLongToast("The connection timed out");
                } else {
                    if (volleyError.networkResponse != null) {
                        Log.d(TAG, "Volley Error: ClientError Code: " + volleyError.networkResponse.statusCode);
                        AppSession.showLongToast("No network response");
                    }
                }
            }


        } catch (NullPointerException e) {
            // no error message...
        }
    }

    /**
     * Print a message notifying about a null item in a list
     * returned from the server.  Used for debugging...
     * @param tag log tag to print message under
     * @param index index of null item
     */
    public static void logNullAtIndex(String tag, int index) {
        Log.e(tag, "JSONObject null at index: " + index);
    }

    /**
     * Test whether the network call was successful.
     * This attribute exists in every API method so far used.
     * @param response data from server
     * @return true if successful, false otherwise
     */
    public static boolean success(JSONObject response) {
        if (response == null) return false;

        try {
            return response.getBoolean("result");
        } catch (JSONException e) {
            return false;
        }
    }
}
