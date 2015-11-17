package com.emilburzo.nexus7sms.activity;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.adapter.SmsViewAdapter;
import com.emilburzo.nexus7sms.manager.ContactsManager;
import com.emilburzo.nexus7sms.manager.SMSManager;
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

    private static final int SMS_MAX_LENGTH = 160;
    private static final int REQUEST_PERMISSIONS = 1;

    private final String TAG = this.getClass().getSimpleName();

    private List<Sms> msgs = new ArrayList<>();
    private SmsViewAdapter adapter;

    private ListView listView;
    private String phoneNo;

    private EditText msgBody;
    private TextView msgLength;

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

        extractMessage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sms_view_activity_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                onDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(getString(R.string.thread_delete_confirm));

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                doDeleteThread();

                Intent intent = new Intent(getApplicationContext(), SmsListActivity.class);
                startActivity(intent);
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

    private void doDeleteThread() {
        // Obtain a Realm instance
        Realm realm = Realm.getInstance(this);

        // All changes to data must happen in a transaction
        realm.beginTransaction();

        // Build the query looking at all users:
        RealmQuery<SmsModel> query = realm.where(SmsModel.class);

        // Add query conditions:
        query.equalTo("phone", phoneNo);

        // Execute the query:
        RealmResults<SmsModel> results = query.findAllSorted("timestamp", true);

        // Delete all matches
        results.clear();

        // commit
        realm.commitTransaction();
    }

    private void setTitleAndColor() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Utils.getColor(phoneNo)));
            getSupportActionBar().setTitle(ContactsManager.getContactName(this, phoneNo));
        }
    }

    private void initUi() {
        listView = (ListView) findViewById(R.id.smsList);
        msgBody = (EditText) findViewById(R.id.msgBody);
        msgLength = (TextView) findViewById(R.id.msgLength);

        adapter = new SmsViewAdapter(this, msgs);
        listView.setAdapter(adapter);
    }

    private void extractMessage() {
        Intent intent = getIntent();
        String msg = intent.getStringExtra(Constants.Intents.MESSAGE);

        msgBody.setText(msg);

        Utils.debug(TAG, String.format("Found message: '%s'", msg));
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

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("SMS", sms.body);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), getString(R.string.msg_copied_to_clipboard), Toast.LENGTH_LONG).show();
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
        if (phoneNo == null) {
            return;
        }

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

        LocalBroadcastManager.getInstance(this).registerReceiver(msgReceiver, new IntentFilter(Constants.IntentActions.MESSAGES_CHANGED));

        loadMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(msgReceiver);
    }

    public void doSendSms(View view) {
        if (!Utils.hasSmsPermission(this)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_PERMISSIONS);
            return;
        }

        final String message = msgBody.getText().toString();

        if (message.trim().isEmpty()) {
            return;
        }

        // realm db persist
        String uuid = SMSManager.persistSmsOut(this, phoneNo, message);

        // GSM send
        sendSms(phoneNo, message, uuid);

        // play sound
        playSound();

        // clear message
        msgBody.setText("");

        // refresh list
        loadMessages();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doSendSms(null);
                }

                return;
            }
        }
    }

    private void playSound() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        boolean play = sp.getBoolean(Constants.Settings.OUTGOING_SMS_SOUND, true);

        if (play) {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.chime);
            mp.start();
        }
    }

    private void sendSms(String phone, String message, String uuid) {
        Intent sendSms = new Intent(Constants.Intents.SEND_SMS);

        sendSms.putExtra(Constants.IntentExtras.PHONE, phone);
        sendSms.putExtra(Constants.IntentExtras.MESSAGE, message);
        sendSms.putExtra(Constants.IntentExtras.UUID, uuid);

        LocalBroadcastManager.getInstance(this).sendBroadcast(sendSms);
    }

    private BroadcastReceiver msgReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            loadMessages();
        }
    };


}
