package com.evelin.spenpal;

/**
 * Created by PC on 2016/2/13.
 */

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.evelin.spenpal.Network.ConnectorController;

import org.w3c.dom.Text;

public class SpendListTimeLineAdapter extends BaseAdapter {

    private Context context;
    private List<Map<String, Object>> spendingList;
    private LayoutInflater inflater;

    public SpendListTimeLineAdapter(Context context, List<Map<String, Object>> spendinglist) {
        super();
        this.context = context;
        this.spendingList = spendinglist;
    }

    @Override
    public int getCount() {
        return spendingList.size();
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
            if (spendingList.get(position).get("reverse") != null) {
                if (spendingList.get(position).get("reverse").equals(1)) {
                    convertView = inflater.inflate(R.layout.spendlist_item_reverse, null);
                } else if (spendingList.get(position).get("reverse").equals(0)) {
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
            viewHolder.sharedView = (ImageView) convertView.findViewById(R.id.shared);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String category = spendingList.get(position).get("category").toString(); // connect the key string to the Widget
        float amount = (float) spendingList.get(position).get("amount");
        String date = (String) spendingList.get(position).get("date");
        boolean shared = (boolean) spendingList.get(position).get("shared");

        viewHolder.categoryText.setText(category);
        viewHolder.amountText.setText(String.valueOf(amount) + "￥");
        viewHolder.dateText.setText(date);


        if (shared) {
            viewHolder.sharedView.setVisibility(View.VISIBLE);
        }

        String username = (String) spendingList.get(position).get("username");
        if (username != null && !username.equalsIgnoreCase("")) {
            viewHolder.usernameText.setText(username);
        }
        viewHolder.avatar = (ImageView) convertView.findViewById(R.id.spendImage);
        ConnectorController.getController().setAvatar((String) spendingList.get(position).get("uname"), viewHolder.avatar);
        return convertView;
    }

    public String formatDate(int dateInt) {
        int day = dateInt % 100;
        int month = ((dateInt - day) % 10000) / 100;
        int year = (dateInt - day - month) / 10000;
        return year + "/" + month + "/" + day;
    }

    static class ViewHolder {
        public ImageView avatar;
        public TextView usernameText;
        public TextView dateText;
        public TextView amountText;
        public TextView categoryText;
        public ImageView sharedView;
    }
}

