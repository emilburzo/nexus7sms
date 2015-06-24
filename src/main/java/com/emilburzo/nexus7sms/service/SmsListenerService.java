package com.emilburzo.nexus7sms.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.activity.SmsListActivity;
import com.emilburzo.nexus7sms.activity.SmsViewActivity;
import com.emilburzo.nexus7sms.misc.Constants;
import com.emilburzo.nexus7sms.misc.Utils;

public class SmsListenerService extends NotificationListenerService {

    private static final String PACKAGE_BASIC_SMS_RECEIVER = "com.android.basicsmsreceiver";
    private static final String ANDROID_TITLE = "android.title";
    private static final String ANDROID_TEXT = "android.text";

    private static int mId;

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

        doNotification(phoneNumber, msgBody);

        // sound notification
        doSoundNotification();

        // refresh UI
        Utils.notifyMessagesChanged(getApplicationContext());
    }

    private void doNotification(String phoneNumber, String msgBody) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        String name = Utils.getContactName(this, phoneNumber);

        boolean notification = sp.getBoolean(Constants.Settings.SHOW_NOTIFICATIONS, true);

        if (notification) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(name)
                    .setContentText(msgBody); // todo trim length

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, SmsViewActivity.class);
            resultIntent.putExtra(Constants.IntentExtras.PHONE, phoneNumber);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(SmsListActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // mId allows you to update the notification later on.
            mNotificationManager.notify(mId++, mBuilder.build());
        }
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
