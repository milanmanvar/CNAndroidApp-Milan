package com.thecn.app.activities.navigation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.adapters.NavDrawerAdapter;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements
        AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private StickyListHeadersListView mDrawerListView;
    private View mFragmentContainerView;
    private NavDrawerAdapter mNavDrawerAdapter;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    //update changes to the user's courses and conexus
    private BroadcastReceiver mCourseBroadcastReceiver, mConexusBroadcastReceiver;
    //update changes to user
    private BroadcastReceiver mUserBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    /**
     * Register broadcast receivers that update the user, courses, and conexuses.
     */
    private void registerBroadcastReceivers() {

        mUserBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mNavDrawerAdapter.notifyDataSetChanged();
            }
        };

        mCourseBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mNavDrawerAdapter.loadList();
            }
        };

        mConexusBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mNavDrawerAdapter.loadList();
            }
        };

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        manager.registerReceiver(mUserBroadcastReceiver, new IntentFilter(AppSession.USER_UPDATE));
        manager.registerReceiver(mCourseBroadcastReceiver, new IntentFilter(AppSession.COURSE_UPDATE));
        manager.registerReceiver(mConexusBroadcastReceiver, new IntentFilter(AppSession.CONEXUS_UPDATE));
    }

    /**
     * Unregister broadcast receivers.
     */
    private void unregisterBroadcastReceivers() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        manager.unregisterReceiver(mUserBroadcastReceiver);
        manager.unregisterReceiver(mCourseBroadcastReceiver);
        manager.unregisterReceiver(mConexusBroadcastReceiver);
    }

    /**
     * Allow fragment to have options menu
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    /**
     * Gets {@link com.emilsjolander.components.stickylistheaders.StickyListHeadersListView} and sets it up.
     * Uses a {@link com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter} as well.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerListView = (StickyListHeadersListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

        mNavDrawerAdapter = new NavDrawerAdapter(getActivity().getApplicationContext());

        mDrawerListView.setOnItemClickListener(this);
        mDrawerListView.setOnHeaderClickListener(this);

        mDrawerListView.setDrawingListUnderStickyHeader(true);
        mDrawerListView.setAreHeadersSticky(true);

        mDrawerListView.setAdapter(mNavDrawerAdapter);

        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        return mDrawerListView;
    }

    /**
     * Closes Navigation drawer
     */
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    /**
     * Click handled in {@link #selectItem(int)}
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        selectItem(position);
    }

    /**
     * Do nothing...
     */
    @Override
    public void onHeaderClick(StickyListHeadersListView l, View header,
                              int itemPosition, long headerId, boolean currentlySticky) {
    }

    /**
     * Tells whether drawer is open.
     * @return true if drawer open, false if drawer closed.
     */
    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                showActivityActionBar();
                NavigationActivity activity = (NavigationActivity) getActivity();
                activity.setSlidingEnabled(true);
                activity.supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                showGlobalContextActionBar();
                NavigationActivity activity = (NavigationActivity) getActivity();
                activity.setSlidingEnabled(false);
                activity.supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            //mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    /**
     * Sets an item checked, closes the drawer, and performs an action (if callbacks not null)
     * @param position position of item in adapter
     */
    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position, mNavDrawerAdapter.getItem(position));
        }
    }

    /**
     * Use activity as {@link com.thecn.app.activities.navigation.NavigationDrawerFragment.NavigationDrawerCallbacks}
     * Registers broadcast receivers.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }

        registerBroadcastReceivers();
    }

    /**
     * Sets callbacks to null.  Unregisters broadcast receivers.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;

        unregisterBroadcastReceivers();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    /**
     * Forward new config to the drawer toggle component.
     * @param newConfig new configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Sets activity title on resume.
     */
    public void onResume() {
        super.onResume();
        showActionBar();
    }

    /**
     * Sets either the nav drawer's title or the activity's title.
     */
    public void showActionBar() {
        if (isDrawerOpen()) {
            showGlobalContextActionBar();
        } else {
            showActivityActionBar();
        }
    }

    /**
     * Also call {@link android.support.v7.app.ActionBarDrawerToggle#onOptionsItemSelected(android.view.MenuItem)}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    public void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle("CourseNetworking");
        actionBar.setTitle(getResources().getString(R.string.navigation_drawer_title));
    }

    /**
     * Shows the action bar specific to this activity.
     */
    public void showActivityActionBar() {
        String title = ((NavigationActivity) getActivity()).getActivityTitle();

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }

    /**
     * Get the action bar of the associated activity
     * @return the action bar of the activity.
     */
    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Tells whether the drawer is visible (on screen).
     * @return true if visible, false otherwise.
     */
    public boolean isDrawerVisible() {
        return mDrawerLayout.isDrawerVisible(mFragmentContainerView);
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position, Object item);
    }
}
