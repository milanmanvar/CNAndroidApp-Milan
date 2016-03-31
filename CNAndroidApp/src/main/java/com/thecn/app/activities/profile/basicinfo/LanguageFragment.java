package com.thecn.app.activities.profile.basicinfo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.verification.ObjectAdapter;
import com.thecn.app.activities.verification.VisibilityFragment;
import com.thecn.app.models.profile.Language;
import com.thecn.app.models.user.UserProfile;
import com.thecn.app.models.util.VisibilitySetting;
import com.thecn.app.stores.SearchStore;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.LoadingViewController;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Used to allow user to pick a language from all those offerred on CN
 */
public class LanguageFragment extends VisibilityFragment {

    private static final String TITLE = "Choose Your Language";
    private static final String LOAD_ERROR = "Could not load data.";
    private static final String RETRY = "Retry";

    private ObjectAdapter<Language> mAdapter;
    private LoadingViewController mLoadingViewController;

    private TextView mVisibilityText;

    private CallbackManager<LanguageFragment> callbackManager;

    /**
     * Set up and begin loading
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

    /**
     * Gets the action to take when a language is clicked.
     * @return a click listener that specifies the action to take when an item is clicked.
     */
    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Language language = (Language) mAdapter.getItem(position);

                FragmentActivity activity = getActionBarActivity();
                StartFragment f = getStartFragment();

                if (f == null) return;

                //set the language into local changes and pop fragment
                UserProfile localChanges = f.getLocalProfileChanges();
                localChanges.setPrimaryLanguageID(language.getId());
                localChanges.setPrimaryLanguage(language.getName());

                activity.onBackPressed();
            }
        };
    }

    /**
     * Back button doesn't make any changes.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActionBarActivity().onBackPressed();
        }

        return true;
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    /**
     * Set the title of the fragment.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActionBarActivity().getSupportActionBar().setTitle(TITLE);
    }

    /**
     * Set up data and start loading languages.
     */
    private void startLoading() {
        String token = getToken();
        if (token == null) {
            return;
        }

        loading = true;
        SearchStore.getLanguageList(new Callback(callbackManager));
    }

    /**
     * Gets the token to be used in calls to server.
     * @return token
     */
    public String getToken() {
        return AppSession.getInstance().getToken();
    }

    /**
     * Used to make decisions when a network call for languages returns.
     */
    private static class Callback extends CallbackManager.NetworkCallback<LanguageFragment> {
        public Callback(CallbackManager<LanguageFragment> manager) {
            super(manager);
        }

        @Override
        public void onResumeWithResponse(LanguageFragment object) {
            if (wasSuccessful()) object.onLoadSuccess(response);
            else object.onLoadFailure();
        }

        @Override
        public void onResumeWithError(LanguageFragment object) {
            object.onLoadFailure();
        }
    }

    /**
     * Called when a network call returns successfully
     * @param response data from the server.
     */
    private void onLoadSuccess(JSONObject response) {
        loading = false;

        ArrayList<Language> languages = SearchStore.getLanguageData(response);
        if (languages == null) {
            onLoadFailure();
            return;
        }

        //set up the view to display languages.
        mAdapter = new ObjectAdapter<>(languages, getActivity().getApplicationContext());
        mAdapter.setTextColor(getResources().getColor(R.color.black));
        initContent(mLoadingViewController.getContentView());
        mLoadingViewController.crossFade();
    }

    /**
     * Show error when unable to load data.
     */
    private void onLoadFailure() {
        loading = false;
        initErrorContent();
    }

    /**
     * Uses {@link com.thecn.app.tools.controllers.LoadingViewController} to show
     * loading or error states to the user.  If data loaded, simply return the intended
     * view content.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.search_choose_list, container, false);

        if (loading) {
            setViewController(content, getActivity());
            mLoadingViewController.showLoading(true);
            return mLoadingViewController.getRootView();
        } else if (mAdapter == null) {
            setViewController(content, getActivity());
            return initErrorContent();
        }

        return initContent(content);
    }

    /**
     * Set up the view content.
     * @param content root view where other views can be found.
     * @return the root view
     */
    private View initContent(View content) {
        final EditText searchBox = (EditText) content.findViewById(R.id.search_box);
        //add a text watcher to search the list when the text changes.
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        //when user presses "X", clear the search field
        content.findViewById(R.id.clear_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBox.setText("");
            }
        });

        //when user presses visibility button, show a dialog of choices
        content.findViewById(R.id.change_visibility).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVisibilityDialog();
            }
        });

        //set up views for initial data
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
     * Get start fragment
     * @return the start fragment, if added to the fragment manager.
     */
    private StartFragment getStartFragment() {
        return (StartFragment) getActionBarActivity().getSupportFragmentManager()
                .findFragmentByTag(StartFragment.TAG);
    }

    /**
     * Gets visibility setting
     * @return visibility setting associated with the user's language.
     */
    @Override
    public VisibilitySetting getVisibilitySetting() {
        StartFragment f = getStartFragment();
        if (f == null) return null;

        return f.getVisibilitySettings()[2];
    }

    /**
     * Sets visibility setting.
     * @param setting visibility setting associated with the user's language choice.
     */
    @Override
    public void setVisibilitySetting(VisibilitySetting setting) {
        StartFragment f = getStartFragment();
        if (f == null) return;

        f.getVisibilitySettings()[2] = setting;
        mVisibilityText.setText(setting.toString());
    }

    /**
     * Set up the {@link com.thecn.app.tools.controllers.LoadingViewController}
     * to show an error view with message and button
     * @return the root view
     */
    private View initErrorContent() {
        mLoadingViewController.showLoading(false);
        mLoadingViewController.showMessage(LOAD_ERROR);
        mLoadingViewController.showButton(RETRY, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //try to get data again
                mLoadingViewController.showLoading(true);
                mLoadingViewController.showMessage(false);
                mLoadingViewController.showButton(false);
                startLoading();
            }
        });

        return mLoadingViewController.getRootView();
    }

    /**
     * Sets up view controller
     * @param content view for this fragment when data properly loaded
     * @param context context used to inflate views.
     */
    private void setViewController(View content, Context context) {
        mLoadingViewController = new LoadingViewController(content, context);
        mLoadingViewController.showLoadingView();
    }
}
