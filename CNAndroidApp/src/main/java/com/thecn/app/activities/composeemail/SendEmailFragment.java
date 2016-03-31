package com.thecn.app.activities.composeemail;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.stores.EmailStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;

import org.json.JSONObject;

/**
* Created by philjay on 3/2/15.
* Used to send emails constructed with the ComposeEmail activity
*/
public class SendEmailFragment extends Fragment {

    private CallbackManager<SendEmailFragment> callbackManager;

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

    /**
     * Sends the email data to the server using {@link com.thecn.app.stores.EmailStore}
     */
    public void sendEmail() {
        JSONObject email = getComposeEmailActivity().constructEmail();
        if (email == null) return;

        getComposeEmailActivity().showProgressDialog(true);

        EmailStore.sendEmail(email, new Callback(callbackManager));
    }

    /**
     * Called when a response is acquired after sending the email
     */
    private static class Callback extends CallbackManager.NetworkCallback<SendEmailFragment> {

        private static final String SUCCESS = "Message sent!";
        private static final String FAIL = "Could not send email.";
        private static final String INPUT_ERROR = "Could not use input.";

        public Callback(CallbackManager<SendEmailFragment> manager) {
            super(manager);
        }

        @Override
        public void onImmediateResponse(JSONObject response) {
            if (wasSuccessful()) {
                AppSession.showLongToast(SUCCESS);
            } else {
                AppSession.showLongToast(FAIL);
            }
        }

        @Override
        public void onImmediateError(VolleyError error) {
            error = error != null ? error : new VolleyError(INPUT_ERROR);
            StoreUtil.showExceptionMessage(error);
        }

        @Override
        public void onResumeBefore(SendEmailFragment object) {
            object.getComposeEmailActivity().showProgressDialog(false);
        }

        @Override
        public void onResumeWithResponse(SendEmailFragment object) {
            if (wasSuccessful()) {
                object.getActivity().finish();
            }
        }
    }

    private ComposeEmailActivity getComposeEmailActivity() {
        return (ComposeEmailActivity) getActivity();
    }
}
