package com.emilburzo.nexus7sms.misc;

public class Constants {

    public class Intents {
        public static final String PHONE_NUMBER = "com.emilburzo.nexus7sms.PHONE_NUMBER";
        public static final String MESSAGE = "com.emilburzo.nexus7sms.MESSAGE";
        public static final String SEND_SMS = "com.emilburzo.nexus7sms.SEND_SMS";
    }

    public static final class IntentActions {
        public static final String MESSAGES_CHANGED = "MSG_CHANGED";
    }

    public static final class IntentExtras {
        public static final String PHONE = "phone";
        public static final String MESSAGE = "msg";
        public static final String UUID = "uuid";
    }

    public static final class SmsTypes {
        public static final String IN = "in";
        public static final String OUT = "out";
    }

    public static final class AndroidSecure {
        public static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
        public static final String NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    }

    public static final class SmsFields {
        public static final String TIMESTAMP = "timestamp";
    }

    public static final class UTF8 {
        public static final String CHECKMARK = "✓";
    }

    public static final class Settings {
        public static final String INCOMING_SMS_SOUND = "pref_incoming_sms_sound";
        public static final String OUTGOING_SMS_SOUND = "pref_outgoing_sms_sound";
        public static final String SHOW_NOTIFICATIONS = "pref_show_notifications";
        public static final String HIDE_SIMPLE_NOTIFICATIONS = "pref_hide_simple_notification";
        public static final String PRIVACY_POLICY = "pref_privacy_policy";
    }

    public static final class Links {
        public static final String PRIVACY_POLICY = "https://github.com/emilburzo/nexus7sms/blob/master/PRIVACY.md";
    }


}
