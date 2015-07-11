package com.emilburzo.nexus7sms.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import com.emilburzo.nexus7sms.manager.NotificationsManager;
import com.emilburzo.nexus7sms.manager.SMSManager;
import com.emilburzo.nexus7sms.misc.Constants;
import com.emilburzo.nexus7sms.misc.Utils;

public class SmsService extends Service {

    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";

    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onCreate() {
        Utils.debug(TAG, "onCreate()");

        LocalBroadcastManager.getInstance(this).registerReceiver(sendSmsRecv, new IntentFilter(Constants.Intents.SEND_SMS));

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.debug(TAG, "onStartCommand()");

        return Service.START_STICKY;
    }

    private void doSendSms(String phone, String message, String uuid) {
        // status receivers
        SendBroadcastReceiver sendRecv = new SendBroadcastReceiver(uuid);
        DeliveryBroadcastReceiver deliveryRecv = new DeliveryBroadcastReceiver(uuid);

        registerReceiver(sendRecv, new IntentFilter(SENT));
        registerReceiver(deliveryRecv, new IntentFilter(DELIVERED));

        // sms send pending intents
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveryIntent = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        // actual sms send
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phone, null, message, sentIntent, deliveryIntent);
        } catch (Exception e) {
            Utils.debug(TAG, e.getMessage());

            SMSManager.onSmsSendFail(getApplicationContext(), uuid);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Utils.debug(TAG, "onDestroy()");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(sendSmsRecv);

        super.onDestroy();
    }

    private BroadcastReceiver sendSmsRecv = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            String phone = extras.getString(Constants.IntentExtras.PHONE);
            String msg = extras.getString(Constants.IntentExtras.MESSAGE);
            String uuid = extras.getString(Constants.IntentExtras.UUID);

            doSendSms(phone, msg, uuid);
        }
    };

    class SendBroadcastReceiver extends BroadcastReceiver {

        private final String uuid;

        public SendBroadcastReceiver(String uuid) {
            this.uuid = uuid;
        }

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    onSmsOk();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    onSmsFail();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    onSmsFail();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    onSmsFail();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    onSmsFail();
                    break;
            }

            unregisterReceiver(this);
        }

        private void onSmsOk() {
            Utils.debug(TAG, "SMS sent");
        }

        private void onSmsFail() {
            SMSManager.onSmsSendFail(getApplicationContext(), uuid);
        }
    }

    class DeliveryBroadcastReceiver extends BroadcastReceiver {

        private final String uuid;

        public DeliveryBroadcastReceiver(String uuid) {
            this.uuid = uuid;
        }

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    onSmsDelivered();
                    break;
                case Activity.RESULT_CANCELED:
                    onSmsNotDelivered();
                    break;
            }

            unregisterReceiver(this);
        }

        private void onSmsDelivered() {
            Utils.debug(TAG, "SMS has been delivered");

            SMSManager.markSmsDelivered(getApplicationContext(), uuid);

            NotificationsManager.notifyMessagesChanged(getApplicationContext());
        }

        private void onSmsNotDelivered() {
            Utils.debug(TAG, "SMS not delivered");
        }
    }
}
