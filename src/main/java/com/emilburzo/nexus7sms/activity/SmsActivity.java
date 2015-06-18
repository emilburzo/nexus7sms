package com.emilburzo.nexus7sms.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.misc.Utils;

public class SmsActivity extends AppCompatActivity {

    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";

    private static final int REQUEST_PICK_CONTACT = 1;
    private static final int SMS_MAX_LENGTH = 160;

    private final String TAG = this.getClass().getSimpleName();

    private ImageButton sendButton;
    private EditText smsDestination;
    private EditText smsContent;
    private TextView msgLength;

    private BroadcastReceiver sendBroadcastReceiver;
    private BroadcastReceiver deliveryBroadcastReceiver;
    private boolean receiversRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sms_activity);

        initUi();

        initHandlers();

        loadMessageFromIntent();
    }

    private void loadMessageFromIntent() {
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        }
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);

        if (sharedText != null) {
            // Update UI to reflect text being shared
            smsContent.setText(sharedText);
        }
    }

    private void initUi() {
        sendButton = (ImageButton) findViewById(R.id.sendButton);
        smsDestination = (EditText) findViewById(R.id.smsDestination);
        smsContent = (EditText) findViewById(R.id.smsContent);
        msgLength = (TextView) findViewById(R.id.msgLength);
    }

    private void initHandlers() {
        initBroadcastReceivers();

        registerReceivers();

        smsContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateMessageLength();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void initBroadcastReceivers() {
        // send BR
        if (sendBroadcastReceiver == null) {

        }

        // delivered BR
        if (deliveryBroadcastReceiver == null) {

        }
    }

    private void registerReceivers() {
        if (receiversRegistered) {
            return;
        }

        registerReceiver(sendBroadcastReceiver, new IntentFilter(SENT));
        registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED));

        receiversRegistered = true;
    }

    private void unregisterReceivers() {
        if (!receiversRegistered) {
            return;
        }

        unregisterReceiver(sendBroadcastReceiver);
        unregisterReceiver(deliveryBroadcastReceiver);

        receiversRegistered = false;
    }

    private void updateMessageLength() {
        msgLength.setText(getMessageLength());
    }

    private String getMessageLength() {
        return String.format("%s/%s", smsContent.length(), SMS_MAX_LENGTH);
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

        if (message.length() > SMS_MAX_LENGTH) {
            smsContent.setError(getString(R.string.message_too_long));
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

        Intent intent = new Intent(this, LookupContactActivity.class);
        startActivityForResult(intent, REQUEST_PICK_CONTACT);
    }


    private void loadContactPhoneNumber(Intent intent) {
        String phoneNumber = Utils.getPhoneNumber(intent);

        if (Utils.isNotEmpty(phoneNumber)) {
            smsDestination.setText(phoneNumber);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                loadContactPhoneNumber(intent);
            }
        }
    }
}
