package com.thecn.app.tools.controllers;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;

import com.thecn.app.views.list.ObservableListView;

/**
 * Controls layouts that will slide on or off the screen depending on the scrolling of an
 * {@link com.thecn.app.views.list.ObservableListView}.  This is used with the post button on post pages.
 */
public class SlidingViewController {

    //mTwinView is shown when on screen (when set), otherwise mView shown
    private View mView, mTwinView;
    //Extension of ListView whose scrolling can be observed
    private ObservableListView mListView;

    //used to normalize view position on screen so that when the views are at the top of the ListView,
    //their positions will be 0 as opposed to their actual positions on screen
    private int mListViewPositionY = 0;

    private ObjectAnimator mViewAnimator;

    //0: controls sliding and settling of view
    //1: listener added by user (optional)
    //2: button visibility toggle listener (only when using two views)
    private AbsListView.OnScrollListener[] mListeners = new AbsListView.OnScrollListener[3];

    /**
     * New instance.
     * @param listView list view to observe in order to translate its movements to sliding view movements.
     */
    public SlidingViewController(ObservableListView listView) {
        mListView = listView;

        //gets position of ListView every time the layout is changed
        mListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mListView != null) {
                    int[] loc = new int[2];
                    mListView.getLocationInWindow(loc);
                    mListViewPositionY = loc[1];
                }
            }
        });

        //master listener delegates all events to additional listeners
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                for (AbsListView.OnScrollListener listener : mListeners) {
                    if (listener != null) {
                        listener.onScrollStateChanged(absListView, scrollState);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                for (AbsListView.OnScrollListener listener : mListeners) {
                    if (listener != null) {
                        listener.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);
                    }
                }
            }
        });

        //controls sliding and settling of view by setting its y translation
        mListeners[0] = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE && mView != null) {
                    //if view on screen and scrolling has stopped, slide it back out
                    if (mView.getTranslationY() > -mView.getHeight()) {
                        mViewAnimator = ObjectAnimator.ofFloat(mView, "translationY", 0);
                        mViewAnimator.start();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //do nothing
            }
        };
    }

    /**
     * Set the y translation attr of the sliding view.
     * @param translation amount of translation
     */
    public void setViewTranslation(float translation) {
        mView.setTranslationY(translation);
    }

    /**
     * Set the view that will scroll with the list view.
     * @param view the view to be moved with the list view.
     * @return this instance (for building).
     */
    public SlidingViewController setSlidingView(View view) {

        if (view != null) {

            mView = view;

            mListView.setObserver(new ObservableListView.ListViewObserver() {
                @Override
                public void onScroll(float deltaY) {

                    if (mViewAnimator != null) {
                        //if view was settling, cancel the animation
                        mViewAnimator.cancel();
                    }

                    if (mView != null) {
                        int mButtonHeight = mView.getHeight();
                        float newTranslationY = mView.getTranslationY() + deltaY;

                        if (newTranslationY > 0) {
                            //don't translate view more than 0
                            mView.setTranslationY(0);
                        } else {
                            float threshold = ((float) mButtonHeight) * -1.3f;
                            if (newTranslationY < threshold) {
                                //don't translate view more upwards more than the threshold
                                mView.setTranslationY(threshold);
                            } else {
                                //if projected translation is within bounds, use it
                                mView.setTranslationY(newTranslationY);
                            }
                        }
                    }
                }
            });
        }

        return this;
    }

    /**
     * Sets a view that will be monitored and shown when it is lower on screen than the sliding view.
     * @param twinView the view to set
     * @return this instance (for building)
     */
    public SlidingViewController setTwinView(View twinView) {
        if (mView != null && twinView != null) {
            mTwinView = twinView;
            setViewVisibility();

            mListeners[2] = new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int scrollState) {}

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    setViewVisibility();
                }
            };
        } else {
            mListeners[2] = null;
        }

        return this;
    }

    /**
     * Get y position relative to the list view's top y position on screen
     * @param view view to get the y coord of
     * @return y position of view
     */
    private int getY(View view) {
        int[] viewCoordinates = new int[2];
        view.getLocationInWindow(viewCoordinates);

        return viewCoordinates[1] - mListViewPositionY;
    }

    /**
     * Set sliding view invisible if its twin view is lower on screen, visible otherwise.
     * This is used in the Course and Conexus activities, for example.
     */
    private void setViewVisibility() {

        int mTwinViewY = getY(mTwinView);
        int mViewY = getY(mView);

        boolean hideMView = mTwinViewY >= mViewY;

        boolean viewVisible = mView.getVisibility() == View.VISIBLE;

        if (hideMView) {
            //don't hide view if was already hidden
            if (viewVisible) {
                mView.setVisibility(View.INVISIBLE);
            }
        } else if (!viewVisible) { //don't show view if already visible
            mView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets additional scroll listener to be used with observable list view.
     * @param scrollListener
     */
    public void setOnScrollListener(AbsListView.OnScrollListener scrollListener) {
        mListeners[1] = scrollListener;
    }
}