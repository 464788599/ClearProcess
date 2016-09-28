package com.kingdom.test.clearprocess.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kingdom.test.clearprocess.R;
import com.kingdom.test.clearprocess.javabean.WifiInfoBean;

import java.util.List;

/**
 * Created by admin on 2016/9/26.
 */
public class PrivateWifiAdapter extends BaseAdapter {
    Context context;
    List<WifiInfoBean> priviatewifiInfo;

    public PrivateWifiAdapter(Context context, List<WifiInfoBean> priviatewifiInfo) {
        this.context = context;
        this.priviatewifiInfo = priviatewifiInfo;
    }

    @Override
    public int getCount() {
        return priviatewifiInfo.size();
    }

    @Override
    public WifiInfoBean getItem(int position) {
        return priviatewifiInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_listview_wifi, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.wifiIcon = (ImageView) convertView.findViewById(R.id.iv_wifi_icon);
            viewHolder.wifiName = (TextView) convertView.findViewById(R.id.tv_wifi_name);
            convertView.setTag(viewHolder);
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        Log.i("qqww",Math.abs(getItem(position).getLevel())+"");
        int level = Math.abs(getItem(position).getLevel());
       if (level>0&&level<50){
           viewHolder.wifiIcon.setImageResource(R.drawable.wifi_2_level);
       }else if (level>=50&&level<70){
           viewHolder.wifiIcon.setImageResource(R.drawable.wifi_1_level);
       }else if (level>=70&&level<100){
           viewHolder.wifiIcon.setImageResource(R.drawable.wifi_1_level);
       }
        viewHolder.wifiName.setText(getItem(position).getWifiName());
        return convertView;
    }

    class ViewHolder {
        ImageView wifiIcon;
        TextView wifiName;
    }

}
