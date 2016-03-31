package com.thecn.app.fragments.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.widget.BaseAdapter;

import com.thecn.app.R;
import com.thecn.app.activities.verification.ObjectAdapter;

/**
* Used to show a choice of options to the user.
*/
public abstract class ChoiceDialog extends DialogFragment {

    /**
     * Uses {@link #getAdapter()} and {@link #getListener()} to
     * set up the dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setAdapter(getAdapter(), getListener());

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    /**
     * Cast activity
     * @return cast activity
     */
    public ActionBarActivity getActionBarActivity() {
        return (ActionBarActivity) getActivity();
    }

    /**
     * Get fragment in the view with id "container"
     * @return container fragment
     */
    public Fragment getContainerFragment() {
        return getActionBarActivity().getSupportFragmentManager().findFragmentById(R.id.container);
    }

    /**
     * Subclasses provide adapter for list view.
     * @return adapter
     */
    public abstract BaseAdapter getAdapter();

    /**
     * Subclasses provide on click listener for list view
     * @return on click listener
     */
    public abstract DialogInterface.OnClickListener getListener();
}
