package com.emilburzo.nexus7sms.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.adapter.SmsViewAdapter;
import com.emilburzo.nexus7sms.misc.Constants;
import com.emilburzo.nexus7sms.misc.Utils;
import com.emilburzo.nexus7sms.model.SmsModel;
import com.emilburzo.nexus7sms.pojo.Sms;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmsViewActivity extends AppCompatActivity {

    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";

    private static final int SMS_MAX_LENGTH = 160;

    private final String TAG = this.getClass().getSimpleName();

    private List<Sms> msgs = new ArrayList<>();
    private SmsViewAdapter adapter;

    private ListView listView;
    private String phoneNo;

    private EditText msgBody;
    private TextView msgLength;

    private Map<String, SendBroadcastReceiver> sendRecvs = new HashMap<>();
    private Map<String, DeliveryBroadcastReceiver> deliveryRecvs = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_view);

        extractPhoneNumber();

        setTitleAndColor();

        initUi();

        initHandlers();

        loadMessages();

        initHandlers();
    }

    private void setTitleAndColor() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Utils.getColor(phoneNo)));
            getSupportActionBar().setTitle(Utils.getContactName(this, phoneNo));
        }
    }

    private void initUi() {
        listView = (ListView) findViewById(R.id.smsList);
        msgBody = (EditText) findViewById(R.id.msgBody);
        msgLength = (TextView) findViewById(R.id.msgLength);

        adapter = new SmsViewAdapter(this, msgs);
        listView.setAdapter(adapter);
    }

    private void extractPhoneNumber() {
        Intent intent = getIntent();
        phoneNo = intent.getStringExtra(Constants.Intents.PHONE_NUMBER);

        Utils.debug(TAG, String.format("Found phone number: '%s'", phoneNo));
    }

    private void initHandlers() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sms sms = (Sms) listView.getItemAtPosition(position);

                // todo
                Toast.makeText(getApplicationContext(), sms.body, Toast.LENGTH_LONG).show();
            }
        });

        msgBody.addTextChangedListener(new TextWatcher() {
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

    private void updateMessageLength() {
        msgLength.setText(getMessageLength());
    }

    private String getMessageLength() {
        return String.format("%s/%s", msgBody.length(), SMS_MAX_LENGTH);
    }

    private void loadMessages() {
        Realm realm = Realm.getInstance(this);

        // Build the query looking at all users:
        RealmQuery<SmsModel> query = realm.where(SmsModel.class);

        // Add query conditions:
        query.equalTo("phone", phoneNo);

        // Execute the query:
        RealmResults<SmsModel> results = query.findAllSorted("timestamp", true);

        msgs.clear();

        for (SmsModel result : results) {
            Sms sms = new Sms(result);

            msgs.add(sms);
        }

        realm.close();

        adapter.notifyDataSetChanged();

        scrollMessageListToBottom();
    }

    private void scrollMessageListToBottom() {
        listView.setSelection(adapter.getCount() - 1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(msgReceiver, new IntentFilter(Constants.IntentActions.MSG_RECEIVED));

        loadMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(msgReceiver);
    }

    public void doSendSms(View view) {
        final String message = msgBody.getText().toString();

        if (message.trim().isEmpty()) {
            return;
        }

        // realm db persist
        String uuid = Utils.persistSmsOut(this, phoneNo, message);

        // GSM send
        sendSms(phoneNo, message, uuid);

        // clear message
        msgBody.setText("");

        // refresh list
        loadMessages();
    }

    private void sendSms(String phone, String message, String uuid) {
        // status receivers
        SendBroadcastReceiver sendRecv = new SendBroadcastReceiver(uuid);
        DeliveryBroadcastReceiver deliveryRecv = new DeliveryBroadcastReceiver(uuid);

        sendRecvs.put(uuid, sendRecv);
        deliveryRecvs.put(uuid, deliveryRecv);

        registerReceiver(sendRecv, new IntentFilter(SENT));
        registerReceiver(deliveryRecv, new IntentFilter(DELIVERED));

        // sms send pending intents
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveryIntent = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        // actual sms send
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phone, null, message, sentIntent, deliveryIntent);
    }

    private BroadcastReceiver msgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadMessages();
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
            Utils.debug(TAG, "SMS fail");

            Utils.markSmsNotSent(getApplicationContext(), uuid);

            loadMessages();
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

            Utils.markSmsDelivered(getApplicationContext(), uuid);

            loadMessages();
        }

        private void onSmsNotDelivered() {
            Utils.debug(TAG, "SMS not delivered");
        }
    }
}
