package com.thecn.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.notification.UserNewMessage;
import com.thecn.app.models.profile.Avatar;
import com.thecn.app.models.user.Score;
import com.thecn.app.models.user.User;
import com.thecn.app.models.user.UserProfile;
import com.thecn.app.models.util.VerificationBundle;
import com.thecn.app.services.AlertNotificationService;
import com.thecn.app.stores.AuthStore;
import com.thecn.app.stores.BaseStore;
import com.thecn.app.stores.NewMessageStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.DatabaseInterface;
import com.thecn.app.tools.network.GlobalGson;
import com.thecn.app.tools.text.InternalURLSpan;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class represents a session of the app.  It is a singleton structure
 * that contains global data needed throughout the different parts of the app.
 * Notably, it contains information about the currently logged in user.
 * It also holds the logged in user's token.
 * It also has methods for working with verification and Google Cloud Messaging (GCM).
 * Finally, it is the source of the global notification count object.
 */
public class AppSession {

    //singleton instance
    private static AppSession sharedSession = new AppSession();

    public static AppSession getInstance() {
        return sharedSession;
    }

    private String mToken; //auth token ref
    private final Object tokenLock = new Object();

    private User mUser; //logged in user
    public final Object userLock = new Object();

    private UserNewMessage mUserNewMessage; //current count of new notifications.
    private final Object mUserMessageLock = new Object();

    private boolean userUpdateNeeded, courseUpdateNeeded, conexusUpdateNeeded;

    private SharedPreferences sharedPrefs; //todo instead use database?
    private CNApp mApp;

    private VerificationBundle vBundle; //used when a user is verifying.

    private static final String PREF_USER = "mUser";
    private static final String PREF_TOKEN = "mToken";
    private static final String PREF_USER_MESSAGE = "mUserMessage";
    private static final String PREF_GCM_REG = "gcmReg";
    private static final String PREF_GCM_SENT_API = "gcmSentToApi";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private static String SENDER_ID = "243524631843";

    private boolean navigationActivityResumed;

    private AppSession() {}

    public VerificationBundle getVerificationBundle() {
        return vBundle;
    }

    public void setVerificationBundle(VerificationBundle vBundle) {
        this.vBundle = vBundle;
    }

    /**
     * Try to get the login token.  If not present, try to get the verification token
     * @return a valid token or null.
     */
    public String getLoginOrVerificationToken() {
        String token = getToken();
        if (token != null) return token;

        if (vBundle == null) return null;

        return vBundle.token;
    }

    public boolean isNavigationActivityResumed() {
        return navigationActivityResumed;
    }

    /**
     * Navigation activity calls to globally notify that it is resumed or paused.
     * @param navigationActivityResumed whether resumed or paused
     */
    public void setNavigationActivityResumed(boolean navigationActivityResumed) {
        this.navigationActivityResumed = navigationActivityResumed;
    }

    /**
     * Get user's verification status
     * @return verification status
     */
    public static int getMyStatus() {
        return sharedSession.getUser().getStatus();
    }

    /**
     * Returns true if the user needs verification
     * @return true if need verification
     */
    public static boolean needsVerification() {
        return getMyStatus() == 99;
    }

    /**
     * Checks if the logged in user needs verification.  If so, show a dialog
     * that tells them to verify.  If not, return false.
     * @param activity used to create dialog fragment
     * @return true if user needs verification.
     */
    public static boolean checkVerification(FragmentActivity activity) {
        if (needsVerification()) {
            NeedVerificationFragment fragment = new NeedVerificationFragment();
            fragment.show(activity.getSupportFragmentManager(), NeedVerificationFragment.TAG);
            return true;
        }

        return false;
    }

    /**
     * Dialog for telling a user that they need to verify before performing a certain action.
     */
    public static class NeedVerificationFragment extends DialogFragment {

        public static final String TAG = "verification_fragment";

        private static final String TITLE = "Verification Needed";
        private static final String MESSAGE = "You must activate your account before you can continue.  " +
                "Please check your email and click on the activation link.\n\n" +
                "If you have questions, send an email to ";
        private static final String EMAIL = "help@thecn.com";

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(TITLE)
                    .setView(getTextView());

            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            return dialog;
        }

        /**
         * Get a text view to contain the verification message
         * @return new TextView
         */
        private TextView getTextView() {
            TextView textView = new TextView(getActivity());
            int padding = (int) getResources().getDimension(R.dimen.dialog_padding);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setPadding(padding, padding, padding, padding);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(getText());
            return textView;
        }

        /**
         * Get message to show user.  Create a link to send email to help@thecn.com
         * @return message to show user.
         */
        private CharSequence getText() {
            SpannableString email = new SpannableString(EMAIL);
            //on click, open email activity
            InternalURLSpan span = new InternalURLSpan(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openEmailApp();
                }
            });
            email.setSpan(span, 0, email.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return TextUtils.concat(MESSAGE, email, ".");
        }

        /**
         * Open an external email app for sending an email if it exists.
         */
        private void openEmailApp() {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", EMAIL, null));
            startActivity(emailIntent);
            dismiss();
        }
    }

    /**
     * Called when application first started to start up the session.
     * @param app new application instance
     */
    public void Initialize(CNApp app) {
        mApp = app;
        mAppToast = Toast.makeText(AppSession.getInstance().getApplicationContext(), "", Toast.LENGTH_LONG);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mApp);

        //if not logged in, return
        if (!isLoggedIn()) return;

        //initialize user new message if it is valid
        String unmString = sharedPrefs.getString(PREF_USER_MESSAGE, null);

        if (unmString != null && !unmString.isEmpty()) {
            //don't user global gson here
            Gson gson = new Gson();
            UserNewMessage message = gson.fromJson(unmString, UserNewMessage.class);
            if (message.isValid()) {
                setUserNewMessage(message);
            }
        }
    }

    /**
     * Check if logged in
     * @return true if logged in, else false
     */
    public boolean isLoggedIn() {
        return getUser() != null && getToken() != null;
    }

    /**
     * Make a network call to get the current new notification count from the server.
     */
    public void getUserNewMessageFromServer() {
        NewMessageStore.getNewMessages(new ResponseCallback() {
            @Override
            public void onResponse(JSONObject response) {
                UserNewMessage message = NewMessageStore.getData(response);

                if (message != null) {
                    setUserNewMessage(message);
                }

                markLastNotificationRefreshTime();
            }

            @Override
            public void onError(VolleyError error) {
                //do nothing
            }
        });
    }

    /**
     * Get {@link com.thecn.app.models.user.User.Settings} object from
     * sql database (see {@link com.thecn.app.tools.DatabaseInterface})
     * Should be run in separate thread.
     */
    public void getSettingsFromDatabase() {
        User user = getUser();
        DatabaseInterface dbi = new DatabaseInterface(getApplicationContext());
        User.Settings settings;

        try {
            dbi.open();
            settings = dbi.getSettings(user.getId());
            dbi.close();
        } catch (Exception e) {
            settings = new User.Settings(user.getId(), true, true);
        }

        synchronized (userLock) {
            user.setSettings(settings);
        }
    }

    /**
     * Mark the time that we last checked for the count of new notifications.
     * Also write this to the database and set an alarm for the next check time.
     */
    public void markLastNotificationRefreshTime() {
        if (mUser == null) return;
        User.Settings settings = mUser.getSettings();
        if (settings == null) getSettingsFromDatabase();
        settings = mUser.getSettings();

        synchronized (userLock) {
            settings.setLastNotificationRefreshTime(System.currentTimeMillis());
        }

        writeSettingsToDatabase();

        long alarmTime = settings.getLastNotificationRefreshTime() + settings.getRefreshNotificationInterval();
        AlertNotificationService.setAlarm(alarmTime, mApp);
    }

    /**
     * Creates a new thread that writes the current {@link com.thecn.app.models.user.User.Settings}
     * object to sql database ({@link com.thecn.app.tools.DatabaseInterface})
     */
    public void writeSettingsToDatabase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                User.Settings settings = mUser.getSettings();
                DatabaseInterface dbi = new DatabaseInterface(mApp);
                try {
                    dbi.open();
                    dbi.insertSettings(settings);
                    dbi.close();
                } catch (Exception e) {
                    //do nothing
                }
            }
        }).start();
    }

    /**
     * Get application context
     * @return application context
     */
    public CNApp getApplicationContext() {
        return mApp;
    }

    /**
     * Clear the user, token, and new message object
     */
    public void clearSession() {
        synchronized (tokenLock) {
            setToken(null);
        }
        synchronized (userLock) {
            setUser(null);
        }
        synchronized (mUserMessageLock) {
            setUserNewMessage(null);
        }
    }

    /**
     * Try to register this device with Google Cloud Messaging.
     * Uses a GCM registration id.  This id is sent to the CourseNetworking server.
     * When a user gets a new notification, the server notifies the Google Cloud Messaging
     * server using this id.  The GCM server will then use the id to determine that it
     * should send a message to this device, which will be a send to sync message that
     * tells the app it should check for new notifications.
     */
    public void registerForGCMInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mApp);
                    String regID = gcm.register(SENDER_ID);

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    writeGCMRegistrationIdSentToServer(false);
                    sendGCMIDToServer(regID);

                    // Persist the regID - no need to register again.
                    writeGCMRegistrationId(regID);
                } catch (IOException ex) {
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
            }
        }).start();
    }

    /**
     * Sends the GCM registration id to the server.
     * On success, sets a flag that indicates the operation was successful.
     * @param id gcm id to send.
     */
    public void sendGCMIDToServer(String id) {
        AuthStore.addGCMID(id, new ResponseCallback() {
            @Override
            public void onResponse(JSONObject response) {
                if (StoreUtil.success(response)) {
                    writeGCMRegistrationIdSentToServer(true);
                }
            }

            @Override
            public void onError(VolleyError error) {

            }
        });
    }

    /**
     * Sends a request to remove the GCM registration id from the user
     * data in on the server database (so that GCM messages won't come to this
     * device any more).
     */
    public void removeGCMIDFromMe() {
        String id = getGCMRegistrationId();
        if (id == null) return;

        AuthStore.removeGCMID(id, new BaseStore.EmptyCallback());
    }

    /**
     * Get the GCM registration id for this device, if it has been created.
     * @return gcm registration id
     */
    public String getGCMRegistrationId() {
        String registrationId = sharedPrefs.getString(PREF_GCM_REG, "");
        if (registrationId.isEmpty()) {
            //registration not found
            return null;
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = sharedPrefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            //app version changed
            return null;
        }

        return registrationId;
    }

    /**
     * Write gcm registration id to shared preferences (not the sql database).
     * @param id gcm reg id
     */
    public void writeGCMRegistrationId(String id) {
        int appVersion = getAppVersion();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_GCM_REG, id);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    /**
     * Check if gcm reg id sent to server
     * @return true if gcm reg id sent to server
     */
    public boolean isGCMRegistrationIdSentToServer() {
        return sharedPrefs.getBoolean(PREF_GCM_SENT_API, false);
    }

    /**
     * Write a flag to shared preferences that indicates whether
     * a gcm registration id has been sent to the server.
     * @param sent
     */
    public void writeGCMRegistrationIdSentToServer(boolean sent) {
        sharedPrefs.edit().putBoolean(PREF_GCM_SENT_API, sent).apply();
    }

    /**
     * Returns the version code of the app.
     * @return Application's version code from the {@code PackageManager}.
     */
    private int getAppVersion() {
        try {
            PackageInfo packageInfo = mApp.getPackageManager()
                    .getPackageInfo(mApp.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Get the logged in user model from shared preferences.  This
     * persists user data over application instances.
     */
    private void getUserFromPref() {
        String json = sharedPrefs.getString(PREF_USER, null);
        Gson gson = GlobalGson.getGson();

        mUser = gson.fromJson(json, User.class);
    }

    /**
     * Write the user to shared preferences.
     */
    private void writeUserToPreferences() {
        if (mUser == null) return;

        Gson gson = GlobalGson.getGson();
        String userJson = gson.toJson(mUser);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_USER, userJson);
        editor.apply();
    }

    /**
     * Gets logged in user object.
     * If user object null, get from preferences.
     * @return logged in user or null
     */
    public User getUser() {
        if (mUser == null) {
            getUserFromPref();
        }

        if (mUser == null) {
            return null;
        }

        return mUser;
    }

    /**
     * Set the user object for this session which represents the logged in user.
     * Write to preferences and broadcast that there was a change.
     * @param user user object to use as logged in user
     */
    public void setUser(User user) {
        synchronized (userLock) {
            mUser = user;
            writeUserToPreferences();
        }

        //broadcast changes
        broadcastUpdatedUser();
        broadcastUpdatedCourses();
        broadcastUpdatedConexuses();
    }

    /**
     * Signal whether a user update is needed.
     * @param needed whether user update is needed.
     */
    public void setUserUpdateNeeded(boolean needed) {
        userUpdateNeeded = needed;
    }

    /**
     * Check if user update needed
     * @return true if update needed
     */
    public boolean isUserUpdateNeeded() {
        return userUpdateNeeded;
    }

    /**
     * Get the authorization token for making network requests to the server.
     * This is acquired by calling the login CN API method.
     * Get from preferences if object null.
     * @return auth token
     */
    public String getToken() {
        if (mToken != null) return mToken;
        return sharedPrefs.getString(PREF_TOKEN, null);
    }

    /**
     * Set the authorization token for making network requests to the server.
     * Also write new token to preferences.  (Write through)
     * @param token auth token
     */
    public void setToken(String token) {
        synchronized (tokenLock) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(PREF_TOKEN, token);
            editor.apply();
            mToken = token;
        }
    }

    /**
     * Indicate whether an update to the user's course data is needed
     * @param needed whether user course update is needed
     */
    public void setCourseUpdateNeeded(boolean needed) {
        courseUpdateNeeded = needed;
    }

    /**
     * Check whether user course data update is needed
     * @return whether user course data update needed
     */
    public boolean isCourseUpdateNeeded() {
        return courseUpdateNeeded;
    }

    /**
     * Get simple object "copies" of user course data, with
     * only the data that is needed for simple display of the course.
     * @return list of course object copies
     */
    public ArrayList<Course> getSimpleUserCourses() {
        synchronized (userLock) {

            if (mUser == null) {
                getUserFromPref();
            }

            if (mUser != null && mUser.getCourses() != null) {

                ArrayList<Course> courses = new ArrayList<>();

                for (Course original : mUser.getCourses()) {
                    Course copy = new Course(original.getId());
                    copy.setCourseNumber(original.getCourseNumber());
                    copy.setName(original.getName());
                    copy.setLogoURL(original.getLogoURL());

                    courses.add(copy);
                }

                return courses;
            }

            return null;
        }
    }

    /**
     * Indicate whether an update to the user's conexus data is needed
     * @param needed whether user conexus update is needed
     */
    public void setConexusUpdateNeeded(boolean needed) {
        conexusUpdateNeeded = needed;
    }

    /**
     * Check whether user conexus data update is needed
     * @return whether user conexus data update needed
     */
    public boolean isConexusUpdateNeeded() {
        return conexusUpdateNeeded;
    }

    /**
     * Get simple object "copies" of user conexus data, with
     * only the data that is needed for simple display of the conexus.
     * @return list of conexus object copies
     */
    public ArrayList<Conexus> getSimpleUserConexuses() {
        synchronized (userLock) {

            if (mUser == null) {
                getUserFromPref();
            }

            if (mUser != null && mUser.getConexuses() != null) {

                ArrayList<Conexus> conexuses = new ArrayList<>();

                for (Conexus original : mUser.getConexuses()) {
                    Conexus copy = new Conexus(original.getId());
                    copy.setConexusNumber(original.getConexusNumber());
                    copy.setName(original.getName());
                    copy.setLogoURL(original.getLogoURL());

                    conexuses.add(copy);
                }

                return conexuses;
            }

            return null;
        }
    }

    /**
     * Set the user's current courses.
     * Broadcast change to data
     * @param courses user's current courses.
     */
    public void setUserCourses(ArrayList<Course> courses) {
        synchronized (userLock) {
            if (mUser != null) {
                mUser.setCourses(courses);
            }
        }

        broadcastUpdatedCourses();
    }

    /**
     * Set user's current conexuses.
     * Broadcast change to data.
     * @param conexuses user's current conexuses
     */
    public void setUserConexuses(ArrayList<Conexus> conexuses) {
        synchronized (userLock) {
            if (mUser != null) {
                mUser.setConexuses(conexuses);
            }
        }

        broadcastUpdatedConexuses();
    }

    /**
     * Set user's profile data.
     * Broadcast change made to data.
     * @param profile user profile data
     */
    public void setUserProfile(UserProfile profile) {
        synchronized (userLock) {
            if (mUser != null) {
                mUser.setUserProfile(profile);
            }
        }

        broadcastUpdatedUser();
    }

    /**
     * Set user's anar score data.
     * Broadcast change in data.
     * @param score anar score
     */
    public void setUserScore(Score score) {
        synchronized (userLock) {
            if (mUser != null) {
                mUser.setScore(score);
            }
        }

        broadcastUpdatedUser();
    }

    /**
     * Set user's relations data (these are relations to the user himself/herself).
     * Broadcast change in data.
     * @param relations relations to the user.
     */
    public void setUserRelations(User.Relations relations) {
        synchronized (userLock) {
            if (mUser != null) {
                mUser.setRelations(relations);
            }
        }

        broadcastUpdatedUser();
    }

    /**
     * Get user new message (count of types of new notifications)
     * @return user new message
     */
    public UserNewMessage getUserNewMessage() {
        if (mUserNewMessage == null || !mUserNewMessage.isValid()) return null;
        else return new UserNewMessage(mUserNewMessage);
    }

    /**
     * Check whether user has a new notification
     * @param newMessage new UserNewMessage used to check against current UserNewMessage object.
     *                   see {@link com.thecn.app.models.notification.UserNewMessage#hasNewNotification(java.util.ArrayList, java.util.ArrayList)}
     * @return true if there is at least one new notification
     */
    public boolean hasNewNotification(UserNewMessage newMessage) {
        return mUserNewMessage == null || mUserNewMessage.hasNewNotification(newMessage);
    }

    /**
     * Set user new message (count of types of new notifications)
     * Broadcast change in data.
     * @param userNewMessage user new message object
     */
    public void setUserNewMessage(final UserNewMessage userNewMessage) {
        if (!isLoggedIn()) return;
        if (mUserNewMessage == null) {
            //create new object from old
            mUserNewMessage = new UserNewMessage(userNewMessage);
            broadcastNewNotifications();
            writeUserNewMessageToPref();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //update must be done in separate thread
                    mUserNewMessage.update(userNewMessage);
                    broadcastNewNotifications();
                    writeUserNewMessageToPref();
                }
            }).start();
        }
    }

    /**
     * Write user new message data into shared preferences.
     */
    private void writeUserNewMessageToPref() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        //don't use global gson here
        Gson gson = new Gson();
        editor.putString(PREF_USER_MESSAGE, gson.toJson(mUserNewMessage));
        editor.apply();
    }

    /**
     * Set the logged in user's avatar
     * Broadcast change in data.
     * @param avatar new avatar to set
     */
    public void setUserAvatar(Avatar avatar) {
        if (avatar == null) return;

        if (mUser == null) {
            //this could happen when user is verifying
            broadcastUpdatedUser(avatar.getView_url());
            return;
        }

        synchronized (userLock) {
            mUser.setAvatar(avatar);
            writeUserToPreferences();
        }

        broadcastUpdatedUser();
    }

    public static final String COURSE_UPDATE = "course_update";
    public static final String CONEXUS_UPDATE = "conexus_update";
    public static final String NOTIFICATION_UPDATE = "notification_update";
    public static final String USER_UPDATE = "user_update";
    public static final String PROFILE_PIC_UPDATE = "profile_pic_update";

    /**
     * Send a broadcast indicating that there are new notifications
     */
    private void broadcastNewNotifications() {
        Intent intent = new Intent(NOTIFICATION_UPDATE);
        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    /**
     * Send a broadcast indicating user's course data has been updated.
     */
    private void broadcastUpdatedCourses() {
        Intent intent = new Intent(COURSE_UPDATE);
        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    /**
     * Send broadcast indicating user's conexus data has been updated.
     */
    private void broadcastUpdatedConexuses() {
        Intent intent = new Intent(CONEXUS_UPDATE);
        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    /**
     * Send broadcast indicating user has been updated.
     */
    private void broadcastUpdatedUser() {
        broadcastUpdatedUser(null);
    }

    /**
     * Send broadcast indicating user has been updated.
     * Include an avatar url.
     * @param avatarURL location of avatar
     */
    private void broadcastUpdatedUser(String avatarURL) {
        Intent intent = new Intent(USER_UPDATE);

        if (avatarURL != null) {
            intent.putExtra("avatar_url", avatarURL);
        }

        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    private Toast mAppToast;

    /**
     * Get the toast used widely in the app.
     * @return app toast.
     */
    public Toast getAppToast() {
        return mAppToast;
    }

    /**
     * Gets toast from singleton instance
     * @return app toast.
     */
    private static Toast getToast() {
        if (sharedSession != null) {
            return sharedSession.getAppToast();
        }

        return null;
    }

    /**
     * Show that an error occurred in loading data.
     * @param dataNotLoaded name of data not loaded
     */
    public static void showDataLoadError(String dataNotLoaded) {
        showLongToast("Could not load " + dataNotLoaded + " data.");
    }

    /**
     * Show a message for a long time
     * @param message message to show
     */
    public static void showLongToast(String message) {
        Toast toast = getToast();
        if (toast == null) return;

        toast.setDuration(Toast.LENGTH_LONG);
        toast.setText(message);
        toast.show();
    }

    /**
     * Show a message for a short time
     * @param message message to show
     */
    public static void showShortToast(String message) {
        Toast toast = getToast();
        if (toast == null) return;

        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setText(message);
        toast.show();
    }

    /**
     * Stop showing a message.
     */
    public static void dismissToast() {
        Toast toast = getToast();
        if (toast != null) {
            toast.cancel();
        }
    }
}
