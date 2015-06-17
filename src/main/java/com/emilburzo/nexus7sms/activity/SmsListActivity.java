package com.emilburzo.nexus7sms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.adapter.SmsListAdapter;
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
                SmsModel sms = (SmsModel) listView.getItemAtPosition(position);

                Toast.makeText(getApplicationContext(), sms.getBody(), Toast.LENGTH_LONG).show();

//                Intent intent = new Intent(LookupContactActivity.this, SmsActivity.class);
//                intent.putExtra(Constants.Intents.PHONE_NUMBER, contact.phone);
//                setResult(RESULT_OK, intent);
//                finish();
            }
        });
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
