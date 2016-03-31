package com.thecn.app;

import android.app.Activity;
import android.app.Application;

import com.thecn.app.tools.volley.BitmapLruCache;
import com.thecn.app.tools.images.MyPicasso;
import com.thecn.app.tools.volley.MyVolley;

public class CNApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        BitmapLruCache.setCacheSize(this);
        MyVolley.init(this);
        MyPicasso.init(this);

        AppSession.getInstance().Initialize(this);
    }

    private Activity mCurrentActivity = null;

//    public Activity getCurrentActivity() {
//        return mCurrentActivity;
//    }
//
//    public void setCurrentActivity(Activity activity) {
//        mCurrentActivity = activity;
//    }
}
