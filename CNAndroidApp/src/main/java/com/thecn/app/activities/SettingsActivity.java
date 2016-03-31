package com.thecn.app.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.broadcastreceivers.AlertNotificationReceiver;
import com.thecn.app.models.util.Time;
import com.thecn.app.models.user.User;
import com.thecn.app.tools.network.GCMUtil;


/**
 * Activity for changing user settings.  Currently only shows notification settings.
 */
public class SettingsActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener, DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {

    private static final String TITLE = "Alert Notification Settings";
    private static final String UNCHANGED = "Settings unchanged";

    //display times for checking notifications
    private static final String[] TIME_NAMES = new String[] {
            "Check every 5 minutes",
            "Check every 10 minutes",
            "Check every 15 minutes",
            "Check every 30 minutes",
            "Check every hour",
            "Check every 2 hours",
            "Check every 5 hours",
            "Check every 10 hours",
            "Check once a day"
    };

    private static final String GP_FAIL = "Unable to install Google Play Services";

    private CheckBox showNotificationCheckbox;

    private TextView headerOne;
    private View methodLayout;
    private TextView methodDisplayText;
    private View checkForNotificationLayout;
    private TextView timeIndicator;
    private SeekBar seekBar;
    private View getGPSLayout;

    private TextView headerTwo;
    private ViewGroup notificationLayout;
    private CheckBox generalCheckbox, emailCheckbox, followerCheckbox;

    private boolean dialogCancelled; //flag that helps in showing an error when install of Google play services fails
    private boolean resumed;

    /**
     * Init and get references to/set up views.  Set up initial state
     * if savedInstanceState null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle(TITLE);
        bar.setDisplayShowTitleEnabled(true);

        showNotificationCheckbox = (CheckBox) findViewById(R.id.show_notification_checkbox);

        headerOne = (TextView) findViewById(R.id.header_1);
        methodLayout = findViewById(R.id.method_layout);
        methodDisplayText = (TextView) findViewById(R.id.method_display_text);
        checkForNotificationLayout = findViewById(R.id.check_for_notification_layout);
        timeIndicator = (TextView) findViewById(R.id.time_indicator);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        getGPSLayout = findViewById(R.id.get_gps_layout);
        Button getGPSButton = (Button) findViewById(R.id.get_gp_button);

        headerTwo = (TextView) findViewById(R.id.header_2);
        notificationLayout = (ViewGroup) findViewById(R.id.notification_layout);
        generalCheckbox = (CheckBox) findViewById(R.id.gen_notif_checkbox);
        emailCheckbox = (CheckBox) findViewById(R.id.email_checkbox);
        followerCheckbox = (CheckBox) findViewById(R.id.followers_checkbox);

        setLayoutForPlayServicesAvailability();

        if (savedInstanceState == null) {
            //set view states based on information in user's settings
            AppSession as = AppSession.getInstance();
            User.Settings settings = as.getUser().getSettings();
            if (settings == null) as.getSettingsFromDatabase();
            settings = as.getUser().getSettings();

            int refreshTime = settings.getUserSpecifiedRefreshTime();

            showNotificationCheckbox.setChecked(settings.isShowNotifications());
            setLayoutForShowingNotifications();

            seekBar.setProgress(refreshTime);
            onProgressChanged(seekBar, refreshTime, false);

            generalCheckbox.setChecked(settings.isShowGeneralNotifications());
            emailCheckbox.setChecked(settings.isShowEmailNotifications());
            followerCheckbox.setChecked(settings.isShowFollowerNotifications());
        }

        seekBar.setOnSeekBarChangeListener(this);
        getGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GCMUtil.showPlayServicesDialog(SettingsActivity.this);
            }
        });

        showNotificationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setLayoutForShowingNotifications();
            }
        });
    }

    /**
     * Restore progress bar state, update notification layout state.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onProgressChanged(seekBar, seekBar.getProgress(), false);
        setLayoutForShowingNotifications();
    }

    /**
     * If show notifications, show all relevant sub-options.  Else, don't show.
     */
    private void setLayoutForShowingNotifications() {
        int visibility = showNotificationCheckbox.isChecked() ? View.VISIBLE : View.GONE;

        headerOne.setVisibility(visibility);
        methodLayout.setVisibility(visibility);
        headerTwo.setVisibility(visibility);
        notificationLayout.setVisibility(visibility);
    }

    /**
     * If google play services is available, we don't need to show the seekbar
     * for controlling when to poll for notifications.  Else, if the user can install play services,
     * show a layout that lets them do this.
     */
    private void setLayoutForPlayServicesAvailability() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            methodDisplayText.setText(getResources().getString(R.string.gp_services_cn_app));
            checkForNotificationLayout.setVisibility(View.GONE);
            getGPSLayout.setVisibility(View.GONE);
        } else {
            methodDisplayText.setText(getResources().getString(R.string.periodic_check));
            checkForNotificationLayout.setVisibility(View.VISIBLE);

            int getGPSLayoutVisibility = GooglePlayServicesUtil.isUserRecoverableError(resultCode) ? View.VISIBLE : View.GONE;
            getGPSLayout.setVisibility(getGPSLayoutVisibility);
        }
    }

    /**
     * Callback that sets a flag telling that a dialog was cancelled
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        dialogCancelled = true;
    }

    /**
     * Checks if the dialog was cancelled.  If it wasn't and we are resumed,
     * show an error message that tells user they were unable to install Google Play Services
     * Question: does GPS allow for this functionality itself?  Can't find it...
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        if (!dialogCancelled && resumed) {
            AppSession.showLongToast(GP_FAIL);
        }

        dialogCancelled = false;
    }

    /**
     * Check if google play services available every time on resume.
     */
    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        setLayoutForPlayServicesAvailability();
    }

    /**
     * Send intent to notification service in case something was changed.
     */
    @Override
    protected void onPause() {
        super.onPause();
        resumed = false;

        sendBroadcast(new Intent(this, AlertNotificationReceiver.class));
    }

    /**
     * Set time indicator text to match seekbar progress.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        timeIndicator.setText(TIME_NAMES[progress]);
        timeIndicator.setTextColor(getTimeIndicatorColor(progress));
    }

    /**
     * Sets time indicator text to be progressively more red as the polling time
     * gets shorter (tells user that this is bad for the battery)
     * @param progress progress of the seekbar, 0 to 9
     * @return color with which to set text.
     */
    private int getTimeIndicatorColor(int progress) {
        int redLevel = 255 * (9 - progress) / 9;
        return Color.rgb(redLevel, 0, 0);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    /**
     * Change the settings of the user and write them to database.
     */
    private void changeSettingsAndFinish() {
        AppSession as = AppSession.getInstance();

        User.Settings settings = as.getUser().getSettings();
        settings.setShowNotifications(showNotificationCheckbox.isChecked());

        settings.setUserSpecifiedRefreshTime(seekBar.getProgress());

        settings.setShowGeneralNotifications(generalCheckbox.isChecked());
        settings.setShowEmailNotifications(emailCheckbox.isChecked());
        settings.setShowFollowerNotifications(followerCheckbox.isChecked());

        long interval;

        switch (seekBar.getProgress()) {
            case User.Settings.FIVE_MINUTES:
                interval = Time.FIVE_MINUTES;
                break;
            case User.Settings.TEN_MINUTES:
                interval = Time.TEN_MINUTES;
                break;
            case User.Settings.FIFTEEN_MINUTES:
                interval = Time.FIFTEEN_MINUTES;
                break;
            case User.Settings.THIRTY_MINUTES:
                interval = Time.THIRTY_MINUTES;
                break;
            case User.Settings.TWO_HOURS:
                interval = Time.TWO_HOURS;
                break;
            case User.Settings.FIVE_HOURS:
                interval = Time.FIVE_HOURS;
                break;
            case User.Settings.TEN_HOURS:
                interval = Time.TEN_HOURS;
                break;
            case User.Settings.DAY:
                interval = Time.DAY;
                break;
            default:
                interval = Time.HOUR;
        }

        settings.setRefreshNotificationInterval(interval);

        as.writeSettingsToDatabase();

        finish();
    }

    /**
     * Adds a checkmark menu item.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_settings, menu);
        return true;
    }

    /**
     * Changes settings if checkmark clicked.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            AppSession.showShortToast(UNCHANGED);
            finish();
            return true;
        } else if (id == R.id.action_confirm) {
            changeSettingsAndFinish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Finishes and shows message that nothing was changed
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AppSession.showShortToast(UNCHANGED);
    }
}
