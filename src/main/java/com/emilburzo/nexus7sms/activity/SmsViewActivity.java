package com.emilburzo.nexus7sms.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import java.util.List;

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

    private BroadcastReceiver sendBroadcastReceiver;
    private BroadcastReceiver deliveryBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_view);

        extractPhoneNumber();

        setTitleAndColor();

        initUi();

        initHandlers();

        Log.i(TAG, String.format("Found phone number: '%s'", phoneNo));


        loadMessages();

        initHandlers();
    }

    private void setTitleAndColor() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(Utils.getColor(phoneNo))));
        getSupportActionBar().setTitle(Utils.getContactName(this, phoneNo));
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
    }

    private void initHandlers() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sms sms = (Sms) listView.getItemAtPosition(position);

                Toast.makeText(getApplicationContext(), sms.body, Toast.LENGTH_LONG).show();

//                Intent intent = new Intent(LookupContactActivity.this, SmsActivity.class);
//                intent.putExtra(Constants.Intents.PHONE_NUMBER, contact.phone);
//                setResult(RESULT_OK, intent);
//                finish();
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

        //////// todo only when sending?
        sendBroadcastReceiver = new BroadcastReceiver() {
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
        };

        deliveryBroadcastReceiver = new BroadcastReceiver() {
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
        };
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
        query.equalTo("phone", phoneNo); // todo fixme
//        query.or().equalTo("name", "Peter");
//        query.sor

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

//    @Override
//    public void onResume() {
//        super.onResume();
//
//        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                System.out.println("SmsViewActivity.onReceive");
//                Log.i(TAG, "Local broadcast received");
//
//                loadMessages();
//            }
//        }, new IntentFilter("com.emilburzo.nexus7sms.Msg"));
//
//        loadMessages();
//    }

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
//        final String phone = smsDestination.getText().toString();
        final String message = msgBody.getText().toString();

//        if (!valid(phone, message)) {
//            return;
//        }

        // GSM send
        sendSms(phoneNo, message);

        // realm db persist
        Utils.persistSmsOut(this, phoneNo, message);

        // clear message
        msgBody.setText("");

        // refresh list
        loadMessages();
    }

    private void sendSms(String phone, String message) {
        // todo
//        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
//        PendingIntent deliveryIntent = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
//
//        SmsManager sms = SmsManager.getDefault();
//        sms.sendTextMessage(phone, null, message, sentIntent, deliveryIntent);
    }

    private BroadcastReceiver msgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadMessages();
        }
    };
}
