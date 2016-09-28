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
public class BigFileAdapter extends BaseAdapter {
    Context context;
    List<RubbishInfoBean> bigFiles;
    public BigFileAdapter(Context context, List<RubbishInfoBean> bigFiles) {
        this.context = context;
        this.bigFiles=bigFiles;
    }

    @Override
    public int getCount() {
        return bigFiles.size();
    }

    @Override
    public RubbishInfoBean getItem(int position) {
        return bigFiles.get(position);
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
            viewHolder.ivBigFileIcon= (ImageView) convertView.findViewById(R.id.iv_item_elv_child_appicon);
            viewHolder.tvBigFileName= (TextView) convertView.findViewById(R.id.tv_item_elv_child_appname);
            viewHolder.tvBigFileSize= (TextView) convertView.findViewById(R.id.tv_item_elv_child_appcachs_size);
            viewHolder.tvBIgFilePath= (TextView) convertView.findViewById(R.id.tv_item_elv_child_apkpath);
            viewHolder.checkBox= (CheckBox) convertView.findViewById(R.id.cb_item_elv_child_checkbox);
            convertView.setTag(viewHolder);
        }
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
//        viewHolder.ivBigFileIcon.setImageDrawable();
        viewHolder.tvBigFileName.setText(getItem(position).getName());
        viewHolder.tvBigFileSize.setText(Formatter.formatFileSize(context, getItem(position).getSize()));
        viewHolder.tvBIgFilePath.setVisibility(View.VISIBLE);
        viewHolder.tvBIgFilePath.setText("来自:"+getItem(position).getPath());
        viewHolder.checkBox.setChecked(getItem(position).isChecked());
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
        ImageView ivBigFileIcon;
        TextView tvBigFileName;
        TextView tvBigFileSize;
        TextView tvBIgFilePath;
        CheckBox checkBox;
    }
}
