package com.thecn.app.stores;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.thecn.app.AppSession;
import com.thecn.app.models.util.VerificationBundle;
import com.thecn.app.tools.images.BitmapUtil;
import com.thecn.app.tools.network.GlobalGson;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Used to make network calls to upload bitmaps to the server.
 */
public class ImageStore {

    /**
     * Uses CN API's attachment_picture_thumb method to crop a picture currently on the server.
     * @param attachmentID id of image on server
     * @param replace whether to replace the original image on the server
     * @param rect denotes section of image to crop
     * @param callback code to run on network return
     */
    public static void createThumbnail(String attachmentID, boolean replace, BitmapUtil.SectionRectF rect, ResponseCallback callback) {
        String token = AppSession.getInstance().getLoginOrVerificationToken();
        if (token == null) return;

        HashMap<String, Object> params = new HashMap<>();
        params.put("id", attachmentID);

        params.put("width", rect.originalWidth);
        params.put("height", rect.originalHeight);
//        params.put("imageW", rect.originalWidth);
//        params.put("imageH", rect.originalHeight);
//        params.put("viewPortW", rect.originalWidth);
//        params.put("viewPortH", rect.originalHeight);

//        params.put("imageX", 0);
//        params.put("imageY", 0);
//        params.put("imageRotate", 0);
        params.put("rotate", 0);

        //coordinates for cropping
//        params.put("selectorX", (int) rect.left);
//        params.put("selectorY", (int) rect.top);
//        params.put("selectorW", (int) rect.right - rect.left);
//        params.put("selectorH", (int) rect.bottom - rect.top);

        params.put("top", (int) rect.left);
        params.put("left", (int) rect.top);

        HashMap<String, Integer> pluginParams = new HashMap<>();
//        pluginParams.put("replaceOriginImg", replace ? 1 : 0);
        pluginParams.put("do_replace", replace ? 1 : 0);
        params.put("pluginParams", pluginParams);

        Gson gson = GlobalGson.getGson();
        try {
            //make request.
//            BaseStore.APIParams apiParams = new BaseStore.APIParams("/attachment_picture_thumb_v2/" + attachmentID, Request.Method.PUT, callback);
            BaseStore.APIParams apiParams = new BaseStore.APIParams("/attachment_picture_thumb/" + attachmentID, Request.Method.PUT, callback);
            apiParams.headers = new HashMap<>();
            apiParams.headers.put("token", token);
            apiParams.jsonObject = new JSONObject(gson.toJson(params));
            BaseStore.api(apiParams);
        } catch (Exception e) {
            callback.onError(new VolleyError("Could not parse data."));
        }
    }

    /**
     * Upload a bitmap to the server.  Uses {@link java.net.HttpURLConnection} instead of volley.
     * @param bitmap bitmap to upload
     * @return response from server.
     */
    public static JSONObject uploadImage(Bitmap bitmap) {
        //images can either be uploaded while verifying (may not be logged in)
        //or may be uploaded while logged in.
        AppSession session = AppSession.getInstance();
        String token = session.getToken();
        if (token == null) {
            //check if in process of verifying.
            VerificationBundle bundle = AppSession.getInstance().getVerificationBundle();
            if (bundle == null || bundle.token == null) return null;

            token = bundle.token;
        }

        HttpURLConnection conn = null;
        OutputStream os = null;
        BufferedReader br = null;

        JSONObject response = null;

        try {
            //set up connection
            String query = BaseStore.BASE_URL + "/attachment_picture/?TOKEN=" + token;
            URL url = new URL(query);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);

            HttpEntity entity = getHttpEntity(bitmap);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                conn.setFixedLengthStreamingMode(entity.getContentLength());
            } else {
                conn.addRequestProperty("Content-length", Long.toString(entity.getContentLength()));
            }

            conn.addRequestProperty(entity.getContentType().getName(), entity.getContentType().getValue());

            //set up connection output
            os = conn.getOutputStream();
            entity.writeTo(os);
            os.close();

            //connect
            conn.connect();

            //if true, failure
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return null;

            //read response
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            response = new JSONObject(sb.toString());

        } catch (Exception e) {
            Log.d("OBS", e.toString() + " " + e.getMessage());
            //whoops
        } finally {
            //clean up
            if (conn != null) conn.disconnect();

            try {
                if (os != null) os.close();
                if (br != null) br.close();
            } catch (Exception e) {
                //oops
            }
        }

        return response;
    }

    /**
     * Construct an HttpEntity around Bitmap parameter
     * @param bitmap bitmap to upload
     * @return new entity that contains byte array of bitmap
     */
    private static HttpEntity getHttpEntity(Bitmap bitmap) {
        ByteArrayBody bab = new ByteArrayBody(getBytesForUpload(bitmap), "User uploaded image.jpeg");

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addPart("upload_file", bab);

        return entityBuilder.build();
    }

    /**
     * Converts bitmap to an array of bytes.
     * @param bitmap bitmap to convert to bytes
     * @return bitmap as byte array
     */
    private static byte[] getBytesForUpload(Bitmap bitmap) {
        ByteArrayOutputStream baos = getBAOS(bitmap);
        byte[] bytes = baos.toByteArray();

        try {
            baos.close();
        } catch (IOException e) {
            // do nothing
        }

        return bytes;
    }

    /**
     * Get byte array output stream for given bitmap
     * @param bitmap bitmap to convert to bytes
     * @return stream used to get bitmap as bytes
     */
    private static ByteArrayOutputStream getBAOS(Bitmap bitmap) {
        if (bitmap == null) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);

        return baos;
    }
}
