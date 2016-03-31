package com.thecn.app.models.util;

import com.thecn.app.models.user.User;
import com.thecn.app.models.profile.Country;
import com.thecn.app.models.profile.TimeZone;

/**
 * Class used to associate data used when in the process of
 * verifying a new user.  See {@link com.thecn.app.activities.verification.VerificationActivity}
 */
public class VerificationBundle {

    public User user;
    public String token;
    public Country country;
    public VisibilitySetting countryVisibility;
    public TimeZone timeZone;
    public VisibilitySetting timeZoneVisibility;

    public void clear() {
        user = null;
        token = null;
        country = null;
        countryVisibility = null;
        timeZone = null;
        timeZoneVisibility = null;
    }
}
