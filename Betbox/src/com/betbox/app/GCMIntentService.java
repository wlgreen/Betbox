/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.betbox.app;

import static com.betbox.app.gcm.CommonUtilities.SENDER_ID;
import static com.betbox.app.gcm.CommonUtilities.displayMessage;

import com.betbox.app.R;
import com.betbox.app.activity.MainActivity;
import com.betbox.app.data.Bet;
import com.betbox.app.data.BetPool;
import com.betbox.app.data.StandPool;
import com.betbox.app.gcm.ServerUtilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	private static final String PARAMETER_JSON = "JSON";
	private static final String TAG = "GCMIntentService";

	public GCMIntentService() {
		super(SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
		displayMessage(context, getString(R.string.gcm_registered));
		ServerUtilities.register(context, registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i(TAG, "Device unregistered");
		displayMessage(context, getString(R.string.gcm_unregistered));
		if (GCMRegistrar.isRegisteredOnServer(context)) {
			ServerUtilities.unregister(context, registrationId);
		} else {
			// This callback results from the call to unregister made on
			// ServerUtilities when the registration to the server failed.
			Log.i(TAG, "Ignoring unregister callback");
		}
	}

	
	/* Receive message from GCM and parse them as JSON format */
	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i(TAG, "Received message");
		String jsonString = intent.getExtras().getString(PARAMETER_JSON);
		try {
			JSONObject mainObject = new JSONObject(jsonString);
			JSONArray jsonArray = mainObject.getJSONArray("data");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String content = jsonObject.getString(Bet.PROPERTY_CONTENT);
				String time = jsonObject.getString(Bet.PROPERTY_TIME);
				String pool = jsonObject.getString(Bet.PROPERTY_POOL);
				String status = jsonObject.getString(Bet.PROPERTY_STATUS);

				Bet bet = BetPool.ITEM_MAP.get(content);
				if (bet == null) {
					bet = new Bet(content, time, pool, status);
					BetPool.addItem(bet);
				} else {
					/* Update pool info when bet already exist */
					if (!bet.pool.equals(pool)) {
						bet.pool = new StandPool(pool);
						String notifyMessage = "Bet pool updated!";
						generateNotification(context, notifyMessage);
					}

					/* Update bet status for checkout */
					if (bet.status.equals(Bet.STATUS_OPEN)
							|| status.equals(Bet.STATUS_CLOSE)) {
						bet.status = status;
						String notifyMessage = "Bet checkout!";
						generateNotification(context, notifyMessage);
					}
				}
				String notifyMessage = getString(R.string.gcm_message);
				displayMessage(context, content);
				// notifies user
				generateNotification(context, notifyMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.i(TAG, "Received deleted messages notification");
		String message = getString(R.string.gcm_deleted, total);
		displayMessage(context, message);
		// notifies user
		generateNotification(context, message);
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
		displayMessage(context, getString(R.string.gcm_error, errorId));
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		displayMessage(context,
				getString(R.string.gcm_recoverable_error, errorId));
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	private static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_stat_gcm;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);
		String title = context.getString(R.string.app_name);
		Intent notificationIntent = new Intent(context, MainActivity.class);
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, notification);
	}

}
