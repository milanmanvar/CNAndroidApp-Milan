package com.thecn.app.activities.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.signup.SignupActivity;
import com.thecn.app.activities.homefeed.HomeFeedActivity;

import java.io.IOException;
import java.util.Arrays;

/**
 * Shows a splash screen and login page.  Also has link to sign up for
 * thecn.
 *
 * Build on code by zhenggl
 */
public class LoginActivity extends FragmentActivity {

    private ImageView appLogoImage;
    private ImageView appLogoImageSmall;

    private EditText usernameField;
    private EditText passwordField;
    private Button loginBtn;
    private TextView registerBtn;

    private ProgressDialog pd;

    //flag that prevents animation if it has been done already.
    private boolean animationCompleted;

    private LoginFragment mLoginFragment;
    private static final String mLoginFragmentTag = "login_fragment";

    private static final String LOGIN_FLAG = "login";

    //reference for making this part of view visible after logo animation
    private LinearLayout loginLayout;

    //for logo animation
    private int fadeViewDuration = 1500;
    private int fadeDuration = 500;
    private int translateScaleDuration = 1000;

    /**
     * If savedInstanceState null, clear the app session, and add LoginFragment.
     * else, check if splash screen animation completed, get the LoginFragment.
     * Initialize and fade in the logo.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean wasLoading;

        if (savedInstanceState != null) {
            animationCompleted = savedInstanceState.getBoolean("animation_completed", false);
            wasLoading = savedInstanceState.getBoolean("was_loading", false);

            if (animationCompleted) {
                //set duration to 0 if animation already shown
                fadeDuration = translateScaleDuration = fadeViewDuration = 0;
            }

            mLoginFragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag(mLoginFragmentTag);

        } else {
            AppSession.getInstance().clearSession();
            wasLoading = false;

            mLoginFragment = new LoginFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(mLoginFragment, mLoginFragmentTag)
                    .commit();
        }

        init(wasLoading);
        fadeInLogo();
    }

    /**
     * Get View references, set visibility of views
     * @param wasLoading
     */
    private void init(boolean wasLoading) {

        //used to pinpoint final x and y of logo animation
        appLogoImageSmall = (ImageView) findViewById(R.id.app_logo_image_small);
        appLogoImageSmall.setVisibility(View.INVISIBLE);
        //used to animate logo animation
        appLogoImage = (ImageView) findViewById(R.id.app_logo_image);
        appLogoImage.setVisibility(View.INVISIBLE);
        loginLayout = (LinearLayout) findViewById(R.id.login_layout);
        loginLayout.setVisibility(View.INVISIBLE);
        usernameField = (EditText) findViewById(R.id.content_text);
        passwordField = (EditText) findViewById(R.id.pass_edit);
        loginBtn = (Button) findViewById(R.id.login_btn);

        //shown when logging in
        pd = new ProgressDialog(this) {
            @Override
            public void onBackPressed() {
                if (isShowing()) {
                    setLoading(false);
                } else super.onBackPressed();
            }
        };
        pd.setCancelable(false);
        pd.setMessage("Logging In");
        setLoading(wasLoading);

        loginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasFields()) {
                    setLoading(true);
                    login();
                }
            }
        });

        //button links user to SignupActivity
        registerBtn = (TextView) findViewById(R.id.register_label);
        registerBtn.setVisibility(View.GONE);
        registerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        try {
            if(Arrays.asList(getApplication().getApplicationContext().getAssets().list("fonts")).contains("AKZIDENZGROTESK-BOLDCOND.ttf")) {
                TextView usernameTv = (TextView) findViewById(R.id.username_label_title);
                TextView passwordTv = (TextView) findViewById(R.id.password_label_title);
                EditText usernameEt = (EditText) findViewById(R.id.content_text);
                EditText passwordEt = (EditText) findViewById(R.id.pass_edit);
                if (usernameTv != null)
                    usernameTv.setTypeface(Typeface.createFromAsset(usernameTv.getContext().getAssets(), "fonts/AKZIDENZGROTESK-BOLDCOND.ttf"));
                if (passwordTv != null)
                    passwordTv.setTypeface(Typeface.createFromAsset(passwordTv.getContext().getAssets(), "fonts/AKZIDENZGROTESK-BOLDCOND.ttf"));
                if (usernameEt != null)
                    usernameEt.setTypeface(Typeface.createFromAsset(usernameEt.getContext().getAssets(), "fonts/Helvetica.ttf"));
                if (passwordEt != null)
                    passwordEt.setTypeface(Typeface.createFromAsset(passwordEt.getContext().getAssets(), "fonts/Helvetica.ttf"));


                PackageInfo pi;
                try {
                    pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                    Button loginBtn = (Button) findViewById(R.id.login_btn);
                    loginBtn.setTypeface(Typeface.createFromAsset(loginBtn.getContext().getAssets(), "fonts/AKZIDENZGROTESK-BOLDCOND.ttf"));
                    loginBtn.setTextSize(13);

                } catch (final PackageManager.NameNotFoundException e) {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uses content of fields to login, using
     * {@link com.thecn.app.activities.login.LoginFragment} to do networking
     */
    private void login() {
        String userName = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        mLoginFragment.login(userName, password);
    }

    /**
     * Returns whether progress dialog was showing
     * @return whether progress dialog was showing
     */
    public boolean isLoading() {
        return pd.isShowing();
    }

    /**
     * Shows or hides progress dialog and enables or disables login button
     * @param isLoading whether or not we are logging in right now.
     */
    public void setLoading(boolean isLoading) {
        if (isLoading) pd.show();
        else pd.dismiss();

        loginBtn.setEnabled(!isLoading);
    }

    /**
     * Push the home activity and finish this one.
     */
    public void launchHomeActivity() {
        Intent intent = new Intent(this, HomeFeedActivity.class);
        intent.putExtra(LOGIN_FLAG, true);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        pd.dismiss();
        AppSession.dismissToast();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("animation_completed", animationCompleted);
        outState.putBoolean("was_loading", pd.isShowing());
    }

    /**
     * Used by login button's onClick() to determine if content of fields is valid.
     * Shows an error message if it is not.
     * @return whether or not content is valid.
     */
    private boolean hasFields() {
        boolean usernamePresent, passwordPresent;
        usernamePresent = usernameField != null && usernameField.getText().toString().length() > 0;
        passwordPresent = passwordField != null && passwordField.getText().toString().length() > 0;
        if (usernamePresent && passwordPresent) {
            return true;
        } else if (!usernamePresent && passwordPresent) {
            AppSession.showLongToast("Username cannot be blank");
        } else if (usernamePresent) {
            AppSession.showLongToast("Password cannot be blank");
        } else {
            AppSession.showLongToast("Fields cannot be blank");
        }
        return false;
    }

    /**
     * Fades in the logo from blue background.
     * Uses {@link android.view.animation.AlphaAnimation}
     * and {@link android.view.animation.DecelerateInterpolator}
     */
    private void fadeInLogo() {

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setStartOffset(1000);
        fadeIn.setDuration(fadeDuration);
        fadeIn.setFillAfter(true);
        fadeIn.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                resizeLogo();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        appLogoImage.startAnimation(fadeIn);

    }

    //animates "CN" logo upon first opening the app

    /**
     * Resizes the logo after it has been faded in.  Uses the small logo as
     * a reference point for the final position of the logo.
     * Uses {@link android.view.animation.AnimationSet}, {@link android.view.animation.TranslateAnimation},
     * {@link android.view.animation.ScaleAnimation}, and {@link android.view.animation.Animation.AnimationListener}
     */
    private void resizeLogo() {

        float scaleX = (float) appLogoImageSmall.getHeight() / (float) appLogoImage.getHeight();
        float scaleY = (float) appLogoImageSmall.getWidth() / (float) appLogoImage.getWidth();

        int[] smallLogoPos = new int[2];
        appLogoImageSmall.getLocationOnScreen(smallLogoPos);

        int[] logoPos = new int[2];
        appLogoImage.getLocationOnScreen(logoPos);

        int deltaX = smallLogoPos[0] - logoPos[0];
        int deltaY = smallLogoPos[1] - logoPos[1];

        AnimationSet set = new AnimationSet(false);
        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0.0f,  // FromX
                TranslateAnimation.ABSOLUTE, deltaX,  // ToX
                TranslateAnimation.ABSOLUTE, 0.0f,  // FromY
                TranslateAnimation.ABSOLUTE, deltaY); // ToY
        translateAnimation.setDuration(translateScaleDuration);

        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, scaleX,
                1.0f, scaleY);
        scaleAnimation.setDuration(translateScaleDuration);
        scaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

        set.setFillAfter(true);
        set.addAnimation(scaleAnimation);
        set.addAnimation(translateAnimation);
        set.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeInView();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        appLogoImage.startAnimation(set);
    }

    /**
     * Fades in rest of Views after CN logo animation
     */
    private void fadeInView() {

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(fadeViewDuration);
        fadeIn.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animationCompleted = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loginLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        loginLayout.startAnimation(fadeIn);

    }
}