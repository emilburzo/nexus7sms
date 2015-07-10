package com.emilburzo.nexus7sms.misc;

import android.content.Intent;
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


}
