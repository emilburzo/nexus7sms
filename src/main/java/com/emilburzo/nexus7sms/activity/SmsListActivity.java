package com.emilburzo.nexus7sms.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.adapter.SmsListAdapter;
import com.emilburzo.nexus7sms.misc.Constants;
import com.emilburzo.nexus7sms.model.SmsModel;
import com.emilburzo.nexus7sms.pojo.Sms;
import com.melnykov.fab.FloatingActionButton;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import java.util.ArrayList;
import java.util.List;

public class SmsListActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private List<Sms> msgs = new ArrayList<>();
    private SmsListAdapter adapter;

    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_list);

        listView = (ListView) findViewById(R.id.smsList);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(listView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LookupContactActivity.class);
                startActivity(intent);
            }
        });

        loadMessages();

        adapter = new SmsListAdapter(this, msgs);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sms sms = (Sms) listView.getItemAtPosition(position);

//                Toast.makeText(getApplicationContext(), sms.body, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(SmsListActivity.this, SmsViewActivity.class);
                intent.putExtra(Constants.Intents.PHONE_NUMBER, sms.phone);
                startActivity(intent);

//                Intent intent = new Intent(LookupContactActivity.this, SmsActivity.class);
//                intent.putExtra(Constants.Intents.PHONE_NUMBER, contact.phone);
//                setResult(RESULT_OK, intent);
//                finish();
            }
        });

        checkForNotificationAccess();
    }

    private void checkForNotificationAccess() {
        if (!Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
            Toast.makeText(this, "No notification access", Toast.LENGTH_LONG).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
//            builder.setMessage(getString(R.string.sms_sendConfirmation));
            builder.setMessage("No notification access, change it?"); // todo
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                    startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
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

    private void loadMessages() {
        Realm realm = Realm.getInstance(this);

        // Build the query looking at all users:
        RealmQuery<SmsModel> query = realm.where(SmsModel.class);

// Add query conditions:
//        query.equalTo("name", "John");
//        query.or().equalTo("name", "Peter");
//        query.sor

// Execute the query:
        RealmResults<SmsModel> results = query.findAllSorted("timestamp", false);

        msgs.clear();

        for (SmsModel result : results) {
            if (!phoneNoExists(result.getPhone())) {
                Sms sms = new Sms(result);

                msgs.add(sms);
            }
        }

        realm.close();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
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
    public void onResume() {
        super.onResume();

        loadMessages();
    }

}
