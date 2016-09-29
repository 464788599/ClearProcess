package com.kingdom.test.clearprocess.listener;

import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
public class PriviteWifiItemListener implements AdapterView.OnItemClickListener {
    List<WifiInfoBean> priviatewifiInfo;
    WifiActivity wifiActivity;
    private boolean isShowPassward;

    public PriviteWifiItemListener(List<WifiInfoBean> priviatewifiInfo, WifiActivity wifiActivity) {
        this.priviatewifiInfo = priviatewifiInfo;
        this.wifiActivity = wifiActivity;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
        //发送消息通知停止刷新周围wifi的信息
        EventBus.getDefault().post(new ScanWifiEvent("stop",true));

        View dialogView = LayoutInflater.from(wifiActivity).inflate(R.layout.private_wifi_dialog, null);
        ImageView wifiIcon = (ImageView) dialogView.findViewById(R.id.iv_private_dialog_wifi_icon);
        TextView wifiName = (TextView) dialogView.findViewById(R.id.tv_private_dialog_wifi_name);
        TextView wifiInfo = (TextView) dialogView.findViewById(R.id.tv_wifi_info);
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
                EventBus.getDefault().post(new ScanWifiEvent("start",false));
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etPassWard.getText().toString().trim();
                //点击链接后
                if (password.length() >= 8) {
                    //点击连接之后设置连接的状态
                    priviatewifiInfo.get(position).setConnecting(true);//正在连接
                    priviatewifiInfo.get(position).setConnected(false);
                    //发送消息，开始连接通知UI改变
                    EventBus.getDefault().post(new WifiConnectEvent("priviatewifiInfo", priviatewifiInfo.get(position).getWifiName()));
                    String passward = etPassWard.getText().toString().trim();
                    connectWifi(position, priviatewifiInfo, passward);
                    dialog.dismiss();
                }
            }
        });
    }

    private void connectWifi(int position, List<WifiInfoBean> wifiInfo, String passward) {
        int type = wifiInfo.get(position).getType();
        WifiAdmin wifiAdmin = new WifiAdmin(wifiActivity);
        wifiAdmin.addNetwork(wifiAdmin.createWifiInfo(wifiInfo.get(position).getWifiName(), passward, type));
    }

}
