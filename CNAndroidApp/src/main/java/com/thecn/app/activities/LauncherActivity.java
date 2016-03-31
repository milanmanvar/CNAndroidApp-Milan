package com.thecn.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.activities.homefeed.HomeFeedActivity;
import com.thecn.app.activities.login.LoginActivity;
import com.thecn.app.broadcastreceivers.AlertNotificationReceiver;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.ConexusStore;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Very first activity launched when a new instance of the application {@link com.thecn.app.CNApp} is created.
 */
public class LauncherActivity extends FragmentActivity {

    private static final String FRAGMENT_TAG = "setup_fragment";

    /**
     * Initializes, adds setup fragment
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            SetupFragment f = new SetupFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(f, FRAGMENT_TAG)
                    .commit();
        }
    }

    /**
     * Used to perform certain functions before pushing either {@link com.thecn.app.activities.homefeed.HomeFeedActivity}
     * or {@link com.thecn.app.activities.login.LoginActivity}
     */
    public static class SetupFragment extends Fragment {
        private CallbackManager<SetupFragment> callbackManager;

        /**
         * Calls either {@link #setup()} or {@link #pushLoginActivity()}
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            callbackManager = new CallbackManager<>();

            if (AppSession.getInstance().isLoggedIn()) {
                setup();
            } else {
                //if not logged in, push login activity
                pushLoginActivity();
            }
        }

        /**
         * Initializes data, starts notification service, refreshes user data,
         * starts {@link com.thecn.app.activities.homefeed.HomeFeedActivity}
         */
        private void setup() {
            AppSession session = AppSession.getInstance();
            session.getSettingsFromDatabase();

            Activity activity = getActivity();

            //start notification service
            activity.sendBroadcast(new Intent(activity, AlertNotificationReceiver.class));

            //update user in case updated elsewhere
            UserStore.getMe(new GetMeCallback());

            Intent intent = new Intent(activity, HomeFeedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
            getActivity().finish();
        }

        /**
         * Actions to perform when get me network request returns
         */
        public static class GetMeCallback implements ResponseCallback {
            private User user;

            /**
             * Check for errors.
             * Get all user courses and conexuses, update user session data.
             */
            @Override
            public void onResponse(JSONObject response) {
                final AppSession session = AppSession.getInstance();
                user = UserStore.getData(response);
                if (user == null) return;

                UserStore.getAllUserCourses(new ResponseCallback() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Course> courses = CourseStore.getListData(response);
                        user.setCourses(courses);

                        UserStore.getAllUserConexuses(new ResponseCallback() {
                            @Override
                            public void onResponse(JSONObject response) {
                                ArrayList<Conexus> conexuses = ConexusStore.getListData(response);
                                user.setConexuses(conexuses);

                                session.setUser(user);
                                session.setUserUpdateNeeded(false);
                                session.setCourseUpdateNeeded(false);
                                session.setConexusUpdateNeeded(false);
                            }

                            @Override
                            public void onError(VolleyError error) {
                                session.setConexusUpdateNeeded(true);
                            }
                        });
                    }

                    @Override
                    public void onError(VolleyError error) {
                        session.setCourseUpdateNeeded(true);
                    }
                });
            }

            @Override
            public void onError(VolleyError error) {
                AppSession.getInstance().setUserUpdateNeeded(true);
            }
        }

        /**
         * Pushes {@link com.thecn.app.activities.login.LoginActivity}
         */
        private void pushLoginActivity() {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
            getActivity().finish();
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
    }
}
