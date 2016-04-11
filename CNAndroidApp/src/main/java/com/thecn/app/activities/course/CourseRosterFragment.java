package com.thecn.app.activities.course;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.adapters.RosterAdapter;
import com.thecn.app.fragments.SwipeRefreshListFragment;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.ListFooterController;

import org.json.JSONObject;

import java.util.ArrayList;

public class CourseRosterFragment extends SwipeRefreshListFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = CourseRosterFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_COURSE_KEY = "course";

    private Course mCourse;

    private RosterAdapter mRosterAdapter;

    private CallbackManager<CourseRosterFragment> callbackManager;

    private int limit, offset;
    private boolean loading, noMore;
    private boolean swipeRefreshing;

    private ListFooterController footer;

    /**
     * Must have a course object for fragment arguments
     */
    public static CourseRosterFragment newInstance(Course mCourse) {
        CourseRosterFragment fragment = new CourseRosterFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_COURSE_KEY, mCourse);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Used to refresh adapter when logged in user is updated
     */
    private BroadcastReceiver mUserUpdater = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mRosterAdapter.notifyDataSetChanged();
        }
    };

    /**
     * Sets up data and broadcast manager
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        limit = 10;
        offset = 0;
        loading = false;
        noMore = false;

        mCourse = (Course) getArguments().getSerializable(FRAGMENT_BUNDLE_COURSE_KEY);
        mRosterAdapter = new RosterAdapter(getActivity());

        callbackManager = new CallbackManager<>();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mUserUpdater,
                new IntentFilter(AppSession.USER_UPDATE)
        );
//        ((CourseActivity_New)getActivity()).setActionBarAndTitle("Roster");
    }

    /**
     * Sets up list view and footer, begins loading users (if there are none) and
     * sets onscroll and onrefresh listeners
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(null);

        ListView listView = getListView();
        footer = new ListFooterController(listView, getLayoutInflater(savedInstanceState));
        listView.setFooterDividersEnabled(false);
        listView.setBackgroundColor(getResources().getColor(R.color.background_color));

        setListAdapter(mRosterAdapter);

        if (mRosterAdapter.getCount() == 0) {
            getUsers();
        }

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {}

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
                    getUsers();
                }
            }
        });

        setOnRefreshListener(this);
    }

    /**
     * reset data and reload new user data from server
     */
    @Override
    public void onRefresh() {
        offset = 0;
        noMore = loading = false;
        mRosterAdapter.clear();
        swipeRefreshing = true;
        getUsers();
    }

    /**
     * Set view states
     */
    @Override
    public void onResume() {
        super.onResume();
        if (swipeRefreshing) setRefreshing(true);
        else if (loading) footer.setLoading();
        callbackManager.resume(this);
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    /**
     * open profile page on item click
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (mRosterAdapter.getCount() < 1) return;

        getListView().setItemChecked(position, true);

        User user = mRosterAdapter.getItem(position);
        ((NavigationActivity) getActivity()).openProfileByID(user.getId());
    }

    /**
     * Get data from server.  Fails if already getting data (loading) or there is no more to get.
     * Uses {@link com.thecn.app.stores.CourseStore#getCourseRoster(com.thecn.app.models.course.Course, int, int, com.thecn.app.stores.ResponseCallback)}
     */
    public void getUsers() {
        if (!loading && !noMore) {
            loading = true;
            //make sure not to show the swipe refreshing indicator and the loading footer
            //at the same time.
            if (swipeRefreshing) {
                footer.clear();
            } else {
                footer.setLoading();
            }
            CourseStore.getCourseRoster(mCourse, limit, offset, new Callback(callbackManager));
        }
    }

    /**
     * Used when the network response returns
     */
    private static class Callback extends CallbackManager.NetworkCallback<CourseRosterFragment> {
        public Callback(CallbackManager<CourseRosterFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(CourseRosterFragment object) {
            if (wasSuccessful()) {
                object.onSuccess(response);
            } else {
                AppSession.showDataLoadError("user list");
            }
        }

        @Override
        public void onResumeWithError(CourseRosterFragment object) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeAfter(CourseRosterFragment object) {
            object.onLoadingComplete();
        }
    }

    /**
     * Get user data.  If no data, then there are no more users
     * @param response data grabbed from server
     */
    public void onSuccess(JSONObject response) {
        ArrayList<User> users = UserStore.getRosterData(response);

        if (users != null) {
            mRosterAdapter.addAll(users);
            int nextOffset = StoreUtil.getNextOffset(response);
            if (nextOffset != -1) offset = nextOffset;
        } else {
            noMore = true;
        }
    }

    /**
     * Set flags and update views to reflect them
     */
    public void onLoadingComplete() {
        loading = swipeRefreshing = false;
        setRefreshing(false);

        footer.clear();
    }

    /**
     * Unregister receiver to prevent {@link android.os.DeadObjectException}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUserUpdater);
    }
}
