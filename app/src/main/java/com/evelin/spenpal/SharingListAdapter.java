package com.evelin.spenpal;

/**
 * Created by PC on 2016/2/13.
 */

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SharingListAdapter extends BaseAdapter {

    private Context context;
    private List<Map<String, Object>> sharingList;
    private LayoutInflater inflater;
    private Map<String, Bitmap> avatarMap;

    public SharingListAdapter(Context context, List<Map<String, Object>> spendinglist, Map<String, Bitmap> aMap) {
        super();
        this.context = context;
        this.sharingList = spendinglist;
        this.avatarMap=aMap;
    }

    @Override
    public int getCount() {
        return sharingList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            inflater = LayoutInflater.from(parent.getContext());
            if (sharingList.get(position).get("reverse") != null) {
                if (sharingList.get(position).get("reverse").equals(1)) {
                    convertView = inflater.inflate(R.layout.spendlist_item_reverse, null);
                } else if (sharingList.get(position).get("reverse").equals(0)){
                    convertView = inflater.inflate(R.layout.spendlist_item_normal, null);
                }
            } else {
                convertView = inflater.inflate(R.layout.spendlist_item_old, null);
            }
            viewHolder = new ViewHolder();

            viewHolder.categoryText = (TextView) convertView.findViewById(R.id.categoryText);
            viewHolder.amountText = (TextView) convertView.findViewById(R.id.amountText);
            viewHolder.dateText = (TextView) convertView.findViewById(R.id.timestamp);
            viewHolder.usernameText = (TextView) convertView.findViewById(R.id.usernameText);
            viewHolder.avatarView = (ImageView) convertView.findViewById(R.id.spendImage);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String category = sharingList.get(position).get("category").toString(); // connect the key string to the Widget
        float amount = (float) sharingList.get(position).get("amount");
        String date = (String) sharingList.get(position).get("date");

        viewHolder.categoryText.setText(category);
        viewHolder.amountText.setText(String.valueOf(amount) + "￥");
        viewHolder.dateText.setText(date);


        String username = (String) sharingList.get(position).get("username");
        Bitmap avatar = avatarMap.get(username);
        if (username != null && !username.equalsIgnoreCase("")) {
            viewHolder.usernameText.setText(username);
            viewHolder.avatarView.setImageBitmap(avatar);
        }

        return convertView;
    }


    static class ViewHolder {
        public TextView usernameText;
        public TextView dateText;
        public TextView amountText;
        public TextView categoryText;
        public ImageView avatarView;
    }
}

