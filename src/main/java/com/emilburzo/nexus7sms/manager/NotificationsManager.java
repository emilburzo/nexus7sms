package com.emilburzo.nexus7sms.manager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.emilburzo.nexus7sms.misc.Constants;

public class NotificationsManager {

    private static final String TAG = "NotificationsManager";

    public static void notifyMessagesChanged(Context context) {
        Intent intent = new Intent(Constants.IntentActions.MESSAGES_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
