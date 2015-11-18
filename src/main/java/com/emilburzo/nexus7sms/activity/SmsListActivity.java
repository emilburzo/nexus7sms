package com.emilburzo.nexus7sms.activity;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.adapter.SmsListAdapter;
import com.emilburzo.nexus7sms.misc.Constants;
import com.emilburzo.nexus7sms.misc.Utils;
import com.emilburzo.nexus7sms.model.SmsModel;
import com.emilburzo.nexus7sms.pojo.Sms;
import com.emilburzo.nexus7sms.service.SmsNotificationListener;
import com.emilburzo.nexus7sms.service.SmsService;
import com.melnykov.fab.FloatingActionButton;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import java.util.ArrayList;
import java.util.List;

public class SmsListActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 0;

    private final String TAG = this.getClass().getSimpleName();

    private List<Sms> msgs = new ArrayList<>();

    private ListView listView;
    private SmsListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sms_list);

        initDefaultPreferences();

        startBackendService();

        initUi();

        initHandlers();

        checkForPermissions();

        checkForNotificationAccess();

        loadMessages();
    }

    private void checkForPermissions() {
        List<String> perms = new ArrayList<>();

        if (!Utils.hasContactsPermission(this)) {
            perms.add(Manifest.permission.READ_CONTACTS);
        }

        if (!Utils.hasSmsPermission(this)) {
            perms.add(Manifest.permission.SEND_SMS);
        }

        if (perms.isEmpty()) {
            return;
        }

        String[] permReq = new String[perms.size()];
        for (int i = 0; i < perms.size(); i++) {
            permReq[i] = perms.get(i);
        }

        ActivityCompat.requestPermissions(this, permReq, REQUEST_PERMISSIONS);
    }

    private void initDefaultPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    private void startBackendService() {
        Intent i = new Intent(this, SmsService.class);
        startService(i);
    }

    private void initUi() {
        // list
        listView = (ListView) findViewById(R.id.smsList);
        adapter = new SmsListAdapter(this, msgs);
        listView.setAdapter(adapter);

        // fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(listView);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ContactsActivity.class);
                startActivity(intent);
            }
        });

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF0099CC")));
    }

    private void initHandlers() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sms sms = (Sms) listView.getItemAtPosition(position);

                Intent intent = new Intent(SmsListActivity.this, SmsViewActivity.class);
                intent.putExtra(Constants.Intents.PHONE_NUMBER, sms.phone);
                startActivity(intent);
            }
        });
    }

    private void checkForNotificationAccess() {
        if (!Utils.isPackageInstalled(this, SmsNotificationListener.PACKAGE_BASIC_SMS_RECEIVER)) {
            return;
        }

        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, Constants.AndroidSecure.ENABLED_NOTIFICATION_LISTENERS);
        String packageName = getPackageName();

        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            builder.setMessage(getString(R.string.notification_permision_prompt));

            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                    startActivity(new Intent(Constants.AndroidSecure.NOTIFICATION_LISTENER_SETTINGS));
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sms_list_activity_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadMessages();
                }

                return;
            }
        }
    }

    private void openSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    private void loadMessages() {
        Realm realm = Realm.getInstance(this);

        RealmQuery<SmsModel> query = realm.where(SmsModel.class);
        RealmResults<SmsModel> results = query.findAllSorted(Constants.SmsFields.TIMESTAMP, false);

        msgs.clear();

        for (SmsModel result : results) {
            if (!phoneNoExists(result.getPhone())) {
                Sms sms = new Sms(result);

                msgs.add(sms);
            }
        }

        realm.close();

        adapter.notifyDataSetChanged();
    }

    private boolean phoneNoExists(String phone) {
        for (Sms msg : msgs) {
            if (msg.phone.equalsIgnoreCase(phone)) {
                return true;
            }
        }

        return false;
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

    private BroadcastReceiver msgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadMessages();
        }
    };
}
