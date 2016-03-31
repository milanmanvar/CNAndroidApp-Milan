package com.thecn.app.activities.signup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.thecn.app.R;

/**
 * Used to send signup request to server.
 */
public class SignupActivity extends FragmentActivity {

    /**
     * Init and add fragment
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_container);

        if (savedInstanceState == null) {

            Fragment fragment = SignupFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    /**
     * Show list of errors to user.
     * @param errors list of errors to show to user.
     */
    public void showErrorDialog(String[] errors) {
        DialogFragment fragment = ErrorDialogFragment.getInstance(errors);
        fragment.show(getSupportFragmentManager(), "error");
    }

    /**
     * Shows a list of errors to the user.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        private String[] mErrors;

        /**
         * Get instance with arguments
         * @param errors array of errors to show
         * @return new instance of this class
         */
        public static ErrorDialogFragment getInstance(String[] errors) {
            Bundle args = new Bundle();
            args.putStringArray("errors", errors);

            ErrorDialogFragment fragment = new ErrorDialogFragment();
            fragment.setArguments(args);

            return fragment;
        }

        /**
         * Get errors from arguments
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mErrors = getArguments().getStringArray("errors");
        }

        /**
         * Create dialog
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Errors")
                    .setMessage(getText());
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);

            return dialog;
        }

        /**
         * Creates text with errors shown line by line.
         * @return
         */
        private String getText() {
            String text = "";
            //assuming that there must always be one error
            int lastPos = mErrors.length - 1;
            for (int i = 0; i < lastPos; i++) {
                text += mErrors[i] + "\n\n";
            }

            text += mErrors[lastPos];
            return text;
        }
    } //end ErrorDialogFragment

}
