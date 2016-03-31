package com.thecn.app.activities.verification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.homefeed.HomeFeedActivity;
import com.thecn.app.activities.picturechooser.PictureChooseActivity;
import com.thecn.app.activities.picturechooser.UploadTask;
import com.thecn.app.fragments.common.ProgressDialogFragment;
import com.thecn.app.models.profile.Avatar;
import com.thecn.app.models.user.User;
import com.thecn.app.models.user.UserProfile;
import com.thecn.app.models.util.VerificationBundle;
import com.thecn.app.stores.BaseStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.volley.MyVolley;

import org.json.JSONObject;

import java.util.HashMap;

/**
* Fragment user can use to change his/her profile picture before finishing verification.
*/
public class ProfilePictureFragment extends BaseFragment {

    private ImageView picture;

    private ProgressBar progressBar;

    private BroadcastReceiver receiver;

    private String picURL; //location of picture

    private ImageCallback callback;

    private Button laterButton; //click to do this later

    private static final String TITLE = "Profile Picture";

    private static final String ERROR = "Could not upload new picture.  Please try again";
    private static final String IMAGE_LOAD_ERROR = "Could not fetch picture";
    private static final String DONE = "Done";
    private static final String LATER = "Later";

    private CallbackManager<ProfilePictureFragment> callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        callbackManager = new CallbackManager<>();
    }

    /**
     * Set action bar title
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActionBarActivity().getSupportActionBar().setTitle(TITLE);
    }

    /**
     * Cancel preexisting callback and load user's current avatar
     */
    private void loadUserAvatar() {
        if (callback != null) callback.cancel();
        callback = new ImageCallback(callbackManager);
        progressBar.setVisibility(View.VISIBLE);

        VerificationBundle bundle = AppSession.getInstance().getVerificationBundle();
        if (bundle == null) {
            finishWithSessionEndMessage();
            return;
        }

        UserStore.getUserByIdPassToken(bundle.user.getId(), bundle.token, callback);
    }

    /**
     * Used when network call for user avatar returns.  Makes decisions based on result.
     */
    private static class ImageCallback extends CallbackManager.NetworkCallback<ProfilePictureFragment> {
        public ImageCallback(CallbackManager<ProfilePictureFragment> manager) {
            super(manager);
        }

        /**
         * Checks for verification validity.  If successful, update data and view.
         */
        @Override
        public void onResumeWithResponse(ProfilePictureFragment object) {
            VerificationBundle bundle = AppSession.getInstance().getVerificationBundle();
            if (bundle == null) {
                object.finishWithSessionEndMessage();
                return;
            }

            if (wasSuccessful()) {
                User user = UserStore.getData(response);
                if (user != null && user.getAvatar() != null) {
                    Avatar a = user.getAvatar();
                    bundle.user.setAvatar(a);
                    object.picURL = a.getView_url();
                    object.setImageView();
                }
            } else {
                object.progressBar.setVisibility(View.GONE);
                // the user has no profile picture set yet
                object.laterButton.setText(LATER);
            }
        }

        /**
         * Show loading error
         */
        @Override
        public void onResumeWithError(ProfilePictureFragment object) {
            object.progressBar.setVisibility(View.GONE);
            AppSession.showLongToast(IMAGE_LOAD_ERROR);
        }
    }

    /**
     * Begin loading picture at given url
     */
    private void setImageView() {
        if (picURL == null) return;

        MyVolley.ImageParams params = new MyVolley.ImageParams(picURL, picture);
        params.placeHolderID = params.errorImageResourceID = R.color.nav_bar;
        params.maxWidth = picture.getWidth();
        params.maxHeight = picture.getHeight();
        params.listener = new ImageListener(callbackManager, params);

        MyVolley.loadImage(params);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_pic_verification, null, false);
    }

    /**
     * Get references, set on click listeners, set up broadcast receiver for
     * receiving image change updates.
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        picture = (ImageView) view.findViewById(R.id.pic_display);
        //start picture choose activity
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PictureChooseActivity.class));
            }
        });

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        loadUserAvatar();

        //reload user avatar when updated
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!intent.getBooleanExtra(UploadTask.SUCCESS_TAG, false)) {
                    AppSession.showLongToast(ERROR);
                    return;
                }

                loadUserAvatar();
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,
                new IntentFilter(UploadTask.AVATAR_SUCCESS));


        laterButton = (Button) view.findViewById(R.id.later_button);
        //complete verification on click
        laterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verify();
            }
        });
    }

    /**
     * Unregister broadcast receiver
     */
    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onDestroyView();
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

    /**
     * Custom image listener for when network call for image returns.
     */
    private static class ImageListener extends MyVolley.MyImageListener {
        private CallbackManager<ProfilePictureFragment> callbackManager;

        public ImageListener(CallbackManager<ProfilePictureFragment> callbackManager, MyVolley.ImageParams params) {
            super(params);
            this.callbackManager = callbackManager;
        }

        @Override
        public void myOnResponse(Bitmap bm, boolean isImmediate) {
            //hide progress bar
            callbackManager.addCallback(new CallbackManager.Callback<ProfilePictureFragment>() {
                @Override
                public void execute(ProfilePictureFragment object) {
                    object.progressBar.setVisibility(View.GONE);
                }
            });

            super.myOnResponse(bm, isImmediate);
            if (bm == null) return;

            //if an image loaded, change button text to "done" instead of "later"
            callbackManager.addCallback(new CallbackManager.Callback<ProfilePictureFragment>() {
                @Override
                public void execute(ProfilePictureFragment object) {
                    object.laterButton.setText(DONE);
                }
            });
        }
    }

    /**
     *
     */
    private void verify() {
        VerificationBundle bundle = AppSession.getInstance().getVerificationBundle();
        if (bundle == null) {
            finishWithSessionEndMessage();
            return;
        }

        ProgressDialogFragment.show(getActivity());

        //post user to the server
        BaseStore.APIParams params = new BaseStore.APIParams(
                "/me/?with_user_count=1&with_user_country=1&with_user_courses=1&with_user_conexuses=1",
                Request.Method.POST,
                new GetMeCallback(callbackManager)
        );
        params.headers = new HashMap<>();
        params.headers.put("token", bundle.token);
        params.jsonObject = getJSONUser();

        BaseStore.api(params);
    }

    /**
     * Get user modeled as JSONObject from information in {@link com.thecn.app.models.util.VerificationBundle}
     * @return json model of user
     */
    private JSONObject getJSONUser() {
        VerificationBundle bundle = AppSession.getInstance().getVerificationBundle();

        try {
            bundle.user.setCountry(bundle.country);

            UserProfile profile = bundle.user.getUserProfile();
            if (profile == null) {
                profile = new UserProfile();
            }

            profile.setTimeZoneID(bundle.timeZone.getId());
            profile.setCountryID(bundle.country.getId());

            bundle.user.setUserProfile(profile);

            User.VisibilitySettings settings = bundle.user.getVisibilitySettings();
            if (settings == null) {
                settings = new User.VisibilitySettings();
            }

            settings.setTimeZoneID(bundle.timeZoneVisibility.toJsonString());
            settings.setCountryID(bundle.countryVisibility.toJsonString());

            bundle.user.setVisibilitySettings(settings);

            Gson gson = new Gson();
            String jsonString = gson.toJson(bundle.user, User.class);

            return new JSONObject(jsonString);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Actions to perform when request for posting user returns.
     */
    private static class GetMeCallback extends CallbackManager.NetworkCallback<ProfilePictureFragment> {

        private CallbackManager<ProfilePictureFragment> manager;

        public GetMeCallback(CallbackManager<ProfilePictureFragment> manager) {
            super(manager);
            this.manager = manager;
        }

        /**
         * Show errors if present, else load data, clear session, and push {@link com.thecn.app.activities.homefeed.HomeFeedActivity}
         */
        @Override
        public void onResumeWithResponse(ProfilePictureFragment object) {
            if (object.loadingDialogCancelled()) return;

            User user = UserStore.getData(response);
            if (user == null) {
                AppSession.showDataLoadError("user");
                object.dismissLoadingDialog();
                return;
            }

            final AppSession session = AppSession.getInstance();
            session.setUser(user);
            session.setToken(session.getVerificationBundle().token);
            session.getUserNewMessageFromServer();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    session.getSettingsFromDatabase();

                    manager.addCallback(new CallbackManager.Callback<ProfilePictureFragment>() {
                        @Override
                        public void execute(ProfilePictureFragment object) {
                            if (object.loadingDialogCancelled()) {
                                AppSession.getInstance().clearSession();
                                return;
                            }

                            VerificationActivity activity = (VerificationActivity) object.getActionBarActivity();

                            //push home feed
                            Intent intent = new Intent(activity, HomeFeedActivity.class);
                            intent.putExtra(VerificationActivity.VERIFICATION_INTENT_TAG, true);
                            activity.startActivity(intent);
                            activity.finish();
                        }
                    });
                }
            }).start();
        }

        @Override
        public void onResumeWithError(ProfilePictureFragment object) {
            if (object.loadingDialogCancelled()) return;

            object.dismissLoadingDialog();
            StoreUtil.showExceptionMessage(error);
        }
    }

    /**
     * Tells whether the loading dialog is showing.
     * @return true if loading dialog not showing
     */
    public boolean loadingDialogCancelled() {
        return ProgressDialogFragment.get(getActivity()) == null;
    }

    /**
     * Dismisses loading dialog.
     */
    public void dismissLoadingDialog() {
        ProgressDialogFragment.dismiss(getActivity());
    }
}
