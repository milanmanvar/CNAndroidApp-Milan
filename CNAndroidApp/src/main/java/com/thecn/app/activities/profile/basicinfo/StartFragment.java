package com.thecn.app.activities.profile.basicinfo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.profile.ProfileHeaderController;
import com.thecn.app.activities.verification.VisibilityDialog;
import com.thecn.app.fragments.common.ErrorDialogFragment;
import com.thecn.app.fragments.common.ProgressDialogFragment;
import com.thecn.app.models.profile.Country;
import com.thecn.app.models.user.User;
import com.thecn.app.models.user.UserProfile;
import com.thecn.app.models.util.VisibilitySetting;
import com.thecn.app.stores.BaseStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.anim.ActivityFragmentAnimationInterface;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.anim.FragmentAnimationInterface;
import com.thecn.app.tools.anim.FragmentAnimationUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Stating point of {@link com.thecn.app.activities.profile.basicinfo.EditBasicInfoActivity}
 * User can edit gender, email, language, country, and time zone from here.
 * Pushes other fragments to handle changes to language, country, and time zone.
 */
public class StartFragment extends Fragment implements FragmentAnimationInterface {

    private static final String TITLE = "Edit Basic Info";

    private static final String SUCCESS = "Profile information changed.";
    private static final String ERROR = "Unable to send changes.";

    //query for making changes.
    private static final String QUERY = "/me/?with_user_visible_settings=1&with_user_profile=1&with_user_country=1";

    public static final String TAG = StartFragment.class.getName();

    private CallbackManager<StartFragment> callbackManager;

    private Button genderButton, languageButton, countryButton, timeZoneButton;
    private EditText emailText;

    private ImageButton genderVisBtn, emailVisBtn, langVisBtn, countryVisBtn, tzVisBtn;

    //hold changes the user has made (but not sent to server)
    private UserProfile localProfileChanges;
    //visibility settings for each of gender, email, language, country, and time zone
    private VisibilitySetting[] visibilitySettings = new VisibilitySetting[5];

    private static final String MALE = "male";
    private static final String FEMALE = "female";

    /**
     * Set the title on activity creation.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(TITLE);
    }

    /**
     * Set up
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        callbackManager = new CallbackManager<>();
        setUpData();
    }

    /**
     * Sets up the data for this fragment.  Creates
     * a local object for storing changes and sets them based on the
     * logged in user's current settings.
     */
    private void setUpData() {
        localProfileChanges = new UserProfile();

        User user = AppSession.getInstance().getUser();
        UserProfile currentProfile = user.getUserProfile();

        localProfileChanges.setGender(currentProfile.getGender());
        localProfileChanges.setGenderFlag(getGenderFlag(currentProfile.getGender()));
        localProfileChanges.setPrimaryEmail(currentProfile.getPrimaryEmail());
        localProfileChanges.setPrimaryLanguageID(currentProfile.getPrimaryLanguageID());
        localProfileChanges.setPrimaryLanguage(currentProfile.getPrimaryLanguage());

        Country country = new Country();
        if (user.getCountry() != null) {
            Country currentCountry = user.getCountry();
            country.setId(currentCountry.getId());
            country.setName(currentCountry.getName());
        }
        localProfileChanges.setCountry(country);

        localProfileChanges.setTimeZoneID(currentProfile.getTimeZoneID());
        localProfileChanges.setTimeZone(currentProfile.getTimeZone());

        User.VisibilitySettings currentSettings = user.getVisibilitySettings();
        visibilitySettings[0] = VisibilitySetting.fromJsonString(currentSettings.getGender());
        visibilitySettings[1] = VisibilitySetting.fromJsonString(currentSettings.getPrimaryEmail());
        visibilitySettings[2] = VisibilitySetting.fromJsonString(currentSettings.getPrimaryLanguage());
        visibilitySettings[3] = VisibilitySetting.fromJsonString(currentSettings.getCountryID());
        visibilitySettings[4] = VisibilitySetting.fromJsonString(currentSettings.getTimeZoneID());
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.setting, menu);
    }

    /**
     * On "action_confirm", upload changes to server.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            getActivity().finish();
        } else if (id == R.id.action_confirm) {
            onConfirmClicked();
        }

        return true;
    }

    /**
     * Check for errors.  If none, construct json object for sending changes to server
     * and send it.
     */
    private void onConfirmClicked() {
        String gender = localProfileChanges.getGender();
        String email = emailText.getText().toString();
        String languageID = localProfileChanges.getPrimaryLanguageID();
        String countryID = localProfileChanges.getCountry().getId();
        String timeZoneID = localProfileChanges.getTimeZoneID();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ErrorDialog d = new ErrorDialog();
            d.show(getActivity().getSupportFragmentManager(), ErrorDialog.class.getName());
            return;
        }

        ProgressDialogFragment.show("Sending information...", getActivity());

        //construct object to convert to json
        Map<String, String> profileMap = new HashMap<>(5);
        profileMap.put("gender", gender);
        profileMap.put("primary_email", email);
        profileMap.put("primary_language", languageID);
        profileMap.put("country_id", countryID);
        profileMap.put("time_zone_id", timeZoneID);

        Map<String, String> vsMap = new HashMap<>(5);
        vsMap.put("gender", visibilitySettings[0].toJsonString());
        vsMap.put("primary_email", visibilitySettings[1].toJsonString());
        vsMap.put("primary_language", visibilitySettings[2].toJsonString());
        vsMap.put("country_id", visibilitySettings[3].toJsonString());
        vsMap.put("time_zone_id", visibilitySettings[4].toJsonString());
        for (VisibilitySetting vs : visibilitySettings) {
            Log.d("OBS", "vs: " + vs.toJsonString());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("profile", profileMap);
        map.put("visible_settings", vsMap);

        Gson gson = new Gson();
        JSONObject userJson;
        try {
            userJson = new JSONObject(gson.toJson(map));
        } catch (Exception e) {
            //show error
            getActivity().getSupportFragmentManager().executePendingTransactions();
            ProgressDialogFragment.dismiss(getActivity());
            AppSession.showLongToast(ERROR);
            return;
        }

        //send to server
        BaseStore.api(QUERY, Request.Method.POST, userJson, new SubmitCallback(callbackManager));
    }

    /**
     * Used to make decisions based on response from server.
     */
    private static class SubmitCallback extends CallbackManager.NetworkCallback<StartFragment> {
        public SubmitCallback(CallbackManager<StartFragment> manager) {
            super(manager);
        }

        @Override
        public void onImmediateResponse(JSONObject response) {
            if (wasSuccessful()) {
                AppSession.showShortToast(SUCCESS);
                updateLocal(UserStore.getData(response));
            } else {
                String error = StoreUtil.getFirstResponseError(response);
                if (error == null || error.isEmpty()) {
                    error = ERROR;
                }

                AppSession.showLongToast(error);
            }
        }

        /**
         * Update logged in user's information to reflect new info on server.
         * @param updatedUser object that holds changes made to user.
         */
        public void updateLocal(User updatedUser) {
            AppSession as = AppSession.getInstance();
            User localUser = as.getUser();

            synchronized (as.userLock) {
                localUser.setUserProfile(updatedUser.getUserProfile());
                localUser.setCountry(updatedUser.getCountry());
            }

            //send broadcast to update views.
            LocalBroadcastManager.getInstance(AppSession.getInstance().getApplicationContext())
                    .sendBroadcast(new Intent(ProfileHeaderController.INFO_CHANGE_SUCCESS));
        }

        @Override
        public void onImmediateError(VolleyError error) {
            StoreUtil.showExceptionMessage(error);
        }

        /**
         * If successful, finish the activity.
         */
        @Override
        public void onResumeBefore(StartFragment object) {
            if (ProgressDialogFragment.dismiss(object.getActivity()) && wasSuccessful()) {
                object.getActivity().finish();
            }
        }
    }

    /**
     * Used to show a list of errors to user.
     */
    public static class ErrorDialog extends ErrorDialogFragment {
        private static final String TITLE = "Error";
        private static final String MESSAGE = "Email is not valid.";

        @Override
        public String getMessage() {
            return MESSAGE;
        }

        @Override
        public String getTitle() {
            return TITLE;
        }
    }

    /**
     * Get local profile changes
     * @return object that holds user's local changes
     */
    public UserProfile getLocalProfileChanges() {
        return localProfileChanges;
    }

    /**
     * Get visibility settings
     * @return array of VisibilitySetting objects that specify user's choice of visibility for each category.
     */
    public VisibilitySetting[] getVisibilitySettings() {
        return visibilitySettings;
    }

    /**
     * Gets gender flag
     * @param gender string representation of gender
     * @return boolean representation of gender
     */
    private boolean getGenderFlag(String gender) {
        return gender != null && gender.equals("male");
    }

    /**
     * Get view references and set up based on state of data.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_basic_info, null, false);

        genderButton = (Button) root.findViewById(R.id.gender_button);
        emailText = (EditText) root.findViewById(R.id.email_text);
        languageButton = (Button) root.findViewById(R.id.language_button);
        countryButton = (Button) root.findViewById(R.id.country_button);
        timeZoneButton = (Button) root.findViewById(R.id.time_zone_button);

        genderVisBtn = (ImageButton) root.findViewById(R.id.gender_vis_btn);
        emailVisBtn = (ImageButton) root.findViewById(R.id.email_vis_btn);
        langVisBtn = (ImageButton) root.findViewById(R.id.language_vis_btn);
        countryVisBtn = (ImageButton) root.findViewById(R.id.country_vis_btn);
        tzVisBtn = (ImageButton) root.findViewById(R.id.time_zone_vis_btn);

        setText(genderButton, localProfileChanges.getGender());
        setText(emailText, localProfileChanges.getPrimaryEmail());
        setText(languageButton, localProfileChanges.getPrimaryLanguage());
        setText(countryButton, localProfileChanges.getCountry().getName());
        setText(timeZoneButton, localProfileChanges.getTimeZone());

        genderVisBtn.setImageResource(visibilitySettings[0].getIconResource());
        emailVisBtn.setImageResource(visibilitySettings[1].getIconResource());
        langVisBtn.setImageResource(visibilitySettings[2].getIconResource());
        countryVisBtn.setImageResource(visibilitySettings[3].getIconResource());
        tzVisBtn.setImageResource(visibilitySettings[4].getIconResource());

        genderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //no fragments needed
                changeGender();
            }
        });

        //these listeners push fragments to handle changes
        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replace(new LanguageFragment());
            }
        });
        countryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replace(new ProfileCountriesFragment());
            }
        });
        timeZoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replace(new ProfileTimeZoneFragment());
            }
        });

        //these listeners show dialogs for visibility
        genderVisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVisibilityDialog(0);
            }
        });
        emailVisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVisibilityDialog(1);
            }
        });
        langVisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVisibilityDialog(2);
            }
        });
        countryVisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVisibilityDialog(3);
            }
        });
        tzVisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVisibilityDialog(4);
            }
        });

        return root;
    }

    /**
     * Utility method for replacing a fragment
     * @param fragment fragment to replace
     */
    public void replace(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Slide in slide out animation
     */
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation animation = FragmentAnimationUtil.getAnimation(
                enter, this, (ActivityFragmentAnimationInterface) getActivity()
        );

        if (animation == null) return super.onCreateAnimation(transit, enter, nextAnim);
        return animation;
    }

    /**
     * Slide in slide out animation
     */
    @Override
    public Animation getReplacementAnimation(boolean enter) {
        int animID = enter ? R.anim.slide_in_left : R.anim.slide_out_left;
        return AnimationUtils.loadAnimation(getActivity(), animID);
    }

    /**
     * Slide in slide out animation
     */
    @Override
    public Animation getPoppingAnimation(boolean enter) {
        int animID = enter ? R.anim.slide_in_right : R.anim.slide_out_right;
        return AnimationUtils.loadAnimation(getActivity(), animID);
    }

    /**
     * Updates data and view for gender.
     */
    private void changeGender() {
        localProfileChanges.setGenderFlag(!localProfileChanges.getGenderFlag());
        localProfileChanges.setGender(localProfileChanges.getGenderFlag() ? MALE : FEMALE);
        genderButton.setText(localProfileChanges.getGender());
    }

    /**
     * Checks for error before setting text.
     * @param b view to set text of
     * @param text text to set to view
     */
    private void setText(TextView b, String text) {
        if (text != null && !text.isEmpty()) {
            b.setText(text);
        }
    }

    /**
     * Shows a visibility choice dialog
     * @param index index of visibility setting to be changed
     */
    private void showVisibilityDialog(int index) {
        ProfileVisibilityDialog d = ProfileVisibilityDialog.getInstance(index);
        d.show(getActivity().getSupportFragmentManager(), ProfileVisibilityDialog.class.getName());
    }

    /**
     * Used to show choice of visibility setting to user.  Takes index as argument to
     * specify which of gender, email, country, etc is having its visiblity changed.
     */
    public static class ProfileVisibilityDialog extends VisibilityDialog {
        private static final String INDEX_KEY = ProfileVisibilityDialog.class.getName() + "index";

        /**
         * Get fragment instance with arguments
         * @param index index of index of visibility setting to be changed
         * @return new instance of this class
         */
        public static ProfileVisibilityDialog getInstance(int index) {
            Bundle args = new Bundle();
            args.putInt(INDEX_KEY, index);

            ProfileVisibilityDialog d = new ProfileVisibilityDialog();
            d.setArguments(args);

            return d;
        }

        /**
         * Checks that proper fragments are present.  Sets visibility to newly
         * specified value.
         */
        @Override
        public DialogInterface.OnClickListener getListener() {
            return new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Fragment fragment = getContainerFragment();
                    if (fragment != null && fragment instanceof StartFragment) {
                        StartFragment sFragment = (StartFragment) fragment;
                        Object item = getAdapter().getItem(i);
                        if (item != null && item instanceof VisibilityTextIconEntry) {
                            int index = getArguments().getInt(INDEX_KEY);
                            VisibilityTextIconEntry entry = (VisibilityTextIconEntry) item;
                            //set the visibility
                            sFragment.setVisibility(index, entry.getVisibilitySetting());
                        }
                    }
                }
            };
        }
    }

    /**
     * Sets visibility setting for index.
     * Translates int index to proper button that needs to be updated and updates it.
     * @param index index of visibility setting to be changed
     * @param setting new visibility setting
     */
    public void setVisibility(int index, VisibilitySetting setting) {
        visibilitySettings[index] = setting;

        ImageButton button;
        switch (index) {
            case 0:
                button = genderVisBtn;
                break;
            case 1:
                button = emailVisBtn;
                break;
            case 2:
                button = langVisBtn;
                break;
            case 3:
                button = countryVisBtn;
                break;
            case 4:
                button = tzVisBtn;
                break;
            default:
                return;
        }

        button.setImageResource(setting.getIconResource());
    }
}
