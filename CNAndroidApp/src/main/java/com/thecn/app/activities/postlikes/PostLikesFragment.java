package com.thecn.app.activities.postlikes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
import com.thecn.app.models.content.Post;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.ListFooterController;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Shows a list of people who have liked a post.
 */
public class PostLikesFragment extends SwipeRefreshListFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = PostLikesFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_POST_KEY = "post";

    private RosterAdapter mPostLikesAdapter;
    private ListFooterController footer;
    private Post mPost;
    private CallbackManager<PostLikesFragment> callbackManager;

    private int limit, offset;
    private boolean noMore, loading;
    private boolean swipeRefreshing;

    private static final String NO_LIKES = "There are no likes for this post.";

    /**
     * Gets a fragment instance with arguments
     * @param mPost the post whose "likes" to display
     * @return a new instance of this class
     */
    public static PostLikesFragment newInstance(Post mPost) {
        PostLikesFragment fragment = new PostLikesFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_POST_KEY, mPost);
        fragment.setArguments(args);

        return fragment;
    }

    //tells adapter to refresh if logged in user is updated
    private BroadcastReceiver mUserUpdater = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPostLikesAdapter.notifyDataSetChanged();
        }
    };

    /**
     * Set up data and adapter, register user update broadcast receiver.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        callbackManager = new CallbackManager<>();

        limit = 10;
        offset = 0;
        noMore = false;

        mPost = (Post) getArguments().getSerializable(FRAGMENT_BUNDLE_POST_KEY);
        mPostLikesAdapter = new RosterAdapter(getActivity());

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mUserUpdater,
                new IntentFilter(AppSession.USER_UPDATE)
        );
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    /**
     * Update views to reflect state of data
     */
    @Override
    public void onResume() {
        super.onResume();
        if (swipeRefreshing) setRefreshing(true);
        else if (loading) footer.setLoading();
        callbackManager.resume(this);
    }

    /**
     * Set up list view, footer, and begin loading if appropriate.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(null);

        ListView listView = getListView();
        footer = new ListFooterController(listView, getLayoutInflater(savedInstanceState));
        listView.setFooterDividersEnabled(false);
        listView.setBackgroundColor(getResources().getColor(R.color.background_color));

        setListAdapter(mPostLikesAdapter);

        if (mPostLikesAdapter.getCount() == 0) {
            // Wait until fragment animation is complete
            long duration = getResources().getInteger(R.integer.fragment_anim_duration);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getUsers();
                }
            }, duration);
        }

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

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
     * Open a profile when a list item is clicked.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (mPostLikesAdapter.getCount() < 1) return;

        getListView().setItemChecked(position, true);

        User user = mPostLikesAdapter.getItem(position);
        ((NavigationActivity) getActivity()).openProfileByID(user.getId());
    }

    /**
     * If not loading already and there could be more users, make a request for more.
     */
    public void getUsers() {
        if (!loading && !noMore) {
            loading = true;
            //if refreshing, don't show the footer progress bar
            if (isRefreshing()) {
                footer.clear();
            } else {
                footer.setLoading();
            }
            PostStore.ListParams params = new PostStore.ListParams(limit, offset);
            params.id = mPost.getId();
            PostStore.getPostLikes(params, new Callback(callbackManager));
        }
    }

    /**
     * Used when user list data network call returns to make decisions.
     */
    private static class Callback extends CallbackManager.NetworkCallback<PostLikesFragment> {
        public Callback(CallbackManager<PostLikesFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(PostLikesFragment object) {
            if (wasSuccessful()) {
                object.onSuccess(response);
            } else {
                AppSession.showDataLoadError("user list");
            }
        }

        @Override
        public void onResumeWithError(PostLikesFragment object) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeAfter(PostLikesFragment object) {
            object.onLoadingComplete();
        }
    }

    /**
     * Called when a network call returns successfully
     * @param response data from server
     */
    public void onSuccess(JSONObject response) {
        ArrayList<User> users = UserStore.getListData(response);

        if (users != null) {
            //add to list
            mPostLikesAdapter.addAll(users);
            int nextOffset = StoreUtil.getNextOffset(response);
            if (nextOffset != -1) offset = nextOffset;
        } else {
            //if list null, no more users
            noMore = true;
        }
    }

    /**
     * Called to update view states after loading is finished.
     */
    public void onLoadingComplete() {
        loading = swipeRefreshing = false;
        setRefreshing(false);

        if (mPostLikesAdapter.getCount() == 0) footer.showMessage(NO_LIKES);
        else footer.clear();
    }

    /**
     * Called when the user has activated swipe to refresh
     */
    @Override
    public void onRefresh() {
        offset = 0;
        loading = noMore = false;
        swipeRefreshing = true;
        mPostLikesAdapter.clear();
        getUsers();
    }

    /**
     * Unregister broadcast receiver
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUserUpdater);
    }
}
