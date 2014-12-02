package com.emilburzo.nexus7sms;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SmsActivity extends ActionBarActivity {

    private Button button;
    private EditText smsDestination;
    private EditText smsContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_activity);

        button = (Button) findViewById(R.id.button);
        smsDestination = (EditText) findViewById(R.id.smsDestination);
        smsContent = (EditText) findViewById(R.id.smsContent);
    }

    public void sendSms(View view) {
        SmsManager sms = SmsManager.getDefault();

        sms.sendTextMessage(smsDestination.getText().toString(), null, smsContent.getText().toString(), null, null);
    }
}
