package com.thecn.app.activities.navigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.thecn.app.fragments.NotificationFragments.BaseNotificationFragment;
import com.thecn.app.fragments.NotificationFragments.EmailNotificationFragment;
import com.thecn.app.fragments.NotificationFragments.FollowNotificationFragment;
import com.thecn.app.fragments.NotificationFragments.GeneralNotificationFragment;

/**
 * Used for the ViewPager in the SlidingMenu to manage the three types of
 * notification fragments.
*/
class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    private BaseNotificationFragment[] fragments;

    FragmentManager mManager;

    //keys for fragments added to a fragment manager
    private static final String NOTIFICATION_FRAGMENT_KEY = "notification_fragment";
    private static final String EMAIL_FRAGMENT_KEY = "email_fragment";
    private static final String FOLLOW_FRAGMENT_KEY = "request_fragment";

    /**
     * If no savedInstanceState, create new fragments.  Else, get the fragments that were already added.
     */
    public MyFragmentPagerAdapter(FragmentManager fm, Bundle savedInstanceState) {
        super(fm);
        mManager = fm;

        fragments = new BaseNotificationFragment[3];

        if (savedInstanceState != null) {
            Fragment holder;

            holder = mManager.getFragment(savedInstanceState, NOTIFICATION_FRAGMENT_KEY);
            fragments[0] = holder != null ?
                    (BaseNotificationFragment) holder : new GeneralNotificationFragment();

            holder = mManager.getFragment(savedInstanceState, EMAIL_FRAGMENT_KEY);
            fragments[1] = holder != null ?
                    (BaseNotificationFragment) holder : new EmailNotificationFragment();

            holder = mManager.getFragment(savedInstanceState, FOLLOW_FRAGMENT_KEY);
            fragments[2] = holder != null ?
                    (BaseNotificationFragment) holder : new FollowNotificationFragment();
        } else {
            fragments[0] = new GeneralNotificationFragment();
            fragments[1] = new EmailNotificationFragment();
            fragments[2] = new FollowNotificationFragment();
        }
    }

    /**
     * Get a fragment specified by the index.
     */
    @Override
    public Fragment getItem(int index) {
        return fragments[index];
    }

    /**
     * Gets number of fragments.
     */
    @Override
    public int getCount() {
        return fragments.length;
    }

    /**
     * Perform {@link com.thecn.app.fragments.NotificationFragments.BaseNotificationFragment#onFocus()}
     * for the selected fragment.
     * @param index
     */
    public void focus(int index) {
        fragments[index].onFocus();
    }

    /**
     * Performs {@link com.thecn.app.fragments.NotificationFragments.BaseNotificationFragment#onMenuClose()}
     * for each fragment.
     */
    public void onMenuClose() {
        for (BaseNotificationFragment f : fragments) {
            f.onMenuClose();
        }
    }

    /**
     * Puts all the fragments into the fragment manager.
     */
    public void onSaveInstanceState(Bundle outState) {
        if (fragments[0] != null) {
            mManager.putFragment(outState, NOTIFICATION_FRAGMENT_KEY, fragments[0]);
        }
        if (fragments[1] != null) {
            mManager.putFragment(outState, EMAIL_FRAGMENT_KEY, fragments[1]);
        }
        if (fragments[2] != null) {
            mManager.putFragment(outState, FOLLOW_FRAGMENT_KEY, fragments[2]);
        }
    }

}
