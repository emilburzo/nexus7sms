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

        int type = getItemViewType(position);

        if (vi == null) {
            if (type == 0) {
                vi = inflater.inflate(R.layout.sms_view_item_in, null);
            } else {
                vi = inflater.inflate(R.layout.sms_view_item_out, null);
            }
        }

        Sms sms = list.get(position);

        ImageView picture = (ImageView) vi.findViewById(R.id.picture);

        if (sms.type.equals(Constants.SmsTypes.IN)) {
            Bitmap photo = Utils.getContactPhoto(context, sms.phone);

            if (photo == null) {
                picture.setImageDrawable(Utils.getContactTextPhoto(context, sms.phone));
            } else {
                picture.setImageBitmap(photo);
            }
        } else {
            picture.setImageBitmap(Utils.getProfilePhoto(context));
        }

        TextView body = (TextView) vi.findViewById(R.id.body);
        body.setText(sms.body);

        TextView date = (TextView) vi.findViewById(R.id.date);
        date.setText(Utils.formatDate(sms.timestamp));

        return vi;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Sms sms = list.get(position);

        if (sms.type.equals(Constants.SmsTypes.IN)) {
            return 0;
        } else {
            return 1;
        }
    }
}
