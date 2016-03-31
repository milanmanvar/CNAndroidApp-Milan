/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thecn.app.services;

import android.app.IntentService;
import android.content.Intent;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.broadcastreceivers.GcmBroadcastReceiver;
import com.thecn.app.models.util.Time;
import com.thecn.app.models.user.User;
import com.thecn.app.models.notification.UserNewMessage;
import com.thecn.app.stores.NewMessageStore;
import com.thecn.app.stores.ResponseCallback;

import org.json.JSONObject;

/**
 * Receives an intent from {@link com.thecn.app.broadcastreceivers.GcmBroadcastReceiver} that acts as a "send to sync" message,
 * which simply tells the app that it should check for new CN notifications.  This eliminates polling for notifications, which helps save battery life.
 *
 * Uses {@link com.thecn.app.services.AlertNotificationService#processNotification(android.content.Context, com.thecn.app.models.notification.UserNewMessage)}
 * to process notifications.
 * Also passes work to {@link com.thecn.app.services.AlertNotificationService} when a network error occurs, which handles it through exponential backoff.
 *
 * Original doc by Google:
 *
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

    public static final String TAG = "GCMIntentService";

    public GcmIntentService() {
        super(TAG);
    }

    /**
     * Immediately sends a network request to the server for a count of
     * new notifications.  If successful, processes the notifications using
     * {@link com.thecn.app.services.AlertNotificationService#processNotification(android.content.Context, com.thecn.app.models.notification.UserNewMessage)}.
     * If unsuccessful, pass the work to {@link com.thecn.app.services.AlertNotificationService} which will handle the error with exponential backoff.
     */
    @Override
    protected void onHandleIntent(final Intent intent) {

        NewMessageStore.getNewMessages(new ResponseCallback() {
            @Override
            public void onResponse(JSONObject response) {
                UserNewMessage message = NewMessageStore.getData(response);

                if (message != null) {
                    //use same method for processing notification as AlertNotificationService
                    AlertNotificationService.processNotification(GcmIntentService.this, message);
                }

                AppSession.getInstance().markLastNotificationRefreshTime();
                GcmBroadcastReceiver.completeWakefulIntent(intent);
            }

            @Override
            public void onError(VolleyError error) {
                //check user still logged in
                AppSession as = AppSession.getInstance();
                if (!as.isLoggedIn()) {
                    GcmBroadcastReceiver.completeWakefulIntent(intent);
                    return;
                }

                User user = as.getUser();
                if (user.getSettings() == null) {
                    as.getSettingsFromDatabase();
                }

                User.Settings s = user.getSettings();

                //start exponential backoff using AlertNotificationService

                long backoffTime = Time.ONE_MINUTE;
                long nextRefreshTime = System.currentTimeMillis() + 2 * s.getRefreshNotificationInterval();

                AlertNotificationService.setBackoffAlarm(backoffTime, nextRefreshTime, GcmIntentService.this);

                GcmBroadcastReceiver.completeWakefulIntent(intent);
            }
        });
    }
}