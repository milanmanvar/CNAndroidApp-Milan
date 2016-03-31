package com.thecn.app.broadcastreceivers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.thecn.app.services.AlertNotificationService;

/**
 * Starts {@link com.thecn.app.services.AlertNotificationService} upon receiving intent.
 */
public class AlertNotificationReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(),
                AlertNotificationService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
    }
}
