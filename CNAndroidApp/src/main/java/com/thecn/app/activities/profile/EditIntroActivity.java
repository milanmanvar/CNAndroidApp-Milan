package com.thecn.app.activities.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.fragments.common.ErrorDialogFragment;
import com.thecn.app.fragments.common.ProgressDialogFragment;
import com.thecn.app.models.user.UserProfile;
import com.thecn.app.stores.BaseStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.network.GlobalGson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to edit a user's introduction and tag line.
 */
public class EditIntroActivity extends ActionBarActivity {

    private static final String TITLE = "Edit Intro and Tagline";

    public static final String INTRO_TAG = EditIntroActivity.class.getName() + "intro";
    public static final String TAGLINE_TAG = EditIntroActivity.class.getName() + "tagline";

    private static final String INTRO_ERROR = "Introduction must be at least 10 characters long";
    private static final String UPDATE_ERROR = "Could not update information";

    private static final String TAGLINE_ERROR = "Tagline must be at least 3 characters long";
    private EditText introText;

    private EditText taglineText;

    //used to send changes to server
    private UpdateFragment updateFragment;

    /**
     * Set up action bar and views, add UpdateFragment
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(TITLE);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.fragment_edit_intro);

        introText = (EditText) findViewById(R.id.intro_text);
        taglineText = (EditText) findViewById(R.id.tagline_text);

        if (savedInstanceState == null) {
            String intro = getIntent().getStringExtra(INTRO_TAG);
            if (intro != null && !intro.isEmpty()) {
                introText.setText(intro);
            }
            String tagline = getIntent().getStringExtra(TAGLINE_TAG);
            if (tagline != null && !tagline.isEmpty()) {
                taglineText.setText(tagline);
            }

            updateFragment = new UpdateFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(updateFragment, UpdateFragment.class.getName())
                    .commit();
        } else {
            updateFragment = (UpdateFragment) getSupportFragmentManager()
                    .findFragmentByTag(UpdateFragment.class.getName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    /**
     * No changes made if back is tapped.  Only if "action_confirm" tapped.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_confirm) {
            onConfirmClicked();
        }

        return true;
    }

    /**
     * Checks for errors.  If no errors, tells update fragment to send changes
     * to the server.
     */
    private void onConfirmClicked() {
        String intro = introText.getText().toString();
        if (intro == null) intro = "";
        String tagline = taglineText.getText().toString();
        if (tagline == null) tagline = "";
        int length;
        String error = null;

        length = intro.length();
        if (length != 0 && length < 10) {
            error = INTRO_ERROR;
        }

        length = tagline.length();
        if (length != 0 && length < 3) {
            if (error != null) {
                error += "\n\n" + TAGLINE_ERROR;
            } else {
                error = TAGLINE_ERROR;
            }
        }

        if (error != null) {
            //show a dialog for the errors
            ErrorDialog d = ErrorDialog.getInstance(error);
            d.show(getSupportFragmentManager(), ErrorDialog.class.getName());
            return;
        }

        //all clear, send data to server.
        updateFragment.sendUpdate(intro, tagline);
    }

    /**
     * Used to send local changes to the server.
     */
    public static class UpdateFragment extends Fragment {
        CallbackManager<UpdateFragment> manager;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            manager = new CallbackManager<>();
        }

        @Override
        public void onResume() {
            super.onResume();
            manager.resume(this);
        }

        @Override
        public void onPause() {
            manager.pause();
            super.onPause();
        }

        /**
         * Sends introduction and tagline updated data to the server.
         * @param intro updated introduction
         * @param tagline updated tagline.
         */
        private void sendUpdate(String intro, String tagline) {
            String query = "/me/";

            //object used to construct json
            Map<String, String> profile = new HashMap<>();
            profile.put("about", intro);
            profile.put("tagline", tagline);

            Map<String, Object> user = new HashMap<>();
            user.put("profile", profile);

            JSONObject payload;
            try {
                Gson gson = GlobalGson.getGson();
                payload = new JSONObject(gson.toJson(user));

            } catch (Exception e) {
                AppSession.showLongToast(UPDATE_ERROR);
                return;
            }

            ProgressDialogFragment.show(getActivity());

            //all clear, send to server.
            BaseStore.api(query, Request.Method.POST, payload, new UpdateResponseCallback(manager, intro, tagline));
        }

        /**
         * Used to make decisions when response returns from server.
         */
        private static class UpdateResponseCallback extends CallbackManager.NetworkCallback<UpdateFragment> {
            private static final String SUCCESS = "Profile data updated";

            private String intro;
            private String tagline;

            public UpdateResponseCallback(CallbackManager<UpdateFragment> manager, String intro, String tagline) {
                super(manager);
                this.intro = intro;
                this.tagline = tagline;
            }

            /**
             * Always tell user whether changes were made
             */
            @Override
            public void onImmediateResponse(JSONObject response) {
                if (wasSuccessful()) {
                    AppSession.showShortToast(SUCCESS);
                    //update local data.
                    UserProfile profile = AppSession.getInstance().getUser().getUserProfile();
                    synchronized (AppSession.getInstance().userLock) {
                        profile.setAbout(intro);
                        profile.setTagLine(tagline);
                    }

                    Context context = AppSession.getInstance().getApplicationContext();
                    //send broadcast for updating views.
                    LocalBroadcastManager.getInstance(context)
                            .sendBroadcast(new Intent(ProfileHeaderController.INTRO_CHANGE_SUCCESS));
                } else {
                    AppSession.showLongToast(UPDATE_ERROR);
                }
            }

            @Override
            public void onImmediateError(VolleyError error) {
                StoreUtil.showExceptionMessage(error);
            }

            /**
             * Finish on success.
             */
            @Override
            public void onResumeWithResponse(UpdateFragment object) {
                if (ProgressDialogFragment.dismiss(object.getActivity()) && wasSuccessful()) {
                    object.getEditIntroActivity().finish();
                }
            }

            /**
             * Dismiss progress dialog on error.
             */
            @Override
            public void onResumeWithError(UpdateFragment object) {
                ProgressDialogFragment.dismiss(object.getActivity());
            }
        }

        /**
         * Casts activity
         * @return activity cast to EditIntroActivity
         */
        private EditIntroActivity getEditIntroActivity() {
            return (EditIntroActivity) getActivity();
        }
    }

    /**
     * Dialog for showing errors to user.
     */
    public static class ErrorDialog extends ErrorDialogFragment {
        private static final String TITLE = "Error";
        private static final String ARG_TAG = ErrorDialog.class.getName() + ".error";

        /**
         * Gets instance with arguments.
         * @param error error to display
         * @return new instance of this class
         */
        public static ErrorDialog getInstance(String error) {
            Bundle args = new Bundle();
            args.putString(ARG_TAG, error);
            ErrorDialog d = new ErrorDialog();
            d.setArguments(args);
            return d;
        }

        @Override
        public String getTitle() {
            return TITLE;
        }

        @Override
        public String getMessage() {
            return getArguments().getString(ARG_TAG);
        }
    }

    /**
     * Add custom sliding animation on finish
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
