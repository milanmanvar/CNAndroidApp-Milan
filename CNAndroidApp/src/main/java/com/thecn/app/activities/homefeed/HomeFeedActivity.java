package com.thecn.app.activities.homefeed;

import android.os.Bundle;

import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;

/**
 * Shows equivalent content to what is shown on the Home feed on the website.
 */
public class HomeFeedActivity extends NavigationActivity {

    private HomeFeedFragment homeFeedFragment;
    private static final String FRAGMENT_TAG = "HOME_FEED";

    /**
     * Add a HomeFeedFragment if savedInstanceState null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavigationActivity.putWholePageResult(this, true);

        setActionBarAndTitle("Home Feed");

        if (savedInstanceState == null) {
            hideProgressBar();
            homeFeedFragment = new HomeFeedFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, homeFeedFragment, FRAGMENT_TAG)
                    .commit();
        } else {
            hideProgressBar();
            homeFeedFragment = (HomeFeedFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        }
    }

    /**
     * Refresh instead of opening another home feed page.
     */
    @Override
    public void openHomeFeedPage() {
        if (homeFeedFragment != null) {
            homeFeedFragment.refresh();
            homeFeedFragment.showPostButton();
        }
    }
}
