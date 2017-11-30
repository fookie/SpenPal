package com.evelin.spenpal;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;
import java.util.Map;

/**
 * Created by PC on 2016/4/12.
 */
public class SettingsListAdapter extends BaseAdapter {

    private Context context;
    private List<Map<String, Object>> settingsList;
    private LayoutInflater inflater;

    public SettingsListAdapter(Context context, List<Map<String, Object>> settingsList) {
        super();
        this.context = context;
        this.settingsList = settingsList;
    }

    @Override
    public int getCount() {
        return settingsList.size();
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
            convertView = inflater.inflate(R.layout.settingslist_item, null);
            viewHolder = new ViewHolder();
            viewHolder.labelText = (TextView) convertView.findViewById(R.id.labelText);
            viewHolder.gravatarView = (ImageView) convertView.findViewById(R.id.avatarView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String label = settingsList.get(position).get("label").toString(); // connect the key string to the Widget
        Bitmap gravatar = (Bitmap) (settingsList.get(position).get("icon"));
        viewHolder.labelText.setText(label);
        if(gravatar!=null){
            viewHolder.gravatarView.setImageBitmap(gravatar);
        }
        return convertView;
    }

    static class ViewHolder {
        public TextView labelText;
        public ImageView gravatarView;
    }

}
