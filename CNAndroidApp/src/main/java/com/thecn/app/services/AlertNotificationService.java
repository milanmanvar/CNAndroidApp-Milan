package com.thecn.app.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.homefeed.HomeFeedActivity;
import com.thecn.app.broadcastreceivers.AlertNotificationReceiver;
import com.thecn.app.models.util.Time;
import com.thecn.app.models.user.User;
import com.thecn.app.models.notification.UserNewMessage;
import com.thecn.app.stores.NewMessageStore;
import com.thecn.app.stores.ResponseCallback;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Service used to send alert notifications to user.
 * Only sends an alert notification when a new notification from
 * the server has been received.  When google play services are
 * installed, this service runs every five hours to make sure
 * everything is working correctly.  Otherwise, this service runs
 * whenever the app polls for notifications.
 * The service also runs when changes are made to notification settings
 * and when {@link com.thecn.app.activities.navigation.NavigationActivity} resumes.
 * However, the service does not make network requests every time it runs.  Only when
 * necessary.
 */
public class AlertNotificationService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    public static final String TAG = "AlertNotificationService";

    private static final String TITLE = "CourseNetworking";

    public static final String IS_BACKOFF = "com.thecn.app.services.EXPONENTIAL_BACKOFF";
    public static final String LAST_BACKOFF_TIME = "com.thecn.app.services.LAST_BACKOFF_TIME";
    public static final String NEXT_REFRESH = "com.thecn.app.services.NEXT_REFRESH";

    public AlertNotificationService() {
        super(TAG);
    }

    /**
     * Checks that proper settings are in place.  If not, seeks to remedy them.
     * If Google Play Services are installed, network requests should usually be
     * handled by {@link com.thecn.app.services.GcmIntentService}, but this service
     * is set to check periodically that everything is working correctly.
     * If google play services are not installed, this service polls for notifications
     * periodically.
     */
    @Override
    protected void onHandleIntent(final Intent intent) {

        if (!notificationPermissionsOK()) {
            AlertNotificationReceiver.completeWakefulIntent(intent);
            return;
        }

        AppSession as = AppSession.getInstance();
        User.Settings settings = as.getUser().getSettings();

        //tells if this intent was intended for error recovery through exponential backoff
        final boolean isBackoff = intent.getBooleanExtra(IS_BACKOFF, false);

        final long currentTime = System.currentTimeMillis();
        final long lastRefreshTime = settings.getLastNotificationRefreshTime();

        final long refreshInterval;
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {

            //check GCM registration
            String regID = as.getGCMRegistrationId();
            if (regID == null) {
                //if not registered, attempt registration, keep checking each hour
                refreshInterval = Time.HOUR;
                as.registerForGCMInBackground();
            } else if (!as.isGCMRegistrationIdSentToServer()) {
                //if server not informed, attempt request, keep checking each hour
                refreshInterval = Time.HOUR;
                as.sendGCMIDToServer(regID);
            } else {
                //GCM is in use, check each five hours for sake of redundancy
                refreshInterval = Time.FIVE_HOURS;
            }

        } else {
            //if no google play services, use notification polling interval set by user
            refreshInterval = settings.getRefreshNotificationInterval();
        }

        //if not a backoff recovery and it's not time to refresh, set new alarm
        //for next check time
        if (!isBackoff && currentTime - lastRefreshTime < refreshInterval) {
            setAlarm(lastRefreshTime + refreshInterval, this);
            AlertNotificationReceiver.completeWakefulIntent(intent);
            return;
        }

        NewMessageStore.getNewMessages(new ResponseCallback() {
            @Override
            public void onResponse(JSONObject response) {
                UserNewMessage message = NewMessageStore.getData(response);

                if (message != null) {
                    processNotification(message);
                }

                AppSession.getInstance().markLastNotificationRefreshTime();

                AlertNotificationReceiver.completeWakefulIntent(intent);
            }

            @Override
            public void onError(VolleyError error) {
                //use exponential backoff upon failure

                long nextRefreshTime, backoffTime;

                if (isBackoff) {
                    //increase backoff time for next try
                    long lastBackoffTime = intent.getLongExtra(LAST_BACKOFF_TIME, refreshInterval);
                    nextRefreshTime = intent.getLongExtra(NEXT_REFRESH, 0);
                    backoffTime = 2 * lastBackoffTime;
                } else {
                    //initial backoff time at one minute
                    nextRefreshTime = currentTime + 2 * refreshInterval;
                    backoffTime = Time.ONE_MINUTE;
                }

                if (currentTime + backoffTime < nextRefreshTime) {
                    //if next retry time is sooner than next refresh time, retry backoff
                    setBackoffAlarm(backoffTime, nextRefreshTime, AlertNotificationService.this);
                } else {
                    //else, stop backing off and set next refresh interval
                    setAlarm(nextRefreshTime, AlertNotificationService.this);
                }

                AlertNotificationReceiver.completeWakefulIntent(intent);
            }
        });
    }

    /**
     * Check that a user is logged in and that the user has notifications
     * turned on.
     * @return true if notifications are turned on for a logged in user.
     */
    public static boolean notificationPermissionsOK() {
        AppSession as = AppSession.getInstance();
        if (!as.isLoggedIn()) {
            return false;
        }

        User user = as.getUser();
        if (user.getSettings() == null) {
            as.getSettingsFromDatabase();
        }
        final User.Settings settings = user.getSettings();

        return settings.isShowNotifications() && (settings.isShowGeneralNotifications() ||
                        settings.isShowEmailNotifications() ||
                        settings.isShowFollowerNotifications());
    }

    /**
     * Sets an alarm.  Defaults to an intent sent to {@link com.thecn.app.broadcastreceivers.AlertNotificationReceiver}
     * @param time specified time for alarm to fire.
     * @param context used to get alarm manager and pending intent.
     */
    public static void setAlarm(long time, Context context) {
        setAlarm(new Intent(context, AlertNotificationReceiver.class), time, context);
    }

    /**
     * Sets a backoff alarm to fire after backoffTime has passed.
     * This is an implementation of exponential backoff.
     * @param backoffTime time to wait from now to fire alarm
     * @param nextRefreshTime the time when the service is set to poll again.
     * @param context used to create intent and set alarm.
     */
    public static void setBackoffAlarm(long backoffTime, long nextRefreshTime, Context context) {
        Intent intent = new Intent(context, AlertNotificationReceiver.class);
        intent.putExtra(IS_BACKOFF, true);
        intent.putExtra(LAST_BACKOFF_TIME, backoffTime);
        intent.putExtra(NEXT_REFRESH, nextRefreshTime);
        setAlarm(intent, System.currentTimeMillis() + backoffTime, context);
    }

    /**
     * Sets an alarm.
     * @param intent intent to use in pending intent.
     * @param time time when alarm should be fired.
     * @param context used to get alarm manager and pending intent.
     */
    private static void setAlarm(Intent intent, long time, Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //update this intent so that the old intent won't be used
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.set(AlarmManager.RTC_WAKEUP,
                time, alarmIntent);
    }

    /**
     * Process notification with this service as context.
     * @param message Notification count received from server.
     */
    private void processNotification(UserNewMessage message) {
        processNotification(this, message);
    }

    /**
     * Process notification.
     * If message contains new notification, express as alert notification
     * and show to user.
     * @param context used to get notification manager, construct intents, etc.
     * @param message Notification count received from server.
     */
    public static void processNotification(Context context, UserNewMessage message) {
        AppSession session = AppSession.getInstance();

        //if navigation activity is resumed, don't send alert notifications
        //if message empty, don't send
        //if notifications are turned off, don't send
        //if there are no new notifications, don't send
        if (session.isNavigationActivityResumed() || message.getTotal() <= 0 || !notificationPermissionsOK() || !session.hasNewNotification(message)) {
            session.setUserNewMessage(message);
            return;
        }

        //set this object as the most up to date model of notification count
        session.setUserNewMessage(message);

        int count;
        String segment;
        ArrayList<String> segments = new ArrayList<>();
        segments.ensureCapacity(3);

        User user = session.getUser();
        User.Settings settings = user.getSettings();
        if (settings == null) session.getSettingsFromDatabase();
        settings = user.getSettings();

        //check if general notifications turned on
        if (settings.isShowGeneralNotifications()) {
            count = message.getGenNotificationCount();

            //if has new, add segment of message
            if (count > 0) {
                segment = count + " notification";
                if (count > 1) {
                    segment += "s";
                }
                segments.add(segment);
            }
        }

        //check if email notifications turned on
        if (settings.isShowEmailNotifications()) {
            count = message.getEmailCount();

            if (count > 0) {
                segment = count + " new email";
                if (count > 1) {
                    segment += "s";
                }
                segments.add(segment);
            }
        }

        //check if follower notifications turned on
        if (settings.isShowFollowerNotifications()) {
            count = message.getFollowerCount();

            if (count > 0) {
                segment = count + " new follower";
                if (count > 1) {
                    segment += "s";
                }
                segments.add(segment);
            }
        }

        int size = segments.size();
        if (size == 0) return; //shouldn't happen, but check anyway...

        //create message from message segments
        String msg = "You have " + segments.get(0);
        if (size == 2) {
            msg += " and " + segments.get(1);
        } else if (size == 3) {
            msg += ", " + segments.get(1);
            msg += ", and " + segments.get(2);
        }

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        //alert notifications open home feed activity
        Intent homeFeedIntent = new Intent(context, HomeFeedActivity.class);
        homeFeedIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //replace this pending intent if it is already there (update the notification if it is already there)
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, homeFeedIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        //use default notification sound
        Uri soundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //build notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_cn_outline)
                        .setContentTitle(TITLE)
                        .setContentText(msg)
                        .setSound(soundURI)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg));

        builder.setContentIntent(contentIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
