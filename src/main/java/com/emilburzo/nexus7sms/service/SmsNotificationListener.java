package com.emilburzo.nexus7sms.service;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.emilburzo.nexus7sms.manager.ContactsManager;
import com.emilburzo.nexus7sms.manager.NotificationsManager;
import com.emilburzo.nexus7sms.manager.SMSManager;
import com.emilburzo.nexus7sms.misc.Constants;
import com.emilburzo.nexus7sms.misc.Utils;
import com.emilburzo.nexus7sms.sms.SmsUtil;

public class SmsNotificationListener extends NotificationListenerService {

    private static final String PACKAGE_BASIC_SMS_RECEIVER = "com.android.basicsmsreceiver";
    private static final String ANDROID_TITLE = "android.title";
    private static final String ANDROID_TEXT = "android.text";

    private static int mId = 0;

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

            onMessageReceived(phoneNumber, msgBody, sbn);
        }
    }

    private void onMessageReceived(String phoneNumber, String msgBody, StatusBarNotification sbn) {
        Utils.debug(TAG, String.format("New message from '%s' with '%s'", phoneNumber, msgBody));

        // persist to db
        SMSManager.persistSmsIn(this, ContactsManager.getContactPhone(this, phoneNumber), msgBody);

        // cancel basicsmsreceiver notification
        doCancelBasicNotification(sbn);

        // our notification
        SmsUtil.doNotification(this, phoneNumber, msgBody, mId);
        mId += 2;

        // sound notification
        SmsUtil.doSoundNotification(getApplicationContext());

        // refresh UI
        NotificationsManager.notifyMessagesChanged(getApplicationContext());
    }

    private void doCancelBasicNotification(StatusBarNotification sbn) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        boolean notification = sp.getBoolean(Constants.Settings.HIDE_SIMPLE_NOTIFICATIONS, true);

        if (!notification || sbn == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cancelNotification(sbn.getKey());
        } else {
            cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
        }
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }
}
