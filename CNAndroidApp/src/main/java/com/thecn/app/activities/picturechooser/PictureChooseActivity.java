package com.thecn.app.activities.picturechooser;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.thecn.app.R;
import com.thecn.app.tools.anim.ActivityFragmentAnimationInterface;

/**
 * Used (currently) to set the user's profile picture or to change the user's banner picture (on the profile page).
 */
public class PictureChooseActivity extends ActionBarActivity implements ActivityFragmentAnimationInterface {

    public static final String TYPE_TAG = PictureChooseActivity.class.getName() + ".type";
    public static final int TYPE_AVATAR = 0;
    public static final int TYPE_BANNER = 1;

    private static final String BASE_STATE = "base_state";

    private boolean poppingFragment = false;
    private boolean skippingAnimations = true;

    /**
     * Set background to black, add {@link com.thecn.app.activities.picturechooser.GalleryFragment}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_container);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.black));
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (savedInstanceState == null) {

            FragmentManager manager = getSupportFragmentManager();

            Fragment gFragment = new GalleryFragment();
            manager.beginTransaction()
                    .replace(R.id.container, gFragment)
                    .addToBackStack(BASE_STATE)
                    .commit();
        }
    }

    /**
     * Finish if no fragment on the backstack.
     */
    @Override
    public void onBackPressed() {
        poppingFragment = true;
        if (!getSupportFragmentManager().popBackStackImmediate(BASE_STATE, 0)) {
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
     * Sets the title of the activity's action bar
     * @param title title for the action bar
     */
    public void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    /**
     * Replaces a fragment and adds the transaction to the backstack.
     * @param fragment fragment to replace the current fragment.
     */
    public void replace(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Gets the type from the intent.  This specifies whether the
     * activity will be used to change the user's profile image or to
     * change the user's banner image.
     * @return 0 for avatar, 1 for banner
     */
    public int getType() {
        return getIntent().getIntExtra(TYPE_TAG, TYPE_AVATAR);
    }
}
