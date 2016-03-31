package com.thecn.app.tools.images;

import android.app.ActivityManager;
import android.content.Context;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

/**
 * Class for singleton of Picasso library.
 * Used for gallery activities, etc, where images are loaded locally (not from network).
 */
public class MyPicasso {

    private static Picasso mPicasso;

    private MyPicasso() {}

    private static final int CACHE_PROPORTION = 7;

    /**
     * Initializes picasso singleton.
     * @param context used to create Picasso instance
     */
    public static void init(Context context) {
        mPicasso = new Picasso.Builder(context)
                .memoryCache(new LruCache(getCacheSize(context)))
                .build();
    }

    /**
     * Get the size of the cache to be used for images.
     * @param context used to get activity manager
     * @return size of cache
     */
    public static int getCacheSize(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int memKilobytes = manager.getMemoryClass() * 1024 * 1024;
        return memKilobytes / CACHE_PROPORTION;
    }

    /**
     * Get picasso singleton.
     * @return picasso singleton
     */
    public static Picasso getPicasso() {
        if (mPicasso != null) {
            return mPicasso;
        } else {
            throw new IllegalStateException("Picasso singleton not initialized");
        }
    }
}
