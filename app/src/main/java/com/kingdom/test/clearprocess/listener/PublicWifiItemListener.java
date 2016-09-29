package com.kingdom.test.clearprocess.listener;

import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kingdom.test.clearprocess.EventBus.ScanWifiEvent;
import com.kingdom.test.clearprocess.EventBus.WifiConnectEvent;
import com.kingdom.test.clearprocess.R;
import com.kingdom.test.clearprocess.activity.WifiActivity;
import com.kingdom.test.clearprocess.javabean.WifiInfoBean;
import com.kingdom.test.clearprocess.utils.WifiAdmin;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by admin on 2016/9/28.
 */
public class PublicWifiItemListener implements AdapterView.OnItemClickListener {
    List<WifiInfoBean> publicwifiInfo;
    WifiActivity wifiActivity;
    public PublicWifiItemListener(List<WifiInfoBean> publicwifiInfo, WifiActivity wifiActivity) {
        this.publicwifiInfo=publicwifiInfo;
        this.wifiActivity =wifiActivity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
        //发送消息通知停止刷新周围wifi的信息
        EventBus.getDefault().post(new ScanWifiEvent("stop",true));


        View dialogView = LayoutInflater.from(wifiActivity).inflate(R.layout.public_wifi_dialog, null);
        ImageView wifiIcon = (ImageView) dialogView.findViewById(R.id.iv_public_dialog_wifi_icon);
        TextView wifiName = (TextView) dialogView.findViewById(R.id.tv_public_dialog_wifi_name);
        TextView wifiInfo = (TextView) dialogView.findViewById(R.id.tv_wifi_info);
        Button noButton = (Button) dialogView.findViewById(R.id.btn_wifi_con_no);
        Button yesButton = (Button) dialogView.findViewById(R.id.btn_wifi_con_yes);
        int level = publicwifiInfo.get(position).getLevel();

        if (level == 0) {
            wifiIcon.setImageResource(R.drawable.wifi_no_lock_0_level);
        } else if (level == 1) {
            wifiIcon.setImageResource(R.drawable.wifi_no_lock_1_level);
        } else if (level == 2) {
            wifiIcon.setImageResource(R.drawable.wifi_no_lock_2_level);
        } else if (level == 3) {
            wifiIcon.setImageResource(R.drawable.wifi_no_lock_3_level);
        }
        wifiName.setText(publicwifiInfo.get(position).getWifiName());
        wifiInfo.setText("信号强度：" + (level+1)*100/4 + "%");
        final AlertDialog dialog = new AlertDialog.Builder(wifiActivity).setView(dialogView).show();
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                //发送消息通知可以开始刷新周围wifi的信息
                EventBus.getDefault().post(new ScanWifiEvent("start",false));
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //点击连接之后设置连接的状态
                publicwifiInfo.get(position).setConnecting(true);//正在连接
                publicwifiInfo.get(position).setConnected(false);
                //点击链接后
                //发送消息，开始连接通知UI改变
                EventBus.getDefault().post(new WifiConnectEvent("PublicWifiInfo",publicwifiInfo.get(position).getWifiName()));
                //连接WIFi
                connectWifi(position, publicwifiInfo, null);
                dialog.dismiss();




            }
        });
    }
    private void connectWifi(int position, List<WifiInfoBean> wifiInfo, String passward) {
        int type = wifiInfo.get(position).getType();
        WifiAdmin wifiAdmin = new WifiAdmin(wifiActivity);
        wifiAdmin.addNetwork(wifiAdmin.createWifiInfo(wifiInfo.get(position).getWifiName(), passward, type));
    }


}
