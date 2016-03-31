package com.thecn.app.activities.profile.basicinfo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.thecn.app.R;
import com.thecn.app.tools.anim.ActivityFragmentAnimationInterface;

/**
 * Activity used to edit a user's basic information.
 */
public class EditBasicInfoActivity extends ActionBarActivity implements ActivityFragmentAnimationInterface {

    private boolean skippingAnimations = true;
    private boolean poppingFragment = false;

    /**
     * Set up action bar and add {@link com.thecn.app.activities.profile.basicinfo.StartFragment}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.fullscreen_container);

        if (savedInstanceState == null) {
            Fragment fragment = new StartFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment, StartFragment.TAG)
                    .commit();
        }
    }

    /**
     * Set values for fragment animation
     */
    @Override
    public void onBackPressed() {
        poppingFragment = true;
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            finish();
        }
        poppingFragment = false;
    }

    @Override
    public boolean isPoppingFragment() {
        return poppingFragment;
    }

    @Override
    public boolean isSkippingAnimations() {
        return skippingAnimations;
    }

    @Override
    public void setSkippingAnimations(boolean skippingAnimations) {
        this.skippingAnimations = skippingAnimations;
    }

    /**
     * Display custom animation on finish
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
