package com.thecn.app.activities.profile.basicinfo;

import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.thecn.app.AppSession;
import com.thecn.app.activities.verification.ObjectAdapter;
import com.thecn.app.activities.verification.TimeZoneFragment;
import com.thecn.app.models.profile.Country;
import com.thecn.app.models.profile.TimeZone;
import com.thecn.app.models.util.VisibilitySetting;

/**
 * Created by philjay on 2/23/15.
 */
public class ProfileTimeZoneFragment extends TimeZoneFragment {

    /**
     * Overridden to be applicable to {@link com.thecn.app.activities.profile.basicinfo.EditBasicInfoActivity}
     * Sets changes to time zone choice on click.
     * @return on item click listener that specifies action to take
     */
    @Override
    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ObjectAdapter<TimeZone> adapter = getTimeZoneAdapter();
                TimeZone timeZone = (TimeZone) adapter.getItem(position);

                FragmentActivity activity = getActionBarActivity();
                StartFragment f = getStartFragment();

                if (f == null) return;

                //set local changes
                f.getLocalProfileChanges().setTimeZone(timeZone.getZone());
                f.getLocalProfileChanges().setTimeZoneID(timeZone.getId());
                activity.onBackPressed();
            }
        };
    }

    /**
     * Gets the user's country id if it exists.
     * Tries to get it from the {@link com.thecn.app.activities.profile.basicinfo.StartFragment}
     * @return the user's country id.
     */
    @Override
    public String getCountryID() {
        StartFragment f = (StartFragment)
                getActionBarActivity().getSupportFragmentManager().findFragmentByTag(StartFragment.TAG);
        if (f == null) return null;

        Country c = f.getLocalProfileChanges().getCountry();
        if (c != null && c.getId() != null && !c.getId().isEmpty()) {
            return c.getId();
        } else {
            return null;
        }
    }

    /**
     * Gets the StartFragment of this {@link com.thecn.app.activities.profile.basicinfo.EditBasicInfoActivity}
     * @return the start fragment
     */
    private StartFragment getStartFragment() {
        return (StartFragment) getActionBarActivity().getSupportFragmentManager()
                .findFragmentByTag(StartFragment.TAG);
    }

    /**
     * Gets the visibility setting
     * @return visibility setting associated with the user's time zone.
     */
    @Override
    public VisibilitySetting getVisibilitySetting() {
        StartFragment f = getStartFragment();
        if (f == null) return null;

        return f.getVisibilitySettings()[4];
    }

    /**
     * Sets visibility setting
     * @param setting visibility setting to associate with user's time zone.
     */
    @Override
    public void setVisibilitySetting(VisibilitySetting setting) {
        StartFragment f = getStartFragment();
        if (f == null) return;

        f.getVisibilitySettings()[4] = setting;
        mVisibilityText.setText(setting.toString());
    }

    /**
     * Pressing back makes no changes.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Get token
     * @return token to use in requests to server.  Overridden from {@link com.thecn.app.activities.verification.TimeZoneFragment}
     */
    @Override
    public String getToken() {
        return AppSession.getInstance().getToken();
    }

}
