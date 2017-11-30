package com.evelin.spenpal;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.evelin.spenpal.R;

public class FriendsAdapter extends BaseAdapter {

    private Context context;
    private List<String> mAppList;
    private Map<String, Bitmap> avatarMap;

    public FriendsAdapter(Context context, List<String> mAppList, Map<String, Bitmap> aMap) {
        this.context = context;
        this.mAppList = mAppList;
        this.avatarMap = aMap;
    }

    @Override
    public int getCount() {
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_list_app, null);
            new ViewHolder(convertView);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        String username = mAppList.get(position);
        Bitmap avatar = avatarMap.get(username);
        holder.iv_icon.setImageBitmap(avatar);
        holder.tv_name.setText(username);
        return convertView;
    }

    class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;

        public ViewHolder(View view) {
            iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            tv_name = (TextView) view.findViewById(R.id.tv_name);
            view.setTag(this);
        }
    }
}
