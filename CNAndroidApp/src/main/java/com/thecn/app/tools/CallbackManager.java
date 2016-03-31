package com.thecn.app.tools;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.VolleyError;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.StoreUtil;

import org.json.JSONObject;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class is used to try to overcome memory problems when keeping references to activities or
 * fragments.
 * Usage: call {@link CallbackManager#resume(Object)} when a fragment is resumed.
 *        call {@link CallbackManager#pause()} when a fragment is paused.
 * Any callbacks that are added while the fragment is paused are not executed until the fragment has
 * resumed again.
 * There will be no references to the fragment or its activity while the callback manager is paused.
 * This will prevent {@link android.os.DeadObjectException} and {@link java.lang.OutOfMemoryError}
 * if activities are being recreated and old references are no longer valid, or an activity has been destroyed.
 *
 * todo There might be a simpler solution, such as using a static inner class that holds the data accessed by
 * todo asynchronous operations.  This type of class could have its fragment/activity references removed on pause
 * todo and set again on resume.  This would effectively cut out the middle man (the callback manager) and allow
 * todo more controlled execution of relevant code instead of executing haphazardly ordered callbacks out of a queue.
 */
public class CallbackManager<T> {
    private ConcurrentLinkedQueue<Callback<T>> callbacks = new ConcurrentLinkedQueue<>(); //queue of callbacks
    private T object; //should be a fragment
    private boolean resumed = false;

    private Activity activity; //optional ref to activity

    /**
     * New instance
     */
    public CallbackManager() {}

    /**
     * New instance with activity reference
     * @param activity reference
     */
    public CallbackManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * Add a callback.  If resumed, execute immediately on UI thread.  Else,
     * add to callback queue.
     * @param callback callback to add.
     */
    public void addCallback(final Callback<T> callback) {
        if (resumed) {

            //if not on main thread, execute on main thread
            if (Looper.myLooper() == Looper.getMainLooper()) {
                callback.execute(object);
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.execute(object);
                    }
                });
            }

        } else {
            callbacks.offer(callback);
        }
    }

    /**
     * Pause callback manager
     */
    public void pause() {
        resumed = false;
        object = null;
    }

    /**
     * Resume callback manager.  If there are callbacks in queue,
     * execute them.
     * @param object fragment reference to keep
     */
    public void resume(T object) {
        this.object = object;
        resumed = true;

        //execute callbacks
        Callback<T> callback = callbacks.poll();
        while (callback != null) {
            callback.execute(object);

            callback = callbacks.poll();
        }
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return activity;
    }

    public T getObject() {
        return object;
    }

    /**
     * Base callback to use.
     * @param <T> fragment reference.
     */
    public abstract static class Callback<T> {
        protected CallbackManager<T> manager;

        /**
         * Overridden to contain code to execute when a fragment is resumed.
         * @param object fragment that is now resumed.
         */
        public abstract void execute(T object);
    }

    /**
     * Extends {@link com.thecn.app.tools.CallbackManager.Callback} but also implements {@code ResponseCallback}
     * Handles adding callbacks for actions after network requests returns to the callback manager's queue.
     * @param <T> fragment class
     */
    public abstract static class NetworkCallback<T> extends Callback<T> implements ResponseCallback {
        protected JSONObject response;
        protected VolleyError error;

        private boolean success = false;
        private boolean cancelled = false;

        protected CallbackManager<T> manager;

        /**
         * New instance
         * @param manager reference to a callback manager
         */
        public NetworkCallback(CallbackManager<T> manager) {
            this.manager = manager;
        }

        /**
         * Final.
         * Stores reference to response, checks whether api call was successful.
         * Calls {@link #onImmediateResponse(org.json.JSONObject)}
         * Adds callback to the callback manager.
         * @param response data from server
         */
        @Override
        final public void onResponse(JSONObject response) {
            this.response = response;
            success = StoreUtil.success(response);
            onImmediateResponse(response);
            manager.addCallback(this);
        }

        /**
         * Final.
         * Stores reference to error,
         * Calls {@link #onImmediateError(com.android.volley.VolleyError)}
         * Adds callback to callback manager.
         * @param error network error
         */
        @Override
        final public void onError(VolleyError error) {
            this.error = error;
            onImmediateError(error);
            manager.addCallback(this);
        }

        @Override
        public void execute(T object) {
            if (cancelled) return;

            onResumeBefore(object);

            if (response != null) {
                onResumeWithResponse(object);
            } else if (error != null) {
                onResumeWithError(object);
            }

            onResumeAfter(object);
        }

        /**
         * Set cancel flag to true
         */
        public void cancel() {
            cancelled = true;
        }

        /**
         * Check if was successful
         * @return true if successful
         */
        public boolean wasSuccessful() {
            return success;
        }

        /**
         * Called immediately when response is received (even if
         * fragment not resumed)
         * @param response data from server
         */
        public void onImmediateResponse(JSONObject response) {}

        /**
         * Called immediately when error received (even if
         * fragment not resumed)
         * @param error network error
         */
        public void onImmediateError(VolleyError error) {}

        /**
         * Always called on resume before calling either
         * {@link #onResumeWithResponse(Object)} or {@link #onResumeWithError(Object)}
         * @param object resumed fragment
         */
        public void onResumeBefore(T object) {};

        /**
         * Called when there was a response from the server.
         * @param object resumed fragment
         */
        public void onResumeWithResponse(T object) {};

        /**
         * Called when there was a network error.
         * @param object resumed fragment
         */
        public void onResumeWithError(T object) {};

        /**
         * Always called on resume after calling either
         * {@link #onResumeWithResponse(Object)} or {@link #onResumeWithError(Object)}
         * @param object resumed fragment
         */
        public void onResumeAfter(T object) {};
    }
}