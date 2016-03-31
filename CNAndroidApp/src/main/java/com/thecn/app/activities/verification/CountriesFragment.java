package com.thecn.app.activities.verification;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.models.profile.Country;
import com.thecn.app.models.util.VerificationBundle;
import com.thecn.app.models.util.VisibilitySetting;
import com.thecn.app.stores.SearchStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.LoadingViewController;

import org.json.JSONObject;

import java.util.ArrayList;

/**
* Used to show a list of countries that the user can choose from.
*/
public class CountriesFragment extends VisibilityFragment {

    private static final String TITLE = "Choose Your Country";
    private static final String LOAD_ERROR = "Could not load data.";
    private static final String RETRY = "Retry";

    private ObjectAdapter<Country> mAdapter;
    private LoadingViewController mLoadingViewController;

    protected TextView mVisibilityText;

    private CallbackManager<CountriesFragment> callbackManager;

    /**
     * Set up and start loading
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = new CallbackManager<>();
        startLoading();
    }

    @Override
    public void onResume() {
        super.onResume();
        callbackManager.resume(this);
    }

    /**
     * Get action to take when item is clicked.  In this case,
     * set user's country and replace this fragment with {@link com.thecn.app.activities.verification.TimeZoneFragment}
     * @return action to take when item clicked
     */
    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Country country = (Country) mAdapter.getItem(position);
                VerificationBundle bundle = AppSession.getInstance().getVerificationBundle();
                if (bundle == null) {
                    finishWithSessionEndMessage();
                    return;
                }

                bundle.country = country;
                bundle.countryVisibility = getVisibilitySetting();

                ((VerificationActivity) getActivity()).hideSoftInput();

                getActionBarActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new TimeZoneFragment())
                        .addToBackStack(null)
                        .commit();
            }
        };
    }

    /**
     * Get the adapter used in list view
     * @return adapter
     */
    public ObjectAdapter<Country> getCountryAdapter() {
        return mAdapter;
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    /**
     * Set action bar title when activity created
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActionBarActivity().getSupportActionBar().setTitle(TITLE);
    }

    /**
     * Get token and start loading countries.
     */
    private void startLoading() {
        String token = getToken();
        if (token == null) {
            return;
        }

        loading = true;
        SearchStore.getCountriesList(token, new Callback(callbackManager));
    }

    /**
     * Gets the token to be used in making network calls for country info
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
     * Used to make decisions when network call for country info returns
     */
    private static class Callback extends CallbackManager.NetworkCallback<CountriesFragment> {
        public Callback(CallbackManager<CountriesFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(CountriesFragment object) {
            if (wasSuccessful()) object.onLoadSuccess(response);
            else object.onLoadFailure();
        }

        @Override
        public void onResumeWithError(CountriesFragment object) {
            object.onLoadFailure();
        }
    }

    /**
     * Called when successfully get information from server
     * @param response data from server
     */
    private void onLoadSuccess(JSONObject response) {
        loading = false;

        ArrayList<Country> countries = SearchStore.getCountryData(response);
        if (countries == null) {
            onLoadFailure();
            return;
        }

        //show content to user since data is now available
        mAdapter = new ObjectAdapter<>(countries, getActivity().getApplicationContext());
        mAdapter.setTextColor(getResources().getColor(R.color.black));
        initContent(mLoadingViewController.getContentView());
        mLoadingViewController.crossFade();
    }

    /**
     * Show error content to user, allow to try again.
     */
    private void onLoadFailure() {
        loading = false;
        initErrorContent();
    }

    /**
     * Uses {@link com.thecn.app.tools.controllers.LoadingViewController}
     * to set view for loading, error, or loaded state
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

        //if not loading and no error, just get the content view.
        return initContent(content);
    }

    /**
     * Set up content to display to user (list of countries with search box).
     * @param content root view
     * @return root view
     */
    private View initContent(View content) {
        final EditText searchBox = (EditText) content.findViewById(R.id.search_box);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                //filter items in list based on search
                mAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        //button for clearing search view
        content.findViewById(R.id.clear_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBox.setText("");
            }
        });

        //button for changing visibility of country
        content.findViewById(R.id.change_visibility).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVisibilityDialog();
            }
        });

        //shows current visibility setting to user
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
     * Sets the visibility setting associated with user's country
     * @param setting setting to set
     */
    @Override
    public void setVisibilitySetting(VisibilitySetting setting) {
        super.setVisibilitySetting(setting);
        mVisibilityText.setText(setting.toString());
    }

    /**
     * Shows error message and button to allow user to retry
     * @return root view
     */
    private View initErrorContent() {
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
     * Set up {@link com.thecn.app.tools.controllers.LoadingViewController}
     * @param content content view to show after loading
     * @param context used to inflate views
     */
    private void initViewController(View content, Context context) {
        mLoadingViewController = new LoadingViewController(content, context);
        mLoadingViewController.showLoadingView();
    }
}
