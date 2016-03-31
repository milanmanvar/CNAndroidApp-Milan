package com.thecn.app.activities.profile.basicinfo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.thecn.app.AppSession;
import com.thecn.app.activities.verification.CountriesFragment;
import com.thecn.app.activities.verification.ObjectAdapter;
import com.thecn.app.models.profile.Country;
import com.thecn.app.models.util.VisibilitySetting;

/**
* Shows a list of countries the user can set as his/her own.
*/
public class ProfileCountriesFragment extends CountriesFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Gets an on item click listener.
     * Overridden from {@link com.thecn.app.activities.verification.CountriesFragment}
     * to be applicable to {@link com.thecn.app.activities.profile.basicinfo.EditBasicInfoActivity}.
     * Sets change and pops fragment if successful.
     * @return listener that specifies action to take.
     */
    @Override
    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ObjectAdapter<Country> adapter = getCountryAdapter();
                Country country = (Country) adapter.getItem(position);

                FragmentActivity activity = getActionBarActivity();
                StartFragment f = getStartFragment();

                if (f == null) return;

                f.getLocalProfileChanges().setCountry(country);
                activity.onBackPressed();
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
        }

        return true;
    }

    /**
     * Gets start fragment
     * @return start fragment associated with {@link com.thecn.app.activities.profile.basicinfo.EditBasicInfoActivity}
     */
    private StartFragment getStartFragment() {
        return (StartFragment) getActionBarActivity().getSupportFragmentManager()
                .findFragmentByTag(StartFragment.TAG);
    }

    /**
     * Get visibility setting
     * @return setting associated with user's country.
     */
    @Override
    public VisibilitySetting getVisibilitySetting() {
        StartFragment f = getStartFragment();
        if (f == null) return null;

        return f.getVisibilitySettings()[3];
    }

    /**
     * Set visibility setting
     * @param setting set the setting to be associated with user's country
     */
    @Override
    public void setVisibilitySetting(VisibilitySetting setting) {
        StartFragment f = getStartFragment();
        if (f == null) return;

        f.getVisibilitySettings()[3] = setting;
        mVisibilityText.setText(setting.toString());
    }

    /**
     * Get token
     * @return token to be used in network calls
     */
    @Override
    public String getToken() {
        return AppSession.getInstance().getToken();
    }
}
