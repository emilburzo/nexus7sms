package com.emilburzo.nexus7sms.misc;

import android.content.Intent;
import android.util.Log;
import com.emilburzo.nexus7sms.BuildConfig;

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
}
