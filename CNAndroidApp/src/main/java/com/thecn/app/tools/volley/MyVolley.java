package com.thecn.app.tools.volley;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.models.content.Attachment;
import com.thecn.app.models.util.VerificationBundle;
import com.thecn.app.stores.BaseStore;
import com.thecn.app.tools.images.ImageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Helper class that is used to provide references to initialized RequestQueue(s) and ImageLoader(s)
 * A lot of extra functionality added by PJ Heebner.
 * Main class used for interaction with volley.
 * based on code by Ognyan Bankov
 */
public class MyVolley {
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;
    private static BitmapLruCache mCache;

    private MyVolley() {
        // no instances
    }

    /**
     * Initialize request queue, image loader, and bitmap lru cache
     * @param context used for volley request queue
     */
    public static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mRequestQueue, BitmapLruCache.getInstance());
        mCache = BitmapLruCache.getInstance();
    }

    /**
     * Gets singleton of volley request queue
     * @return singleton
     */
    public static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    /**
     * Cancel all requests with the associated tag
     * @param tag request tag to cancel
     */
    public static void cancelRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    /**
     * Returns singleton of image loader
     * @return singleton
     */
    public static ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }

    public static void act_log(String contentType, String postId, String viewURL) {
            BaseStore.APIParams params = new BaseStore.APIParams(
                    "/act_log/",
                    Request.Method.POST,
                    new BaseStore.EmptyCallback()
            );
            params.headers = new HashMap<>();
            params.headers.put("token",  AppSession.getInstance().getLoginOrVerificationToken());
            try {
                params.jsonObject = new JSONObject("{\n" +
                        "    \"act\": \"" + contentType + "\",\n" +
                        "    \"oid\": \""+ postId + "-" + viewURL +"\"\n" +
                        "}");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            BaseStore.api(params);
    }

    public static void incrementDownloadNum(String download_attachment, Attachment attachment, String taskId) {

    }


    /**
     * Params used to send a request to volley for an image from network.
     * Also specify how the image may be handled after the request returns.
     */
    public static class ImageParams {
        public ImageView imageView = null;

        public boolean circle = false; //whether to make a circle out of the image
        public boolean tryRedirect = true; //try to get the image from another location on failure

        public int placeHolderID = R.color.black;
        public int errorImageResourceID = R.drawable.ic_broken_picture;

        public boolean fade = true; //do fade animation
        public int fadeTime = MyImageListener.DEFAULT_FADE_TIME;

        public int maxWidth = 0;
        public int maxHeight = 0;

        public int index = -1;

        public String url;

        public MyImageListener listener = null;

        private String cacheKey;

        private static String CIRCLE = "#CIRCLE";

        /**
         * New instance
         * @param url url of image to load
         * @param imageView view to load image into
         */
        public ImageParams(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        /**
         * Gets the key that this bitmap would have if it was cached.
         * @return cache key.
         */
        public String getCacheKey() {
            if (cacheKey == null) {
                StringBuilder b = new StringBuilder(url.length() + 19);

                //show that cached image is a circle image.
                if (circle) {
                    b.append(CIRCLE);
                }

                //create rest of string like volley does internally
                b.append("#W").append(maxWidth)
                        .append("#H").append(maxHeight)
                        .append(url);

                cacheKey = b.toString();
            }

            return cacheKey;
        }
    }

    /**
     * ImageListener used to handle image requests using volley throughout the app.
     */
    public static class MyImageListener implements ImageLoader.ImageListener {
        public static final int DEFAULT_FADE_TIME = 250;

        protected ImageParams params; //params used for this request
        protected Resources resources;

        /**
         * New instance
         * @param params params for this image request
         */
        public MyImageListener(ImageParams params) {
            this.params = params;

            resources = AppSession.getInstance().getApplicationContext().getResources();
        }

        /**
         * Get the params that were used in the volley request.
         * @return params used in this request
         */
        public ImageParams getParams() {
            return params;
        }

        /**
         * Made final
         */
        @Override
        final public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
            myOnResponse(response.getBitmap(), isImmediate);
        }

        /**
         * This method can be overridden by subclasses.
         * @param bm bitmap returned from volley network request
         * @param isImmediate if true, don't show an animation, immediately set into image view.
         */
        public void myOnResponse(Bitmap bm, boolean isImmediate) {
            if (bm == null) return;

            if (params.circle) {
                //circle bitmap
                Bitmap circleBitmap = mCache.getBitmap(params.getCacheKey());
                if (isImmediate && circleBitmap != null) {
                    params.imageView.setImageBitmap(circleBitmap);
                } else {
                    //make circle image if was not in cache
                    makeCircleImage(bm);
                }
            } else {
                //not a circle
                Drawable drawable = getDrawable(bm);
                setImage(drawable, isImmediate);
            }
        }

        /**
         * Try redirect if flag set.  If not, handle error.
         */
        @Override
        public void onErrorResponse(VolleyError error) {
            if (params.tryRedirect) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        tryImageRedirection(params);
                    }
                }).start();
            } else {
                //set error images (using null parameters)
                if (params.circle) {
                    makeCircleImage(null);
                } else {
                    setImage(getDrawable(null), false);
                }
            }
        }

        /**
         * Create a circle bitmap in background thread.
         * Afterwards, sets bitmap into image view.
         * @param bitmap bitmap to make circle out of
         */
        private void makeCircleImage(final Bitmap bitmap) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Drawable drawable = getCircleDrawable(bitmap);

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            setImage(drawable, false);
                        }
                    });
                }
            }).start();
        }

        /**
         * If immediate, set image into image view.  Else, show a fade animation.
         * @param drawable drawable to set into image view
         * @param isImmediate if true, don't show an animation, immediately set into image view.
         */
        protected void setImage(Drawable drawable, boolean isImmediate) {
            if (params.fade && !isImmediate) {
                fadeTransitionDrawables(drawable);
            } else {
                params.imageView.setImageDrawable(drawable);
            }
        }

        /**
         * Get a drawable made using this bitmap
         * @param bitmap bitmap to make drawable from
         * @return drawable made from bitmap
         */
        public Drawable getDrawable(Bitmap bitmap) {
            if (bitmap == null) {
                //show error image
                return resources.getDrawable(params.errorImageResourceID);
            } else {
                return new BitmapDrawable(resources, bitmap);
            }
        }

        /**
         * Should be run in background thread.
         * Creates circle bitmap from original.
         * Creates drawable from circle bitmap and returns it.
         * @param bitmap original bitmap
         * @return circle bitmap drawable
         */
        public Drawable getCircleDrawable(Bitmap bitmap) {
            if (bitmap == null) {
                return resources.getDrawable(params.errorImageResourceID);
            }

            bitmap = ImageUtil.getCircularBitmap(bitmap);

            mCache.putBitmap(params.getCacheKey(), bitmap);
            return new BitmapDrawable(resources, bitmap);
        }

        /**
         * Cross fades two drawables.  Tries to use the drawable in the image view.
         * If not there, use transparent drawable.
         * @param fadeIn the drawable that is fading into the image view.
         */
        public void fadeTransitionDrawables(Drawable fadeIn) {
            Drawable fadeOut = params.imageView.getDrawable();
            fadeOut = fadeOut != null ? fadeOut : new ColorDrawable(android.R.color.transparent);

            TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                    fadeOut,
                    fadeIn
            });
            td.setCrossFadeEnabled(true);

            params.imageView.setImageDrawable(td);
            td.startTransition(params.fadeTime);
        }
    }

    /**
     * Same as ImageListener but checks to see if index is the same as when the request was made
     * If not, then don't set the image to the ImageView.  Good for images in list views.
     */
    public static class MyIndexedImageListener extends MyImageListener {

        public MyIndexedImageListener(ImageParams params) {
            super(params);
        }

        @Override
        public void myOnResponse(Bitmap bm, boolean isImmediate) {
            if (isDifferentIndex()) return;

            super.myOnResponse(bm, isImmediate);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (isDifferentIndex()) return;

            super.onErrorResponse(error);
        }

        /**
         * Checks if the index of this image view is different from the one
         * associated with the volley request.
         * @return true if the index is different
         */
        private boolean isDifferentIndex() {
            ImageParams currentParams = (ImageParams) params.imageView.getTag();
            return currentParams != null && currentParams.index != params.index;
        }
    }

    /**
     * Load an image using given {@link com.thecn.app.tools.volley.MyVolley.ImageParams}
     * @param params params for this image request
     */
    public static void loadImage(ImageParams params) {
        params.imageView.setTag(params);
        params.imageView.setImageResource(params.placeHolderID);

        if (params.listener == null) {
            //create a default listener
            if (params.index == -1) {
                params.listener = new MyImageListener(params);
            } else {
                params.listener = new MyIndexedImageListener(params);
            }
        }

        if (params.circle) {
            //if circle bitmap is in cache, don't make network call.
            Bitmap bm = mCache.getBitmap(params.getCacheKey());
            if (bm != null) {
                params.listener.myOnResponse(bm, true);
                return;
            }
        }

        //call image loader
        mImageLoader.get(
                params.url,
                params.listener,
                params.maxWidth,
                params.maxHeight
        );
    }

    /**
     * Load a user's image.
     * @param url location of image
     * @param imageView view to load image into
     * @param position index in list
     * @param maxDimen max dimension of square image (user images are square)
     */
    public static void loadIndexedUserImage(String url, ImageView imageView, int position, int maxDimen) {
        ImageParams params = new ImageParams(url, imageView);
        params.circle = true;
        params.placeHolderID = params.errorImageResourceID = R.drawable.default_user_icon;
        params.index = position;
        params.maxWidth = maxDimen;
        params.maxHeight = maxDimen;

        loadImage(params);
    }

    /**
     * Try image redirection using {@link java.net.HttpURLConnection} instead of Volley.
     * I don't think volley can do redirection like this...
     * Some images on CourseNetworking have been permanently moved.  If using only volley,
     * then these images will always return as an error instead of redirecting to the new location of the image.
     * Should be run in background thread.
     * @param params image params used in the initial volley request
     * @return true if redirection was successful.
     */
    public static boolean tryImageRedirection(final ImageParams params) {
        //check login or verification token is there
        AppSession session = AppSession.getInstance();
        String token = session.getToken();
        if (token == null) {
            VerificationBundle bundle = session.getVerificationBundle();
            if (bundle == null || bundle.token == null) return false;

            token = bundle.token;
        }

        boolean retVal = false;
        HttpURLConnection connection = null;

        try {

            URL url = new URL(params.url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.addRequestProperty("token", token);
            connection.addRequestProperty("User-Agent", BaseStore.getUserAgentString());

            connection.connect();

            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP) {

                final String newURL = connection.getHeaderField("Location");

                if (newURL != null && !newURL.isEmpty()) {
                    retVal = true;
                    params.url = newURL;
                    params.tryRedirect = false;

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadImage(params);
                        }
                    });
                }
            }

        } catch (Exception e) {
            Log.d("OBS", e.toString() + " " + e.getMessage());
        } finally {
            if (connection != null) connection.disconnect();
        }

        return retVal;
    }

    /**
     * Trust every server - dont check for any certificate
     */
    public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setFollowRedirects(true);
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

                @Override
                public boolean verify(final String hostname, final SSLSession session) {
                    return true;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
