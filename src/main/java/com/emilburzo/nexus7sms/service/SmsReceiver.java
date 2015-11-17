package com.emilburzo.nexus7sms.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import com.emilburzo.nexus7sms.manager.ContactsManager;
import com.emilburzo.nexus7sms.manager.NotificationsManager;
import com.emilburzo.nexus7sms.manager.SMSManager;
import com.emilburzo.nexus7sms.sms.SmsUtil;

public class SmsReceiver extends BroadcastReceiver {

    private static int mId = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];

            // For every SMS message received
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
        }

        for (SmsMessage msg : msgs) {
            onSmsReceived(context, msg);
        }
    }

    private void onSmsReceived(Context context, SmsMessage sms) {
        String phone = sms.getOriginatingAddress();
        String body = sms.getMessageBody();

        // persist to db
        SMSManager.persistSmsIn(context, ContactsManager.getContactPhone(context, phone), body);

        // our notification
        SmsUtil.doNotification(context, phone, body, mId);
        mId += 2;

        // sound notification
        SmsUtil.doSoundNotification(context);

        // refresh UI
        NotificationsManager.notifyMessagesChanged(context);
    }
}
