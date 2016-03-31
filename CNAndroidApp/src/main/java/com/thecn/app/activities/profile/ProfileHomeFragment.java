package com.thecn.app.activities.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ListView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.picturechooser.UploadTask;
import com.thecn.app.fragments.BasePostListFragment;
import com.thecn.app.models.user.User;
import com.thecn.app.models.user.UserProfile;
import com.thecn.app.services.UpdateService;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.network.PostLoader;

/**
* Shows the profile header and a list of this user's posts below it.
*/
public class ProfileHomeFragment extends BasePostListFragment {

    public static final String TAG = ProfileHomeFragment.class.getSimpleName();

    private final Object userLock = new Object(); //prevents race conditions
    private UpdateService updateService; //navigation activity's bound service
    private UpdateService.Updater userUpdater; //used to periodically update user

    private ProfileHeaderController mProfileHeaderController;
    private CallbackManager<ProfileHomeFragment> callbackManager;

    private BroadcastReceiver avatarUpdateReceiver; //receives updates to user avatar

    private static final String NEW_POSTS = "New Posts";
    private static final String NEW_REFLECTIONS = "New Reflections";
    private static final String MOST_LIKED = "Most Liked Posts";
    private static final String MOST_REFLECTED = "Most Reflected Posts";
    private static final String MOST_VISITED = "Most Visited Links";

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getProfileActivity().setActionBarAndTitle(getUser().getDisplayName());
    }

    /**
     * Get list of methods for displaying posts in list.
     * @return list of post methods
     */
    @Override
    public PostLoader.MethodList getPostLoaderMethodList() {
        PostLoader.MethodList list = new PostLoader.MethodList();
        PostLoader.Method method;
        list.startSecondList();

        PostLoader.Method baseMethod = new PostLoader.Method(PostLoader.SOURCE_USER);
        baseMethod.id = getUser().getId();

        method = new PostLoader.Method(baseMethod);
        method.name = NEW_POSTS;
        list.add(method);

        method = new PostLoader.Method(baseMethod);
        method.name = NEW_REFLECTIONS;
        method.filterType = PostLoader.FILTER_NEW_REFLECTIONS;
        list.add(method);

        method = new PostLoader.Method(baseMethod);
        method.name = MOST_LIKED;
        method.filterType = PostLoader.FILTER_MOST_LIKED;
        list.add(method);

        method = new PostLoader.Method(baseMethod);
        method.name = MOST_REFLECTED;
        method.filterType = PostLoader.FILTER_MOST_REFLECTED;
        list.add(method);

        method = new PostLoader.Method(baseMethod);
        method.name = MOST_VISITED;
        method.filterType = PostLoader.FILTER_MOST_VISITED;
        list.add(method);

        list.setAllowContentTypeChange(true);

        return list;
    }

    //receivers for changes made to the profile
    private IntroChangeReceiver introChangeReceiver;
    private BasicInfoChangeReceiver infoChangeReceiver;
    private PicChangeReceiver picChangeReceiver;
    private BannerChangeReceiver bannerChangeReceiver;

    /**
     * Set up broadcast receivers if user is me for changes user might make to profile.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = new CallbackManager<>(getActivity());

        if (getUser().isMe()) {
            introChangeReceiver = new IntroChangeReceiver(callbackManager);
            infoChangeReceiver = new BasicInfoChangeReceiver(callbackManager);
            picChangeReceiver = new PicChangeReceiver(callbackManager);
            bannerChangeReceiver = new BannerChangeReceiver(callbackManager);
            LocalBroadcastManager bcManager = LocalBroadcastManager.getInstance(getActivity());
            bcManager.registerReceiver(
                    introChangeReceiver,
                    new IntentFilter(ProfileHeaderController.INTRO_CHANGE_SUCCESS)
            );
            bcManager.registerReceiver(
                    infoChangeReceiver,
                    new IntentFilter(ProfileHeaderController.INFO_CHANGE_SUCCESS)
            );
            bcManager.registerReceiver(
                    picChangeReceiver,
                    new IntentFilter(UploadTask.AVATAR_SUCCESS)
            );
            bcManager.registerReceiver(
                    bannerChangeReceiver,
                    new IntentFilter(UploadTask.BANNER_SUCCESS)
            );
        }

        initUserUpdater();
    }

    /**
     * Handles changes to basic information of user.
     */
    private static class BasicInfoChangeReceiver extends BroadcastReceiver {
        private CallbackManager<ProfileHomeFragment> manager;

        public BasicInfoChangeReceiver(CallbackManager<ProfileHomeFragment> manager) {
            this.manager = manager;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            manager.addCallback(new CallbackManager.Callback<ProfileHomeFragment>() {
                @Override
                public void execute(ProfileHomeFragment object) {
                    User user = object.getUser();
                    UserProfile profile = user.getUserProfile();
                    User localUser = AppSession.getInstance().getUser();
                    UserProfile localProfile = localUser.getUserProfile();

                    //update data
                    synchronized (object.userLock) {
                        user.setCountry(localUser.getCountry());
                        profile.setGender(localProfile.getGender());
                        profile.setPrimaryEmail(localProfile.getPrimaryEmail());
                        profile.setPrimaryLanguage(localProfile.getPrimaryLanguage());
                        profile.setPrimaryLanguageID(localProfile.getPrimaryLanguageID());
                        profile.setCountryID(localProfile.getCountryID());
                        profile.setTimeZone(localProfile.getTimeZone());
                        profile.setTimeZoneID(localProfile.getTimeZoneID());
                    }

                    object.getProfileHeaderController().layoutInfo(user);
                }
            });
        }
    }

    /**
     * Handles changes to user's introduction and tagline
     */
    private static class IntroChangeReceiver extends BroadcastReceiver {
        private CallbackManager<ProfileHomeFragment> manager;

        public IntroChangeReceiver(CallbackManager<ProfileHomeFragment> manager) {
            this.manager = manager;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            manager.addCallback(new CallbackManager.Callback<ProfileHomeFragment>() {
                @Override
                public void execute(ProfileHomeFragment object) {
                    User user = object.getUser();
                    UserProfile profile = user.getUserProfile();
                    UserProfile localProfile = AppSession.getInstance().getUser().getUserProfile();
                    String about = localProfile.getAbout();
                    String tagline = localProfile.getTagLine();

                    //update data
                    synchronized (object.userLock) {
                        profile.setAbout(about);
                        profile.setTagLine(tagline);
                    }

                    object.getProfileHeaderController().layoutIntro(user);
                }
            });
        }
    }

    /**
     * Handles changes to user's profile picture
     */
    private static class PicChangeReceiver extends BroadcastReceiver {
        private CallbackManager<ProfileHomeFragment> manager;

        public PicChangeReceiver(CallbackManager<ProfileHomeFragment> manager) {
            this.manager = manager;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            manager.addCallback(new CallbackManager.Callback<ProfileHomeFragment>() {
                @Override
                public void execute(ProfileHomeFragment object) {
                    object.userUpdater.update();
                }
            });
        }
    }

    /**
     * Handles changes to user's banner image
     */
    private static class BannerChangeReceiver extends BroadcastReceiver {
        private CallbackManager<ProfileHomeFragment> manager;

        public BannerChangeReceiver(CallbackManager<ProfileHomeFragment> manager) {
            this.manager = manager;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            manager.addCallback(new CallbackManager.Callback<ProfileHomeFragment>() {
                @Override
                public void execute(ProfileHomeFragment object) {
                    String bannerURL =
                            AppSession.getInstance().getUser().getUserProfile().getBannerURL();
                    User user = object.getUser();
                    UserProfile profile = user.getUserProfile();

                    synchronized (object.userLock) {
                        profile.setBannerURL(bannerURL);
                    }

                    object.getProfileHeaderController().layoutBanner(user);
                }
            });
        }
    }

    /**
     * Remove this fragment and insert a new one.
     */
    @Override
    public void onRefresh() {

        ProfileActivity a = getProfileActivity();
        //make sure no more network calls return for this fragment
        callbackManager.pause();
        mProfileHeaderController.resetFollowingUsers();
        a.getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .remove(this)
                .commit();

        //reload
        a.getDataFragment().loadUser();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (callbackManager != null) callbackManager.setActivity(activity);
    }

    @Override
    public void onDetach() {
        if (callbackManager != null) callbackManager.setActivity(null);
        super.onDetach();
    }

    /**
     * Periodically refreshes following status
     */
    private void initUserUpdater() {
        updateService = getNavigationActivity().getUpdateService();
        if (updateService == null) return;

        final String userId = getUser().getId();
        userUpdater = new UpdateService.Updater() {
            @Override
            public void update() {
                UserStore.getUserById(userId, new UserUpdateCallback(callbackManager));
            }
        };

        updateService.addUpdater(userUpdater);
    }

    /**
     * Refresh following status data and following button
     */
    public static class UserUpdateCallback extends CallbackManager.NetworkCallback<ProfileHomeFragment> {
        public UserUpdateCallback(CallbackManager<ProfileHomeFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(ProfileHomeFragment object) {
            if (!wasSuccessful()) return;

            User user = UserStore.getData(response);
            if (user == null) return;
            User.Relations r = user.getRelations();
            if (r == null) return;

            synchronized (object.getUserLock()) {
                User thisUser = object.getUser();
                thisUser.setRelations(user.getRelations());
            }

            ProfileHeaderController controller = object.getProfileHeaderController();
            controller.setProfilePicture(user);

            if (user.isMe()) return;
            controller.setFollowButtonState(r.isFollowing());
        }
    }

    /**
     * Set up profile header and list view.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mProfileHeaderController != null) mProfileHeaderController.resetFollowingUsers();

        View headerView = getLayoutInflater(savedInstanceState).inflate(R.layout.profile_header, null);
        getListView().addHeaderView(headerView, null, false);
        setListViewScrollListener();

        mProfileHeaderController = new ProfileHeaderController(getUser(), headerView, callbackManager);
        mProfileHeaderController.setUpHeader(getUser());
    }

    /**
     * Shows entire activity dedicated to showing user's introduction.
     */
    public void showIntroFragment() {
        ProfileActivity activity = getProfileActivity();
        Intent intent = new Intent(activity, IntroductionActivity.class);
        intent.putExtra(IntroductionActivity.ARG_ID, getUser().getId());
        intent.putExtra(IntroductionActivity.ARG_CN_NUMBER, getUser().getCNNumber());
        intent.putExtra(IntroductionActivity.ARG_CONTENT, getUser().getUserProfile().getAbout());

        Bundle translateBundle = ActivityOptionsCompat.makeCustomAnimation(
                activity, R.anim.slide_in_left, R.anim.slide_out_left
        ).toBundle();

        ActivityCompat.startActivity(activity, intent, translateBundle);
    }

    /**
     * Get user associated with activity.
     * @return user associated with activity.
     */
    public User getUser() {
        return getProfileActivity().getUser();
    }

    /**
     * Casts activity to ProfileActivity
     * @return cast activity
     */
    private ProfileActivity getProfileActivity() {
        return (ProfileActivity) getActivity();
    }

    /**
     * Get object for syncing changes to user
     * @return lock object
     */
    public Object getUserLock() {
        return userLock;
    }

    /**
     * Get controller for profile header
     * @return controller for profile header
     */
    public ProfileHeaderController getProfileHeaderController() {
        return mProfileHeaderController;
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
     * Unregister receivers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (updateService == null || userUpdater == null) return;
        updateService.removeUpdater(userUpdater);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(AppSession.getInstance().getApplicationContext());
        if (picChangeReceiver != null) {
            manager.unregisterReceiver(picChangeReceiver);
        }
        if (bannerChangeReceiver != null) {
            manager.unregisterReceiver(bannerChangeReceiver);
        }
        if (introChangeReceiver != null) {
            manager.unregisterReceiver(introChangeReceiver);
        }
        if (infoChangeReceiver != null) {
            manager.unregisterReceiver(infoChangeReceiver);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        position --;
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public String toString() {
        return TAG + getUser().getId();
    }
}
