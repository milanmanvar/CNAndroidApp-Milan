package com.thecn.app.fragments.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Used to contain a dialog created by {@link com.google.android.gms.common.GooglePlayServicesUtil}
 * so that it will be recreated and properly dismissed on orientation changes, etc.
 */
public class GCMMessageDialogFragment extends DialogFragment {

    public static final String TAG = "gcm_message_dialog";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Get dialog from {@link com.google.android.gms.common.GooglePlayServicesUtil}
     * use {@link android.content.DialogInterface.OnCancelListener} if Activity implements it.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnCancelListener) {
            return GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_MISSING, activity,
                    PLAY_SERVICES_RESOLUTION_REQUEST, (DialogInterface.OnCancelListener) activity);
        } else {
            return GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_MISSING, getActivity(),
                    PLAY_SERVICES_RESOLUTION_REQUEST);
        }
    }

    /**
     * Call activity's on cancel method
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (getActivity() instanceof DialogInterface.OnCancelListener) {
            DialogInterface.OnCancelListener listener = (DialogInterface.OnCancelListener) getActivity();
            listener.onCancel(dialog);
        }
    }

    /**
     * Call activity's on dismiss method
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof DialogInterface.OnDismissListener) {
            DialogInterface.OnDismissListener listener = (DialogInterface.OnDismissListener) getActivity();
            listener.onDismiss(dialog);
        }
    }
}
