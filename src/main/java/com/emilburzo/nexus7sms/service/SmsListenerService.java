package com.emilburzo.nexus7sms.service;

import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.emilburzo.nexus7sms.misc.Constants;
import com.emilburzo.nexus7sms.misc.Utils;

public class SmsListenerService extends NotificationListenerService {

    private static final String PACKAGE_BASIC_SMS_RECEIVER = "com.android.basicsmsreceiver";

    private String TAG = this.getClass().getSimpleName();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        /*
        06-17 08:23:51.227  14595-14608/com.emilburzo.nexus7sms I/Package﹕ com.android.basicsmsreceiver
        06-17 08:23:51.227  14595-14608/com.emilburzo.nexus7sms I/Ticker﹕ ghshshjs shshshhs shxhhdjd sjdjdjsj sjsjdhsj sjsjejxicjejficksjw djdijjc e dusjbejcjakxj
        06-17 08:23:51.227  14595-14608/com.emilburzo.nexus7sms I/Title﹕ +40745321948
        06-17 08:23:51.227  14595-14608/com.emilburzo.nexus7sms I/Text﹕ ghshshjs shshshhs shxhhdjd sjdjdjsj sjsjdhsj sjsjejxicjejficksjw djdijjc e dusjbejcjakxj
         */
        String pack = sbn.getPackageName();

        if (pack.equalsIgnoreCase(PACKAGE_BASIC_SMS_RECEIVER)) {
            Bundle extras = sbn.getNotification().extras;
            String phoneNumber = extras.getString("android.title");
            String msgBody = extras.getCharSequence("android.text").toString();

            Log.i("Package", pack);
            Log.i("phoneNumber", phoneNumber);
            Log.i("msgBody", msgBody);

            Utils.persistSmsIn(this, phoneNumber, msgBody);
//            Utils.persistSmsIn(this, "+40742622603", msgBody); // todo fixme wtf

            notifyNewMessage();
        }
    }

    private void notifyNewMessage() {
        Intent intent = new Intent(Constants.IntentActions.MSG_RECEIVED);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }
}
