package com.kingdom.test.clearprocess.listener;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.kingdom.test.clearprocess.adapter.PrivateWifiAdapter;
import com.kingdom.test.clearprocess.javabean.WifiInfoBean;
import com.kingdom.test.clearprocess.utils.WifiAdmin;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by admin on 2016/9/28.
 */
public class PriviteWifiItemListener implements AdapterView.OnItemClickListener, View.OnClickListener {
    List<WifiInfoBean> priviatewifiInfo;
    WifiActivity wifiActivity;
    private boolean isShowPassward;
    private WifiAdmin wifiAdmin;
    private RelativeLayout layoutCheckSpeed;
    private RelativeLayout layoutCheckCen;
    private RelativeLayout layoutCheckSafe;
    private RelativeLayout layoutConnect;
    private RelativeLayout layoutDisconect;
    private RelativeLayout layoutForgetWifi;
    private RelativeLayout layoutSeeWifi;
    private Button btnCancel;
    private WifiConfiguration exsitsConfig;
    private WifiManager mWifiManger;
    private String wifiName;
    PrivateWifiAdapter privateWifiAdapter;
    private PopupWindow popupWindow;

    public PriviteWifiItemListener(List<WifiInfoBean> priviatewifiInfo, WifiActivity wifiActivity, PrivateWifiAdapter privateWifiAdapter) {
        this.priviatewifiInfo = priviatewifiInfo;
        this.wifiActivity = wifiActivity;
        this.privateWifiAdapter =privateWifiAdapter;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
        if (priviatewifiInfo.get(position).isConnecting()==false){
            //发送消息通知停止刷新周围wifi的信息
            EventBus.getDefault().post(new ScanWifiEvent("stop", true));
            //判断该WIFi时候已经配置过
            mWifiManger = (WifiManager) wifiActivity.getSystemService(Context.WIFI_SERVICE);
            wifiAdmin = new WifiAdmin(wifiActivity);
            exsitsConfig = wifiAdmin.IsExsits(priviatewifiInfo.get(position).getWifiName());
            if (exsitsConfig == null) {//为空说名没有配置过
                priviatewifiInfo.get(position).setSave(false);//设置该wifi没有配置过
                connectWifiNoConfiguration(position);
            } else {//该wifi已经配置过
                connectWifiConfig(position);
            }
        }

    }

    //连接配置过的WIFi
    private void connectWifiConfig(int position) {
        priviatewifiInfo.get(position).setSave(true);//设置该wifi已经配置过
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


        int level = priviatewifiInfo.get(position).getLevel();
        if (level == 0) {
            ivWifiIcon.setImageResource(R.drawable.wifi_0_level);
        } else if (level == 1) {
            ivWifiIcon.setImageResource(R.drawable.wifi_1_level);
        } else if (level == 2) {
            ivWifiIcon.setImageResource(R.drawable.wifi_2_level);
        } else if (level == 3) {
            ivWifiIcon.setImageResource(R.drawable.wifi_3_level);
        }
        wifiName = priviatewifiInfo.get(position).getWifiName();
        tvWifiName.setText(wifiName);
        //连接状态下
        if (priviatewifiInfo.get(position).isConnecting() == false && priviatewifiInfo.get(position).isConnected() == true) {
            connectedBtns.setVisibility(View.VISIBLE);
            layoutConnect.setVisibility(View.GONE);
            layoutDisconect.setVisibility(View.VISIBLE);
        }
        //没有连接
        if (priviatewifiInfo.get(position).isConnecting() == false && priviatewifiInfo.get(position).isConnected() == false) {
            //已保存
            if (priviatewifiInfo.get(position).isSave()) {
                connectedBtns.setVisibility(View.GONE);
                layoutConnect.setVisibility(View.VISIBLE);
                layoutDisconect.setVisibility(View.GONE);
            }
        }
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

    //连接没有配置过的WIfi
    private void connectWifiNoConfiguration(final int position) {
        View dialogView = LayoutInflater.from(wifiActivity).inflate(R.layout.private_wifi_dialog, null);
        ImageView wifiIcon = (ImageView) dialogView.findViewById(R.id.iv_private_dialog_wifi_icon);
        TextView wifiName = (TextView) dialogView.findViewById(R.id.tv_private_dialog_wifi_name);
        final TextView wifiInfo = (TextView) dialogView.findViewById(R.id.tv_wifi_info);
        Button noButton = (Button) dialogView.findViewById(R.id.btn_wifi_con_no);
        final Button yesButton = (Button) dialogView.findViewById(R.id.btn_wifi_con_yes);
        final ImageView showPassward = (ImageView) dialogView.findViewById(R.id.iv_checkbox);
        final EditText etPassWard = (EditText) dialogView.findViewById(R.id.et_passward);

        int level = priviatewifiInfo.get(position).getLevel();

        if (level == 0) {
            wifiIcon.setImageResource(R.drawable.wifi_0_level);
        } else if (level == 1) {
            wifiIcon.setImageResource(R.drawable.wifi_1_level);
        } else if (level == 2) {
            wifiIcon.setImageResource(R.drawable.wifi_2_level);
        } else if (level == 3) {
            wifiIcon.setImageResource(R.drawable.wifi_3_level);
        }
        wifiName.setText(priviatewifiInfo.get(position).getWifiName());
        wifiInfo.setText("信号强度：" + (level + 1) * 100 / 4 + "%");
        showPassward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShowPassward) {
                    showPassward.setImageResource(R.drawable.select_yes);
                    etPassWard.setTransformationMethod(HideReturnsTransformationMethod.getInstance());//显示密码
                    isShowPassward = true;
                } else {
                    showPassward.setImageResource(R.drawable.select_no);
                    etPassWard.setTransformationMethod(PasswordTransformationMethod.getInstance());//隐藏密码
                    isShowPassward = false;
                }
            }
        });


        //监听编辑框输入的变化
        etPassWard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String etStr = etPassWard.getText().toString().trim();
                if (etStr.length() >= 8) {
                    yesButton.setBackgroundResource(android.R.color.holo_green_dark);
                }
                if (etStr.length() < 8) {
                    yesButton.setBackgroundResource(R.color.con_btn_bg);
                }
            }
        });


        final AlertDialog dialog = new AlertDialog.Builder(wifiActivity).setView(dialogView).show();
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //发送消息通知可以开始刷新周围wifi的信息
                EventBus.getDefault().post(new ScanWifiEvent("start", false));
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etPassWard.getText().toString().trim();
                //点击链接后
                if (password.length() >= 8) {
                    //发送消息，开始连接通知UI改变
                    EventBus.getDefault().post(new WifiConnectEvent("priviatewifiInfo", priviatewifiInfo.get(position).getWifiName(),position));
                    String passward = etPassWard.getText().toString().trim();
                    int type = priviatewifiInfo.get(position).getType();
                    WifiAdmin wifiAdmin = new WifiAdmin(wifiActivity);
                    wifiAdmin.addNetwork(wifiAdmin.createWifiInfo(priviatewifiInfo.get(position).getWifiName(), passward, type));
                    dialog.dismiss();
                }
            }
        });
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
                EventBus.getDefault().post(new ScanWifiEvent("start",true));
                popupWindow.dismiss();
                break;
        }

    }

    private void forgetWifi() {
        if (exsitsConfig!=null){
            mWifiManger.removeNetwork(exsitsConfig.networkId);
            for (int i=0;i<priviatewifiInfo.size();i++){
                if (priviatewifiInfo.get(i).getWifiName()==wifiName){
                    priviatewifiInfo.get(i).setSave(false);
                    priviatewifiInfo.get(i).setConnected(false);
                    priviatewifiInfo.get(i).setConnecting(false);
                    privateWifiAdapter.notifyDataSetChanged();
                }
            }
            //忘记网络后发送消息通知改变UI，,通知可以开始扫描周围WIFi
            EventBus.getDefault().post(new WifiDisconnectEvent("privatewifi",0));
            popupWindow.dismiss();
        }
    }

    private void disconnectAction() {
        if (exsitsConfig!=null){
            int netId =exsitsConfig.networkId;
            mWifiManger.disableNetwork(netId);
            mWifiManger.disconnect();
            for (int i=0;i<priviatewifiInfo.size();i++){
                if (priviatewifiInfo.get(i).getWifiName()==wifiName){
                    priviatewifiInfo.get(i).setSave(true);
                    priviatewifiInfo.get(i).setConnected(false);
                    priviatewifiInfo.get(i).setConnecting(false);
                    privateWifiAdapter.notifyDataSetChanged();
                }
            }
            //断开网络后发送消息通知改变UI,通知可以开始扫描周围WIFi
            EventBus.getDefault().post(new WifiDisconnectEvent("privatewifi",0));
            popupWindow.dismiss();
        }
    }

    private void connectAction() {
        if (exsitsConfig!=null){
            for (int i=0;i<priviatewifiInfo.size();i++){
                if (priviatewifiInfo.get(i).getWifiName()==wifiName){
                    priviatewifiInfo.get(i).setSave(true);
                    //发送消息，开始连接通知UI改变
                    EventBus.getDefault().post(new WifiConnectEvent("priviatewifiInfo", priviatewifiInfo.get(i).getWifiName(), i));
                }else {
                    WifiConfiguration existConfig = wifiAdmin.IsExsits(priviatewifiInfo.get(i).getWifiName());
                    if (existConfig!=null){
                        priviatewifiInfo.get(i).setSave(true);
                        privateWifiAdapter.notifyDataSetChanged();
                    }
                }
            }
            int wcgID = mWifiManger.addNetwork(exsitsConfig);
            boolean b = mWifiManger.enableNetwork(wcgID, true);
            popupWindow.dismiss();
        }
    }
}
