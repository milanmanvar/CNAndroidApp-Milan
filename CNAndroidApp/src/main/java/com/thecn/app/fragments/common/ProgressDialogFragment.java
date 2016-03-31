package com.thecn.app.fragments.common;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

/**
 * Used to show an indeterminate progress dialog to the user.
 */
public class ProgressDialogFragment extends DialogFragment {

    public static final String TAG = "loading_dialog";

    private static final String MESSAGE_TAG = "message_tag";
    private static final String DEFAULT_MESSAGE = "Loading...";

    private static final String CANCELLABLE_TAG = "cancellable";

    private String message;
    private boolean cancellable;

    /**
     * Set the message shown by dialog
     * @param message message to show
     */
    public void setMessage(String message) {
        this.message = message;
        ProgressDialog dialog = (ProgressDialog) getDialog();
        if (dialog == null) return;
        dialog.setMessage(message);
    }

    /**
     * Show the dialog, cancellable
     * @param activity activity to use
     */
    public static void show(FragmentActivity activity) {
        Bundle b = new Bundle();
        b.putBoolean(CANCELLABLE_TAG, true);
        internalShow(activity, b);
    }

    /**
     * Show the dialog with custom message
     * @param message custom message
     * @param activity activity to use
     */
    public static void show(String message, FragmentActivity activity) {
        Bundle b = new Bundle();
        b.putString(MESSAGE_TAG, message);
        b.putBoolean(CANCELLABLE_TAG, true);
        internalShow(activity, b);
    }

    /**
     * Show the dialog, uncancellable
     * @param activity activity to use
     */
    public static void showUncancelable(FragmentActivity activity) {
        Bundle b = new Bundle();
        b.putBoolean(CANCELLABLE_TAG, false);
        internalShow(activity, b);
    }

    /**
     * Show dialog, uncancellable, with custom message
     * @param message custom message
     * @param activity activity to use
     */
    public static void showUncancelable(String message, FragmentActivity activity) {
        Bundle b = new Bundle();
        b.putString(MESSAGE_TAG, message);
        b.putBoolean(CANCELLABLE_TAG, false);
        internalShow(activity, b);
    }

    /**
     * Creates instance of this class and uses to show dialog to user.
     * @param activity activity to use
     * @param args arguments to set to instance
     */
    private static void internalShow(FragmentActivity activity, Bundle args) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        if (args != null) fragment.setArguments(args);
        fragment.show(activity.getSupportFragmentManager(), ProgressDialogFragment.TAG);
    }

    /**
     * Gets fragment from fragment manager using {@link #TAG}
     * @param activity activity to use
     * @return instance of this class if found
     */
    public static ProgressDialogFragment get(FragmentActivity activity) {
        return (ProgressDialogFragment) activity.getSupportFragmentManager()
                .findFragmentByTag(ProgressDialogFragment.TAG);
    }

    /**
     * Dismiss instance of this class if found using {@link #get}
     * @param activity activity to use
     * @return true if dismissed, false if not found
     */
    public static boolean dismiss(FragmentActivity activity) {
        ProgressDialogFragment f = get(activity);
        if (f == null) return false;

        f.dismiss();
        return true;
    }

    /**
     * Get custom message if present and cancellable flag
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            message = getArguments().getString(MESSAGE_TAG);
        } else {
            message = savedInstanceState.getString(MESSAGE_TAG);
        }

        cancellable = getArguments().getBoolean(CANCELLABLE_TAG);
        setCancelable(cancellable);
    }

    /**
     * Fixes bug in support library when retaining instance of {@link android.support.v4.app.DialogFragment}
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null)
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    /**
     * Creates and returns dialog using flags and values set.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());

        Bundle b = getArguments();
        if (b != null) {
            if (cancellable) {
                dialog.setCanceledOnTouchOutside(false);
            }
        }

        String message = this.message == null ? DEFAULT_MESSAGE : this.message;
        dialog.setMessage(message);

        return dialog;
    }

    /**
     * Save message field
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (message == null) return;
        outState.putString(MESSAGE_TAG, message);
    }

    /**
     * Call activity's on dismiss
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        if (getActivity() instanceof DialogInterface.OnDismissListener) {
            DialogInterface.OnDismissListener listener = (DialogInterface.OnDismissListener) getActivity();
            listener.onDismiss(dialog);
        }

        super.onDismiss(dialog);
    }

    /**
     * Call activity's on cancel
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        if (getActivity() instanceof DialogInterface.OnCancelListener) {
            DialogInterface.OnCancelListener listener = (DialogInterface.OnCancelListener) getActivity();
            listener.onCancel(dialog);
        }

        super.onCancel(dialog);
    }
}
