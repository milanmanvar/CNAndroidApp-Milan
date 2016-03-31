package com.thecn.app.fragments.NotificationFragments;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.thecn.app.R;

/**
 * Base notification fragment from which the others extend.
 */
public abstract class BaseNotificationFragment extends ListFragment {

    private boolean hasBeenViewed;

    /**
     * Called when a fragment is being shown by the view pager
     * AND the sliding menu is open (all the way) so that it is visible
     */
    public void onFocus() {

        boolean refresh;

        if (!hasBeenViewed) {
            refresh = hasBeenViewed = true;
        } else {
            refresh = hasNewData();
        }

        if (refresh) {
            emptyList();
            getData();
        }
    }

    /**
     * Resets {@link #hasBeenViewed} flag to false
     */
    public void onMenuClose() {
        hasBeenViewed = false;
    }

    /**
     * Retain instances
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * Set up list view dividers
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = getListView();
        Resources r = getResources();
        listView.setDivider(new ColorDrawable(r.getColor(R.color.background_color)));
        listView.setDividerHeight((int) getResources().getDimension(R.dimen.list_divider_height));
    }

    /**
     * When new activity created, sliding menu will be closed, so call
     * {@link #onMenuClose()}
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onMenuClose();
    }

    /**
     * Get whether fragment has been viewed
     * @return whether fragment has been viewed.
     */
    public boolean hasBeenViewed() {
        return hasBeenViewed;
    }

    /**
     * Tells whether it is known that there are new notifications for this fragment to show.
     * @return true if has new data
     */
    public abstract boolean hasNewData();

    /**
     * Subclass gets its relevant data from server
     */
    public abstract void getData();

    /**
     * Clear this fragment's list of data.
     */
    public abstract void emptyList();
}
