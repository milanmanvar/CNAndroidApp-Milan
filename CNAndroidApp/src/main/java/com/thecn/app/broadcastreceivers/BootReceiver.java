package com.thecn.app.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Sends intent to {@link com.thecn.app.broadcastreceivers.AlertNotificationReceiver} on device boot
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) return;

        context.sendBroadcast(new Intent(context, AlertNotificationReceiver.class));
    }
}
