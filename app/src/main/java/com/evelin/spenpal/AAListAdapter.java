package com.evelin.spenpal;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.evelin.spenpal.Network.ConnectorController;
import com.evelin.spenpal.Network.SSLConnector;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by PC on 2016/5/11.
 */
public class AAListAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, Object>> friendslist;
    private LayoutInflater inflater;
    private Map<String, Bitmap> avatarMap;

    public AAListAdapter(Context context, List<Map<String, Object>> friendslist, Map<String, Bitmap> aMap) {
        super();
        this.context = context;
        this.friendslist = friendslist;
        this.avatarMap = aMap;

    }

    @Override
    public int getCount() {
        return friendslist.size();
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
            convertView = inflater.inflate(R.layout.aalist_item, null);
            viewHolder = new ViewHolder();
            viewHolder.nameText = (TextView) convertView.findViewById(R.id.friendNameView);
            viewHolder.gravatarView = (ImageView) convertView.findViewById(R.id.smallHeadView);
            viewHolder.paidText = (TextView) convertView.findViewById(R.id.friendPaidTextView);
            viewHolder.resultText = (TextView) convertView.findViewById(R.id.resultTextView);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        DecimalFormat formatter = new DecimalFormat("0.00");
        String name = friendslist.get(position).get("name").toString(); // connect the key string to the Widget
        String paid = formatter.format((float) friendslist.get(position).get("paid")) + "$";
        String result = "";
        if (Float.parseFloat((String) friendslist.get(position).get("result")) != 0) {
            result = "You should pay " + friendslist.get(position).get("result") + "$ to " + name;
        }
        viewHolder.nameText.setText(name);
        viewHolder.paidText.setText(paid);
        viewHolder.resultText.setText(result);
        viewHolder.gravatarView.setImageBitmap(avatarMap.get(name));

        return convertView;
    }

    static class ViewHolder {
        public TextView nameText;
        public ImageView gravatarView;
        public TextView paidText;
        public TextView resultText;
    }
}
