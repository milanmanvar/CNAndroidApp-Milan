package com.thecn.app.activities.navigation;

import android.content.Intent;

/**
* Used to open a page AFTER either the navigation drawer or the sliding menu is closed.
*/
public class OpenPageRunnable implements Runnable {

    NavigationActivity mActivity;
    Intent mIntent;

    public OpenPageRunnable(NavigationActivity activity, Intent intent) {
        mActivity = activity;
        mIntent = intent;
    }

    /**
     * Open some activity specified by the intent.
     */
    public void run() {
        if (mActivity == null || mIntent == null) return;

        mActivity.setTouchable(true);

        //result code doesn't matter
        mActivity.startActivityForResult(mIntent, NavigationActivity.RESERVED_REQUEST_CODE);
    }
}
