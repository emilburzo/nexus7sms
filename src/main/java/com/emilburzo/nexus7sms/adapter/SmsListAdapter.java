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
import com.emilburzo.nexus7sms.pojo.Sms;

import java.util.List;

public class SmsListAdapter extends BaseAdapter {

    private final List<Sms> list;
    private final Context context;

    private static LayoutInflater inflater = null;

    public SmsListAdapter(Context context, List<Sms> list) {
        this.list = list;
        this.context = context;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        if (vi == null) {
            vi = inflater.inflate(R.layout.sms_list_item, null);
        }

        Sms sms = list.get(position);

        ImageView picture = (ImageView) vi.findViewById(R.id.picture);

        Bitmap photo = Utils.getContactPhoto(context, sms.phone);

        if (photo == null) {
            picture.setImageDrawable(Utils.getContactTextPhoto(context, sms.phone));
        } else {
            picture.setImageBitmap(photo);
        }

        TextView name = (TextView) vi.findViewById(R.id.phone);
        name.setText(Utils.getContactName(context, sms.phone, true));

        TextView phone = (TextView) vi.findViewById(R.id.body);
        phone.setText(sms.body);

        TextView date = (TextView) vi.findViewById(R.id.date);
        date.setText(Utils.formatDate(sms.timestamp));

        return vi;
    }
}
