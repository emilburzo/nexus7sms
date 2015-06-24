package com.emilburzo.nexus7sms.service;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.emilburzo.nexus7sms.misc.Constants;
import com.emilburzo.nexus7sms.misc.Utils;

public class SmsListenerService extends NotificationListenerService {

    private static final String PACKAGE_BASIC_SMS_RECEIVER = "com.android.basicsmsreceiver";
    private static final String ANDROID_TITLE = "android.title";
    private static final String ANDROID_TEXT = "android.text";

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
            String phoneNumber = extras.getString(ANDROID_TITLE);
            String msgBody = extras.getCharSequence(ANDROID_TEXT).toString();

            onMessageReceived(phoneNumber, msgBody);
        }
    }

    private void onMessageReceived(String phoneNumber, String msgBody) {
        Utils.debug(TAG, String.format("New message from '%s' with '%s'", phoneNumber, msgBody));

        // persist to db
        Utils.persistSmsIn(this, Utils.getContactPhone(this, phoneNumber), msgBody);

        // sound notification
        doSoundNotification();

        // refresh UI
        Utils.notifyMessagesChanged(getApplicationContext());
    }

    private void doSoundNotification() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        boolean sound = sp.getBoolean(Constants.Settings.INCOMING_SMS_SOUND, true);

        if (sound) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }
}
