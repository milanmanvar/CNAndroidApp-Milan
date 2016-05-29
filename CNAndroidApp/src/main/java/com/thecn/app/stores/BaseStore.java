package com.thecn.app.stores;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.thecn.app.AppSession;
import com.thecn.app.tools.volley.MyVolley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base store, which other stores extend.
 * Contains central methods for making network requests.
 * Uses {@code Volley} and {@code JsonObjectRequest} under the hood
 * to make these calls.
 */
public class BaseStore {

    private static final String TAG = BaseStore.class.getSimpleName();

    //testing site
    //public static final String SITE_URL = "http://v4.coursenetworking.com";
   // public static final String SITE_URL = "https://v4.thecn.com";
    public static final String SITE_URL = "https://www.thecn.com";

    public static final String BASE_URL = SITE_URL + "/api";

    public static final boolean showLogging = true;

    /**
     * Params used to create a {@code JsonObjectRequest}
     */
    public static class APIParams {
        public String query;
        public int method;
        public ResponseCallback callback;

        public JSONObject jsonObject;
        public Map<String, String> headers;
        public Object tag;

        /**
         * Instance must have query, method, callback
         * @param query url of the request
         * @param method {@link com.android.volley.Request.Method} int (post, get, etc.)
         * @param callback code to run on request return
         */
        public APIParams(String query, int method, ResponseCallback callback) {
            this.query = query;
            this.method = method;
            this.callback = callback;
        }
    }

    /**
     * Creates request with 3 params and empty hash map
     * @param query url of the request
     * @param method {@link com.android.volley.Request.Method} int (post, get, etc.)
     * @param callback code to run on request return
     */
    public static void api(String query,
                           int method,
                           ResponseCallback callback) {

        api(query, method, new HashMap<String, String>(), callback);
    }

    /**
     * Creates request with base params and constructs
     * {@code JSONObject} payload from given map
     * @param query url of the request
     * @param method {@link com.android.volley.Request.Method} int (post, get, etc.)
     * @param params map to use for JSONObject payload
     * @param callback code to run on request return
     */
    public static void api(String query,
                           int method,
                           Map<String, String> params,
                           ResponseCallback callback) {

        api(query, method, new JSONObject(params), callback);
    }

    /**
     * Creates request with base params and JSONObject.  Constructs headers
     * and adds token.
     * @param query url of the request
     * @param method {@link com.android.volley.Request.Method} int (post, get, etc.)
     * @param jsonObject payload
     * @param callback code to run on request return
     */
    public static void api(String query,
                           int method,
                           JSONObject jsonObject,
                           ResponseCallback callback) {

        api(query, method, jsonObject, getHeadersWithToken(), callback);
    }

    /**
     * Get new Map with one entry added, namely, a token.
     * @param token token to add to map
     * @return new map with one entry
     */
    public static Map<String, String> getHeadersFromToken(String token) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("token", token);
        return headers;
    }

    /**
     * Get new Map with token added from {@link com.thecn.app.AppSession}
     * @return new map with one entry
     */
    public static Map<String, String> getHeadersWithToken() {
        Map<String, String> headers = new HashMap<String, String>();

        headers.put("token", AppSession.getInstance().getToken());

        return headers;
    }

    /**
     * Creates request with base params.  Also constructs JSONObject from
     * 3rd param and passes in headers from 4th param.
     * @param query url of the request
     * @param method {@link com.android.volley.Request.Method} int (post, get, etc.)
     * @param params map to use for JSONObject payload
     * @param headers map to use as headers
     * @param callback code to run on request return
     */
    public static void api(String query,
                           int method,
                           Map<String, String> params,
                           final Map<String, String> headers,
                           final ResponseCallback callback) {

        api(query, method, new JSONObject(params), headers, callback);
    }

    /**
     * Creates request with base params.  Also passes in JSONObject and
     * headers.  Passes all params to {@link #api(com.thecn.app.stores.BaseStore.APIParams)}
     * using {@link com.thecn.app.stores.BaseStore.APIParams}
     * @param query url of the request
     * @param method {@link com.android.volley.Request.Method} int (post, get, etc.)
     * @param jsonObject map to use for JSONObject payload
     * @param headers map to use as headers
     * @param callback code to run on request return
     */
    public static void api(String query,
                           int method,
                           JSONObject jsonObject,
                           final Map<String, String> headers,
                           final ResponseCallback callback) {

        APIParams params = new APIParams(query, method, callback);
        params.jsonObject = jsonObject;
        params.headers = headers;

        api(params);
    }

    /**
     * Base api methods that all other api methods call to perform operation.
     * Constructs all necessary params and creates a {@code JsonObjectRequest}.
     * Finally, adds request to volley's request queue.
     * @param params params used to construct request
     */
    public static void api(final APIParams params) {

        RequestQueue queue = MyVolley.getRequestQueue();

        String url = BASE_URL + params.query;
        if (showLogging) Log.d(TAG, "API CALL => " + url);

        if (params.jsonObject == null) {
            params.jsonObject = new JSONObject();
        }

        if (params.headers == null) {
            params.headers = getHeadersWithToken();
        }

        JsonObjectRequest request = new JsonObjectRequest(params.method, url, params.jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (showLogging) Log.d(TAG, response.toString());
                        //byte[] bytestr=response.toString().getBytes();
                        String multiplelanguages = null;
                        try {
                            byte[] bytesstring = response.toString().getBytes("ISO-8859-1");
                            multiplelanguages= new String(bytesstring,"UTF-8");
                        }catch(UnsupportedEncodingException ex) {
                        }
                        JSONObject newresponse=null;
                        try {
                            newresponse = new JSONObject(multiplelanguages);
                        }catch(JSONException ej)
                        {

                        }

                        //String jsonString = new String(bytestr, Charset.forName("GB2312"));
                        params.callback.onResponse(newresponse);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        params.callback.onError(error);
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //add the user agent so that the server knows it's from android
                params.headers.put("Content-Type", "application/json; charset=utf-8");
                params.headers.put("User-Agent", getUserAgentString());
                return params.headers;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 1.0f));
        request.setShouldCache(Boolean.FALSE);
        if (params.tag != null) request.setTag(params.tag);

        queue.add(request);

        /*if (queue.getCache().get(url) != null) {

            // cache exists
            try {
                String cachedResponseStr = new String(queue.getCache().get(url).data);
                JSONObject response = new JSONObject(cachedResponseStr);
                Log.d(TAG, "CACHE: "+response.toString());
                callback.response(response);
                queue.add(request);
            } catch (JSONException e) {
                // something went wrong
            }

        } else {

            //no cache
            request.setShouldCache(Boolean.FALSE);
            queue.add(request);

        }*/

    }

    /**
     * Checks whether requests can be made at the moment.
     * @param context used to get connectivity manager.
     * @return true if online, false otherwise.
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    /**
     * Construct and return string to be used in the "User-Agent" header
     * of network requests.  Server uses to determine that request is from android.
     * @return user agent string
     */
    public static String getUserAgentString() {
        Context context = AppSession.getInstance().getApplicationContext();

        String versionName = "";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            //can't get version name
        } catch (NullPointerException e) {
            //can't get version name
        }

        String string = "CNAndroidApp/";

        string += versionName + " ";
        string += "(Android; " + Build.MANUFACTURER + "; " + Build.MODEL + "; ";
        string += getVersionCode() + " " + Build.VERSION.RELEASE + "; ";
        string += getDensity(context) + ")";

        return string;
    }

    /**
     * Get the density of this device's screen.
     * @param context used for display metrics
     * @return string that represents density of screen.
     */
    private static String getDensity(Context context) {
        String string;

        float density = context.getResources().getDisplayMetrics().density;

        if (density < 1f) string = "ldpi";
        else if (density < 1.5f) string = "mdpi";
        else if (density < 2.0f) string = "hdpi";
        else if (density < 3.0f) string = "xhdpi";
        else if (density < 4.0f) string = "xxhdpi";
        else string = "xxxhdpi";

        return string;
    }

    /**
     * Get version code
     * @return version code name of Android version
     */
    private static String getVersionCode() {

        switch (Build.VERSION.SDK_INT) {
            case 1:
                return "Base";
            case 2:
                return "Petit_Four";
            case 3:
                return "Cupcake";
            case 4:
                return "Donut";
            case 5:
                return "Eclair";
            case 6:
                return "Eclair";
            case 7:
                return "Eclair";
            case 8:
                return "Froyo";
            case 9:
                return "Gingerbread";
            case 10:
                return "Gingerbread";
            case 11:
                return "Honeycomb";
            case 12:
                return "Honeycomb";
            case 13:
                return "Honeycomb";
            case 14:
                return "Ice_Cream_Sandwich";
            case 15:
                return "Ice_Cream_Sandwich";
            case 16:
                return "Jelly_Bean";
            case 17:
                return "Jelly_Bean";
            case 18:
                return "Jelly_Bean";
            case 19:
                return "KitKat";
            default:
                return "";
        }
    }

    /**
     * Empty callback
     */
    public static class EmptyCallback implements ResponseCallback {
        @Override
        public void onResponse(JSONObject response) {

        }

        @Override
        public void onError(VolleyError error) {

        }
    }
}
