package com.thecn.app.activities.verification;

import com.thecn.app.models.util.VisibilitySetting;

/**
* Base class for fragments that have a visibility setting associated with them.
 * These include {@link com.thecn.app.activities.verification.CountriesFragment}
 * and {@link com.thecn.app.activities.verification.TimeZoneFragment}
*/
public abstract class VisibilityFragment extends BaseFragment {

    private VisibilitySetting mVisibilitySetting;

    public void setVisibilitySetting(VisibilitySetting setting) {
        mVisibilitySetting = setting;
    }

    public VisibilitySetting getVisibilitySetting() {
        return mVisibilitySetting;
    }
}
