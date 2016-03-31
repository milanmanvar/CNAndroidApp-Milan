package com.thecn.app.activities.navigation;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.PhotoGalleryViewerActivity;
import com.thecn.app.activities.SettingsActivity;
import com.thecn.app.activities.composeemail.ComposeEmailActivity;
import com.thecn.app.activities.conexus.ConexusActivity;
import com.thecn.app.activities.course.CourseActivity_New;
import com.thecn.app.activities.createpost.CreatePostActivity;
import com.thecn.app.activities.email.EmailActivity;
import com.thecn.app.activities.homefeed.HomeFeedActivity;
import com.thecn.app.activities.login.LoginActivity;
import com.thecn.app.activities.poll.PollActivity;
import com.thecn.app.activities.post.PostActivity;
import com.thecn.app.activities.postlikes.PostLikesActivity;
import com.thecn.app.activities.profile.ProfileActivity;
import com.thecn.app.activities.verification.VerificationActivity;
import com.thecn.app.broadcastreceivers.AlertNotificationReceiver;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.content.Email;
import com.thecn.app.models.content.Picture;
import com.thecn.app.models.content.Post;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.notification.UserNewMessage;
import com.thecn.app.models.user.User;
import com.thecn.app.services.AlertNotificationService;
import com.thecn.app.services.UpdateService;
import com.thecn.app.stores.AuthStore;
import com.thecn.app.stores.BaseStore;
import com.thecn.app.tools.network.GCMUtil;
import com.thecn.app.views.NotificationActionProvider;

import java.util.ArrayList;

/**
 * Base activity for all activities that are analogous to any page on thecn.com.
 * Displays a navigation drawer to the left and uses jfeinstein's {@link com.jeremyfeinstein.slidingmenu.lib.SlidingMenu}
 * to show notifications to the right.  Shows the page's content in the center, along with a notification button in the ActionBar.
 */
public abstract class NavigationActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {

    //todo use to tell if menu open so can restore the state on rotation
    private static final String MENU_OPEN_KEY = "menu_open";
    //index for notification fragments
    private static final String PAGER_INDEX_KEY = "pager_index";

    //given to openPage() to tell it to start activity for result
    public static final String WHOLE_PAGE_TAG = "whole_page";
    //used for getting results from "whole page" activities
    public static final int RESERVED_REQUEST_CODE = 255;

    //used to tell if activity should be refreshed
    public static final String REFRESH_FLAG_KEY = "refresh_flag_key";

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private String activityTitle = "";

    private SlidingMenu slidingMenu;

    //for notification fragments
    private ViewPager mViewPager;
    private MyFragmentPagerAdapter mFragmentPagerAdapter;

    //indicators for the current notification fragment
    private View[] arrows;

    private boolean menuLastAction; //true for opened, false for closed

    //buttons that show notification counts
    private RelativeLayout notificationButton, emailButton, requestButton;

    //tells activity whether user has navigated to another whole content page: Course, Conexus, or Profile
    private boolean wholePageResult;

    //flag for whether Google Play Services Dialog was cancelled.
    boolean gpsDialogCancelled;

    private static final String GP_FAIL = "Unable to install Google Play Services";

    //used for notification menu item
    private NotificationActionProvider notificationActionProvider;

    private UpdateService mService;
    private boolean mBound = false;

    //used to update notification views when something has changed.
    protected BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setAllNotificationDisplays();
        }
    };

    //connection used for service connection and disconnection callbacks
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            UpdateService.UpdateServiceBinder binder = (UpdateService.UpdateServiceBinder) iBinder;

            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    /**
     * Sets content view, sets up NavigationDrawerFragment, sets up sliding menu,
     * notification buttons/fragments with ViewPager, and registers notification broadcast receiver.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mySetContentView(); //subclasses may override

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        setUpSlidingMenu();
        setUpNotificationButtons();

        mViewPager = (ViewPager) findViewById(R.id.notif_pager);
        mViewPager.setOffscreenPageLimit(2);
        mFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), savedInstanceState);
        mViewPager.setAdapter(mFragmentPagerAdapter);
        mViewPager.setPageMarginDrawable(R.color.background_color);
        mViewPager.setPageMargin(10);

        setUpArrows();

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setArrowVisibility(position);
                mFragmentPagerAdapter.focus(position);
            }
        });

        menuLastAction = false;

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mNotificationReceiver, new IntentFilter(AppSession.NOTIFICATION_UPDATE));
    }

    /**
     * Closes navigation drawer or the sliding menu.  If neither are open,
     * finishes the activity.
     */
    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
        } else if (slidingMenu.isMenuShowing()) {
            slidingMenu.showContent();
        } else {
            finish();
        }
    }

    /**
     * Used to set a flag for refreshing the page if the VerificationActivity has just registered
     * a new user.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(VerificationActivity.VERIFICATION_INTENT_TAG, false)) {
            intent.putExtra(REFRESH_FLAG_KEY, true);
        }
        setIntent(intent);
    }

    /**
     * Used to indicate whether this activity is considered to be a "whole page."
     * @param activity the activity to set the result to
     * @param isWholePage whether or not the activity is a "whole page"
     */
    public static void putWholePageResult(Activity activity, boolean isWholePage) {
        Intent intent = new Intent();
        intent.putExtra(WHOLE_PAGE_TAG, isWholePage);
        activity.setResult(RESULT_OK, intent);
    }

    /**
     * Used to set a flag that indicates whether or not the activity that just finished
     * is considered a "whole page" or not.  If it is, then some activities may choose to
     * refresh their data or perform some other operation.
     *
     * E.G., the PostActivity is not a whole page, and returning to the previous activity
     * will not refresh the list of posts (this would get annoying).
     *
     * @param data may hold whether or not this is a whole page result, in which case this
     *             activity may want to perform some operation.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            wholePageResult = false;
            return;
        }

        if (data.getBooleanExtra(WHOLE_PAGE_TAG, false)) {
            //if user navigated to another whole content page, tell other activities in the backstack
            putWholePageResult(this, true);
        }

        wholePageResult = data.getBooleanExtra(WHOLE_PAGE_TAG, false);
    }

    /**
     * Tells if there was an activity on the stack above this one that finished and was
     * considered to be a "whole page."  See {@link #onActivityResult(int, int, android.content.Intent)}
     * @return whether or not there was a wholePageResult
     */
    public boolean isWholePageResult() {
        return wholePageResult;
    }

    /**
     * Tells if the navigation drawer is open
     * @return whether or not the navigation drawer is open
     */
    public boolean isDrawerOpen() {
        return mNavigationDrawerFragment.isDrawerOpen();
    }

    /**
     * Tells whether or not the navigation drawer is on screen
     * @return whether or not the navigation drawer is on screen
     */
    public boolean isDrawerVisible() {
        return mNavigationDrawerFragment.isDrawerVisible();
    }

    /**
     * Gets the sliding menu View, which contains the rest of the content.
     * @return the sliding menu.
     */
    public SlidingMenu getSlidingMenu() {
        return slidingMenu;
    }

    /**
     * Binds the updating service to this activity.
     */
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(this, UpdateService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Sets a global flag that indicates the NavigationActivity is resumed.
     * Refreshes notification button displays and cancels alert notifications corresponding to CN notifications.
     * Shows a dialog if Google Play Services are not installed, but only once for each user that has logged into
     * the app.
     */
    public void onResume() {
        super.onResume();

        AppSession session = AppSession.getInstance();
        session.setNavigationActivityResumed(true);

        //keeps sliding menu from sliding when the nav drawer is open
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            setSlidingEnabled(false);
        }

        setAllNotificationDisplays();

        //cancel push notification once user is on an activity that displays notifications
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.cancel(AlertNotificationService.NOTIFICATION_ID);

        User.Settings settings = session.getUser().getSettings();

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (settings == null || settings.isPlayQuery()) return;

            synchronized (session.userLock) {
                settings.setPlayQuery(true);
            }

            session.writeSettingsToDatabase();

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GCMUtil.showPlayServicesDialog(this);
            }
        }

        //make sure notification settings and GCM registration is up-to-date
        sendBroadcast(new Intent(this, AlertNotificationReceiver.class));
    }

    /**
     * Used by GCM dialog to set flag for when it is cancelled
     * @param dialog the GCM dialog
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        gpsDialogCancelled = true;
    }

    /**
     * GPS: google play services
     * When dialog dismissed and nothing happened to install GPS, show an error message to
     * the user.  NOTE: Why isn't this done by GooglePlayServicesUtil automatically..?
     * @param dialog dialog that was dismissed
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        if (!gpsDialogCancelled && AppSession.getInstance().isNavigationActivityResumed()) {
            AppSession.showLongToast(GP_FAIL);
        }

        gpsDialogCancelled = false;
    }

    /**
     * Set global flag to indicate navigation activity is not resumed.
     */
    @Override
    protected void onPause() {
        super.onPause();
        AppSession.getInstance().setNavigationActivityResumed(false);
    }

    /**
     * Unbind updating service.
     */
    public void onStop() {
        super.onStop();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     * Unregister notification update receiver.
     */
    public void onDestroy() {
        unregisterReceiver();
        super.onDestroy();
    }


    /**
     * Unregister the notification update receiver.
     */
    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotificationReceiver);
    }

    /**
     * Returns service if it is bound.
     * @return bound service
     */
    public UpdateService getUpdateService() {
        return mService;
    }

    /**
     * Sets initial visibility of notification indicator arrows
     * Initially points to General notifications button.
     */
    private void setUpArrows() {
        arrows = new View[3];
        arrows[0] = findViewById(R.id.notification_arrow);
        arrows[1] = findViewById(R.id.email_arrow);
        arrows[2] = findViewById(R.id.colleague_arrow);


        arrows[0].setVisibility(View.VISIBLE);
        arrows[1].setVisibility(View.INVISIBLE);
        arrows[2].setVisibility(View.INVISIBLE);
    }

    /**
     * Sets specified arrow visible.  Uses mod to cycle through and set others to invisible.
     * @param index index of arrow to set visible
     */
    private void setArrowVisibility(int index) {
        arrows[index].setVisibility(View.VISIBLE);

        for (int i = (index + 1) % 3; i != index; i = (i + 1) % 3) {
            arrows[i].setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Gets View references and sets on click listeners to notification buttons
     * in the sliding menu layout.
     */
    private void setUpNotificationButtons() {
        notificationButton = (RelativeLayout) findViewById(R.id.notification_button_layout);
        emailButton = (RelativeLayout) findViewById(R.id.email_button_layout);
        requestButton = (RelativeLayout) findViewById(R.id.colleague_request_button_layout);

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() == 0) {
                    mFragmentPagerAdapter.focus(0);
                } else {
                    mViewPager.setCurrentItem(0);
                }
            }
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() == 1) {
                    mFragmentPagerAdapter.focus(1);
                } else {
                    mViewPager.setCurrentItem(1);
                }
            }
        });

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() == 2) {
                    mFragmentPagerAdapter.focus(2);
                } else {
                    mViewPager.setCurrentItem(2);
                }
            }
        });
    }

    /**
     * Sets up the {@link com.jeremyfeinstein.slidingmenu.lib.SlidingMenu} to be on the right, slide the WHOLE screen,
     * attach to this activity, etc.
     * Sets on open and on close listeners to the sliding menu as well.
     */
    private void setUpSlidingMenu() {
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.RIGHT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.drawer_shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset_2);
        slidingMenu.setFadeDegree(0.5f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMenu(R.layout.notification_layout);

        slidingMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
            @Override
            public void onOpened() {
                if (!menuLastAction) {
                    int index = mViewPager.getCurrentItem();

                    //focus the selected fragment
                    mFragmentPagerAdapter.focus(index);

                    menuLastAction = true;
                }

                mNavigationDrawerFragment.closeDrawer();
            }
        });

        slidingMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
            @Override
            public void onClosed() {
                menuLastAction = false;
                mFragmentPagerAdapter.onMenuClose();
            }
        });
    }

    /**
     * Saves various important things...
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mFragmentPagerAdapter.onSaveInstanceState(outState);
        outState.putBoolean(MENU_OPEN_KEY, slidingMenu.isMenuShowing());
        outState.putInt(PAGER_INDEX_KEY, mViewPager.getCurrentItem());
    }

    /**
     * Sets sliding enabled for the {@link com.jeremyfeinstein.slidingmenu.lib.SlidingMenu}
     * @param enabled
     */
    public void setSlidingEnabled(boolean enabled) {
        slidingMenu.setSlidingEnabled(enabled);
    }

    /**
     * Utility method for setting up the action bar
     */
    protected void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    /**
     * Can be used by subclasses to set the title.  Subclasses should not use
     * {@link android.support.v7.app.ActionBar#setTitle(CharSequence)}
     * because this will only work once, until the NavigationDrawer is first opened
     * and the title is reset to say "CourseNetworking."
     * @param title
     */
    public void setActivityTitle(String title) {
        this.activityTitle = title;
    }

    /**
     * Returns this activity's title.
     * @return the activity title.
     */
    public String getActivityTitle() {
        return activityTitle;
    }

    /**
     * Sets up action bar and sets title all in one method.
     * Subclasses should use this to set the activity's title.
     * @param title the activity's title
     */
    public void setActionBarAndTitle(String title) {
        setActionBar();
        setActivityTitle(title);
        getSupportActionBar().setTitle(activityTitle);
    }

    /**
     * Subclasses should use this to specify custom layouts to use
     * instead of the default.
     */
    protected void mySetContentView() {
        setContentView(R.layout.activity_navigation);
    }

    /**
     * Perform action based on which navigation drawer item was selected.
     * @param position position in ListView
     * @param item item from the adapter of the ListView
     */
    @Override
    public void onNavigationDrawerItemSelected(int position, Object item) {

        //item should be a String
        if (item.equals("Home")) {

            openHomeFeedPage();

        } else if (item.equals("Profile")) {

            User user = AppSession.getInstance().getUser();
            openProfileByID(user.getId());

        } else if (item.equals("Logout")) {

            logout();

        } else {

            if (item instanceof Course) {

                Course course = (Course) item;
                openCoursePage(course);

            } else if (item instanceof Conexus) {

                Conexus conexus = (Conexus) item;
                openConexusPage(conexus);

            }

        }
    }

    /**
     * Allows for the drawer or the sliding menu to close before a new activity is opened.
     * Sets activity to untouchable until the action has been performed.
     * Uses {@link com.thecn.app.activities.navigation.OpenPageRunnable} to perform a
     * delayed action.
     * @param intent intent to use to start another activity.
     */
    public void openPage(Intent intent) {
        boolean menuOpen = false;

        OpenPageRunnable runnable = new OpenPageRunnable(this, intent);

        if (mNavigationDrawerFragment.isDrawerOpen()) {
            menuOpen = true;
            setTouchable(false);
            mNavigationDrawerFragment.closeDrawer();
        } else if (slidingMenu.isMenuShowing()) {
            menuOpen = true;
            setTouchable(false);
            slidingMenu.showContent();
        }

        if (menuOpen) {
            long duration = getResources().getInteger(R.integer.close_drawer_duration);

            final Handler handler = new Handler();
            handler.postDelayed(runnable, duration);
        } else {
            runnable.run();
        }
    }

    /**
     * Closes the notification drawer
     */
    protected void closeNotificationDrawer() {
        slidingMenu.showContent();
    }

    /**
     * Sets the activity to touchable or not touchable based on boolean flag.
     * @param touchable whether or not activity is touchable.
     */
    public void setTouchable(boolean touchable) {
        if (touchable) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    /**
     * Opens {@link com.thecn.app.activities.homefeed.HomeFeedActivity}
     */
    public void openHomeFeedPage() {
        final Intent intent = new Intent(this, HomeFeedActivity.class);
        if (!(this instanceof HomeFeedActivity)) {
            intent.putExtra(REFRESH_FLAG_KEY, true);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openPage(intent);
    }

    /**
     * Opens {@link com.thecn.app.activities.email.EmailActivity}
     * @param email
     */
    public void openEmailPage(Email email) {
        final Intent intent = new Intent(this, EmailActivity.class);
        intent.putExtra("email", email);
        openPage(intent);
    }

    /**
     * Opens {@link com.thecn.app.activities.profile.ProfileActivity}
     * by user id
     * @param userID id of user
     */
    public void openProfileByID(String userID) {
        final Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user_id", userID);
        openPage(intent);
    }

    /**
     * Opens {@link com.thecn.app.activities.profile.ProfileActivity}
     * by CN number
     * @param cnNumber cn number of user
     */
    public void openProfileByCNNumber(String cnNumber) {
        final Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("cn_number", cnNumber);
        openPage(intent);
    }

    /**
     * Opens either {@link com.thecn.app.activities.post.PostActivity} or
     * {@link com.thecn.app.activities.poll.PollActivity} based on content of
     * Post object.
     * @param post the post to display
     * @param textFocus whether to bring up the soft keyboard for reflection entry.
     */
    public void openPostPage(Post post, boolean textFocus) {
        if (isPostActivityOpen(post)) return;

        openPage(getPostIntent(post, textFocus));
    }

    /**
     * Tells whether this activity is already showing this post.
     * @param post the post to be examined
     * @return true if this activity is displaying this post, false otherwise
     */
    public boolean isPostActivityOpen(Post post) {
        if (this instanceof PostActivity) {
            Post otherPost = ((PostActivity) this).getPost();
            return arePostsTheSame(post, otherPost);
        } else if (this instanceof PollActivity) {
            Post otherPost = ((PollActivity) this).getPost();
            return arePostsTheSame(post, otherPost);
        }

        return false;
    }

    /**
     * Tells whether two post objects have the same id
     * @param post1 post one
     * @param post2 post two
     * @return true if posts are the same, false otherwise
     */
    public boolean arePostsTheSame(Post post1, Post post2) {
        if (post1 == null || post2 == null) return false;

        String id1 = post1.getId();
        String id2 = post2.getId();

        return !(id1 == null || id2 == null) && id1.equals(id2);
    }

    /**
     * Gets an intent for opening an activity for displaying a post.
     * This will be either a {@link com.thecn.app.activities.post.PostActivity} or
     * a {@link com.thecn.app.activities.poll.PollActivity}
     * @param post the post to get an intent for
     * @param textFocus whether to open the soft keyboard to make a reflection
     * @return the Intent that will open the appropriate activity.
     */
    public Intent getPostIntent(Post post, boolean textFocus) {
        Intent intent;

        Post.Type type = post.getEnumType();
        if (type == null) return null;

        if (type == Post.Type.POLL) {
            intent = new Intent(this, PollActivity.class);
        } else {
            intent = new Intent(this, PostActivity.class);
        }

        intent.putExtra("post", post);
        intent.putExtra("textFocus", textFocus);
        return intent;
    }

    /**
     * Opens {@link com.thecn.app.activities.poll.PollActivity}
     * opens to a specified poll question
     * @param post the post that contains the poll
     * @param index the index of the question to go to
     */
    public void openPollPage(Post post, Integer index) {
        final Intent intent = new Intent(this, PollActivity.class);
        intent.putExtra("post", post);
        if (index != null) {
            intent.putExtra("index", index);
        }
        openPage(intent);
    }

    /**
     * Opens {@link com.thecn.app.activities.course.CourseActivity}
     * @param course the course to display
     */
    public void openCoursePage(Course course) {
        final Intent intent = new Intent(this, CourseActivity_New.class);
        intent.putExtra("course", course);
        openPage(intent);
    }



    /**
     * Opens {@link com.thecn.app.activities.conexus.ConexusActivity}
     * @param conexus the conexus to display
     */
    public void openConexusPage(Conexus conexus) {
        final Intent intent = new Intent(this, ConexusActivity.class);
        intent.putExtra("conexus", conexus);
        openPage(intent);
    }

    /**
     * Opens {@link com.thecn.app.activities.PhotoGalleryViewerActivity}
     * @param pics pictures to show
     * @param currentIndex index of the picture from which to start
     */
    public void openPhotoGalleryViewerActivity(ArrayList<Picture> pics, int currentIndex) {
        Intent intent = new Intent(this, PhotoGalleryViewerActivity.class);
        intent.putExtra("pics", pics);
        intent.putExtra("currentIndex", currentIndex);
        startActivity(intent);
    }

    /**
     * Opens {@link com.thecn.app.activities.postlikes.PostLikesActivity}
     * @param post the post to get the likes from to display
     */
    public void openPostLikesActivity(Post post) {
        Intent intent = new Intent(this, PostLikesActivity.class);
        intent.putExtra("post", post);
        startActivity(intent);
    }

    /**
     * Opens an activity that can handle a web intent.  If none found, shows an error.
     * @param url
     */
    public void openURL(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);

        } catch (ActivityNotFoundException e) {
            AppSession.showLongToast("Please install a web browser");
        }
    }

    /**
     * Shows options menu either for the navigation drawer (if it's open)
     * or for this activity (if the drawer is closed).
     * Show total notification count button if showing the activity's menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Only show items in the action bar relevant to this screen
        // if the drawer is not showing. Otherwise, let the drawer
        // decide what to show in the action bar.
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            return super.onCreateOptionsMenu(menu);
        }

        getMenuInflater().inflate(R.menu.navigation, menu);

        //use MenuItemCompat to implement notification button
        //needed because the view changes depending on the notification count.
        MenuItem item = menu.findItem(R.id.action_notifications);
        notificationActionProvider =
                (NotificationActionProvider) MenuItemCompat.getActionProvider(item);
        notificationActionProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingMenu.showMenu();
            }
        });

        //set the count of notifications.
        UserNewMessage message = AppSession.getInstance().getUserNewMessage();
        int total = message != null ? message.getTotal() : 0;
        notificationActionProvider.setInitialCount(total);

        return true;
    }

    /**
     * Sets the counts for all the notification indicators.  If there is no
     * {@link com.thecn.app.models.notification.UserNewMessage} object,
     * create a new one to use.
     */
    public synchronized void setAllNotificationDisplays() {
        UserNewMessage userNewMessage = AppSession.getInstance().getUserNewMessage();
        userNewMessage = userNewMessage != null ? userNewMessage : new UserNewMessage();

        setTotalNotificationDisplay(userNewMessage.getTotal());
        setNotificationDisplay(userNewMessage.getGenNotificationCount());
        setEmailDisplay(userNewMessage.getEmailCount());
        setRequestDisplay(userNewMessage.getFollowerCount());
    }

    /**
     * Sets the display for the notification options menu button (in the action bar).
     * Shows the summation of all counts of all notification types.
     * @param count count to display
     */
    private void setTotalNotificationDisplay(int count) {
        if (notificationActionProvider == null) return;
        notificationActionProvider.setCount(count);
    }

    /**
     * Sets the display for the count of new general notifications (first in view pager).
     * @param count count to display
     */
    private void setNotificationDisplay(int count) {
        if (notificationButton != null) {

            View indicator = notificationButton.findViewById(R.id.notification_indicator);
            TextView indicatorText = (TextView) notificationButton.findViewById(R.id.notification_indicator_text);

            setCountAndVisibility(count, indicator, indicatorText);
        }
    }

    /**
     * Sets the display for the count of new email notifications (second in view pager).
     * @param count count to display
     */
    private void setEmailDisplay(int count) {
        if (emailButton != null) {

            View indicator = emailButton.findViewById(R.id.email_indicator);
            TextView indicatorText = (TextView) emailButton.findViewById(R.id.email_indicator_text);

            setCountAndVisibility(count, indicator, indicatorText);
        }
    }

    /**
     * Sets the display for the count of new follower notifications (third in view pager).
     * @param count count to display
     */
    private void setRequestDisplay(int count) {
        if (requestButton != null) {

            View indicator = requestButton.findViewById(R.id.request_indicator);
            TextView indicatorText = (TextView) requestButton.findViewById(R.id.request_indicator_text);

            setCountAndVisibility(count, indicator, indicatorText);
        }
    }

    /**
     * Sets the content of a Count text view in a notification button.
     * Shows the relevant views if there is a count, hides if count is 0.
     * Shows "99+" if there are more than 99 new notifications.
     * @param count
     * @param view
     * @param textView
     */
    private void setCountAndVisibility(int count, View view, TextView textView) {
        String countText = count > 99 ? "99+" : Integer.toString(count);
        int visibility = count > 0 ? View.VISIBLE : View.INVISIBLE;

        view.setVisibility(visibility);
        textView.setText(countText);
    }

    /**
     * If the navigation drawer is showing, don't perform an action with a menu button.
     * Else, perform an action.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mNavigationDrawerFragment.isDrawerVisible()) return super.onOptionsItemSelected(item);

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_notifications) {
            slidingMenu.showMenu();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Logs the user out of the app.  Sends a request to remove the
     * Google Cloud Messaging id from the user on the server.
     */
    public void logout() {
        AuthStore.logout(new BaseStore.EmptyCallback());
        AppSession.getInstance().removeGCMIDFromMe();

        pushLoginActivity();
    }

    /**
     * Opens {@link com.thecn.app.activities.createpost.CreatePostActivity}
     * Only one instance allowed.
     */
    public void pushCreatePostActivity() {
        if (AppSession.checkVerification(this)) {
            return;
        }

        final Intent intent = new Intent(this, CreatePostActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openPage(intent);
    }

    /**
     * Opens {@link com.thecn.app.activities.composeemail.ComposeEmailActivity}
     * Only one instance allowed.
     */
    public void pushComposeEmailActivity() {
        final Intent intent = new Intent(this, ComposeEmailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openPage(intent);
    }

    /**
     * Opens {@link com.thecn.app.activities.login.LoginActivity}
     * Clears the task so that no other activities are in the backstack.
     */
    public void pushLoginActivity() {
        unregisterReceiver();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Hides background progress bar.
     */
    public void hideProgressBar() {
//        findViewById(R.id.activityProgressBar).setVisibility(View.INVISIBLE);
    }
}
