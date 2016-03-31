package com.thecn.app.fragments.NotificationFragments;

import android.os.Bundle;

import com.thecn.app.AppSession;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.notification.UserNewMessage;
import com.thecn.app.stores.NotificationStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;

/**
 * Fragment for showing notifications of new followers.
 */
public class FollowNotificationFragment extends NotificationFragment {

    private static final String TITLE = "New Followers";
    private static final String NONE_MESSAGE = "You have no new followers.";

    private CallbackManager<FollowNotificationFragment> callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = new CallbackManager<>();
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
     * Uses {@link com.thecn.app.stores.NotificationStore#getFollowNotifications(int, int, com.thecn.app.stores.ResponseCallback)}
     */
    @Override
    public void getNotifications() {
        NotificationStore.getFollowNotifications(limit, offset, new Callback(callbackManager));
    }

    /**
     * Actions for when network call returns.
     */
    private static final class Callback extends CallbackManager.NetworkCallback<FollowNotificationFragment> {
        public Callback(CallbackManager<FollowNotificationFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(FollowNotificationFragment object) {
            if (wasSuccessful()) {
                object.onSuccess(response);
            } else {
                AppSession.showDataLoadError(object.getTitle().toLowerCase());
            }
        }

        @Override
        public void onResumeWithError(FollowNotificationFragment object) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeAfter(FollowNotificationFragment object) {
            object.onLoadingComplete();
        }
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public boolean hasNewData() {
        UserNewMessage message = AppSession.getInstance().getUserNewMessage();
        int count = message != null ? message.getFollowerCount() : 0;

        return count > 0;
    }

    @Override
    public void setNotificationDisplayZero() {
        UserNewMessage message = AppSession.getInstance().getUserNewMessage();
        if (message != null) {
            message.clearFollowerNotifications();
            AppSession.getInstance().setUserNewMessage(message);
        }

        ((NavigationActivity) getActivity()).setAllNotificationDisplays();
    }

    @Override
    public String getNoneMessage() {
        return NONE_MESSAGE;
    }
}
