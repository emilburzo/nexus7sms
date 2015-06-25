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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.emilburzo.nexus7sms.BuildConfig;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.model.SmsModel;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class Utils {

    private static final String TAG = "Utils";

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

    public static String getContactName(Context context, String phoneNumber, boolean withPhone) {
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
            Utils.debug(TAG, String.format("Couldn't find contact for phone number '%s'", phoneNumber));
            return phoneNumber;
        }

        Utils.debug(TAG, String.format("Found contact name '%s' for '%s'", contactName, phoneNumber));

        if (withPhone) {
            return String.format("%s (%s)", contactName, phoneNumber);
        } else {
            return contactName;
        }
    }

    public static String getContactPhone(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.NUMBER}, null, null, null);

        if (cursor == null) {
            return null;
        }

        String number = null;

        if (cursor.moveToFirst()) {
            number = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.NUMBER));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        if (number == null || number.trim().isEmpty()) {
            return phoneNumber;
        }

        return number;
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

    public static Bitmap getContactPhoto(Context context, String phone) {
        String contactId = getContactId(context, phone);

        // no contact found for given phone number, return boring profile pic
        if (contactId == null) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.contact_picture);
        }

        Bitmap photo = null;

        ContentResolver contentResolver = context.getContentResolver();

        InputStream inputStream;

        try {
            inputStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId)));

            if (inputStream != null) {
                // found contact photo
                photo = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return photo;
    }

    public static Bitmap getProfilePhoto(Context context) {
        Bitmap photo = null;

        ContentResolver cr = context.getContentResolver();
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, ContactsContract.Profile.CONTENT_URI);

        if (input == null) {
            photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.contact_picture);
        } else {
            photo = BitmapFactory.decodeStream(input);
        }

        return photo;
    }

    public static int getColor(String phone) {
        ColorGenerator generator = ColorGenerator.MATERIAL;

        if (phone == null) {
            return generator.getRandomColor();
        } else {
            return generator.getColor(phone);
        }
    }

    public static TextDrawable getContactTextPhoto(Context context, String phone) {
        String name = getContactName(context, phone, false);

        String contactFirstLetter = null;

        if (name == null) {
            contactFirstLetter = "X";
        } else {
            contactFirstLetter = name.substring(0, 1);
        }

        return TextDrawable.builder().buildRect(contactFirstLetter, getColor(phone));
    }

    public static String formatDate(Date date) {
        DateFormat df = DateFormat.getDateTimeInstance();
        return df.format(date);
    }

    public static void notifyMessagesChanged(Context context) {
        Intent intent = new Intent(Constants.IntentActions.MESSAGES_CHANGED);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
