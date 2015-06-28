package com.emilburzo.nexus7sms.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.misc.Utils;
import com.emilburzo.nexus7sms.pojo.Contact;

import java.util.List;

public class ContactsAdapter extends BaseAdapter {

    private final Context context;
    private final List<Contact> contactList;
    private static LayoutInflater inflater = null;

    public ContactsAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return contactList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        if (vi == null) {
            vi = inflater.inflate(R.layout.contacts_item, null);
        }

        Contact contact = contactList.get(position);

        // picture
        ImageView picture = (ImageView) vi.findViewById(R.id.picture);
        Bitmap photo = Utils.getContactPhoto(context, contact.phone);

        if (photo == null) {
            picture.setImageDrawable(Utils.getContactTextPhoto(context, contact.phone));
        } else {
            picture.setImageBitmap(photo);
        }

        // name
        TextView name = (TextView) vi.findViewById(R.id.contactName);
        name.setText(contact.name);

        // phone
        TextView phone = (TextView) vi.findViewById(R.id.contactPhone);
        if (contact.phoneType == null) {
            phone.setText(String.format("%s", contact.phone));
        } else {
            phone.setText(String.format("%s (%s)", contact.phone, contact.phoneType));
        }

        return vi;
    }
}
