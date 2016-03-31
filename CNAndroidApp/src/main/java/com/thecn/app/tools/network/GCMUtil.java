package com.thecn.app.tools.network;

import android.support.v4.app.FragmentActivity;

import com.thecn.app.fragments.common.GCMMessageDialogFragment;

/**
 * Utility methods for working with Google Play Services' Google Cloud Messaging
 */
public class GCMUtil {

    /**
     * Display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static void showPlayServicesDialog(FragmentActivity activity) {
        GCMMessageDialogFragment f = new GCMMessageDialogFragment();
        f.show(activity.getSupportFragmentManager(), GCMMessageDialogFragment.TAG);
    }
}
