package com.emilburzo.nexus7sms.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.adapter.ContactsAdapter;
import com.emilburzo.nexus7sms.misc.Constants;
import com.emilburzo.nexus7sms.misc.Utils;
import com.emilburzo.nexus7sms.pojo.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private List<Contact> contacts = new ArrayList<>();
    private ContactsAdapter adapter;

    private ListView listView;
    private EditText search;

    private String sharedText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initUi();

        initHandlers();

        loadContacts();

        loadMessageFromIntent();
    }

    private void initUi() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF0099CC")));
        getSupportActionBar().setTitle(getString(R.string.contacts_label));

        // list
        listView = (ListView) findViewById(R.id.list);
        adapter = new ContactsAdapter(this, contacts);
        listView.setAdapter(adapter);

        // search
        search = (EditText) findViewById(R.id.search);
    }

    private void initHandlers() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = (Contact) listView.getItemAtPosition(position);

                Intent intent = new Intent(ContactsActivity.this, SmsViewActivity.class);
                intent.putExtra(Constants.Intents.PHONE_NUMBER, contact.phone);

                if (sharedText != null) {
                    intent.putExtra(Constants.Intents.MESSAGE, sharedText);
                }

                startActivity(intent);
            }
        });

        search.addTextChangedListener(new ContactsSearch());
    }

    private void loadMessageFromIntent() {
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }
    }

    private void loadContacts() {
        contacts.clear();

        // required fields
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY};

        // filtering query
        String selection = "";
        selection += ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?";
        selection += " OR ";
        selection += ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?";
        selection += " OR ";
        selection += "replace(replace(replace(replace(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ", '+', ''), '-', ''), '(', ''), ')', '')" + " LIKE ?";

        // filtering args
        String selectionArg = "%" + search.getText() + "%";
        String[] selectionArgs = new String[]{selectionArg, selectionArg, selectionArg};

        // order by
        String orderBy = "lower(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC, " + ContactsContract.CommonDataKinds.Phone.TYPE + " ASC";

        // query
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, selectionArgs, orderBy);

        if (phones != null) {
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int phoneType = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                String lookupKey = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)); // todo decide if we need this

                Utils.debug(TAG, String.format("Found name: '%s', phone number: '%s', type: '%s', key: '%s'", name, phoneNumber, phoneType, lookupKey));

                Contact contact = new Contact(name, phoneNumber, phoneType);
                contacts.add(contact);
            }

            phones.close();
        }

        // are we searching for a number?
        // if yes, give the user an option to send SMS directly to that number
        // (when there is no contact added)
        String search = this.search.getText().toString();

        if (PhoneNumberUtils.isWellFormedSmsAddress(search)) {
            Contact contact = new Contact(String.format("Send message to %s", search), search, null);
            contacts.add(0, contact);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadContacts();
    }

    public void doClearSearch(View view) {
        search.setText("");

        loadContacts();
    }

    private class ContactsSearch implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            loadContacts();
        }
    }
}
