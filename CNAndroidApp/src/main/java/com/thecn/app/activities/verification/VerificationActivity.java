package com.thecn.app.activities.verification;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.inputmethod.InputMethodManager;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.models.util.VerificationBundle;
import com.thecn.app.tools.anim.ActivityFragmentAnimationInterface;

/**
 * Activity for getting basic information from a user and verifying them afterwards.
 */
public class VerificationActivity extends ActionBarActivity implements ActivityFragmentAnimationInterface {

    private static final String INTENT_ERROR = "Could not get data...";
    //used to pass to home feed activity so it knows to refresh itself
    public static final String VERIFICATION_INTENT_TAG = "verification_intent";

    private static final String SESSION_END = "Session has timed out";

    //used for fragment animations
    private boolean poppingFragment = false;
    private boolean skippingAnimations = true;

    /**
     * Set up and add {@link com.thecn.app.activities.verification.VerifyFragment}
     * to fragment manager
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_container);
        int color = getResources().getColor(R.color.background_color);
        getWindow().getDecorView().setBackgroundColor(color);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (savedInstanceState == null) {
            //should be verification uri from email sent to user
            Uri data = getIntent().getData();
            FragmentManager manager = getSupportFragmentManager();

            if (data != null) {
                AppSession.getInstance().clearSession();

                VerifyFragment fragment = VerifyFragment.getInstance(data.toString());
                manager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
            } else {
                AppSession.showShortToast(INTENT_ERROR);
                finish();
            }

            AppSession.getInstance().setVerificationBundle(new VerificationBundle());
        }
    }

    @Override
    public void onBackPressed() {
        poppingFragment = true;
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            finish();
        }
        poppingFragment = false;
    }

    /**
     * Check to make sure verification information is still valid
     */
    @Override
    protected void onResume() {
        super.onResume();
        VerificationBundle bundle = AppSession.getInstance().getVerificationBundle();
        if (bundle == null) {
            finishWithSessionEndMessage();
        }
    }

    /**
     * Finish with message that session has ended.
     */
    public void finishWithSessionEndMessage() {
        AppSession.showLongToast(SESSION_END);
        finish();
    }

    /**
     * Hide soft keyboard
     */
    public void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
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
     * Set action bar title.
     * @param title title to set to action bar
     */
    public void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}