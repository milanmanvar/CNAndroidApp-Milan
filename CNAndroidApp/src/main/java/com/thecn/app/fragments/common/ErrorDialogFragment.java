package com.thecn.app.fragments.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Used to show a list of errors to the user.
 */
public abstract class ErrorDialogFragment extends DialogFragment {

    /**
     * Uses {@link #getTitle()} and {@link #getMessage()} to set up dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getTitle())
                .setMessage(getMessage());

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    /**
     * Subclasses provide title
     * @return title
     */
    public abstract String getTitle();

    /**
     * Subclasses provide message (errors)
     * @return message
     */
    public abstract String getMessage();
}
