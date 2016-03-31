package com.thecn.app.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Used to notify activities when the user has changed his following status
 * in relation to another user.  Extending classes implement {@link #onChangeReceived(String, boolean)}
 */
public abstract class FollowChangeReceiver extends BroadcastReceiver {

    public static final String FILTER = "com.thecn.app.FollowChangeReceiver";

    public static final String USER_ID_TAG = "user_id";
    public static final String STATUS_TAG = "status";

    /**
     * Get data ready for {@link #onChangeReceived(String, boolean)}
     */
    @Override
    final public void onReceive(Context context, Intent intent) {
        final String userID = intent.getStringExtra(USER_ID_TAG);
        if (userID == null) return;

        final boolean status = intent.getBooleanExtra(STATUS_TAG, false);

        onChangeReceived(userID, status);
    }

    /**
     * Implemented by subclasses
     * @param userID id of user who logged in user has changed following state
     * @param followingState new following state
     */
    public abstract void onChangeReceived(String userID, boolean followingState);

    /**
     * Send broadcast that a following change has occurred
     * @param userID id of user who logged in user has changed following state
     * @param followingState new following state
     * @param context context to get LocalBroadcastManager from
     */
    public static void sendFollowChangeBroadcast(String userID, boolean followingState, Context context) {
        Intent intent = new Intent(FILTER);
        intent.putExtra(USER_ID_TAG, userID);
        intent.putExtra(STATUS_TAG, followingState);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
