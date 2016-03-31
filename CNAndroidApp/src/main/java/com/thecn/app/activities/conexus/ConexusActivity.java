package com.thecn.app.activities.conexus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.thecn.app.AppSession;
import com.thecn.app.activities.ContentPageActivity;
import com.thecn.app.activities.createpost.CreatePostActivity;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.stores.ConexusStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;

/**
 * Activity analogous to a Conexus page on the website
 */
public class ConexusActivity extends ContentPageActivity {

    public static final int ABOUT_FRAGMENT = 0;
    public static final int ROSTER_FRAGMENT = 1;
    //fragment tag
    private static final String mLoadConexusFragmentTag = "load_conexus";
    private Conexus mConexus;
    private String conexusID;
    //loads data from network
    private DataGrabber mDataGrabber;

    /**
     * Must be passed a Conexus object in the intent.
     * Begins loading if no savedInstanceState
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            //get conexus data
            try {
                mConexus = (Conexus) getIntent().getSerializableExtra("conexus");
                conexusID = mConexus.getId();
            } catch (NullPointerException e) {
                onLoadingError();
                return;
            }

            //begin loading
            mDataGrabber = DataGrabber.getInstance(conexusID);
            getSupportFragmentManager().beginTransaction()
                    .add(mDataGrabber, mLoadConexusFragmentTag)
                    .commit();
        } else {
            //get data loading fragment that already exists
            mDataGrabber =
                    (DataGrabber) getSupportFragmentManager().findFragmentByTag(mLoadConexusFragmentTag);

            if (!mDataGrabber.loading) {
                mConexus = (Conexus) savedInstanceState.getSerializable("conexus");
            }

            hideProgressBar();
        }

        setActionBarAndTitle("Conexus");
    }

    /**
     * @return the fragment responsible for getting conexus data from the network
     */
    public DataGrabber getDataGrabber() {
        return mDataGrabber;
    }

    /**
     * Initialize the rest of the activity when data properly loaded
     *
     * @param conexus the conexus this activity pertains to
     */
    public void onSuccess(Conexus conexus) {
        setConexus(conexus);
        hideProgressBar();
        initFragments(ConexusActivity.ABOUT_FRAGMENT);
        getDataGrabber().loading = false;
    }

    public void setConexus(Conexus conexus) {
        mConexus = conexus;
    }

    /**
     * Finish if couldn't load the data.
     */
    private void onLoadingError() {
        AppSession.showDataLoadError("conexus");
        finish();
    }

    /**
     * Adds this conexus as an argument to {@link com.thecn.app.activities.createpost.CreatePostActivity}
     */
    @Override
    public void pushCreatePostActivity() {
        final Intent intent = new Intent(this, CreatePostActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("CONEXUS", mConexus);
        openPage(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("conexus", mConexus);
    }

    /**
     * Specify that the posts fragment is the fragment underneath
     * the sliding layout
     *
     * @return fragment and data about it
     */
    @Override
    protected FragmentPackage getStaticFragmentPackage() {
        String fragmentKey = "CONEXUS_" + conexusID + "_POSTS";
        return new FragmentPackage("POSTS", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return ConexusPostsFragment.newInstance(mConexus);
            }
        });
    }

    /**
     * Specify that the about and roster fragments are in the sliding panel
     * of the sliding layout.
     *
     * @return fragments and data about htem
     */
    @Override
    protected FragmentPackage[] getFragmentPackages() {
        FragmentPackage[] packages = new FragmentPackage[2];
        String fragmentKey;

        fragmentKey = "CONEXUS_" + conexusID + "_ABOUT";
        packages[ABOUT_FRAGMENT] = new FragmentPackage("ABOUT", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return ConexusAboutFragment.newInstance(mConexus);
            }
        });

        fragmentKey = "CONEXUS_" + conexusID + "_ROSTER";
        packages[ROSTER_FRAGMENT] = new FragmentPackage("ROSTER", fragmentKey, new FragmentCallback() {
            @Override
            public Fragment getFragment() {
                return ConexusRosterFragment.newInstance(mConexus);
            }
        });

        return packages;
    }

    /**
     * Make sure duplicate conexus pages are not opened.
     *
     * @param conexus the conexus to open
     */
    @Override
    public void openConexusPage(Conexus conexus) {
        // dont open duplicate conexus page
        if (!mConexus.getId().equals(conexus.getId())) {
            super.openConexusPage(conexus);
        } else {
            closeNotificationDrawer();
        }
    }

    /**
     * Loads conexus data upon creation of activity.
     */
    public static class DataGrabber extends Fragment {

        public static final String ID_KEY = "id_key";
        public boolean loading = false;
        private CallbackManager<DataGrabber> manager;

        /**
         * Creates instance of fragment
         *
         * @param conexusID must have a conexus ID
         * @return fragment instance
         */
        public static DataGrabber getInstance(String conexusID) {
            Bundle args = new Bundle();
            args.putString(ID_KEY, conexusID);

            DataGrabber grabber = new DataGrabber();
            grabber.setArguments(args);
            return grabber;
        }

        /**
         * Initialize and start loading using {@link com.thecn.app.stores.ConexusStore#getConexusById(String, com.thecn.app.stores.ResponseCallback)}
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            manager = new CallbackManager<>();

            loading = true;
            String id = getArguments().getString(ID_KEY);
            ConexusStore.getConexusById(id, new Callback(manager));
        }

        @Override
        public void onResume() {
            super.onResume();
            manager.resume(this);
        }

        @Override
        public void onPause() {
            manager.pause();
            super.onPause();
        }

        /**
         * Called after network call returns
         */
        private static class Callback extends CallbackManager.NetworkCallback<DataGrabber> {
            public Callback(CallbackManager<DataGrabber> grabber) {
                super(grabber);
            }

            @Override
            public void onResumeWithResponse(DataGrabber object) {
                Conexus conexus = ConexusStore.getData(response);
                ConexusActivity a = (ConexusActivity) object.getActivity();

                if (conexus != null) {
                    a.onSuccess(conexus);
                } else {
                    a.onLoadingError();
                }
            }

            @Override
            public void onResumeWithError(DataGrabber object) {
                StoreUtil.showExceptionMessage(error);
                object.getActivity().finish();
            }
        }
    }
}
