package com.thecn.app.activities.email;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.content.Email;
import com.thecn.app.stores.EmailStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;

import java.util.ArrayList;

/**
 * Activity for viewing email threads
 */
public class EmailActivity extends NavigationActivity {

    private Email mEmail;

    //fetches data from network
    private DataGrabber mDataGrabber;
    private static final String mLoadEmailFragmentTag = "load_email";

    /**
     * Set title. If savedInstanceState null, get parent email ID and
     * use {@link com.thecn.app.activities.email.EmailActivity.DataGrabber} to
     * get data from server.
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarAndTitle("Email");

        if(savedInstanceState == null) {
            String parentId;

            try {
                mEmail = (Email) getIntent().getSerializableExtra("email");

                //mEmail should always be set to the parent email,
                //so the whole thread can be seen
                if (mEmail.isReply()) {
                    parentId = mEmail.getParentId();
                } else {
                    parentId = mEmail.getId();
                }

                if (parentId == null) {
                    throw new NullPointerException();
                }
            } catch (NullPointerException e) {
                onLoadingError();
                return;
            }

            mDataGrabber = DataGrabber.getInstance(parentId);

            getSupportFragmentManager().beginTransaction()
                    .add(mDataGrabber, mLoadEmailFragmentTag)
                    .commit();

        } else {
            mDataGrabber =
                    (DataGrabber) getSupportFragmentManager().findFragmentByTag(mLoadEmailFragmentTag);

            if (!mDataGrabber.loading) {
                mEmail = (Email) savedInstanceState.getSerializable("email");
            }

            hideProgressBar();
        }
    }

    public DataGrabber getDataGrabber() {
        return mDataGrabber;
    }

    /**
     * Used to load email data
     */
    public static class DataGrabber extends Fragment {

        public boolean loading = false;
        private CallbackManager<DataGrabber> manager;

        public static final String ID_KEY = "id_key";

        /**
         * Takes the parent email ID as an argument
         * @param emailID the email id
         * @return new instance of this class
         */
        public static DataGrabber getInstance(String emailID) {
            Bundle args = new Bundle();
            args.putString(ID_KEY, emailID);

            DataGrabber grabber = new DataGrabber();
            grabber.setArguments(args);
            return grabber;
        }

        /**
         * Begin loading the email
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            manager = new CallbackManager<>();

            String id = getArguments().getString(ID_KEY);
            loadEmail(id);
        }

        /**
         * Set flag and use {@link com.thecn.app.stores.EmailStore#getEmailById(String, com.thecn.app.stores.ResponseCallback)}
         * to load the email.
         * @param parentID
         */
        public void loadEmail(String parentID) {
            loading = true;
            EmailStore.getEmailById(parentID, new Callback(manager));
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
         * Used when request for email data returns.
         */
        private static class Callback extends CallbackManager.NetworkCallback<DataGrabber> {
            public Callback(CallbackManager<DataGrabber> grabber) {
                super(grabber);
            }

            @Override
            public void onResumeWithResponse(DataGrabber object) {
                Email email = EmailStore.getData(response);
                EmailActivity a = (EmailActivity) object.getActivity();

                if (email != null) {
                    a.onSuccess(email);
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

    /**
     * Sets the email for this activity and pushes {@link com.thecn.app.activities.email.EmailFragment}
     * @param email the email data returned from the server
     */
    private void onSuccess(Email email) {
        setEmail(email);
        hideProgressBar();

        FragmentManager manager = getSupportFragmentManager();

        //if reloading data, remove the old fragment and replace with new
        Fragment fragment = manager.findFragmentByTag("email_fragment");
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
        }

        fragment = EmailFragment.newInstance(email);
        manager.beginTransaction()
                .replace(R.id.container, fragment, "email_fragment")
                .commit();

        getDataGrabber().loading = false;
    }

    /**
     * Finish the activity on error.
     */
    private void onLoadingError() {
        AppSession.showDataLoadError("email");
        finish();
    }

    public void setEmail(Email email) {
        mEmail = email;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("email", mEmail);
    }

    /**
     * Check whether this is the same email page.  If it is,
     * check if it should be reloaded.
     * @param email The new email the user has attempted to view.
     */
    @Override
    public void openEmailPage(Email email) {
        try {
            boolean samePage = false;
            boolean reload = false;

            String parentId;

            if (email.isReply()) {
                parentId = email.getParentId();
                if (parentId.equals(mEmail.getId())) {
                    samePage = true;

                    //Check if the email used to open the page
                    //is in the sub-email list of its parent.
                    //If not, then this is a new message and the
                    //fragment should be reloaded
                    ArrayList<Email> subEmails = mEmail.getSubEmails();
                    reload = true;
                    if (subEmails != null) {
                        for (Email subEmail : subEmails) {
                            try {
                                if (subEmail.getId().equals(email.getId())) {
                                    reload = false;
                                    break;
                                }
                            } catch (NullPointerException e) {
                                //on error, treat as if subEmail did not equal
                            }
                        }
                    }
                }

            } else {
                parentId = email.getId();
                if (parentId.equals(mEmail.getId())) {
                    samePage = true;
                }
            }

            //don't open the same email page
            if (!samePage) {
                super.openEmailPage(email);
            } else {
                closeNotificationDrawer();
                if (reload) mDataGrabber.loadEmail(parentId);
            }

        } catch (NullPointerException e) {
            // data not there...
        }
    }
}
