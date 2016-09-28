package com.kingdom.test.clearprocess.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.kingdom.test.clearprocess.EventBus.FirstEvent;
import com.kingdom.test.clearprocess.R;
import com.kingdom.test.clearprocess.javabean.RubbishInfoBean;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by admin on 2016/9/22.
 */
public class ApkAdapter extends BaseAdapter {
    Context context;
    List<RubbishInfoBean> apkInfo;
    public ApkAdapter(Context context, List<RubbishInfoBean> apkInfo) {
        this.context= context;
        this.apkInfo=apkInfo;
    }

    @Override
    public int getCount() {
        return apkInfo.size();
    }

    @Override
    public RubbishInfoBean getItem(int position) {
        return apkInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView ==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_exlistview_child,null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.apkIcon= (ImageView) convertView.findViewById(R.id.iv_item_elv_child_appicon);
            viewHolder.apkName= (TextView) convertView.findViewById(R.id.tv_item_elv_child_appname);
            viewHolder.apkSize= (TextView) convertView.findViewById(R.id.tv_item_elv_child_appcachs_size);
            viewHolder.checkBox= (CheckBox) convertView.findViewById(R.id.cb_item_elv_child_checkbox);
            viewHolder.apkPath= (TextView) convertView.findViewById(R.id.tv_item_elv_child_apkpath);
            convertView.setTag(viewHolder);
        }
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.apkIcon.setImageDrawable(getItem(position).getIcon());
        viewHolder.apkName.setText(getItem(position).getName());
        viewHolder.apkSize.setText(Formatter.formatFileSize(context, getItem(position).getSize()));
        viewHolder.checkBox.setChecked(getItem(position).isChecked());
        viewHolder.apkPath.setVisibility(View.VISIBLE);
        viewHolder.apkPath.setText(getItem(position).getPath());
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getItem(position).isChecked()){
                    //发送消息
                    EventBus.getDefault().post(new FirstEvent("reduce",getItem(position).getSize()));
                }else {
                    //发送消息
                    EventBus.getDefault().post(new FirstEvent("add",getItem(position).getSize()));
                }
                getItem(position).setTag(getItem(position).isChecked());
            }
        });
        return convertView;
    }

    class ViewHolder{
        ImageView apkIcon;
        TextView apkName;
        TextView apkSize;
        CheckBox checkBox;
        TextView apkPath;
    }
}
