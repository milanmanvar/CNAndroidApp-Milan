package com.thecn.app.tools.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thecn.app.R;

/**
 * Used to show a loading view while waiting for data.  Can also show a message and a button (for errors)
 */
public class LoadingViewController {

    private int mShortAnimationDuration;

    private FrameLayout mViewsContainer; //root view

    private View mContentView;
    private View mLoadingView;

    private ProgressBar mProgressBar;
    private TextView mTextView;
    private Button mButton;

    /**
     * New instance of controller.
     * @param view the root view to be shown when loading is complete
     * @param context used for resources
     */
    public LoadingViewController(View view, Context context) {
        mContentView = view;

        //loading view contains indeterminate progress bar, text view, and button
        LayoutInflater inflater = LayoutInflater.from(context);
        mLoadingView = inflater.inflate(R.layout.message_load_view, null, false);
        mProgressBar = (ProgressBar) mLoadingView.findViewById(R.id.progressBar);
        mTextView = (TextView) mLoadingView.findViewById(R.id.message);
        mButton = (Button) mLoadingView.findViewById(R.id.button);

        mViewsContainer = new FrameLayout(context);
        mViewsContainer.setLayoutParams(
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mViewsContainer.addView(mContentView);
        mViewsContainer.addView(mLoadingView);

        //used for fading between loading and content views
        mShortAnimationDuration =
                context.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    /**
     * Get the root view that contains loading view and content view
     * @return root view
     */
    public View getRootView() {
        return mViewsContainer;
    }

    /**
     * Get content view, should be shown after loading finished
     * @return content view
     */
    public View getContentView() {
        return mContentView;
    }

    /**
     * Set background color of the loading view.
     * @param color background color
     */
    public void setBackgroundColor(int color) {
        mLoadingView.setBackgroundColor(color);
    }

    /**
     * Set text color of the loading view
     * @param color text color
     */
    public void setTextColor(int color) {
        mTextView.setTextColor(color);
        mButton.setTextColor(color);
    }

    /**
     * Show the loading view (which is also used to show loading errors)
     */
    public void showLoadingView() {
        mContentView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
    }

    /**
     * Show the content view after loading complete
     */
    public void showContent() {
        mContentView.setVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.GONE);
    }

    /**
     * Show (or not) the progress bar
     * @param show whether to show progress bar
     */
    public void showLoading(boolean show) {
        mProgressBar.setVisibility(getVisibility(show));
    }

    /**
     * Set message displayed by message text
     * @param text message
     */
    public void setMessage(String text) {
        mTextView.setText(text);
    }

    /**
     * Set message and then show it
     * @param text message
     */
    public void showMessage(String text) {
        setMessage(text);
        showMessage(true);
    }

    /**
     * Set visibility of message
     * @param show whether to show message text view
     */
    public void showMessage(boolean show) {
        mTextView.setVisibility(getVisibility(show));
    }

    /**
     * Sets button text and listener
     * @param text to display on button
     * @param listener action to perform on button click
     */
    public void setButton(String text, View.OnClickListener listener) {
        mButton.setText(text);
        mButton.setOnClickListener(listener);
    }

    /**
     * Sets button text, listener, and shows button
     * @param text button text
     * @param listener action to perform on button click
     */
    public void showButton(String text, View.OnClickListener listener) {
        setButton(text, listener);
        showButton(true);
    }

    /**
     * Sets visibility of button
     * @param show whether to show button or set to "gone"
     */
    public void showButton(boolean show) {
        mButton.setVisibility(getVisibility(show));
    }

    /**
     * Cross fade content and loading views (content will be shown afterwards)
     * Use {@link android.animation.ObjectAnimator} to perform alpha animations
     */
    public void crossFade() {
        if (mContentView == null || mLoadingView == null) return;

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mContentView, "alpha", 0f, 1f);
        fadeIn.setDuration(mShortAnimationDuration);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mLoadingView, "alpha", 1f, 0f);
        fadeOut.setDuration(mShortAnimationDuration);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //set loading view visibility to gone on completion of animation
                mLoadingView.setVisibility(View.GONE);
            }
        });

        mContentView.setAlpha(0f);
        mContentView.setVisibility(View.VISIBLE);
        fadeIn.start();
        fadeOut.start();
    }

    /**
     * Converts boolean to visibility integer
     * @param show true for VISIBLE, false for GONE
     * @return integer representing visibility
     */
    private int getVisibility(boolean show) {
        return show ? View.VISIBLE : View.GONE;
    }

}
