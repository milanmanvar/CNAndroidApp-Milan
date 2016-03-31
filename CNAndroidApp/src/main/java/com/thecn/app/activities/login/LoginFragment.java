package com.thecn.app.activities.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.thecn.app.AppSession;
import com.thecn.app.broadcastreceivers.AlertNotificationReceiver;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.AuthStore;
import com.thecn.app.stores.ConexusStore;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;

/**
* Created by philjay on 3/2/15.
*/
public class LoginFragment extends Fragment {

    private CallbackManager<LoginFragment> callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        callbackManager = new CallbackManager<>();
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        callbackManager.resume(this);
    }

    public void login(String userName, String password) {
        AuthStore.login(userName, password, new LoginCallback(callbackManager));
    }

    private void getMe() {
        UserStore.getMe(new GetMeCallback(callbackManager));
    }

    public static class LoginCallback extends CallbackManager.NetworkCallback<LoginFragment> {
        public LoginCallback(CallbackManager<LoginFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(LoginFragment object) {
            LoginActivity activity = object.getLoginActivity();
            if (!wasSuccessful()) {
                StoreUtil.showFirstResponseError(response);
                activity.setLoading(false);
                return;
            }

            if (AuthStore.setToken(response) && activity.isLoading()) {
                object.getMe();
            } else {
                activity.setLoading(false);
            }
        }

        @Override
        public void onResumeWithError(LoginFragment object) {
            StoreUtil.showExceptionMessage(error);
            object.getLoginActivity().setLoading(false);
        }
    }

    public static class GetMeCallback extends CallbackManager.NetworkCallback<LoginFragment> {
        private User user;

        public GetMeCallback(CallbackManager<LoginFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(LoginFragment object) {
            LoginActivity activity = object.getLoginActivity();
            if (!activity.isLoading()) return;

            if (!wasSuccessful()) {
                StoreUtil.showFirstResponseError(response);
                activity.setLoading(false);
                return;
            }

            user = UserStore.getData(response);
            if (user == null) {
                onFailure(object);
                return;
            }


            final AppSession session = AppSession.getInstance();

            UserStore.getAllUserCourses(new CallbackManager.NetworkCallback<LoginFragment>(manager) {
                @Override
                public void onResumeWithResponse(LoginFragment object) {
                    if (!object.getLoginActivity().isLoading()) return;

                    user.setCourses(CourseStore.getListData(response));

                    UserStore.getAllUserConexuses(new CallbackManager.NetworkCallback<LoginFragment>(manager) {
                        @Override
                        public void onResumeWithResponse(LoginFragment object) {
                            if (!object.getLoginActivity().isLoading()) return;

                            user.setConexuses(ConexusStore.getListData(response));

                            session.setUser(user);
                            object.getSettings();
                            //get new messages upon login
                            session.getUserNewMessageFromServer();
                        }

                        @Override
                        public void onResumeWithError(LoginFragment object) {
                            onFailure(object);
                        }
                    });
                }

                @Override
                public void onResumeWithError(LoginFragment object) {
                    onFailure(object);
                }
            });
        }

        @Override
        public void onResumeWithError(LoginFragment object) {
            StoreUtil.showExceptionMessage(error);
            object.getLoginActivity().setLoading(false);
        }

        private void onFailure(LoginFragment object) {
            AppSession.showDataLoadError("user");
            object.getLoginActivity().setLoading(false);
        }
    }

    public void getSettings() {
        new Thread(new GetSettingsRunnable(callbackManager))
                .run();
    }

    private static class GetSettingsRunnable implements Runnable {
        private CallbackManager<LoginFragment> callbackManager;

        public GetSettingsRunnable(CallbackManager<LoginFragment> manager) {
            callbackManager = manager;
        }

        @Override
        public void run() {
            AppSession.getInstance().getSettingsFromDatabase();
            callbackManager.addCallback(new GetSettingsCallback());
        }
    }

    private static class GetSettingsCallback extends CallbackManager.Callback<LoginFragment> {
        @Override
        public void execute(LoginFragment object) {
            LoginActivity activity = object.getLoginActivity();

            if (activity.isLoading()) {
                //start notification service
                activity.sendBroadcast(new Intent(activity, AlertNotificationReceiver.class));
                activity.launchHomeActivity();
            }
        }
    }

    public LoginActivity getLoginActivity() {
        return (LoginActivity) getActivity();
    }
}
