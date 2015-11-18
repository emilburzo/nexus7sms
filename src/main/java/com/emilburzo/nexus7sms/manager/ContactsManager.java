package com.emilburzo.nexus7sms.manager;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import com.amulyakhare.textdrawable.TextDrawable;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.misc.Utils;

import java.io.IOException;
import java.io.InputStream;

public class ContactsManager {

    private static final String TAG = "ContactsManager";

    public static String getContactName(Context context, String phoneNumber) {
        if (Utils.isEmpty(phoneNumber)) {
            return "N/A";
        }

        // android >= 6.0: check if we have permission
        if (!Utils.hasContactsPermission(context)) {
            return phoneNumber;
        }

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

        return contactName;
    }

    public static String getContactPhone(Context context, String phoneNumber) {
        if (Utils.isEmpty(phoneNumber)) {
            return "N/A";
        }

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
        String contactId = null;

        if (Utils.isEmpty(phoneNumber)) {
            return contactId;
        }

        // android >= 6.0: check if we have permission
        if (!Utils.hasContactsPermission(context)) {
            return contactId;
        }

        ContentResolver contentResolver = context.getContentResolver();

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
        if (Utils.isEmpty(phone)) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.contact_picture);
        }

        String contactId = getContactId(context, phone);

        // no contact found for given phone number, return boring profile pic
        if (contactId == null) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.contact_picture);
        }

        ContentResolver contentResolver = context.getContentResolver();

        Bitmap photo = null;
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

        // android >= 6.0: check if we have permission
        if (!Utils.hasContactsPermission(context)) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.contact_picture);
        }

        ContentResolver cr = context.getContentResolver();
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, ContactsContract.Profile.CONTENT_URI);

        if (input == null) {
            photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.contact_picture);
        } else {
            photo = BitmapFactory.decodeStream(input);
        }

        return photo;
    }

    public static TextDrawable getContactTextPhoto(Context context, String phone) {
        String name = getContactName(context, phone);

        String contactFirstLetter = null;

        if (name == null) {
            contactFirstLetter = "?";
        } else {
            contactFirstLetter = name.substring(0, 1);
        }

        return TextDrawable.builder().buildRect(contactFirstLetter, Utils.getColor(phone));
    }

}
