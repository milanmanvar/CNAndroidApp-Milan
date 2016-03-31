package com.thecn.app.activities.profile;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.broadcastreceivers.FollowChangeReceiver;
import com.thecn.app.fragments.SwipeRefreshListFragment;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.ListFooterController;

import java.util.ArrayList;

/**
 * Shows a list of either followers or following for a given user.
 */
public class FollowActivity extends NavigationActivity {

    public static final String ARG_ID = "id";
    public static final String ARG_CN_NUMBER = "cn_number";
    public static final String ARG_TYPE = "type";

    /**
     * Get user information, add the fragment if savedInstance null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            String userID = getIntent().getStringExtra(ARG_ID);
            String cnNumber = getIntent().getStringExtra(ARG_CN_NUMBER);
            int type = getIntent().getIntExtra(ARG_TYPE, 0);
            FollowFragment f = FollowFragment.newInstance(userID, cnNumber, type);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, f)
                    .commit();
        }
    }

    /**
     * Set custom finishing animation
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    /**
    * Displays a list of either followers or following for a given user.  Also shows a button
     * next to each user to allow THE user to change following status.
    */
    public static class FollowFragment extends SwipeRefreshListFragment implements SwipeRefreshLayout.OnRefreshListener {

        public static final String TAG = FollowFragment.class.getSimpleName();
        private static final int LIMIT = 20;

        public static final int TYPE_FOLLOWERS = 0;
        public static final int TYPE_FOLLOWING = 1;

        private static final String FOLLOWERS = "Followers of ";
        private static final String FOLLOWING = "Followed by ";

        private CallbackManager<FollowFragment> callbackManager;

        private int offset;
        private boolean loading, noMore;
        private boolean swipeRefreshing;
        private boolean error;

        private FollowAdapter followAdapter;
        private ListFooterController footer;

        private ChangeReceiver followChangeReceiver;

        /**
         * Get fragment instance with arguments
         * @param userID id of user
         * @param cnNumber cn number of user
         * @param type denotes "followers" or "following"
         * @return new instance of this class
         */
        public static FollowFragment newInstance(String userID, String cnNumber, int type) {
            Bundle args = new Bundle();
            args.putString(ARG_ID, userID);
            args.putString(ARG_CN_NUMBER, cnNumber);
            args.putInt(ARG_TYPE, type);

            FollowFragment f = new FollowFragment();
            f.setArguments(args);

            return f;
        }

        /**
         * Set title on activity creation.
         */
        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            String title;
            String cnNumber = getArguments().getString(ARG_CN_NUMBER);

            if (getArguments().getInt(ARG_TYPE) == TYPE_FOLLOWERS) {
                title = FOLLOWERS + cnNumber;
            } else {
                title = FOLLOWING + cnNumber;
            }

            getFollowActivity().setActionBarAndTitle(title);
        }

        /**
         * Set up data and adapters.  Register broadcast receiver.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            callbackManager = new CallbackManager<>(getActivity());

            offset = 0;
            loading = noMore = swipeRefreshing = false;

            followAdapter = new FollowAdapter(callbackManager);

            followChangeReceiver = new ChangeReceiver(followAdapter, callbackManager);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                    followChangeReceiver, new IntentFilter(FollowChangeReceiver.FILTER)
            );
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            if (callbackManager != null) callbackManager.setActivity(getActivity());
        }

        @Override
        public void onDetach() {
            if (callbackManager != null) callbackManager.setActivity(getActivity());
            super.onDetach();
        }

        /**
         * Get the follow adapter.
         * @return adapter used in the list view of this fragment.
         */
        public FollowAdapter getFollowAdapter() {
            return followAdapter;
        }

        /**
         * Unregister receiver.
         */
        @Override
        public void onDestroy() {
            Context c = AppSession.getInstance().getApplicationContext();
            LocalBroadcastManager.getInstance(c).unregisterReceiver(followChangeReceiver);
            super.onDestroy();
        }

        /**
         * Used to receive updates to following status for a particular user.
         * Uses separate thread to find updated user and change the local status.
         */
        private static class ChangeReceiver extends FollowChangeReceiver {

            private FollowAdapter adapter;
            private CallbackManager<FollowFragment> manager;

            public ChangeReceiver(FollowAdapter adapter, CallbackManager<FollowFragment> manager) {
                this.adapter = adapter;
                this.manager = manager;
            }

            @Override
            public void onChangeReceived(final String userID, final boolean status) {

                final ArrayList<User> users = adapter.getUsers();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean keepLooking = true;

                        //find user with this id
                        for (int i = 0; keepLooking && i < users.size(); i++) {
                            User user = users.get(i);

                            if (userID.equals(user.getId())) {
                                keepLooking = false;

                                User.Relations r = user.getRelations();

                                if (r != null) {
                                    //change following status
                                    r.setFollowing(status);

                                    manager.addCallback(new CallbackManager.Callback<FollowFragment>() {
                                        @Override
                                        public void execute(FollowFragment object) {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        }
                    }
                }).start();
            }
        }

        /**
         * Set up list view and footer, set on scroll listener and on refresh listener.
         */
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            setListAdapter(null);

            ListView listView = getListView();
            footer = new ListFooterController(listView, getLayoutInflater(savedInstanceState));
            listView.setFooterDividersEnabled(false);
            setFooter();

            setListAdapter(followAdapter);

            //load new data if need be
            if (followAdapter.getCount() == 0 && !noMore) {
                getFollowUsers();
            }

            getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {}

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (!noMore && (totalItemCount - visibleItemCount) <= firstVisibleItem) {
                        getFollowUsers();
                    }
                }
            });

            setOnRefreshListener(this);
        }

        /**
         * If not loading and there could be more, load more users.
         * Content depends on type (following or followers).
         */
        public void getFollowUsers() {
            if (loading || noMore) return;

            loading = true;
            setFooter();
            String userID = getArguments().getString(ARG_ID);

            if (getArguments().getInt(ARG_TYPE) == TYPE_FOLLOWERS) {
                UserStore.getUserFollowers(userID, LIMIT, offset, new GetFollowCallback(callbackManager));
            } else {
                UserStore.getUserFollowing(userID, LIMIT, offset, new GetFollowCallback(callbackManager));
            }
        }

        /**
         * Used to make decision on network call return
         */
        private static class GetFollowCallback extends CallbackManager.NetworkCallback<FollowFragment> {
            public GetFollowCallback(CallbackManager<FollowFragment> manager) {
                super(manager);
            }

            @Override
            public void onResumeWithResponse(FollowFragment object) {
                if (!wasSuccessful()) {
                    object.error = true;
                    AppSession.showDataLoadError("user list");
                    return;
                }

                object.error = false;

                ArrayList<User> users = UserStore.getListData(response);
                if (users == null) {
                    //no more users if list null
                    object.noMore = true;
                    return;
                }

                object.followAdapter.addAll(users);
                int nextOffset = StoreUtil.getNextOffset(response);
                if (nextOffset != -1) object.offset = nextOffset;
            }

            @Override
            public void onResumeWithError(FollowFragment object) {
                object.error = true;
                StoreUtil.showExceptionMessage(error);
            }

            @Override
            public void onResumeAfter(FollowFragment object) {
                object.onLoadingComplete();
            }
        }

        /**
         * Reset data and update view
         */
        @Override
        public void onRefresh() {
            followAdapter.clear();
            offset = 0;
            noMore = loading = false;
            swipeRefreshing = true;
        }

        /**
         * Used to update data and view when loading complete
         */
        public void onLoadingComplete() {
            loading = swipeRefreshing = false;
            setRefreshing(false);
            setFooter();
        }

        /**
         * Sets the footer based on flags.
         * todo this pattern can be used throughout app with lists.
         */
        private void setFooter() {
            if (swipeRefreshing) {
                //don't show footer
                setRefreshing(true);
                footer.clear();
            } else if (loading) {
                //show footer
                footer.setLoading();
            } else if (error) {
                //show error message, allow retry
                footer.showMessage(ListFooterController.RETRY);
                footer.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getFollowUsers();
                    }
                });
            } else {
                //show either end of list message or none message
                footer.getView().setOnClickListener(null);

                if (followAdapter.getCount() == 0) {
                    footer.showMessage(ListFooterController.NONE);
                } else if (noMore) {
                    footer.showMessage(ListFooterController.END);
                } else {
                    footer.clear();
                }
            }
        }

        /**
         * Set views for data and update adapter.
         */
        @Override
        public void onResume() {
            super.onResume();
            if (swipeRefreshing) {
                setRefreshing(true);
                footer.clear();

            } else if (loading) {
                setRefreshing(false);
                footer.setLoading();
            }

            followAdapter.notifyDataSetChanged();

            callbackManager.resume(this);
        }

        @Override
        public void onPause() {
            callbackManager.pause();
            super.onPause();
        }

        /**
         * Cast the activity into FollowActivity
         */
        public FollowActivity getFollowActivity() {
            return (FollowActivity) getActivity();
        }
    }
}
