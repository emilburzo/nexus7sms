package com.emilburzo.nexus7sms.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.*;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.misc.Utils;

public class SmsActivity extends ActionBarActivity {

    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";

    private static final int REQUEST_PICK_CONTACT = 1;

    private ImageButton sendButton;
    private EditText smsDestination;
    private EditText smsContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sms_activity);

        initUi();

        initHandlers();
    }

    private void loadValues(Intent intent) {
        String phoneNumber = Utils.getPhoneNumber(intent);

        if (Utils.isNotEmpty(phoneNumber)) {
            smsDestination.setText(phoneNumber);
        }
    }

    private void initUi() {
        sendButton = (ImageButton) findViewById(R.id.sendButton);
        smsDestination = (EditText) findViewById(R.id.smsDestination);
        smsContent = (EditText) findViewById(R.id.smsContent);
    }

    private void initHandlers() {
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), getString(R.string.sent_smsSent), Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), getString(R.string.sent_genericError), Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), getString(R.string.sent_noService), Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), getString(R.string.sent_noPdu), Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), getString(R.string.sent_radioOff), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), getString(R.string.delivery_ok), Toast.LENGTH_LONG).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), getString(R.string.delivery_fail), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
    }

    public void onSend(View view) {
        final String phone = smsDestination.getText().toString();
        final String message = smsContent.getText().toString();

        if (!valid(phone, message)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(getString(R.string.sms_sendConfirmation));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                sendSms(phone, message);
            }
        });

        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendSms(String phone, String message) {
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveryIntent = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phone, null, message, sentIntent, deliveryIntent);
    }

    private boolean valid(String phone, String message) {
        if (phone == null || phone.isEmpty()) {
            smsDestination.setError(getString(R.string.error_noPhone));
            return false;
        }

        if (message == null || message.isEmpty()) {
            smsContent.setError(getString(R.string.error_noMessage));
            return false;
        }

        return true;
    }

    public void clearInput(View view) {
        smsDestination.setText("");

        clearInputErrors();
    }

    private void clearInputErrors() {
        // clear any possible error messages
        smsDestination.setError(null);
    }

    public void lookup(View view) {
        clearInputErrors();

        Intent intent = new Intent(this, LookupContact.class);
        startActivityForResult(intent, REQUEST_PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                loadValues(intent);
            }
        }
    }
}
