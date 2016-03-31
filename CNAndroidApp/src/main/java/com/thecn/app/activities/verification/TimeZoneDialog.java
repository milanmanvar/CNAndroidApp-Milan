package com.thecn.app.activities.verification;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;

import com.thecn.app.fragments.common.ChoiceDialog;

import java.util.Arrays;

/**
* Dialog for showing choice of whether to show time zones of the user's country or show ALL available time zone choices.
*/
public class TimeZoneDialog extends ChoiceDialog {
    public static final String TAG = "time_zone_dialog";

    private static String[] choices = new String[] {
            "Show your country's time zones",
            "Show all time zones"
    };

    /**
     * Get action to perform when item is clicked
     * @return appropriate action to perform
     */
    @Override
    public DialogInterface.OnClickListener getListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Fragment fragment = getContainerFragment();
                if (fragment != null && fragment instanceof TimeZoneFragment) {
                    TimeZoneFragment tzFragment = (TimeZoneFragment) fragment;
                    boolean showByCountry = i == 0;
                    tzFragment.assertTimeZoneFilterType(showByCountry);
                }
            }
        };
    }

    /**
     * Get adapter for showing list items
     * @return adapter
     */
    @Override
    public ObjectAdapter getAdapter() {
        return new ObjectAdapter<>(
                Arrays.asList(choices), getActivity()
        );
    }
}
