package com.thecn.app.activities.signup;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.fragments.common.ProgressDialogFragment;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.text.TextUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* Used to send signup requests to the server
*/
public class SignupFragment extends Fragment {

    private View formLayout;
    private TextView signupTextView;

    private EditText mNameText, mEmailText, mPasswordText;
    private Button mSignupButton;

    private String email;
    private Spannable signupSpan;

    private CallbackManager<SignupFragment> callbackManager;

    private static final String SIGNUP_MSG_HEAD = "Please check your email (";
    private static final String SIGNUP_MSG_TAIL = ") to verify your CN Account.";
    private static final String REG_SUCCESSFUL = "Registration Successful";
    private static final String DONE = "DONE";

    private boolean hasSignedUp = false;

    public static SignupFragment newInstance() {
        return new SignupFragment();
    }

    /**
     * Set up
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        callbackManager = new CallbackManager<>();
    }

    /**
     * Get view references, set on click listener.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_signup, container, false);

        formLayout = view.findViewById(R.id.form_layout);
        signupTextView = (TextView) view.findViewById(R.id.signup_msg);

        mNameText = (EditText) view.findViewById(R.id.name);
        mEmailText = (EditText) view.findViewById(R.id.email);
        mPasswordText = (EditText) view.findViewById(R.id.password);

        mSignupButton = (Button) view.findViewById(R.id.sign_up_btn);
        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasSignedUp) {
                    getActivity().finish();
                } else {
                    signUp();
                }
            }
        });

        setViewsForSignupState();

        return view;
    }

    /**
     * Sets the state of the views depending on whether user has signed up.
     */
    private void setViewsForSignupState() {
        Resources r = getResources();

        if (hasSignedUp) {
            formLayout.setVisibility(View.GONE);
            signupTextView.setVisibility(View.VISIBLE);
            signupTextView.setText(signupSpan);
            mSignupButton.setText(DONE);
        } else {
            formLayout.setVisibility(View.VISIBLE);
            signupTextView.setVisibility(View.GONE);
            mSignupButton.setText(r.getString(R.string.sign_up));
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
     * Sends request to server to sign up using info in fields
     */
    private void signUp() {
        ArrayList<String> errors = new ArrayList<String>();

        //methods fill error list with errors if there are errors
        email = getEmail(errors);
        String[] name = getName(errors);
        String password = getPassword(errors);

        //if errors, don't proceed.  Show error dialog
        if (errors.size() > 0) {
            String[] errorArray = errors.toArray(new String[errors.size()]);
            getSignupActivity().showErrorDialog(errorArray);
            return;
        }

        //construct payload
        JSONObject jsonObject = UserStore.getNewUserJSON(
                name[0],
                name[1],
                email,
                password
        );
        if (jsonObject == null) {
            AppSession.showLongToast("Unable to create new user.");
            return;
        }

        ProgressDialogFragment.show(getActivity());

        UserStore.createNewUser(jsonObject, new SignupCallback(callbackManager));
    }

    /**
     * Make decisions based on response from creating a new user.
     */
    private static class SignupCallback extends CallbackManager.NetworkCallback<SignupFragment> {
        public SignupCallback(CallbackManager<SignupFragment> manager) {
            super(manager);
        }

        /**
         * Show errors if not successful, otherwise set view state, show success and instructions to verify account
         */
        @Override
        public void onResumeWithResponse(final SignupFragment object) {
            if (!wasSuccessful()) {
                ArrayList<String> list = StoreUtil.getResponseErrors(response);
                if (list == null) return;
                String[] errors = list.toArray(new String[list.size()]);

                object.getSignupActivity().showErrorDialog(errors);
                return;
            }

            object.hasSignedUp = true;

            String signupText = REG_SUCCESSFUL + "\n\n" + SIGNUP_MSG_HEAD + object.email + SIGNUP_MSG_TAIL;
            object.signupSpan = new SpannableString(signupText);
            object.signupSpan.setSpan(new RelativeSizeSpan(1.5f), 0, REG_SUCCESSFUL.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            object.setViewsForSignupState();

            ProgressDialogFragment.dismiss(object.getActivity());
        }

        /**
         * Show error
         */
        @Override
        public void onResumeWithError(SignupFragment object) {
            ProgressDialogFragment.dismiss(object.getActivity());

            StoreUtil.showExceptionMessage(error);
        }
    }

    private static final String BLANK_TAIL = " cannot be blank.";

    private static final String ALPHA_SPACE_REGEX = "[a-zA-Z\\s]*";
    private static final Pattern NAME_PATTERN = Pattern.compile("([\\S]{2,})\\s+([\\S]{2,})");

    private static final String ALPHA_ERROR = "Only English letters are accepted, please remove invalid characters.";
    private static final String LENGTH_ERROR = "Full name requires two characters for both first and last name.";

    /**
     * Get user name and check for errors
     * @param errors list to add errors to
     * @return valid name or null
     */
    private String[] getName(ArrayList<String> errors) {
        String fullName = mNameText.getText().toString();
        if (fullName == null || fullName.length() < 1) {
            errors.add("Name" + BLANK_TAIL);
            return null;
        }

        //name must only contain alphabetic or space characters
        if (!fullName.matches(ALPHA_SPACE_REGEX)) {
            errors.add(ALPHA_ERROR);
        }

        //name must have two words with two characters each
        Matcher matcher = NAME_PATTERN.matcher(fullName);
        if (!matcher.find()) {
            errors.add(LENGTH_ERROR);
        }

        if (errors.size() > 0) {
            return null;
        } else {
            return new String[]{
                    matcher.group(1),
                    matcher.group(2)
            };
        }
    }

    private static final String EMAIL_ERROR = "Email address is invalid.";

    /**
     * Get user's email, check for errors
     * @param errors list to add errors to
     * @return valid email or null
     */
    private String getEmail(ArrayList<String> errors) {
        String email = mEmailText.getText().toString();
        if (email == null || email.length() < 1) {
            errors.add("Email" + BLANK_TAIL);
            return null;
        }

        if (TextUtil.isValidEmail(email)) {
            return email;
        } else {
            errors.add(EMAIL_ERROR);
            return null;
        }
    }

    private static final String PASSWORD_ERROR = "Passwords must be at least 6 characters long.";

    /**
     * Get user's password and check for errors
     * @param errors list to add errors to
     * @return valid password or null
     */
    private String getPassword(ArrayList<String> errors) {
        String password = mPasswordText.getText().toString();
        if (password == null || password.length() < 1) {
            errors.add("Password" + BLANK_TAIL);
            return null;
        }

        //password must be more than 6 characters
        if (password.length() < 6) {
            errors.add(PASSWORD_ERROR);
            return null;
        } else {
            return password;
        }
    }

    /**
     * Cast activity to signup activity
     * @return cast activity
     */
    protected SignupActivity getSignupActivity() {
        return (SignupActivity) getActivity();
    }
} //end StartFragment
