package com.kingdom.test.clearprocess.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kingdom.test.clearprocess.R;
import com.kingdom.test.clearprocess.adapter.PrivateWifiAdapter;
import com.kingdom.test.clearprocess.adapter.PublicWifiAdapter;
import com.kingdom.test.clearprocess.javabean.WifiInfoBean;
import com.kingdom.test.clearprocess.utils.WifiAdmin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION;

public class WifiActivity extends AppCompatActivity implements View.OnClickListener {


    private ListView lvPrivateWifi;
    private ListView lvPublicWifi;
    private TextView tvWifiName;
    private Button btnWifioOptimize;
    private TextView tvUploadSpeed;
    private TextView tvDownloadSpeed;
    private ImageView ivBack;
    private ImageView ivSwitch;
    private boolean isOpen;
    int type;
    private WifiManager wManager;
    private List<WifiInfoBean> priviatewifiInfo = new ArrayList<>();
    private List<WifiInfoBean> publicwifiInfo = new ArrayList<>();
    private PrivateWifiAdapter privateWifiAdapter;
    private PublicWifiAdapter publicWifiAdapter;
    private AlertDialog alertDialog;
    private boolean isShowPassward;
    private Handler handler = new Handler();
    //每隔一秒重新扫描一次WIFi，实现实时更新周围的wifi情况
    private Runnable upDate = new Runnable() {
        @Override
        public void run() {
            getWifis();
            handler.postDelayed(upDate, 3000);

        }
    };
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            if (alertDialog != null) alertDialog.dismiss();
            layoutWifiList.setVisibility(View.VISIBLE);
            layoutWifiClose.setVisibility(View.GONE);


        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case WIFI_STATE_CHANGED_ACTION://接受打开和关闭wifi时系统发送的广播
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_DISABLING://wifi正在关闭
                            break;
                        case WifiManager.WIFI_STATE_DISABLED://wif关闭了
                            handler.removeCallbacks(upDate);//停止重复扫描wifi
                            ivSwitch.setImageResource(R.drawable.switch_off);
                            isOpen = false;
                            layoutWifiList.setVisibility(View.GONE);
                            layoutWifiClose.setVisibility(View.VISIBLE);
                            break;
                        case WifiManager.WIFI_STATE_ENABLED://wifi打开了
                            ivSwitch.setImageResource(R.drawable.switch_on);
                            isOpen = true;
                            handler.post(upDate);//开始执行重复扫描wifi
                            handler.postDelayed(run, 3000);//2秒后执行确保扫描完整
                            break;
                        case WifiManager.WIFI_STATE_ENABLING://wifi正在打开
                            alertDialog = new AlertDialog.Builder(context).create();
                            View view = LayoutInflater.from(context).inflate(R.layout.openning_wifi, null);
                            alertDialog.setView(view);
                            alertDialog.show();
                            break;
                    }
                    break;
                case NETWORK_STATE_CHANGED_ACTION:
                    break;
            }
        }
    };
    private LinearLayout layoutWifiList;
    private RelativeLayout layoutWifiClose;
    private Button btnOpenWifi;
    private LinearLayout layoutPublicWifi;
    private ImageView ivLine;
    private LinearLayout layoutPrivateWifi;
    private RelativeLayout layoutNoWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        //动态注册广播
        registBroadCast();
        lvPrivateWifi = (ListView) findViewById(R.id.lv_private_wifi);
        lvPublicWifi = (ListView) findViewById(R.id.lv_public_wifi);
        tvWifiName = (TextView) findViewById(R.id.tv_wifi_name);
        btnWifioOptimize = (Button) findViewById(R.id.btn_wifi_optimize);
        tvUploadSpeed = (TextView) findViewById(R.id.tv_upload_speed);
        tvDownloadSpeed = (TextView) findViewById(R.id.tv_download_speed);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivSwitch = (ImageView) findViewById(R.id.iv_switch);
        layoutWifiList = (LinearLayout) findViewById(R.id.layout_wifi_list);
        layoutWifiClose = (RelativeLayout) findViewById(R.id.layout_close_wifi);
        btnOpenWifi = (Button) findViewById(R.id.btn_open_wifi);

        layoutPublicWifi = (LinearLayout) findViewById(R.id.layout_public_wifi);
        layoutPrivateWifi = (LinearLayout) findViewById(R.id.layout_private_wifi);
        ivLine = (ImageView) findViewById(R.id.iv_line);
        layoutNoWifi = (RelativeLayout) findViewById(R.id.layout_no_wifi);

        btnWifioOptimize.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivSwitch.setOnClickListener(this);
        btnOpenWifi.setOnClickListener(this);

        //设置适配器
        privateWifiAdapter = new PrivateWifiAdapter(getBaseContext(), priviatewifiInfo);
        lvPrivateWifi.setAdapter(privateWifiAdapter);
        publicWifiAdapter = new PublicWifiAdapter(getBaseContext(), publicwifiInfo);
        lvPublicWifi.setAdapter(publicWifiAdapter);

        //listview item点击监听
        setListviewItemListener();

        wManager = (WifiManager) getSystemService(WIFI_SERVICE);
    }

    private void setListviewItemListener() {
        //需要密码的wifi设置点击事件
        lvPrivateWifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                View dialogView = LayoutInflater.from(WifiActivity.this).inflate(R.layout.private_wifi_dialog, null);
                ImageView wifiIcon = (ImageView) dialogView.findViewById(R.id.iv_private_dialog_wifi_icon);
                TextView wifiName = (TextView) dialogView.findViewById(R.id.tv_private_dialog_wifi_name);
                TextView wifiInfo = (TextView) dialogView.findViewById(R.id.tv_wifi_info);
                Button noButton = (Button) dialogView.findViewById(R.id.btn_wifi_con_no);
                final Button yesButton = (Button) dialogView.findViewById(R.id.btn_wifi_con_yes);
                final ImageView showPassward = (ImageView) dialogView.findViewById(R.id.iv_checkbox);
                final EditText etPassWard = (EditText) dialogView.findViewById(R.id.et_passward);


                int level = Math.abs(priviatewifiInfo.get(position).getLevel());
                if (level > 0 && level < 50) {
                    wifiIcon.setImageResource(R.drawable.wifi_2_level);
                } else if (level >= 50 && level < 70) {
                    wifiIcon.setImageResource(R.drawable.wifi_1_level);
                } else if (level >= 70 && level < 100) {
                    wifiIcon.setImageResource(R.drawable.wifi_0_level);
                }
                wifiName.setText(priviatewifiInfo.get(position).getWifiName());
                wifiInfo.setText("信号强度：" + (100 - level) + "%");

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


                final AlertDialog dialog = new AlertDialog.Builder(WifiActivity.this).setView(dialogView).show();
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String password = etPassWard.getText().toString().trim();
                        //点击链接后
                        if (password.length() >= 8) {
                            String passward = etPassWard.getText().toString().trim();
                            for (WifiInfoBean x : priviatewifiInfo) {
                                Log.i("priviatewifiInfo", "ssid:" + x.getWifiName() + "    level:" + x.getLevel() + "   type:" + x.getType());
                            }

                            connectWifi(position, priviatewifiInfo, passward);
                            dialog.dismiss();
                        }
                    }
                });

            }
        });

        //公共wifi设置点击事件
        lvPublicWifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                View dialogView = LayoutInflater.from(WifiActivity.this).inflate(R.layout.public_wifi_dialog, null);
                ImageView wifiIcon = (ImageView) dialogView.findViewById(R.id.iv_public_dialog_wifi_icon);
                TextView wifiName = (TextView) dialogView.findViewById(R.id.tv_public_dialog_wifi_name);
                TextView wifiInfo = (TextView) dialogView.findViewById(R.id.tv_wifi_info);
                Button noButton = (Button) dialogView.findViewById(R.id.btn_wifi_con_no);
                Button yesButton = (Button) dialogView.findViewById(R.id.btn_wifi_con_yes);
                int level = Math.abs(publicwifiInfo.get(position).getLevel());
                if (level > 0 && level < 50) {
                    wifiIcon.setImageResource(R.drawable.wifi_2_no_lock);
                } else if (level >= 50 && level < 70) {
                    wifiIcon.setImageResource(R.drawable.wifi_1_no_lock);
                } else if (level >= 70 && level < 100) {
                    wifiIcon.setImageResource(R.drawable.wifi_0_no_lock);
                }
                wifiName.setText(publicwifiInfo.get(position).getWifiName());
                wifiInfo.setText("信号强度：" + (100 - level) + "%");
                final AlertDialog dialog = new AlertDialog.Builder(WifiActivity.this).setView(dialogView).show();
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //点击链接后

                        for (WifiInfoBean x : publicwifiInfo) {
                            Log.i("publicwifiInfo", "ssid:" + x.getWifiName() + "    level:" + x.getLevel() + "   type:" + x.getType());
                        }
                        connectWifi(position, publicwifiInfo, null);
                        dialog.dismiss();

                    }
                });

            }
        });
    }

    private void connectWifi(int position, List<WifiInfoBean> wifiInfo, String passward) {
        int type = wifiInfo.get(position).getType();
        WifiAdmin wifiAdmin = new WifiAdmin(WifiActivity.this) {
            @Override
            public Intent myRegisterReceiver(BroadcastReceiver receiver, IntentFilter filter) {
                WifiActivity.this.registerReceiver(receiver, filter);
                return null;
            }

            @Override
            public void myUnregisterReceiver(BroadcastReceiver receiver) {
                WifiActivity.this.unregisterReceiver(receiver);
            }

            @Override
            public void onNotifyWifiConnected() {
                Toast.makeText(WifiActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNotifyWifiConnectFailed() {
                Toast.makeText(WifiActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            }
        };

        wifiAdmin.addNetwork(wifiInfo.get(position).getWifiName(), passward, type);
    }


    private void registBroadCast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NETWORK_STATE_CHANGED_ACTION);//监听网络连接的广播
        filter.addAction(WIFI_STATE_CHANGED_ACTION);//监听wifi打开的广播
        registerReceiver(receiver, filter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wifi_optimize:
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_open_wifi:
                wManager.setWifiEnabled(true);
                break;
            case R.id.iv_switch:
                if (isOpen) {
                    wManager.setWifiEnabled(false);
                    isOpen = false;
                } else {
                    wManager.setWifiEnabled(true);
                    isOpen = true;
                }
                break;
        }
    }

    //获取周围的wifi
    private void getWifis() {
        priviatewifiInfo.clear();
        publicwifiInfo.clear();
        wManager.startScan();
        List<ScanResult> scanResults = wManager.getScanResults();
        if (scanResults != null) {
            for (ScanResult result : scanResults) {
                String wifiName = result.SSID;
                int level = result.level;
                String capabilities = result.capabilities;//包含WPA都是需要密码
                if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                    type = WifiAdmin.TYPE_WPA;
                } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                    type = WifiAdmin.TYPE_WEP;
                } else {
                    type = WifiAdmin.TYPE_NO_PASSWD;
                }
                if (type == WifiAdmin.TYPE_WPA || type == WifiAdmin.TYPE_WEP) {
                    if (priviatewifiInfo != null) {
                        priviatewifiInfo.add(new WifiInfoBean(wifiName, level, type));
                        privateWifiAdapter.notifyDataSetChanged();
                    }

                } else if (type == WifiAdmin.TYPE_NO_PASSWD) {
                    publicwifiInfo.add(new WifiInfoBean(wifiName, level, type));
                    publicWifiAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "存在加密方式不存在的WIFi", Toast.LENGTH_SHORT).show();
                }
            }
        }

        //如果没有需要密码的WIFi时
        if (priviatewifiInfo.size() <= 0) {
            layoutPrivateWifi.setVisibility(View.GONE);
            ivLine.setVisibility(View.GONE);
        } else {
            layoutPrivateWifi.setVisibility(View.VISIBLE);
            ivLine.setVisibility(View.VISIBLE);
            priviatewifiInfo = removeTheSameNameWifi(priviatewifiInfo);
            Collections.sort(priviatewifiInfo);
            Collections.reverse(priviatewifiInfo);
            privateWifiAdapter.notifyDataSetChanged();
        }

        //如果没有公共WIFi时
        if (publicwifiInfo.size() <= 0) {
            layoutPublicWifi.setVisibility(View.GONE);
        } else {
            layoutPublicWifi.setVisibility(View.VISIBLE);
            publicwifiInfo = removeTheSameNameWifi(publicwifiInfo);
            Collections.sort(publicwifiInfo);
            Collections.reverse(publicwifiInfo);
            publicWifiAdapter.notifyDataSetChanged();

//            for (WifiInfoBean x : publicwifiInfo) {
//                Log.i("publicwifiInfo","ssid:"+x.getWifiName()+"    level:"+x.getLevel()+"   type:"+x.getType());
//            }
        }

        //如果需要密码的WIFi和公共WIFI都没有时
        if (priviatewifiInfo.size() <= 0 && publicwifiInfo.size() <= 0) {
            layoutWifiList.setVisibility(View.GONE);
            layoutNoWifi.setVisibility(View.VISIBLE);
        } else {
            layoutWifiList.setVisibility(View.VISIBLE);
            layoutNoWifi.setVisibility(View.GONE);
        }

    }

    //去除重复名字且信号更低的wifi
    public List<WifiInfoBean> removeTheSameNameWifi(List<WifiInfoBean> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(i).getWifiName().equals(list.get(j).getWifiName())) {
                    if (list.get(i).getLevel() > list.get(j).getLevel()) {
                        list.remove(j);
                    } else {
                        list.remove(i);
                    }
                }
            }
        }
        return list;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

}
