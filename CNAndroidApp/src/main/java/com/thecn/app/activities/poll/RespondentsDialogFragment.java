package com.thecn.app.activities.poll;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.thecn.app.AppSession;
import com.thecn.app.activities.profile.ProfileActivity;
import com.thecn.app.adapters.RosterAdapter;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.PollStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.ListFooterController;
import com.thecn.app.views.list.MyDialogListView;

import org.json.JSONObject;

import java.util.ArrayList;

/**
* Shows a list of poll respondents in a dialog.
*/
public class RespondentsDialogFragment extends DialogFragment {
    private int offset = 0;
    private final int limit = 10;
    private boolean loading = false;
    private boolean noMore = false;

    private final Object layoutLock = new Object();

    private RosterAdapter mAdapter;
    private ListFooterController mFooter;

    private CallbackManager<RespondentsDialogFragment> callbackManager;

    //json keys
    private static final String POLL_ITEM_ID = "poll_item_id";
    private static final String CONTENT_ID = "content_id";

    private static final String TITLE = "Respondents";

    /**
     * Builds an instance with arguments
     * @param pollItemID id of the poll item
     * @param contentID id of the poll (which is content)
     * @return a new instance of this class
     */
    public static RespondentsDialogFragment getInstance(String pollItemID, String contentID) {
        Bundle args = new Bundle();
        args.putString(POLL_ITEM_ID, pollItemID);
        args.putString(CONTENT_ID, contentID);

        RespondentsDialogFragment f = new RespondentsDialogFragment();
        f.setArguments(args);
        return f;
    }

    /**
     * Set up callback manager, adapter.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        callbackManager = new CallbackManager<>();

        mAdapter = new RosterAdapter(getActivity());
    }

    /**
     * Set up list view.  Add on scroll and on item click listeners.  Begin loading data if need be.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle(TITLE);

        ListView listView = MyDialogListView.getListViewForDialog(getActivity());

        mFooter = new ListFooterController(listView, inflater);
        listView.setFooterDividersEnabled(false);
        listView.setAdapter(mAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {}

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
                    getUsers();
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = mAdapter.getItem(i);
                if (user == null) return;

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);

                dismiss();
            }
        });

        if (loading) {
            mFooter.setLoading();
        } else if (mAdapter.getCount() == 0) {
            getUsers();
        } else {
            mFooter.remove();
        }

        return listView;
    }

    /**
     * Fixes bug in the support library when a dialog fragment's
     * instance is retained.
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);


        super.onDestroyView();
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
     * Get user data from the server if there could be more and we are not already making the same network call.
     */
    public void getUsers() {
        if (!loading && !noMore) {
            loading = true;
            mFooter.setLoading();

            String contentID = getArguments().getString(CONTENT_ID);
            String itemID = getArguments().getString(POLL_ITEM_ID);
            PollStore.getPollRespondents(contentID, itemID, limit, offset, new RespondentsCallback(callbackManager));
        }
    }

    /**
     * Used to make decisions when a network call returns.
     */
    private static class RespondentsCallback extends CallbackManager.NetworkCallback<RespondentsDialogFragment> {
        public RespondentsCallback(CallbackManager<RespondentsDialogFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(RespondentsDialogFragment object) {
            if (wasSuccessful()) {
                object.onSuccess(response);
            } else {
                AppSession.showDataLoadError("user list");
            }
        }

        @Override
        public void onResumeWithError(RespondentsDialogFragment object) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeAfter(RespondentsDialogFragment object) {
            object.onLoadingComplete();
        }
    }

    /**
     * Called when data successfully grabbed from server.
     * @param response data from server.
     */
    public void onSuccess(JSONObject response) {
        ArrayList<User> users = UserStore.getListData(response);

        if (users != null) {
            mAdapter.addAll(users);
            int nextOffset = StoreUtil.getNextOffset(response);
            if (nextOffset != -1) offset = nextOffset;
        } else {
            //no more users if list is null
            noMore = true;
        }
    }

    /**
     * Called when network calls finish and the view needs to be updated.
     */
    public void onLoadingComplete() {
        synchronized (layoutLock) {
            loading = false;

            mFooter.remove();
        }
    }
}
