package com.emilburzo.nexus7sms.manager;

import android.content.Context;
import com.emilburzo.nexus7sms.misc.Constants;
import com.emilburzo.nexus7sms.misc.Utils;
import com.emilburzo.nexus7sms.model.SmsModel;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import java.util.Date;
import java.util.UUID;

public class SMSManager {

    private static final String TAG = "Sms";

    public static void markSmsDelivered(Context context, String uuid) {
        Realm realm = Realm.getInstance(context);

        // Build the query looking at all users:
        RealmQuery<SmsModel> query = realm.where(SmsModel.class);

        // Add query conditions:
        query.equalTo("uuid", uuid);

        // Execute the query:
        RealmResults<SmsModel> results = query.findAll();

        Utils.debug(TAG, String.format("Found '%d' results for uuid '%s', marking as delivered", results.size(), uuid));

        if (!results.isEmpty()) {
            SmsModel result = results.first();

            realm.beginTransaction();
            result.setDelivered(true);
            realm.commitTransaction();
        }
        realm.close();
    }

    public static void markSmsNotSent(Context context, String uuid) {
        Realm realm = Realm.getInstance(context);

        // Build the query looking at all users:
        RealmQuery<SmsModel> query = realm.where(SmsModel.class);

        // Add query conditions:
        query.equalTo("uuid", uuid);

        // Execute the query:
        RealmResults<SmsModel> results = query.findAll();

        Utils.debug(TAG, String.format("Found '%d' results for uuid '%s', marking as sent", results.size(), uuid));

        if (!results.isEmpty()) {
            SmsModel result = results.first();

            realm.beginTransaction();
            result.setSent(false);
            realm.commitTransaction();
        }

        realm.close();
    }

    public static String persistSmsIn(Context context, String phoneNo, String msgBody) {
        return persistSms(context, Constants.SmsTypes.IN, phoneNo, msgBody);
    }

    public static String persistSmsOut(Context context, String phoneNo, String msgBody) {
        return persistSms(context, Constants.SmsTypes.OUT, phoneNo, msgBody);
    }

    private static String persistSms(Context context, String type, String phoneNo, String msgBody) {
        Realm realm = null;
        String uuid = UUID.randomUUID().toString();

        try {
            realm = Realm.getInstance(context);

            realm.beginTransaction();

            SmsModel sms = realm.createObject(SmsModel.class);

            sms.setUuid(uuid);
            sms.setBody(msgBody);
            sms.setPhone(phoneNo);
            sms.setTimestamp(new Date());
            sms.setType(type);
            sms.setSent(true); // assume we can send, mark as fail if not, when actually sending

            realm.commitTransaction();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return uuid;
    }

    public static void onSmsSendFail(Context context, String uuid) {
        Utils.debug(TAG, "SMS send fail");

        SMSManager.markSmsNotSent(context, uuid);

        NotificationsManager.notifyMessagesChanged(context);
    }
}
