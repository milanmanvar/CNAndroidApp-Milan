package com.thecn.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.activities.LauncherActivity;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.course.Course;
import com.thecn.app.stores.ConexusStore;
import com.thecn.app.stores.CourseStore;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Binds to {@link com.thecn.app.activities.navigation.NavigationActivity} and
 * polls for updates to data every ten seconds.  Only runs when navigation activity is resumed.
 */
public class UpdateService extends Service {

    private final IBinder mBinder = new UpdateServiceBinder();

    private static final int TEN_SECONDS = 10000;

    private ArrayList<Updater> mUpdaters = new ArrayList<Updater>(); //customized updaters can be given to service
    private final Object updaterLock = new Object();

    private Timer mTimer;

    /**
     * Binder used to bind this service to a {@link com.thecn.app.activities.navigation.NavigationActivity}
     */
    public class UpdateServiceBinder extends Binder {
        public UpdateService getService() {
            return UpdateService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Specifies a custom action to take when it is time to update.
     */
    public interface Updater {
        public void update();
    }

    /**
     * Uses a timer to schedule an update every ten seconds.
     * By default, updates the user and his/her courses and conexuses
     * when needed.
     * todo should this be changed to alarms?
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                final AppSession session = AppSession.getInstance();

                if (session.isUserUpdateNeeded()) {
                    //already update courses and conexuses
                    session.setCourseUpdateNeeded(false);
                    session.setConexusUpdateNeeded(false);

                    UserStore.getMe(new LauncherActivity.SetupFragment.GetMeCallback());
                }

                if (session.isCourseUpdateNeeded()) {
                    UserStore.getAllUserCourses(new ResponseCallback() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (StoreUtil.success(response)) {
                                ArrayList<Course> courses = CourseStore.getListData(response);

                                session.setUserCourses(courses);
                                session.setCourseUpdateNeeded(false);
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {}
                    });
                }

                if (session.isConexusUpdateNeeded()) {
                    UserStore.getAllUserConexuses(new ResponseCallback() {

                        @Override
                        public void onResponse(JSONObject response) {
                            if (StoreUtil.success(response)) {
                                ArrayList<Conexus> conexuses = ConexusStore.getListData(response);

                                session.setUserConexuses(conexuses);
                                session.setCourseUpdateNeeded(false);
                            }
                        }

                        public void onError(VolleyError error) {}
                    });
                }

                synchronized (updaterLock) {
                    for (Updater updater : mUpdaters) {
                        updater.update();
                    }
                }

                AppSession a = AppSession.getInstance();

                //if registered with Google Cloud Messaging, don't poll for notification updates
                if (a.isGCMRegistrationIdSentToServer()) return;

                a.getUserNewMessageFromServer();

            }

        }, 0, TEN_SECONDS);
    }

    /**
     * Add an action to take when service updates
     * @param updater custom action to take
     */
    public void addUpdater(Updater updater) {
        synchronized (updaterLock) {
            mUpdaters.add(updater);
        }
    }

    /**
     * Remove updater previously added
     * @param updater updater to be removed.
     */
    public void removeUpdater(Updater updater) {
        synchronized (updaterLock) {
            mUpdaters.remove(updater);
        }
    }

    /**
     * Cancel timer
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mTimer != null) {
            mTimer.cancel();
        }
    }
}
