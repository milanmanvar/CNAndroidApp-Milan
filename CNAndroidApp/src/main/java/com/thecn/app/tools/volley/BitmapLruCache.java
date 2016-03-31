/**
 * Copyright 2013 Ognyan Bankov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thecn.app.tools.volley;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Least recently used cache used to store bitmaps that have been grabbed from network.
 * It is very important to make sure the size of this cache is controlled well.
 */
public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
    private static BitmapLruCache instance = null;
    private static int cacheSize;

    private static final int CACHE_PROPORTION = 7; //proportion of free memory given to cache.

    /**
     * Uses {@link android.app.ActivityManager#getMemoryClass()} to set cache size.
     * @param context used to get {@link android.app.ActivityManager}
     */
    public static void setCacheSize(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int memKilobytes = manager.getMemoryClass() * 1024;
        cacheSize =  memKilobytes / CACHE_PROPORTION;
    }

    /**
     * New instance
     */
    private BitmapLruCache() {
        super(cacheSize);
    }

    /**
     * Returns a singleton.
     * @return singleton
     */
    public static BitmapLruCache getInstance() {
        if (instance == null)
            instance = new BitmapLruCache();
        return instance;
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return (value.getRowBytes() * value.getHeight()) / 1024;
    }

    @Override
    public Bitmap getBitmap(String key) {
        return get(key);
    }

    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        if (getBitmap(key) == null)
            put(key, bitmap);
    }
}
