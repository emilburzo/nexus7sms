package com.emilburzo.nexus7sms.misc;

public class Constants {

    public class Intents {
        public static final String PHONE_NUMBER = "com.emilburzo.nexus7sms.PHONE_NUMBER";
        public static final String SEND_SMS = "SEND_SMS";
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


}
