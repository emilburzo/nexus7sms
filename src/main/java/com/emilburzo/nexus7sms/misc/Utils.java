package com.emilburzo.nexus7sms.misc;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.emilburzo.nexus7sms.BuildConfig;

import java.text.DateFormat;
import java.util.Date;

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

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static int getColor(String phone) {
        ColorGenerator generator = ColorGenerator.MATERIAL;

        if (phone == null) {
            return generator.getRandomColor();
        } else {
            return generator.getColor(phone);
        }
    }

    public static String formatDate(Date date) {
        DateFormat df = DateFormat.getDateTimeInstance();
        return df.format(date);
    }


    public static boolean hasContactsReadPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }
}
