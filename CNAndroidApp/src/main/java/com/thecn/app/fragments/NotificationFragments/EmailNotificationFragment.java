package com.thecn.app.fragments.NotificationFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.adapters.NotificationAdapters.EmailNotificationAdapter;
import com.thecn.app.activities.email.EmailFragment;
import com.thecn.app.models.content.Email;
import com.thecn.app.models.notification.UserNewMessage;
import com.thecn.app.stores.EmailStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.ListFooterController;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Shows email notifications to the user.
 */
public class EmailNotificationFragment extends BaseNotificationFragment {

    public static final String TAG = EmailFragment.class.getSimpleName();

    private EmailNotificationAdapter mAdapter;
    private ListFooterController mFooter;

    private int limit, offset;
    private boolean noMore, loading;

    private Button composeEmailButton;

    private CallbackManager<EmailNotificationFragment> callbackManager;

    private static final String NO_EMAIL = "You have no emails.";

    /**
     * Set up flags and variables, callback manager and adapter
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        limit = 20;
        offset = 0;
        noMore = false;

        callbackManager = new CallbackManager<>();

        mAdapter = new EmailNotificationAdapter(callbackManager, getActivity());
    }

    /**
     * Inflate compose email button, return root view.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        composeEmailButton = (Button) inflater.inflate(R.layout.compose_email_button, null, false);
        return inflater.inflate(R.layout.fragment_email_notifications, container, false);
    }

    /**
     * Cast activity
     * @return cast activity
     */
    private NavigationActivity getNavigationActivity() {
        return (NavigationActivity) getActivity();
    }

    /**
     * Set up list view.  Add compose button as header.  Set up footer,
     * add on scroll listener that will load more notifications.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListAdapter(null);

        ListView listView = getListView();

        listView.addHeaderView(composeEmailButton);

        composeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppSession.checkVerification(getNavigationActivity())) {
                    return;
                }

                getNavigationActivity().pushComposeEmailActivity();
            }
        });

        mFooter = new ListFooterController(listView, getLayoutInflater(savedInstanceState));
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
     * Calls same method of adapter
     */
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void emptyList() {
        mAdapter.clear();
        offset = 0;
        noMore = false;
    }

    /**
     * Mark notification as read and take to {@link com.thecn.app.activities.email.EmailActivity}
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        position--; //account for header
        super.onListItemClick(l, v, position, id);
        if (mAdapter.getCount() < 1) return;

        Email email = mAdapter.getItem(position);
        EmailStore.markEmailRead(email.getId());

        if (!email.isDeleted()) {
            getNavigationActivity().openEmailPage(email);
        }
    }

    @Override
    public void getData() {
        if (!loading && !noMore) {
            loading = true;
            mFooter.setLoading();
            EmailStore.getEmails(limit, offset, new Callback(callbackManager));
        }
    }

    /**
     * Actions to perform when network call for notifications returns.
     */
    private static class Callback extends CallbackManager.NetworkCallback<EmailNotificationFragment> {
        public Callback(CallbackManager<EmailNotificationFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(EmailNotificationFragment object) {
            if (wasSuccessful()) {
                object.onSuccess(response);
            } else {
                AppSession.showDataLoadError("email");
            }
        }

        @Override
        public void onResumeWithError(EmailNotificationFragment object) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeAfter(EmailNotificationFragment object) {
            object.onLoadingComplete();
        }
    }

    /**
     * Called when data successfully retrieved from server.
     * @param response data from server
     */
    public void onSuccess(JSONObject response) {
        ArrayList<Email> emails = EmailStore.getListData(response);

        if (emails != null) {
            mAdapter.addAll(emails);
            int nextOffset = StoreUtil.getNextOffset(response);
            if (nextOffset != -1) offset = nextOffset;
        } else {
            noMore = true;
        }
    }

    @Override
    public boolean hasNewData() {
        UserNewMessage userNewMessage = AppSession.getInstance().getUserNewMessage();
        int count = userNewMessage != null ? userNewMessage.getEmailCount() : 0;

        return count > 0;
    }

    /**
     * Called when network call returns.  Sets flags, resets notification count display to zero,
     * updates footer.
     */
    public void onLoadingComplete() {
        loading = false;

        setEmailDisplayZero();

        if (mAdapter.getCount() == 0) mFooter.showMessage(NO_EMAIL);
        else mFooter.clear();
    }

    /**
     * Sets email notification data to zero and then updates the view.
     */
    private void setEmailDisplayZero() {
        UserNewMessage userNewMessage = AppSession.getInstance().getUserNewMessage();
        if (userNewMessage != null) {
            userNewMessage.clearEmailNotifications();
            AppSession.getInstance().setUserNewMessage(userNewMessage);
        }

        ((NavigationActivity) getActivity()).setAllNotificationDisplays();
    }
}
