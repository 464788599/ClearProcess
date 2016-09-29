package com.kingdom.test.clearprocess.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kingdom.test.clearprocess.R;
import com.kingdom.test.clearprocess.javabean.WifiInfoBean;

import java.util.List;

/**
 * Created by admin on 2016/9/26.
 */
public class PublicWifiAdapter extends BaseAdapter {
    Context context;
    List<WifiInfoBean> publicwifiInfo;
    public PublicWifiAdapter(Context context, List<WifiInfoBean> publicwifiInfo) {
        this.context=context;
        this.publicwifiInfo=publicwifiInfo;
    }

    @Override
    public int getCount() {
        return publicwifiInfo.size();
    }

    @Override
    public WifiInfoBean getItem(int position) {
        return publicwifiInfo.get(position);
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
            viewHolder.ivConnented = (ImageView) convertView.findViewById(R.id.iv_select);
            viewHolder.proConnenting = (ProgressBar) convertView.findViewById(R.id.pro_connect);
            viewHolder.tvConnected = (TextView)convertView.findViewById(R.id.tv_connented);
            convertView.setTag(viewHolder);
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        Log.i("qqww",Math.abs(getItem(position).getLevel())+"");
        int level = Math.abs(getItem(position).getLevel());
        if (level==0){
            viewHolder.wifiIcon.setImageResource(R.drawable.wifi_no_lock_0_level);
        }else if (level==1){
            viewHolder.wifiIcon.setImageResource(R.drawable.wifi_no_lock_1_level);
        }else if (level==2){
            viewHolder.wifiIcon.setImageResource(R.drawable.wifi_no_lock_2_level);
        }else if (level==3){
            viewHolder.wifiIcon.setImageResource(R.drawable.wifi_no_lock_3_level);
        }
        viewHolder.wifiName.setText(getItem(position).getWifiName());

        //正在连接
        if (publicwifiInfo.get(position).isConnected()==false&&publicwifiInfo.get(position).isConnecting()==true){
            viewHolder.proConnenting.setVisibility(View.VISIBLE);
            viewHolder.ivConnented.setVisibility(View.GONE);
            viewHolder.tvConnected.setVisibility(View.GONE);
        }
        //连接完成
        if (publicwifiInfo.get(position).isConnected()==true&&publicwifiInfo.get(position).isConnecting()==false){
            viewHolder.proConnenting.setVisibility(View.GONE);
            viewHolder.ivConnented.setVisibility(View.VISIBLE);
            viewHolder.tvConnected.setVisibility(View.VISIBLE);
            viewHolder.tvConnected.setText("(已连接)");
        }

        //没有连接
        if (publicwifiInfo.get(position).isConnected()==false&&publicwifiInfo.get(position).isConnecting()==false){
            viewHolder.proConnenting.setVisibility(View.GONE);
            viewHolder.ivConnented.setVisibility(View.GONE);
            //如果该WIFi已经配置过
            if (publicwifiInfo.get(position).isSave()==true){
                viewHolder.tvConnected.setVisibility(View.VISIBLE);
                viewHolder.tvConnected.setText("(连接过)");
            }else {
                viewHolder.tvConnected.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView wifiIcon;
        TextView wifiName;
        ImageView ivConnented;
        ProgressBar proConnenting;
        TextView tvConnected;
    }
}
