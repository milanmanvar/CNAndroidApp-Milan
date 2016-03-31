package com.thecn.app.activities.verification;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.fragments.common.ProgressDialogFragment;
import com.thecn.app.models.profile.TimeZone;
import com.thecn.app.models.util.VerificationBundle;
import com.thecn.app.models.util.VisibilitySetting;
import com.thecn.app.stores.SearchStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.LoadingViewController;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shows list of time zones to user.  User can choose a time zone from list.
 * Can either show time zones associated with user's country or show ALL time zones available.
*/
public class TimeZoneFragment extends VisibilityFragment {
    private static final String TITLE = "Choose Your Time Zone";
    private static final String LOAD_ERROR = "Could not load data.";
    private static final String RETRY = "Retry";

    private ObjectAdapter<TimeZone> mAdapter;
    private LoadingViewController mLoadingViewController;

    protected TextView mVisibilityText;
    private TextView mSearchBox;

    private ArrayList<TimeZone> timeZones; //all time zones
    private ArrayList<TimeZone> countryTimeZones; //time zones of country

    private boolean showByCountry = true;

    private static final Pattern ZONE_PATTERN = Pattern.compile("(\\([^)]*\\))(.*)");

    private CallbackManager<TimeZoneFragment> callbackManager;

    /**
     * Set up and start loading
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        callbackManager = new CallbackManager<>();
        startLoading();
    }

    @Override
    public void onResume() {
        super.onResume();
        callbackManager.resume(this);
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    /**
     * Get list view adapter
     * @return adapter
     */
    public ObjectAdapter<TimeZone> getTimeZoneAdapter() {
        return mAdapter;
    }

    /**
     * Shows option for choosing whether time zones are shown by user country
     * or by all time zones available.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getCountryID() == null) {
            super.onCreateOptionsMenu(menu, inflater);
            return;
        }

        inflater.inflate(R.menu.time_zone, menu);
        int titleID = showByCountry ? R.string.country : R.string.all;
        menu.findItem(R.id.action_change_filter).setTitle(titleID);

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Show time zone dialog when button clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_change_filter) {
            showTimeZoneDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Change actionbar title
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActionBarActivity().getSupportActionBar().setTitle(TITLE);
    }

    /**
     * Get token and load time zones.  If no country id present, load all time zones.  Else load countrie's time zone
     */
    private void startLoading() {

        String token = getToken();
        if (token == null) {
            return;
        }

        String countryID = getCountryID();
        if (countryID == null) {
            loading = true;
            SearchStore.getTimeZoneList(token, new CountryLoadCallback(callbackManager));
        } else {
            loading = true;
            SearchStore.getTimeZoneListByCountry(token, countryID, new CountryLoadCallback(callbackManager));
        }
    }

    /**
     * Get's the country's id associated with user.
     * @return country id
     */
    public String getCountryID() {
        VerificationBundle bundle = AppSession.getInstance().getVerificationBundle();
        if (bundle == null) {
            return null;
        } else {
            return bundle.country.getId();
        }
    }

    /**
     * Actions to perform when network call for time zones by country returns
     */
    private static class CountryLoadCallback extends CallbackManager.NetworkCallback<TimeZoneFragment> {
        public CountryLoadCallback(CallbackManager<TimeZoneFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(TimeZoneFragment object) {
            if (wasSuccessful()) object.onCountryLoadSuccess(response);
            else object.onCountryLoadFailure();
        }

        @Override
        public void onResumeWithError(TimeZoneFragment object) {
            object.onCountryLoadFailure();
        }
    }

    /**
     * Called when network call for getting time zones by country returns successfully
     * @param response data from server
     */
    public void onCountryLoadSuccess(JSONObject response) {
        loading = false;

        countryTimeZones = SearchStore.getTimeZoneData(response);
        if (countryTimeZones == null) {
            onCountryLoadFailure();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                //process list data
                countryTimeZones = processTimeZoneList(countryTimeZones);
                callbackManager.addCallback(new CountryProcessingCallback(
                        countryTimeZones.size() != 0
                ));
            }
        }).start();
    }

    /**
     * Called when list of time zones by country is fully processed
     */
    private static class CountryProcessingCallback extends CallbackManager.Callback<TimeZoneFragment> {

        private boolean success;

        public CountryProcessingCallback(boolean success) {
            this.success = success;
        }

        @Override
        public void execute(TimeZoneFragment object) {
            if (success) object.onCountryLoadComplete();
            else object.onCountryLoadFailure();
        }
    }

    /**
     * Called when time zones by country are ready to display to the user.
     */
    private void onCountryLoadComplete() {
        mAdapter = new ObjectAdapter<>(countryTimeZones, getActivity().getApplicationContext());
        mAdapter.setTextColor(getResources().getColor(R.color.black));
        initContent(mLoadingViewController.getContentView());
        mLoadingViewController.crossFade();
    }

    /**
     * Called when there was an error loading time zones by country.
     */
    private void onCountryLoadFailure() {
        loading = false;
        initErrorContent();
    }

    /**
     * Called when user picks an option for either showing time zones by country or all time zones.
     * @param showByCountry true if show time zones by country, false otherwise
     */
    public void assertTimeZoneFilterType(boolean showByCountry) {
        //if different, a change has been made
        if (this.showByCountry ^ showByCountry) {
            //determine whether to show by country or show all
            if (!showByCountry) {
                if (timeZones == null) {
                    //if time zone list null, get from server
                    String token = getToken();
                    if (token == null) return;

                    ProgressDialogFragment.show(getActivity());

                    SearchStore.getTimeZoneList(token, new AllTimeZoneNetworkCallback(callbackManager));
                    return;
                }
            } else {
                if (countryTimeZones == null) {
                    //if country time zone list null, get from server
                    String token = getToken();
                    if (token == null) return;

                    String countryID = getCountryID();
                    if (countryID == null) return;

                    ProgressDialogFragment.show(getActivity());

                    SearchStore.getTimeZoneListByCountry(token, countryID, new CountryLoadCallback(callbackManager));
                }
            }

            setTimeZoneFilterType(showByCountry);
        }
    }

    /**
     * Get token for making network calls.  Gets from verification bundle
     * @return token
     */
    public String getToken() {
        VerificationBundle bundle = AppSession.getInstance().getVerificationBundle();
        if (bundle == null) {
            finishWithSessionEndMessage();
            return null;
        } else {
            return bundle.token;
        }
    }

    /**
     * Actions to take when network call for all time zones returns.
     */
    private static class AllTimeZoneNetworkCallback extends CallbackManager.NetworkCallback<TimeZoneFragment> {
        public AllTimeZoneNetworkCallback(CallbackManager<TimeZoneFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeBefore(TimeZoneFragment object) {
            DialogFragment loadingDialog = object.getLoadingDialog();
            if (loadingDialog == null) {
                //ensures neither of methods below are used
                response = null;
                error = null;
                return;
            }

            loadingDialog.dismiss();
        }

        @Override
        public void onResumeWithResponse(TimeZoneFragment object) {
            if (wasSuccessful()) object.onAllTimeZoneNetworkSuccess(response);
            else object.onAllTimeZoneFailure();
        }

        @Override
        public void onResumeWithError(TimeZoneFragment object) {
            StoreUtil.showExceptionMessage(error);
        }
    }

    /**
     * Called when network call for all time zones is successful
     * @param response data from server
     */
    public void onAllTimeZoneNetworkSuccess(JSONObject response) {
        timeZones = SearchStore.getTimeZoneData(response);
        if (timeZones == null) {
            onAllTimeZoneFailure();
            return;
        }

        //process time zone data in separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                timeZones = processTimeZoneList(timeZones);
                callbackManager.addCallback(new AllTimeZoneProcessingCallback(
                        timeZones.size() != 0
                ));
            }
        }).start();
    }

    /**
     * Called when network call for all time zones fails
     */
    public void onAllTimeZoneFailure() {
        AppSession.showDataLoadError("Time Zone");
    }

    /**
     * Used when finished processing all time zones and ready to display.
     */
    private static class AllTimeZoneProcessingCallback extends CallbackManager.Callback<TimeZoneFragment> {
        private boolean success;

        public AllTimeZoneProcessingCallback(boolean success) {
            this.success = success;
        }

        @Override
        public void execute(TimeZoneFragment object) {
            if (success) object.setTimeZoneFilterType(false);
            else object.onAllTimeZoneFailure();
        }
    }

    /**
     * Updates menu item display, switches adapter's data source, resets filter (search box).
     * These are set depending on whether showing or not showing timezones by country.
     * @param showByCountry whether to show all time zones or just by country
     */
    private void setTimeZoneFilterType(boolean showByCountry) {
        this.showByCountry = showByCountry;
        getActivity().supportInvalidateOptionsMenu();
        ArrayList<TimeZone> source = showByCountry ? countryTimeZones : timeZones;
        mSearchBox.setText("");
        mAdapter.changeDataSource(source);
        mAdapter.getFilter().filter("");
    }

    /**
     * Reverses order of strings in time zone list to make them more readable.
     * This should be done on the server, but...
     * @param timeZones list of time zones.
     * @return processed list of time zones.
     */
    private ArrayList<TimeZone> processTimeZoneList(ArrayList<TimeZone> timeZones) {
        ArrayList<TimeZone> retList = new ArrayList<>();
        retList.ensureCapacity(timeZones.size());

        for (int i = 0; i < timeZones.size(); i++) {
            TimeZone timeZone = timeZones.get(i);
            if (timeZone == null) {
                timeZones.remove(i);
            } else {
                String zone = timeZone.getZone();
                Matcher matcher = ZONE_PATTERN.matcher(zone);
                if (matcher.find()) {
                    try {
                        //reverse time zone strings so they can be more easily read and searched
                        String time = matcher.group(1).trim();
                        String location = matcher.group(2).trim();

                        String[] segments = location.split("-");

                        if (segments.length != 0) {
                            location = "";

                            for (int j = segments.length - 1; j > 0; j--) {
                                location += segments[j].trim() + " - ";
                            }

                            location += segments[0].trim();
                        }

                        zone = location + " " + time;
                        timeZone.setZone(zone);

                        retList.add(timeZone);
                    } catch (NullPointerException e) {
                        timeZones.remove(i);
                    }
                }
            }
        }

        return retList;
    }

    /**
     * Uses {@link com.thecn.app.tools.controllers.LoadingViewController} to change
     * view to show either loading, error, or loaded state.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.search_choose_list, container, false);

        if (loading) {
            initViewController(content, getActivity());
            mLoadingViewController.showLoading(true);
            return mLoadingViewController.getRootView();
        } else if (mAdapter == null) {
            initViewController(content, getActivity());
            return initErrorContent();
        }

        return initContent(content);
    }

    /**
     * Data loaded successfully, set up view to show time zones to user.
     * @param content root view
     * @return root view
     */
    public View initContent(View content) {
        mSearchBox = (EditText) content.findViewById(R.id.search_box);
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                //filter out time zones that do not contain search string
                mAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //button for clearing search text
        content.findViewById(R.id.clear_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchBox.setText("");
            }
        });

        //button for changing visibility
        content.findViewById(R.id.change_visibility).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVisibilityDialog();
            }
        });

        //displays current visibility of user's time zone
        mVisibilityText = (TextView) content.findViewById(R.id.visibility_display);
        VisibilitySetting setting = getVisibilitySetting();
        if (setting == null) {
            setting = new VisibilitySetting(VisibilitySetting.PUBLIC);
            setVisibilitySetting(setting);
        } else {
            mVisibilityText.setText(setting.toString());
        }

        ListView listView = (ListView) content.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(getOnItemClickListener());

        return content;
    }

    /**
     * Get action to perform when a time zone is clicked
     * @return an action to perform
     */
    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VerificationBundle bundle = AppSession.getInstance().getVerificationBundle();
                if (bundle == null) {
                    finishWithSessionEndMessage();
                    return;
                }

                //set user time zone
                bundle.timeZone = (TimeZone) mAdapter.getItem(position);
                bundle.timeZoneVisibility = getVisibilitySetting();

                ((VerificationActivity) getActivity()).hideSoftInput();

                //push profile picture fragment
                getActionBarActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new ProfilePictureFragment())
                        .addToBackStack(null)
                        .commit();
            }
        };
    }

    /**
     * Get visibility setting of user's time zone
     * @param setting visibility setting
     */
    @Override
    public void setVisibilitySetting(VisibilitySetting setting) {
        super.setVisibilitySetting(setting);
        mVisibilityText.setText(setting.toString());
    }

    /**
     * Show error message and button to retry network call
     * @return root view
     */
    public View initErrorContent() {
        mLoadingViewController.showLoading(false);
        mLoadingViewController.showMessage(LOAD_ERROR);
        mLoadingViewController.showButton(RETRY, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //retry network call
                mLoadingViewController.showLoading(true);
                mLoadingViewController.showMessage(false);
                mLoadingViewController.showButton(false);
                startLoading();
            }
        });

        return mLoadingViewController.getRootView();
    }

    /**
     * Initialize {@link com.thecn.app.tools.controllers.LoadingViewController}
     * @param content content to be shown to user once loading complete
     * @param context used to inflate views
     */
    private void initViewController(View content, Context context) {
        mLoadingViewController = new LoadingViewController(content, context);
        mLoadingViewController.showLoadingView();
    }
}
