package com.thecn.app.fragments.NotificationFragments;

import android.os.Bundle;

import com.thecn.app.AppSession;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.notification.UserNewMessage;
import com.thecn.app.stores.NotificationStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;

/**
 * Shows general notifications, i.e. notifications that are not follower notifications or email notifications.
 */
public class GeneralNotificationFragment extends NotificationFragment {

    private static final String TITLE = "Notifications";
    private static final String NONE_MESSAGE = "You have no new notifications.";

    private CallbackManager<GeneralNotificationFragment> callbackManager;

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
     * Uses {@link com.thecn.app.stores.NotificationStore#getNotifications(int, int, com.thecn.app.stores.ResponseCallback)}
     */
    @Override
    public void getNotifications() {
        NotificationStore.getNotifications(limit, offset, new Callback(callbackManager));
    }

    /**
     * Actions to perform when network call returns.
     */
    private static final class Callback extends CallbackManager.NetworkCallback<GeneralNotificationFragment> {
        public Callback(CallbackManager<GeneralNotificationFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(GeneralNotificationFragment object) {
            if (wasSuccessful()) {
                object.onSuccess(response);
            } else {
                AppSession.showDataLoadError(object.getTitle().toLowerCase());
            }
        }

        @Override
        public void onResumeWithError(GeneralNotificationFragment object) {
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeAfter(GeneralNotificationFragment object) {
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
        int count = message != null ? message.getGenNotificationCount() : 0;

        return count > 0;
    }

    @Override
    public void setNotificationDisplayZero() {
        UserNewMessage message = AppSession.getInstance().getUserNewMessage();
        if (message != null) {
            message.clearGeneralNotifications();
            AppSession.getInstance().setUserNewMessage(message);
        }

        ((NavigationActivity) getActivity()).setAllNotificationDisplays();
    }

    @Override
    public String getNoneMessage() {
        return NONE_MESSAGE;
    }
}
