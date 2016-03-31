package com.thecn.app.activities.poll;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.activities.post.PostFragment;
import com.thecn.app.models.content.Post;
import com.thecn.app.views.google.slidingtabs.SlidingTabLayout;

/**
 * Used to show "survey" (poll) types of {@link com.thecn.app.models.content.Post}s
 * todo * perhaps this might be better implemented with another "hovering" activity over
 * todo * an instance of {@link com.thecn.app.activities.post.PostActivity} instead?
 * todo * this would have a button or buttons that would pop up the poll questions
 */
public class PollActivity extends NavigationActivity {

    private Post mPost; //(the poll)

    private DataGrabber mDataGrabber;
    private static final String mLoadPostFragmentTag = "load_post";

    private View mLoadingView, mContentView;
    private ViewPager mViewPager;
    private PollFragmentAdapter mPollFragmentAdapter;

    private int mShortAnimationDuration;
    private boolean textFocus; //should the PostFragment focus

    /**
     * If savedInstanceState null, create {@link com.thecn.app.activities.poll.DataGrabber}
     * and begin loading data.  Else, get the data grabber and proceed from there.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            try {
                mPost = (Post) getIntent().getSerializableExtra("post");
                if (mPost.getId() == null) throw new NullPointerException();
            } catch (Exception e) {
                finishWithError();
                return;
            }

            textFocus = getIntent().getBooleanExtra("textFocus", false);

            setLoadingContentView(true);

            mDataGrabber = DataGrabber.getInstance(mPost.getId());
            getSupportFragmentManager().beginTransaction()
                    .add(mDataGrabber, mLoadPostFragmentTag)
                    .commit();
        } else {
            mDataGrabber =
                    (DataGrabber) getSupportFragmentManager().findFragmentByTag(mLoadPostFragmentTag);

            textFocus = savedInstanceState.getBoolean("textFocus");

            if (!mDataGrabber.loading) {
                mPost = (Post) savedInstanceState.getSerializable("post");
                setLoadingContentView(false);
            } else {
                setLoadingContentView(true);
            }
        }
    }

    @Override
    public String getActivityTitle() {
        return "Poll";
    }

    /**
     * If still loading, show just the progress bar.  Otherwise, show the content
     * of the activity.
     * @param stillLoading whether or not the data has been loaded
     */
    private void setLoadingContentView(boolean stillLoading) {

        mContentView = getLayoutInflater().inflate(R.layout.poll_layout, null, false);
        ((ViewGroup) findViewById(R.id.container)).addView(mContentView);
        mLoadingView = findViewById(R.id.activityProgressBar);

        getSlidingMenu().addIgnoredView(mContentView.findViewById(R.id.sliding_tabs));

        if (stillLoading) {

            mContentView.setVisibility(View.GONE);

            //get this for when the animation will be used.
            mShortAnimationDuration = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);
        } else {
            mLoadingView.setVisibility(View.GONE);

            initViewPager();
        }
    }

    /**
     * Sets up the view pager with {@link com.thecn.app.views.google.slidingtabs.SlidingTabLayout}
     * to show the post on the first page and the questions of the poll on subsequent pages.
     */
    private void initViewPager() {
        mPollFragmentAdapter = new PollFragmentAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPollFragmentAdapter);

        final SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(mViewPager);

        slidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageSelected(int i) {
                setTouchMode(i);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //hide the soft keyboard when a page is changed
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }
            }
        });

        if (mDataGrabber.loading) {
            int index = getIntent().getIntExtra("index", 0);
            setViewPagerIndex(index);
            setTouchMode(index);
        } else {
            setTouchMode(mViewPager.getCurrentItem());
        }
    }

    /**
     * Sets the current item of the view pager.
     * @param index the index of the desired current item.
     */
    public void setViewPagerIndex(int index) {
        if (mViewPager == null || mPollFragmentAdapter == null) return;

        if (mPollFragmentAdapter.getCount() > index) {
            mViewPager.setCurrentItem(index);
        }
    }

    /**
     * Sets the touch mode of the sliding menu.  This is used so that unless
     * the view pager is on its last page, the sliding menu won't be opened with
     * a swipe left gesture.
     * @param pagerIndex current item index in the pager
     */
    private void setTouchMode(int pagerIndex) {

        int touchMode = pagerIndex == mPollFragmentAdapter.getCount() - 1 ?
                SlidingMenu.TOUCHMODE_FULLSCREEN : SlidingMenu.TOUCHMODE_NONE;

        getSlidingMenu().setTouchModeAbove(touchMode);
    }

    /**
     * Cross fade the progress bar and the content of the activity.
     * Used when the data has been loaded from the server.
     */
    private void crossFade() {
        if (mContentView == null || mLoadingView == null) return;

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mContentView, "alpha", 0f, 1f);
        fadeIn.setDuration(mShortAnimationDuration);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mLoadingView, "alpha", 1f, 0f);
        fadeOut.setDuration(mShortAnimationDuration);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //set loading view gone once loaded.
                mLoadingView.setVisibility(View.GONE);
            }
        });

        mContentView.setAlpha(0f);
        mContentView.setVisibility(View.VISIBLE);
        fadeIn.start();
        fadeOut.start();
    }

    /**
     * Called when data has been retrieved from the server.
     * Checks for errors.  If none, then initialize and {@link #crossFade()}
     * @param post
     */
    public void onLoadSuccess(Post post) {
        boolean fault = post == null
                || !post.getPostType().equals("survey")
                || post.getItems() == null;

        if (fault) {
            finishWithError();
            return;
        }

        setPost(post);
        initViewPager();
        //posting this with a delay keeps the view from "skipping" the fade animation
        //this seems to happen when the network call returns quickly
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                crossFade();
            }
        }, 400);

        mDataGrabber.loading = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("post", mPost);
        outState.putSerializable("textFocus", textFocus);
    }

    /**
     * Sets the activity post object
     * @param post the post data
     */
    public void setPost(Post post) {
        mPost = post;
    }

    /**
     * Gets the activity post object
     * @return the post data
     */
    public Post getPost() {
        return mPost;
    }

    /**
     * Finish with an data load error message.
     */
    public void finishWithError() {
        AppSession.showLongToast("Could not get poll data.");
        finish();
    }

    /**
     * Shows a {@link com.thecn.app.activities.post.PostFragment} as the first item
     * and shows {@link com.thecn.app.activities.poll.PollFragment} (poll questions) as subsequent items.
      */
    public class PollFragmentAdapter extends FragmentStatePagerAdapter {

        public PollFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                Fragment fragment = PostFragment.newInstance(mPost, textFocus);
                textFocus = false;
                return fragment;
            } else {
                position --;
                return PollFragment.newInstance(mPost.getItems().get(position));
            }
        }

        @Override
        public int getCount() {
            return mPost.getItems().size() + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Main";
            }

            return Integer.toString(position);
        }
    }

}
