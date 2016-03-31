package com.thecn.app.fragments.NotificationFragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.adapters.NotificationAdapters.NotificationAdapter;
import com.thecn.app.models.notification.Notification;
import com.thecn.app.stores.NotificationStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.ListFooterController;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Base fragment that contains functionality used by both {@link com.thecn.app.fragments.NotificationFragments.GeneralNotificationFragment}
 * and {@link com.thecn.app.fragments.NotificationFragments.FollowNotificationFragment}
 */
public abstract class NotificationFragment extends BaseNotificationFragment {

    private NotificationAdapter mAdapter;
    private ListFooterController footer;

    protected int limit, offset;
    protected boolean noMore, loading;

    private CallbackManager<NotificationFragment> callbackManager;

    /**
     * Set up data, callback manager, and adapter
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);

        limit = 20;
        offset = 0;
        noMore = false;
        loading = false;

        callbackManager = new CallbackManager<>(getActivity());

        mAdapter = new NotificationAdapter(getActivity(), callbackManager);
    }

    /**
     * Get root view, set the title.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(getTitle());
        return view;
    }

    /**
     * Give activity ref. to callback manager
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (callbackManager != null) callbackManager.setActivity(activity);
    }

    /**
     * Take away activity ref. from callback manager.
     */
    @Override
    public void onDetach() {
        if (callbackManager != null) callbackManager.setActivity(null);
        super.onDetach();
    }

    /**
     * Subclasses return their title.
     * @return title
     */
    public abstract String getTitle();

    /**
     * Set up list view, footer, and dividers.  Set on scroll listener
     * that loads more notifications when user scrolls to bottom.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(null);

        ListView listView = getListView();
        footer = new ListFooterController(listView, getLayoutInflater(savedInstanceState));
        listView.setFooterDividersEnabled(false);

        setListAdapter(mAdapter);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean shouldLoad = hasBeenViewed() && ((totalItemCount - visibleItemCount) <= firstVisibleItem);
                if (shouldLoad) {
                    getData();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        callbackManager.resume(this);
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    /**
     * Get message to display when there are no notifications.
     * @return none message
     */
    public abstract String getNoneMessage();

    @Override
    public void emptyList() {
        mAdapter.clear();
        offset = 0;
        noMore = false;
    }

    /**
     * Calls notification's {@link com.thecn.app.models.notification.Notification.NotificationClickCallback#onLinkClick()}
     * to perform an action.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (mAdapter.getCount() < 1) return;

        Notification notification = mAdapter.getItem(position);
        try {
            notification.getCallback().onLinkClick();
        } catch (NullPointerException e) {
            // no callback set
        }
    }

    @Override
    public void getData() {
        if (!loading && !noMore) {
            loading = true;
            footer.setLoading();
            getNotifications();
        }
    }

    /**
     * Subclasses implement method for getting data from server.
     */
    public abstract void getNotifications();

    /**
     * Called when successful response received from server.
     * @param response data from server.
     */
    public void onSuccess(JSONObject response) {
        ArrayList<Notification> notifications = NotificationStore.getListData(response);

        if (notifications != null) {
            mAdapter.addAll(notifications);
            int nextOffset = StoreUtil.getNextOffset(response);
            if (nextOffset != -1) offset = nextOffset;
        } else {
            noMore = true;
        }
    }

    /**
     * Called when network calls complete.  Resets data, sets notification data
     * to zero new notifications, refreshes view.
     */
    public void onLoadingComplete() {
        loading = false;

        setNotificationDisplayZero();

        if (mAdapter.getCount() == 0) footer.showMessage(getNoneMessage());
        else footer.clear();
    }

    /**
     * Implemented by subclass to set respective notification display to zero.
     */
    public abstract void setNotificationDisplayZero();
}
