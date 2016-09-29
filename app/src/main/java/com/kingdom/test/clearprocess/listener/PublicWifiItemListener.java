package com.kingdom.test.clearprocess.listener;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kingdom.test.clearprocess.EventBus.ScanWifiEvent;
import com.kingdom.test.clearprocess.EventBus.WifiConnectEvent;
import com.kingdom.test.clearprocess.EventBus.WifiDisconnectEvent;
import com.kingdom.test.clearprocess.R;
import com.kingdom.test.clearprocess.activity.WifiActivity;
import com.kingdom.test.clearprocess.adapter.PublicWifiAdapter;
import com.kingdom.test.clearprocess.javabean.WifiInfoBean;
import com.kingdom.test.clearprocess.utils.WifiAdmin;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by admin on 2016/9/28.
 */
public class PublicWifiItemListener implements AdapterView.OnItemClickListener,View.OnClickListener {
    List<WifiInfoBean> publicwifiInfo;
    WifiActivity wifiActivity;
    private WifiManager mWifiManger;
    private WifiAdmin wifiAdmin;
    private WifiConfiguration exsitsConfig;
    private PopupWindow popupWindow;
    private RelativeLayout layoutCheckSpeed;
    private RelativeLayout layoutCheckCen;
    private RelativeLayout layoutCheckSafe;
    private RelativeLayout layoutConnect;
    private RelativeLayout layoutDisconect;
    private RelativeLayout layoutForgetWifi;
    private RelativeLayout layoutSeeWifi;
    private Button btnCancel;
    private String wifiName;
    PublicWifiAdapter publicWifiAdapter;
    public PublicWifiItemListener(List<WifiInfoBean> publicwifiInfo, WifiActivity wifiActivity, PublicWifiAdapter publicWifiAdapter) {
        this.publicwifiInfo=publicwifiInfo;
        this.wifiActivity =wifiActivity;
        this.publicWifiAdapter =publicWifiAdapter;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
        //发送消息通知停止刷新周围wifi的信息
        EventBus.getDefault().post(new ScanWifiEvent("stop",true));
//判断该WIFi时候已经配置过
        mWifiManger = (WifiManager) wifiActivity.getSystemService(Context.WIFI_SERVICE);
        wifiAdmin = new WifiAdmin(wifiActivity);
        exsitsConfig = wifiAdmin.IsExsits(publicwifiInfo.get(position).getWifiName());
        if (exsitsConfig == null) {//为空说名没有配置过
            publicwifiInfo.get(position).setSave(false);//设置该wifi没有配置过
            connectWifiNoConfiguration(position);
        } else {//该wifi已经配置过
            connectWifiConfig(position);
        }
    }

    //连接配置过的WIFi
    private void connectWifiConfig(int position) {
        publicwifiInfo.get(position).setSave(true);//设置该wifi已经配置过
        View popview = LayoutInflater.from(wifiActivity).inflate(R.layout.privat_wifi_dialog_saved, null);
        setPopWindow(popview);
        ImageView ivWifiIcon = (ImageView) popview.findViewById(R.id.iv_private_dialog_wifi_icon);
        TextView tvWifiName = (TextView) popview.findViewById(R.id.tv_private_dialog_wifi_name);
        LinearLayout connectedBtns = (LinearLayout) popview.findViewById(R.id.layout_btns2);//连接上WIFi后显示出来的按钮
        layoutCheckSpeed = (RelativeLayout) popview.findViewById(R.id.layout_check_speed);
        layoutCheckCen = (RelativeLayout) popview.findViewById(R.id.layout_check_wifi);//蹭网检测
        layoutCheckSafe = (RelativeLayout) popview.findViewById(R.id.layout_check_safe);
        layoutConnect = (RelativeLayout) popview.findViewById(R.id.layout_connect);
        layoutDisconect = (RelativeLayout) popview.findViewById(R.id.layout_disconnect);
        layoutForgetWifi = (RelativeLayout) popview.findViewById(R.id.layout_delete_wifi);
        layoutSeeWifi = (RelativeLayout) popview.findViewById(R.id.layout_see_wifi);
        btnCancel = (Button) popview.findViewById(R.id.btn_cancel);

        layoutCheckSpeed.setOnClickListener(this);
        layoutCheckCen.setOnClickListener(this);
        layoutCheckSafe.setOnClickListener(this);
        layoutConnect.setOnClickListener(this);
        layoutDisconect.setOnClickListener(this);
        layoutForgetWifi.setOnClickListener(this);
        layoutSeeWifi.setOnClickListener(this);
        btnCancel.setOnClickListener(this);


        int level = publicwifiInfo.get(position).getLevel();
        if (level == 0) {
            ivWifiIcon.setImageResource(R.drawable.wifi_0_level);
        } else if (level == 1) {
            ivWifiIcon.setImageResource(R.drawable.wifi_1_level);
        } else if (level == 2) {
            ivWifiIcon.setImageResource(R.drawable.wifi_2_level);
        } else if (level == 3) {
            ivWifiIcon.setImageResource(R.drawable.wifi_3_level);
        }
        wifiName = publicwifiInfo.get(position).getWifiName();
        tvWifiName.setText(wifiName);
        //连接状态下
        if (publicwifiInfo.get(position).isConnecting() == false && publicwifiInfo.get(position).isConnected() == true) {
            connectedBtns.setVisibility(View.VISIBLE);
            layoutConnect.setVisibility(View.GONE);
            layoutDisconect.setVisibility(View.VISIBLE);
        }
        //没有连接
        if (publicwifiInfo.get(position).isConnecting() == false && publicwifiInfo.get(position).isConnected() == false) {
            //已保存
            if (publicwifiInfo.get(position).isSave()) {
                connectedBtns.setVisibility(View.GONE);
                layoutConnect.setVisibility(View.VISIBLE);
                layoutDisconect.setVisibility(View.GONE);
            }
        }
    }

    private void connectWifiNoConfiguration(final int position) {
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
                EventBus.getDefault().post(new WifiConnectEvent("PublicWifiInfo",publicwifiInfo.get(position).getWifiName(), position));
                //连接WIFi
                int type = publicwifiInfo.get(position).getType();
                WifiAdmin wifiAdmin = new WifiAdmin(wifiActivity);
                wifiAdmin.addNetwork(wifiAdmin.createWifiInfo(publicwifiInfo.get(position).getWifiName(), null, type));
                dialog.dismiss();
            }
        });
    }
    //设置POPWindow
    private void setPopWindow(View popview) {
        popupWindow = new PopupWindow(popview, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        popupWindow.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
//            popupWindow.setAnimationStyle(R.style.AnimBottom);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景,// 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable(dw);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("mengdd", "onTouch : ");
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });
        popupWindow.showAtLocation(wifiActivity.findViewById(R.id.activity_wifi), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_check_speed://测试网速
                break;
            case R.id.layout_check_wifi://检测蹭网
                break;
            case R.id.layout_check_safe://安全检测
                break;
            case R.id.layout_connect://连接网络(已经配置好的WIFI)
                connectAction();
                break;
            case R.id.layout_disconnect://断开网络
                disconnectAction();
                break;
            case R.id.layout_delete_wifi://忘记网络
                forgetWifi();
                break;
            case R.id.layout_see_wifi://查看网络
                break;
            case R.id.btn_cancel://取消
                popupWindow.dismiss();
                break;
        }

    }

    private void forgetWifi() {
        if (exsitsConfig!=null){
            mWifiManger.removeNetwork(exsitsConfig.networkId);
            for (int i=0;i<publicwifiInfo.size();i++){
                if (publicwifiInfo.get(i).getWifiName()==wifiName){
                    publicwifiInfo.get(i).setSave(false);
                    publicwifiInfo.get(i).setConnected(false);
                    publicwifiInfo.get(i).setConnecting(false);
                    publicWifiAdapter.notifyDataSetChanged();
                }
            }
            popupWindow.dismiss();
        }
    }

    private void disconnectAction() {
        if (exsitsConfig!=null){
            int netId =exsitsConfig.networkId;
            mWifiManger.disableNetwork(netId);
            mWifiManger.disconnect();
            for (int i=0;i<publicwifiInfo.size();i++){
                if (publicwifiInfo.get(i).getWifiName()==wifiName){
                    publicwifiInfo.get(i).setSave(true);
                    publicwifiInfo.get(i).setConnected(false);
                    publicwifiInfo.get(i).setConnecting(false);
                    publicWifiAdapter.notifyDataSetChanged();
                }
            }
            //断开网络后发送消息通知改变UI
            EventBus.getDefault().post(new WifiDisconnectEvent("privatewifi",0));
            popupWindow.dismiss();
        }
    }

    private void connectAction() {
        if (exsitsConfig!=null){
            int wcgID = mWifiManger.addNetwork(exsitsConfig);
            boolean b = mWifiManger.enableNetwork(wcgID, true);

            for (int i=0;i<publicwifiInfo.size();i++){
                if (publicwifiInfo.get(i).getWifiName()==wifiName){
                    publicwifiInfo.get(i).setSave(true);
                    //点击连接之后设置连接的状态
                    publicwifiInfo.get(i).setConnecting(true);//正在连接
                    publicwifiInfo.get(i).setConnected(false);
                    //发送消息，开始连接通知UI改变
                    EventBus.getDefault().post(new WifiConnectEvent("PublicWifiInfo", publicwifiInfo.get(i).getWifiName(), i));
                    publicWifiAdapter.notifyDataSetChanged();
                }else {
                    WifiConfiguration existConfig = wifiAdmin.IsExsits(publicwifiInfo.get(i).getWifiName());
                    if (existConfig!=null){
                        publicwifiInfo.get(i).setSave(true);
                        publicWifiAdapter.notifyDataSetChanged();
                    }
                }
            }
            popupWindow.dismiss();
        }
    }
}
