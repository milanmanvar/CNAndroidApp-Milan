package com.thecn.app.activities.profile;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;

/**
 * Master activity for showing profile information about a user.
 */
public class ProfileActivity extends NavigationActivity {

    private ProgressBar progressBar;
    private TextView message;
    private Button button;

    /**
     * Set up action bar and views, set up data fragment.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarAndTitle(getResources().getString(R.string.profile));

        progressBar = (ProgressBar) findViewById(R.id.activityProgressBar);

        View view = findViewById(R.id.activity_message_button_view);
        message = (TextView) view.findViewById(R.id.message);
        button = (Button) view.findViewById(R.id.button);

        Resources r = getResources();
        message.setText(r.getString(R.string.data_load_error));
        button.setText(r.getString(R.string.retry));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataFragment().loadUser();
            }
        });

        if (savedInstanceState == null) {
            setLoadingBackground();

            DataFragment f = DataFragment.getInstance(this);

            if (f == null) {
                finishWithError();
                return;
            }

            FragmentManager manager = getSupportFragmentManager();

            manager.beginTransaction()
                    .add(f, DataFragment.TAG)
                    .commit();

            manager.executePendingTransactions();

        } else {
            DataFragment f = getDataFragment();

            if (f.loading) {
                setLoadingBackground();
            } else if (f.user == null) {
                setErrorBackground();
            } else {
                setActionBarAndTitle(getUser().getDisplayName());
            }
        }
    }

    /**
     * Set the view in a loading state
     */
    private void setLoadingBackground() {
        progressBar.setVisibility(View.VISIBLE);
        message.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
    }

    /**
     * Set the view to show an error
     */
    private void setErrorBackground() {
        progressBar.setVisibility(View.GONE);
        button.setVisibility(View.VISIBLE);
        message.setVisibility(View.VISIBLE);
    }

    /**
     * Clear the background of the view
     */
    private void clearBackground() {
        progressBar.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        message.setVisibility(View.GONE);
    }

    /**
     * Get the user
     * @return user associated with profile activity
     */
    public User getUser() {
        return getDataFragment().user;
    }

    /**
     * Get data fragment
     * @return data fragment of this activity
     */
    public DataFragment getDataFragment() {
        return (DataFragment) getSupportFragmentManager().findFragmentByTag(DataFragment.TAG);
    }

    /**
     * SHow an error and finish
     */
    public void finishWithError() {
        AppSession.showDataLoadError("profile");
        finish();
    }

    /**
     * Override to make sure duplicate profile is not opened.
     */
    @Override
    public void openProfileByID(String id) {
        // dont open duplicate profile page
        if (!getUser().getId().equals(id)) {
            super.openProfileByID(id);
        } else {
            closeNotificationDrawer();
        }
    }

    /**
     * Override to make sure duplicate profile not opened.
     */
    @Override
    public void openProfileByCNNumber(String cnNumber) {
        if (!getUser().getCNNumber().equals(cnNumber)) {
            super.openProfileByCNNumber(cnNumber);
        } else {
            closeNotificationDrawer();
        }
    }

    /**
     * Used to load and hold user data for profile activity
     */
    public static class DataFragment extends Fragment {

        public static final String TAG = "ProfileActivity.ProfileFragment";

        //true for ID, false for CNNumber
        private static final String TYPE_TAG = "type_tag";
        private static final String ID_TAG = "id_tag";

        private User user;
        private boolean loading;

        private CallbackManager<DataFragment> callbackManager;

        /**
         * Get instance with arguments
         * @param activity activity this fragment will associate with.
         * @return new instance of this class
         */
        public static DataFragment getInstance(Activity activity) {
            Intent intent = activity.getIntent();

            //determine whether to load by id or by cn number
            String identifier = intent.getStringExtra("user_id");
            if (identifier == null || identifier.isEmpty()) {
                identifier = intent.getStringExtra("cn_number");
                if (identifier == null || identifier.isEmpty()) {
                    return null;
                }

                return get(identifier, false);
            }

            return get(identifier, true);
        }

        /**
         * Get instance with arguments
         * @param identifier either cn number or id of user
         * @param type true for by id, false for by cn number
         * @return new instance of this class
         */
        private static DataFragment get(String identifier, boolean type) {
            Bundle args = new Bundle();
            args.putBoolean(TYPE_TAG, type);
            args.putString(ID_TAG, identifier);

            DataFragment f = new DataFragment();
            f.setArguments(args);
            return f;
        }

        /**
         * Set up and begin loading
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            callbackManager = new CallbackManager<>();

            callbackManager.addCallback(new CallbackManager.Callback<DataFragment>() {
                @Override
                public void execute(DataFragment object) {
                    loadUser();
                }
            });
        }

        /**
         * Start loading data.  Set view for loading.
         */
        public void loadUser() {
            loading = true;

            getProfileActivity().setLoadingBackground();

            String identifier = getArguments().getString(ID_TAG);
            if (getArguments().getBoolean(TYPE_TAG)) {
                UserStore.getUserById(identifier, new Callback(callbackManager));
            } else {
                UserStore.getUserByCNNumber(identifier, new Callback(callbackManager));
            }
        }

        /**
         * Called when network call for user data returns.  Used to make decisions.
         */
        private static class Callback extends CallbackManager.NetworkCallback<DataFragment> {

            private User user;
            private CallbackManager<DataFragment> callbackManager;

            public Callback(CallbackManager<DataFragment> callbackManager) {
                super(callbackManager);
                this.callbackManager = callbackManager;
            }

            @Override
            public void onResumeWithResponse(DataFragment object) {
                if (!onCheckSuccess(object)) return;

                user = UserStore.getData(response);

                //process user basic info beforehand
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final User user = UserStore.getData(response);
                        //pre processing
                        user.getBasicInfoSpannable();
                        callbackManager.addCallback(new ReadyCallback());
                    }
                }).start();
            }

            /**
             * Used when processing done for user's basic info and activity is
             * ready to add {@link com.thecn.app.activities.profile.ProfileHomeFragment}
             */
            private class ReadyCallback extends CallbackManager.Callback<DataFragment> {
                @Override
                public void execute(DataFragment object) {
                    ProfileActivity activity = object.getProfileActivity();

                    object.user = user;
                    object.loading = false;

                    //add profile home fragment
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new ProfileHomeFragment(), ProfileHomeFragment.TAG)
                            .commit();

                    activity.clearBackground();
                }
            }

            /**
             * Check the success of network call
             * @param object fragment
             * @return true if successful, false if failure
             */
            public boolean onCheckSuccess(DataFragment object) {
                if (!wasSuccessful()) {
                    object.loading = false;
                    object.getProfileActivity().setErrorBackground();
                    return false;
                }

                return true;
            }

            /**
             * Show error
             * @param object fragment
             */
            @Override
            public void onResumeWithError(DataFragment object) {
                object.loading = false;
                StoreUtil.showExceptionMessage(error);
                object.getProfileActivity().setErrorBackground();
            }
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
         * Cast activity to ProfileActivity
         * @return cast activity
         */
        public ProfileActivity getProfileActivity() {
            return (ProfileActivity) getActivity();
        }
    }
}