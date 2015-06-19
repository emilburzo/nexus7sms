package com.emilburzo.nexus7sms.misc;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import com.emilburzo.nexus7sms.BuildConfig;
import com.emilburzo.nexus7sms.model.SmsModel;
import io.realm.Realm;

import java.util.Date;
import java.util.UUID;

public class Utils {

    public static String getPhoneNumber(Intent intent) {
        return intent.getStringExtra(Constants.Intents.PHONE_NUMBER);
    }

    public static void debug(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static boolean isNotEmpty(String str) {
        return str != null && str.length() > 0;
    }

    public static void persistSmsIn(Context context, String phoneNo, String msgBody) {
        persistSms(context, Constants.SmsTypes.IN, phoneNo, msgBody);
    }

    public static void persistSmsOut(Context context, String phoneNo, String msgBody) {
        persistSms(context, Constants.SmsTypes.OUT, phoneNo, msgBody);
    }

    private static void persistSms(Context context, String type, String phoneNo, String msgBody) {
        Realm realm = null;

        try {
            realm = Realm.getInstance(context);

            realm.beginTransaction();

            SmsModel sms = realm.createObject(SmsModel.class);

            sms.setUuid(UUID.randomUUID().toString());
            sms.setBody(msgBody);
            sms.setPhone(phoneNo);
            sms.setTimestamp(new Date());
            sms.setType(type);

            realm.commitTransaction();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor == null) {
            return null;
        }

        String contactName = null;

        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        if (contactName == null || contactName.trim().isEmpty()) {
            return phoneNumber;
        }

        return contactName;
    }
}
