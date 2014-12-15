package com.emilburzo.nexus7sms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class SmsActivity extends ActionBarActivity {

    private ImageButton sendButton;
    private EditText smsDestination;
    private EditText smsContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_activity);

        sendButton = (ImageButton) findViewById(R.id.sendButton);
        smsDestination = (EditText) findViewById(R.id.smsDestination);
        smsContent = (EditText) findViewById(R.id.smsContent);
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
        SmsManager sms = SmsManager.getDefault();

        sms.sendTextMessage(phone, null, message, null, null);

        Toast.makeText(this, getString(R.string.sms_sent), Toast.LENGTH_LONG).show();
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
}
