package com.thecn.app.activities.verification;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.fragments.common.ProgressDialogFragment;
import com.thecn.app.models.user.User;
import com.thecn.app.models.util.VerificationBundle;
import com.thecn.app.stores.AuthStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.LoadingViewController;
import com.thecn.app.tools.text.TextUtil;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fragment for verifying that an email and password has been registered with the server.
 * Server also checks if user is already verified or if registration has expired.
 * Before anything else, fragment checks to make sure that verification code used to get here is a valid code.
*/
public class VerifyFragment extends BaseFragment {

    private static final String ARG_KEY = "url_arg";
    private static final Pattern VERIFY_PATTERN = Pattern.compile(".*/user/verify/(\\w*)");
    private static final String VERIFY_ERROR = "Unexpected error during verification.";
    private static final String EMAIL_ERROR = "Could not verify email.";
    private static final String NAME_ERROR = "Username cannot be blank.";
    private static final String PASSWORD_ERROR = "Password cannot be blank.";
    private static final String TITLE = "Verification";

    private String mCode; //verification code

    private LoadingViewController mLoadingViewController;
    private EditText mUserNameText;
    private EditText mPasswordText;

    private CallbackManager<VerifyFragment> callbackManager;

    /**
     * Get instance with arguments
     * @param url URL to use to check verification
     * @return new instance of this class
     */
    public static VerifyFragment getInstance(String url) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, url);

        VerifyFragment fragment = new VerifyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Set up and start loading
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = new CallbackManager<>();
        startLoading();
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
     * Set action bar title
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActionBarActivity().getSupportActionBar().setTitle(TITLE);
    }

    /**
     * Uses {@link com.thecn.app.tools.controllers.LoadingViewController} to set view for
     * either loading, error, or loaded states
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.fragment_verification, null, false);

        if (loading) {
            initViewController(content, getActivity());
            mLoadingViewController.showLoading(true);
            return mLoadingViewController.getRootView();
        } else if (mCode == null) {
            initViewController(content, getActivity());
            return initErrorContent();
        }

        return initContent(content);
    }

    /**
     * Data loaded successfully, show content to user.
     * @param content root view
     * @return root view.
     */
    private View initContent(View content) {
        Button button = (Button) content.findViewById(R.id.submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCredentials();
            }
        });

        mUserNameText = (EditText) content.findViewById(R.id.name);
        mPasswordText = (EditText) content.findViewById(R.id.password);

        return content;
    }

    /**
     * Error occurred, show message and retry button
     * @return root view
     */
    private View initErrorContent() {
        mLoadingViewController.showLoading(false);
        mLoadingViewController.showMessage(VERIFY_ERROR);
        return mLoadingViewController.getRootView();
    }

    /**
     * Set up {@link com.thecn.app.tools.controllers.LoadingViewController}
     * @param content root view
     * @param context for inflating other views
     */
    private void initViewController(View content, Context context) {
        mLoadingViewController = new LoadingViewController(content, context);
        mLoadingViewController.showLoadingView();
    }

    /**
     * Make network call to get code.
     */
    public void startLoading() {
        loading = true;

        final String url = getArguments().getString(ARG_KEY);
        if (url == null) {
            onCodeError();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                getCode(url);
            }
        }).start();
    }

    /**
     * Gets verification code from server.  Follows a redirect (can't use volley here).
     * @param query url for getting code.
     */
    private void getCode(String query) {
        //need to use httpurlconnection (instead of volley) so as to disable redirect handling
        HttpURLConnection conn = null;
        URL url;

        try {
            url = new URL(query);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "Keep-Alive");

            conn.connect();

            //make sure no errors occurred

            if (conn.getResponseCode() != 302) {
                conn.disconnect();
                addCodeErrorCallback();
                return;
            }

            //gets redirect url here
            String redirectURL = conn.getHeaderField("Location");
            Log.d("OBS", "location: " + redirectURL);
            if (redirectURL == null) {
                conn.disconnect();
                addCodeErrorCallback();
                return;
            }

            conn.disconnect();

            url = new URL(redirectURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "Keep-Alive");

            conn.connect();

            //handle error
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                conn.disconnect();
                addCodeErrorCallback();
                return;
            }

            conn.disconnect();

            Matcher matcher = VERIFY_PATTERN.matcher(redirectURL);

            if (matcher.find()) {
                callbackManager.addCallback(new CodeSuccessCallback(matcher.group(1)));
            } else {
                addCodeErrorCallback();
            }

        } catch (Exception e) {
            addCodeErrorCallback();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * Adds callback to perform when error occurs
     */
    private void addCodeErrorCallback() {
        callbackManager.addCallback(new CodeErrorCallback());
    }

    /**
     * Action for when code successfully retrieved from server.
     */
    private static class CodeSuccessCallback extends CallbackManager.Callback<VerifyFragment> {

        private String code;

        public CodeSuccessCallback(String code) {
            this.code = code;
        }

        @Override
        public void execute(VerifyFragment object) {
            object.onCodeSuccess(code);
        }
    }

    /**
     * Action for when error occurs retrieving code from server.
     */
    private static class CodeErrorCallback extends CallbackManager.Callback<VerifyFragment> {
        @Override
        public void execute(VerifyFragment object) {
            object.onCodeError();
        }
    }

    /**
     * Called when verification code successfully grabbed from server.
     * @param code verification code
     */
    public void onCodeSuccess(String code) {
        loading = false;
        mCode = code;
        mLoadingViewController.crossFade();
        initContent(mLoadingViewController.getContentView());
    }

    /**
     * Called when error occurs trying to get verification code from server.
     */
    public void onCodeError() {
        loading = false;
        initErrorContent();
    }

    /**
     * Used to verify credentials entered by user.  May return error
     * if user is already verified or registration has expired.  Shows errors in dialog.
     */
    private void verifyCredentials() {
        ArrayList<String> errors = new ArrayList<>();

        String userName = mUserNameText.getText().toString();
        if (userName == null || userName.length() < 1) {
            errors.add(NAME_ERROR);
        }

        String password = mPasswordText.getText().toString();
        if (password == null || password.length() < 1) {
            errors.add(PASSWORD_ERROR);
        }

        if (errors.size() > 0) {
            showErrorDialog(errors);
            return;
        }

        ProgressDialogFragment.show(getActivity());

        AuthStore.login(userName, password, new CredentialCallback(callbackManager));
    }

    /**
     * Called when user login successful.  Creates {@link com.thecn.app.models.util.VerificationBundle}
     * to use in order to gather verification data about the user.
     * @param response data from server
     */
    public void onCredentialSuccess(final JSONObject response) {
        VerificationBundle bundle = new VerificationBundle();
        AppSession.getInstance().setVerificationBundle(bundle);
        bundle.token = AuthStore.getToken(response);

        UserStore.postUserVerification(mCode, bundle.token, new VerifyCallback(callbackManager));
    }

    /**
     * Called when server states that user credentials were not valid.  Shows dialog
     * with errors from server.
     * @param response data from server
     */
    public void onCredentialFailure(JSONObject response) {
        ArrayList<String> errors = StoreUtil.getResponseErrors(response);
        showErrorDialog(errors);
    }

    /**
     * Called when call to post user verification returns from server successfully
     * @param response data from server
     */
    public void onVerificationResponse(JSONObject response) {
        if (!StoreUtil.success(response)) {
            String error = StoreUtil.getFirstResponseError(response);
            if (error != null) {
                onVerificationFailure(error);
            }

            return;
        }

        //get user data
        User user = UserStore.getData(response);
        String email;
        try {
            email = user.getUserProfile().getPrimaryEmail();
        } catch (NullPointerException e) {
            onVerificationFailure(EMAIL_ERROR);
            return;
        }

        if (TextUtil.isNullOrEmpty(email)) {
            onVerificationFailure(EMAIL_ERROR);
            return;
        }

        //check verification status
        VerificationBundle bundle = AppSession.getInstance().getVerificationBundle();
        if (bundle == null) {
            finishWithSessionEndMessage();
            return;
        }

        //set user data into verification bundle
        bundle.user = user;

        ((VerificationActivity) getActivity()).hideSoftInput();

        //push terms of use fragment
        getActionBarActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new TermsOfUseFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * Called when network call to post user verification fails.
     * Shows dialog with error
     * @param error error to display in dialog
     */
    public void onVerificationFailure(String error) {
        showErrorDialog(error);
    }

    /**
     * Base callback to use for network calls.  Other network callbacks extend from this.
     * Checks if loading dialogs present.  If not, does not take further action.
     * Dismisses loading dialog if present.
     */
    private static class BaseCallback extends CallbackManager.NetworkCallback<VerifyFragment> {
        public BaseCallback(CallbackManager<VerifyFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeBefore(VerifyFragment object) {
            DialogFragment dialog = object.getLoadingDialog();
            if (dialog == null) {
                //user cancelled dialog, don't do anything
                response = null;
                error = null;
                return;
            }

            dialog.dismiss();
        }

        @Override
        public void onResumeWithError(VerifyFragment object) {
            StoreUtil.showExceptionMessage(error);
        }
    }

    /**
     * Action to perform when network call to verify user credentials returns.
     */
    private static class CredentialCallback extends BaseCallback {
        public CredentialCallback(CallbackManager<VerifyFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(VerifyFragment object) {
            if (wasSuccessful()) object.onCredentialSuccess(response);
            else object.onCredentialFailure(response);
        }
    }

    /**
     * Action to perform when network call to post user verification returns.
     */
    private static class VerifyCallback extends CallbackManager.NetworkCallback<VerifyFragment> {
        public VerifyCallback(CallbackManager<VerifyFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(VerifyFragment object) {
            object.onVerificationResponse(response);
        }
    }
}
