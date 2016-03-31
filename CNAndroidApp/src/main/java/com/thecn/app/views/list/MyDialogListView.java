package com.thecn.app.views.list;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.ListView;

/**
* List view for a dialog that doesn't exceed a certain height.
*/
public class MyDialogListView extends ListView {

    public MyDialogListView(Context context) {
        super(context);
    }

    int maxHeight = getResources().getDisplayMetrics().heightPixels * 5 / 9;

    /**
     * Make sure height cannot exceed a certain limit.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //make sure height cannot exceed a certain limit
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * If in landscape, return regular list view.
     * If in portrait, return instance of this class.
     * @param activity for getting configuration
     * @return a list view
     */
    public static ListView getListViewForDialog(Activity activity) {
        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return new ListView(activity);
        } else {
            return new MyDialogListView(activity);
        }
    }
}
