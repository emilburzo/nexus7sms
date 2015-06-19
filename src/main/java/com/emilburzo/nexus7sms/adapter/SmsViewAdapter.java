package com.emilburzo.nexus7sms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.emilburzo.nexus7sms.R;
import com.emilburzo.nexus7sms.misc.Constants;
import com.emilburzo.nexus7sms.misc.Utils;
import com.emilburzo.nexus7sms.pojo.Sms;

import java.util.List;

public class SmsViewAdapter extends BaseAdapter {

    private Context context;
    private final List<Sms> list;
    private static LayoutInflater inflater = null;

    public SmsViewAdapter(Context context, List<Sms> list) {
        this.context = context;
        this.list = list;

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
            vi = inflater.inflate(R.layout.sms_view_item, null);
        }

        Sms sms = list.get(position);

        TextView name = (TextView) vi.findViewById(R.id.phone);
        name.setText(Utils.getContactName(context, sms.phone));

        TextView phone = (TextView) vi.findViewById(R.id.body);
        phone.setText(sms.body);

        if (sms.type.equals(Constants.SmsTypes.IN)) {
            name.setGravity(View.TEXT_ALIGNMENT_VIEW_END);
            phone.setGravity(View.TEXT_ALIGNMENT_VIEW_END);
        } else {
            name.setGravity(View.TEXT_ALIGNMENT_VIEW_START);
            phone.setGravity(View.TEXT_ALIGNMENT_VIEW_START);
        }

        return vi;
    }
}
