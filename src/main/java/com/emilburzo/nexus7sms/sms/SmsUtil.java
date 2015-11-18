package com.emilburzo.nexus7sms.sms;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.activity.SmsViewActivity;
import com.emilburzo.nexus7sms.manager.ContactsManager;
import com.emilburzo.nexus7sms.misc.Constants;

public class SmsUtil {

    public static void doNotification(Context context, String phoneNumber, String msgBody, int mId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        boolean notification = sp.getBoolean(Constants.Settings.SHOW_NOTIFICATIONS, true);

        if (notification) {
            String name = ContactsManager.getContactName(context, phoneNumber);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setLargeIcon(ContactsManager.getContactPhoto(context, phoneNumber))
                    .setAutoCancel(true)
                    .setContentTitle(name)
                    .setContentText(msgBody); // todo trim length

            // Creates an explicit intent for an Activity in your app
            Intent intent = new Intent(context, SmsViewActivity.class);
            intent.putExtra(Constants.Intents.PHONE_NUMBER, ContactsManager.getContactPhone(context, phoneNumber));

            PendingIntent pendingIntent = PendingIntent.getActivity(context, mId, intent, PendingIntent.FLAG_ONE_SHOT);
            mBuilder.setContentIntent(pendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // mId allows you to update the notification later on.
            mNotificationManager.notify(mId, mBuilder.build());
        }
    }

    public static void doSoundNotification(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        boolean sound = sp.getBoolean(Constants.Settings.INCOMING_SMS_SOUND, true);

        if (sound) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        }
    }
}
