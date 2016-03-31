package com.thecn.app.stores;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Interface for callbacks to be used when network recalls return from the server or with an error.
 */
public interface ResponseCallback {

    public void onResponse(JSONObject response);

    public void onError(VolleyError error);
}
