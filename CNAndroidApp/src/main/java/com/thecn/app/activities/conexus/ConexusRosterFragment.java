package com.thecn.app.activities.conexus;

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
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.ConexusStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.ListFooterController;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Shows a list of users that are part of this Conexus.
 */

public class ConexusRosterFragment extends SwipeRefreshListFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = ConexusRosterFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_CONEXUS_KEY = "conexus";

    private Conexus mConexus;

    private RosterAdapter mRosterAdapter;

    private CallbackManager<ConexusRosterFragment> callbackManager;

    private int limit, offset;
    private boolean noMore, loading;
    private boolean swipeRefreshing;

    private ListFooterController footer;

    /**
     * @param mConexus must be passed a conexus to put in arguments
     * @return new instance of this class
     */
    public static ConexusRosterFragment newInstance(Conexus mConexus) {
        ConexusRosterFragment fragment = new ConexusRosterFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_CONEXUS_KEY, mConexus);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Refreshes the adapter when an update has been made to the logged in user.
     */
    private BroadcastReceiver mUserUpdater = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mRosterAdapter.notifyDataSetChanged();
        }
    };

    /**
     * Initializes conexus data, list data, roster adapter, and broadcast receiver.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        limit = 30;
        offset = 0;
        loading = false;
        noMore = false;
        swipeRefreshing = false;

        mConexus = (Conexus) getArguments().getSerializable(FRAGMENT_BUNDLE_CONEXUS_KEY);
        mRosterAdapter = new RosterAdapter(getActivity());

        callbackManager = new CallbackManager<>();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mUserUpdater,
                new IntentFilter(AppSession.USER_UPDATE)
        );
    }

    /**
     * Sets up the list, begins loading user data, and sets the scroll and refresh listeners.
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
     * Resets data for this fragment
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
     * Restore view states
     */
    @Override
    public void onResume() {
        super.onResume();
        if(swipeRefreshing) setRefreshing(true);
        else if (loading) footer.setLoading();
        callbackManager.resume(this);
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    /**
     * When a list item is clicked, open the user's profile page.
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
     * Attempt to load user list data from the CN Api.  Fails if already loading
     * or there are no more users
     */
    public void getUsers() {
        if (!loading && !noMore) {
            loading = true;
            //don't show swipe refresh and footer loading symbols at same time.
            if (swipeRefreshing) {
                footer.clear();
            } else {
                footer.setLoading();
            }
            ConexusStore.getConexusRoster(mConexus, limit, offset, new Callback(callbackManager));
        }
    }

    /**
     * Used when request for user data returns.
     */
    private static class Callback extends CallbackManager.NetworkCallback<ConexusRosterFragment> {
        public Callback(CallbackManager<ConexusRosterFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(ConexusRosterFragment object) {
            if (wasSuccessful()) {
                object.onSuccess(response);
            } else {
                AppSession.showDataLoadError("user list");
            }
        }

        @Override
        public void onResumeWithError(ConexusRosterFragment object) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeAfter(ConexusRosterFragment object) {
            object.onLoadingComplete();
        }
    }

    /**
     * Adds data to the roster adapter.  If there is no more data, set the flag.
     * @param response json representation from the server.
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
     * Sets flags and updates the view.
     */
    public void onLoadingComplete() {
        loading = swipeRefreshing = false;
        setRefreshing(false);

        footer.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUserUpdater);
    }
}
