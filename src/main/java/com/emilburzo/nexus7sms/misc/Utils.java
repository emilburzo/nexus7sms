package com.emilburzo.nexus7sms.misc;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import com.emilburzo.nexus7sms.BuildConfig;
import com.emilburzo.nexus7sms.model.SmsModel;
import io.realm.Realm;

import java.io.IOException;
import java.io.InputStream;
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

    public static String getContactId(Context context, String phoneNumber) {
        ContentResolver contentResolver = context.getContentResolver();

        String contactId = null;

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};

        Cursor cursor = contentResolver.query(uri, projection, null, null, null);

        if (cursor != null) {

            while (cursor.moveToNext()) {
                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            }

            cursor.close();
        }

        return contactId;
    }

    public static Bitmap getContactPhoto(Context context, String contactId) {
        if (contactId == null) {
            return null;
        }

        ContentResolver contentResolver = context.getContentResolver();

        Bitmap photo = null;
        InputStream inputStream;

        try {
            inputStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return photo;
    }


//    public static Bitmap getContactPhoto(Context context, String phoneNumber) {
//        ContentResolver contentResolver = context.getContentResolver();
//
//        String contactId = null;
//
//        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
//
//        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};
//
//        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
//
//        if (cursor != null) {
//
//            while (cursor.moveToNext()) {
//                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
//            }
//
//            cursor.close();
//        }
//
//        Bitmap photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_image);
//
//        try {
//            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));
//
//            if (inputStream != null) {
//                photo = BitmapFactory.decodeStream(inputStream);
//                inputStream.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return photo;
//    }
}
