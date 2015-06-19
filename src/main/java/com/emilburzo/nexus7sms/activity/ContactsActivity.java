package com.emilburzo.nexus7sms.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
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
    private ContactsAdapter contactsAdapter = new ContactsAdapter(this, contacts);

    private ListView listView;
    private EditText contactsSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initUi();

        initHandlers();

        loadContacts();
    }

    private void initUi() {
        // list
        listView = (ListView) findViewById(R.id.contactsList);
        listView.setAdapter(contactsAdapter);

        // search
        contactsSearch = (EditText) findViewById(R.id.contactsSearch);
        contactsSearch.addTextChangedListener(new ContactsSearch());
    }

    private void initHandlers() {
        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = (Contact) listView.getItemAtPosition(position);

                Intent intent = new Intent(ContactsActivity.this, SmsViewActivity.class);
                intent.putExtra(Constants.Intents.PHONE_NUMBER, contact.phone);
                startActivity(intent);
            }
        });
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
        String selectionArg = "%" + contactsSearch.getText() + "%";
        String[] selectionArgs = new String[]{selectionArg, selectionArg, selectionArg};

        // order by
        String orderBy = "lower(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC, " + ContactsContract.CommonDataKinds.Phone.TYPE + " ASC";

        // query
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, selectionArgs, orderBy);

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

        if (contactsAdapter != null) {
            contactsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        loadContacts();
    }

    public void doClearSearch(View view) {
        contactsSearch.setText("");

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
