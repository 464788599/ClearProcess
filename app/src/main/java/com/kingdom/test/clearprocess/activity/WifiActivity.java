package com.kingdom.test.clearprocess.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kingdom.test.clearprocess.EventBus.ScanWifiEvent;
import com.kingdom.test.clearprocess.EventBus.WifiConnectEvent;
import com.kingdom.test.clearprocess.EventBus.WifiDisconnectEvent;
import com.kingdom.test.clearprocess.R;
import com.kingdom.test.clearprocess.adapter.PrivateWifiAdapter;
import com.kingdom.test.clearprocess.adapter.PublicWifiAdapter;
import com.kingdom.test.clearprocess.javabean.ConfigWifiInfoBean;
import com.kingdom.test.clearprocess.javabean.WifiInfoBean;
import com.kingdom.test.clearprocess.listener.PriviteWifiItemListener;
import com.kingdom.test.clearprocess.listener.PublicWifiItemListener;
import com.kingdom.test.clearprocess.utils.WifiAdmin;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY;
import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.RSSI_CHANGED_ACTION;
import static android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION;
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

    private LinearLayout layoutWifiList;
    private RelativeLayout layoutWifiClose;
    private Button btnOpenWifi;
    private LinearLayout layoutPublicWifi;
    private ImageView ivLine;
    private LinearLayout layoutPrivateWifi;
    private RelativeLayout layoutNoWifi;
    private LinearLayout layoutWifiSpeed;
    private TextView tvConnectState;
    private List<ConfigWifiInfoBean> configWifiInfo=new ArrayList<>();
    private static final int NETWORK_WIFI_CONNECTION = 0;//代表连接的网络是WIFI
    private static final int NETWORK_MOBILE_CONNECTION = 1;//代表连接的手机网络
    private static final int NETWORK_DISCONNECTION =2 ;//代表没有连接网络；
    private Handler speedHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };
    //每隔一秒重新扫描一次WIFi，实现实时更新周围的wifi情况
    private Runnable upDate = new Runnable() {
        @Override
        public void run() {
            if (!isStopScanWifi) {
                temp = "refresh";
                getWifis();
            }
            handler.postDelayed(upDate, 3000);

        }
    };
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            if (alertDialog != null) alertDialog.dismiss();
            layoutWifiList.setVisibility(View.VISIBLE);
            layoutWifiClose.setVisibility(View.GONE);
            getCurrentWifi();

        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case WIFI_STATE_CHANGED_ACTION://接受打开和关闭wifi时系统发送的广播
                    IsOpenForWifi(context, intent);
                    break;
                case NETWORK_STATE_CHANGED_ACTION://WIFI连接状态的改变
                    changeWithWifiConnectState(intent);
                    break;
                case SUPPLICANT_STATE_CHANGED_ACTION://是不是正在获得IP地址
                    break;
                case CONNECTIVITY_ACTION://网络状态发生改变时，没有变化这是false，变化这是true
                    boolean b = intent.getBooleanExtra(EXTRA_NO_CONNECTIVITY, false);
                    if (b == false) {//网络变化，连接网络完成时(不一定连接成功)
                    } else {//网络变化，正在连接网络时
                    }
                    break;
            }
        }
    };
    private WifiInfo mWifiInfo;
    private WifiAdmin wifiAdmin;
    private String temp;
    private String wifiName;
    private boolean isStopScanWifi;
    private String autoConnectwifiName;
    private long lastTotalTxBytes;

    private void changeWithWifiConnectState(Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {//网络连接断开

        } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {//已经连接网络或连接上网络

            connectedWifi();

        } else if (info.getState().equals(NetworkInfo.State.CONNECTING)) {//网络正在连接

            connectingWifi();
        }
    }

    //正在连接wifi
    private void connectingWifi() {
        //设置当前正在连接的wifi的连接状态
        if (temp != null) {
            //点击公共热点
            if (temp.equals("PublicWifiInfo")) {
                for (int i = 0; i < publicwifiInfo.size(); i++) {
                    if (publicwifiInfo.get(i).getWifiName().equals(wifiName)) {
                        publicwifiInfo.get(i).setConnected(false);
                        publicwifiInfo.get(i).setConnecting(true);
                        publicWifiAdapter.notifyDataSetChanged();
                    }
                }
                //点击需要密码热点
            } else if (temp.equals("priviatewifiInfo")) {
                for (int i = 0; i < priviatewifiInfo.size(); i++) {
                    if (priviatewifiInfo.get(i).getWifiName().equals(wifiName)) {
                        priviatewifiInfo.get(i).setConnected(false);
                        priviatewifiInfo.get(i).setConnecting(true);
                        privateWifiAdapter.notifyDataSetChanged();
                    }
                }
                //自动连接配置过的热点
            }else if (temp.equals("configwifi")){
                for (int i = 0; i < priviatewifiInfo.size(); i++) {
                    if (priviatewifiInfo.get(i).getWifiName().equals(autoConnectwifiName)) {
                        priviatewifiInfo.get(i).setConnected(false);
                        priviatewifiInfo.get(i).setConnecting(true);
                        privateWifiAdapter.notifyDataSetChanged();
                    }else {
                        priviatewifiInfo.get(i).setConnected(false);
                        priviatewifiInfo.get(i).setConnecting(false);
                        privateWifiAdapter.notifyDataSetChanged();
                    }
                }
                for (int i = 0; i < publicwifiInfo.size(); i++) {
                    if (publicwifiInfo.get(i).getWifiName().equals(autoConnectwifiName)) {
                        publicwifiInfo.get(i).setConnected(false);
                        publicwifiInfo.get(i).setConnecting(true);
                        publicWifiAdapter.notifyDataSetChanged();
                    }else {
                        publicwifiInfo.get(i).setConnected(false);
                        publicwifiInfo.get(i).setConnecting(false);
                        publicWifiAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
        tvConnectState.setText("正在获取IP地址...");
    }

    //已经成功连接网络
    private void connectedWifi() {

//            if (tvConnectState != null) tvConnectState.setVisibility(View.GONE);
//            layoutWifiSpeed.setVisibility(View.VISIBLE);
        //点击连接网络成功后执行，设置当前连接的wifi的连接状态
        if (temp != null) {
            //点击公共热点连接成功后
            if (temp.equals("PublicWifiInfo")) {
                isConnectSuccess =true;
                Toast.makeText(WifiActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                for (int i = 0; i < publicwifiInfo.size(); i++) {
                    if (publicwifiInfo.get(i).getWifiName().equals(wifiName)) {
                        publicwifiInfo.get(i).setConnected(true);
                        publicwifiInfo.get(i).setConnecting(false);
                        publicWifiAdapter.notifyDataSetChanged();
                    }
                }
                tvConnectState.setText("连接成功");
                isNetworkOnline();
                //点击需要密码的热点连接成功后
            } else if (temp.equals("priviatewifiInfo")) {
                isConnectSuccess =true;
                Toast.makeText(WifiActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                for (int i = 0; i < priviatewifiInfo.size(); i++) {
                    if (priviatewifiInfo.get(i).getWifiName().equals(wifiName)) {
                        priviatewifiInfo.get(i).setConnected(true);
                        priviatewifiInfo.get(i).setConnecting(false);
                        priviatewifiInfo.get(i).setSave(true);
                        privateWifiAdapter.notifyDataSetChanged();
                    }
                }
                isNetworkOnline();

                //自动连接配置后的WIFI连接成功后
            }else if (temp.equals("configwifi")){
                isConnectSuccess =true;
                Toast.makeText(WifiActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                for (int i = 0; i < priviatewifiInfo.size(); i++) {
                    if (priviatewifiInfo.get(i).getWifiName().equals(autoConnectwifiName)) {
                        priviatewifiInfo.get(i).setConnected(true);
                        priviatewifiInfo.get(i).setConnecting(false);
                        priviatewifiInfo.get(i).setSave(true);
                        privateWifiAdapter.notifyDataSetChanged();
                    }else {
                        priviatewifiInfo.get(i).setConnected(false);
                        priviatewifiInfo.get(i).setConnecting(false);
                        privateWifiAdapter.notifyDataSetChanged();
                    }
                }
                for (int i = 0; i < publicwifiInfo.size(); i++) {
                    if (publicwifiInfo.get(i).getWifiName().equals(autoConnectwifiName)) {
                        publicwifiInfo.get(i).setConnected(true);
                        publicwifiInfo.get(i).setConnecting(false);
                        publicwifiInfo.get(i).setSave(true);
                        publicWifiAdapter.notifyDataSetChanged();
                    }else {
                        publicwifiInfo.get(i).setConnected(false);
                        publicwifiInfo.get(i).setConnecting(false);
                        publicWifiAdapter.notifyDataSetChanged();
                    }
                }
                tvConnectState.setText("连接成功");
                isNetworkOnline();
            }

            setConfigWifiState();
            //获取当前连接的wifi
            getCurrentWifi();
        }
    }

    //判断是否可以上网
    private void isNetworkOnline() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvConnectState.setText("正在检测是否可以上网...");
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(WifiAdmin.getNetWorkStatus(WifiActivity.this)!=NETWORK_DISCONNECTION&&wifiAdmin.isNetworkOnline()==true){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvConnectState.setText("可以上网");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tvConnectState != null) tvConnectState.setVisibility(View.GONE);
                                layoutWifiSpeed.setVisibility(View.VISIBLE);
                            isStopScanWifi = false;//可以刷新WIFi
                        }
                    });

                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvConnectState.setText("不能上网");
                            isStopScanWifi = false;//可以刷新WIFi
                        }
                    });

                }


            }
        }.start();
    }

    //设置已经配置的WIFI为已经配置的状态
    private void setConfigWifiState() {
        for (int i = 0; i < priviatewifiInfo.size(); i++) {
            WifiConfiguration existConfig = wifiAdmin.IsExsits(priviatewifiInfo.get(i).getWifiName());
            if (existConfig!=null){
                priviatewifiInfo.get(i).setSave(true);
                privateWifiAdapter.notifyDataSetChanged();
            }
        }

        for (int i = 0; i < publicwifiInfo.size(); i++) {
            WifiConfiguration existConfig = wifiAdmin.IsExsits(publicwifiInfo.get(i).getWifiName());
            if (existConfig!=null){
                publicwifiInfo.get(i).setSave(true);
                publicWifiAdapter.notifyDataSetChanged();
            }
        }
    }

    //判断wifi是否打开
    private void IsOpenForWifi(Context context, Intent intent) {
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
                tvWifiName.setText("当前没有网络连接");
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
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        EventBus.getDefault().register(this);
        //动态注册广播
        registBroadCast();
        //获取下载速度和上传速度
        getDownloadSpeedAndUploadSpeed();

        wifiAdmin = new WifiAdmin(WifiActivity.this);
        lvPrivateWifi = (ListView) findViewById(R.id.lv_private_wifi);
        lvPublicWifi = (ListView) findViewById(R.id.lv_public_wifi);
        tvWifiName = (TextView) findViewById(R.id.tv_wifi_name);
        btnWifioOptimize = (Button) findViewById(R.id.btn_wifi_optimize);
        layoutWifiSpeed = (LinearLayout) findViewById(R.id.layout_wifi_speed);
        tvUploadSpeed = (TextView) findViewById(R.id.tv_upload_speed);
        tvDownloadSpeed = (TextView) findViewById(R.id.tv_download_speed);
        tvConnectState = (TextView) findViewById(R.id.tv_connect_state);
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

    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;
    //获取下载速度和上传速度
    private void getDownloadSpeedAndUploadSpeed() {
        //获取现在总的接收到的数据
        lastTotalRxBytes = TrafficStats.getTotalRxBytes()/1024;//总的接收到的数据
        lastTotalTxBytes = TrafficStats.getTotalTxBytes()/1024;//总的传输的数据
        lastTimeStamp = System.currentTimeMillis();
        Log.i("就大阿达啊","nowTotalRxBytes:"+lastTotalRxBytes+"  lastTimeStamp:"+lastTimeStamp);
        new Timer().schedule(task,1000,2000);

    }
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            showNetSpeed();
        }
    };
    private void showNetSpeed() {
        long nowTotalRxBytes = TrafficStats.getTotalRxBytes()/1024;
        long nowTotalTxBytes = TrafficStats.getTotalTxBytes()/1024;
        long nowTimeStamp = System.currentTimeMillis();
        long downloadSpeed = (nowTotalRxBytes-lastTotalRxBytes)/((nowTimeStamp-lastTimeStamp)/1000);
        long uploadSpeed = (nowTotalTxBytes-lastTotalTxBytes)/((nowTimeStamp-lastTimeStamp)/1000);

        lastTotalRxBytes=nowTotalRxBytes;
        lastTotalTxBytes=nowTotalTxBytes;
        lastTimeStamp=nowTimeStamp;
        Log.i("网速","downloadSpeed:"+String.valueOf(downloadSpeed)+"kB/s"+"  uploadSpeed:"+String.valueOf(uploadSpeed)+"kB/s");
//        Message msg = speedHandler.obtainMessage();
//        Bundle bundle = new Bundle();
//        bundle

    }
    @Override
    protected void onResume() {
        super.onResume();
        getCurrentWifi();
    }

    //获取当前的连接的WIFi,如果没有连接网络自动连接配置好的WIFi
    private void getCurrentWifi() {
        if (isOpen == true) {//wifi已经打开
            mWifiInfo = wManager.getConnectionInfo();
            int netWorkId = mWifiInfo.getNetworkId();
            int speed = mWifiInfo.getLinkSpeed();
            int rssi = mWifiInfo.getRssi();
            String bssid=mWifiInfo.getBSSID();
            Log.i("当前连接的网络","netWorkId:"+netWorkId+" speed:"+speed+"  rssi:"+rssi+"  bssid:"+bssid);
            if (netWorkId==-1&&speed==-1&&rssi==-200&&bssid.equals("00:00:00:00:00:00")){
                tvWifiName.setText("当前没有网络连接");
                isStopScanWifi=true;
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //在没有联网的情况下2S后自动连接已经配置好的WIFi
                                autoConnectConfigurationWifi();
                            }
                        });
                    }
                }.start();

            }else {
                String ssid = mWifiInfo.getSSID();
                String wifiName = ssid.substring(1, ssid.length() - 1);
                tvWifiName.setText(wifiName);
                for (int i = 0; i < publicwifiInfo.size(); i++) {
                    if (publicwifiInfo.get(i).getWifiName().equals(wifiName)) {
                        publicwifiInfo.get(i).setConnected(true);
                        publicwifiInfo.get(i).setConnecting(false);
                        publicWifiAdapter.notifyDataSetChanged();
                    }
                }
                for (int i = 0; i < priviatewifiInfo.size(); i++) {
                    if (priviatewifiInfo.get(i).getWifiName().equals(wifiName)) {
                        priviatewifiInfo.get(i).setConnected(true);
                        priviatewifiInfo.get(i).setConnecting(false);
                        privateWifiAdapter.notifyDataSetChanged();
                    }
                }
                //实时判断连接的网络是否可以联网(重复刷新wifi时)
                if (temp.equals("refresh")){
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            if(WifiAdmin.getNetWorkStatus(WifiActivity.this)!=NETWORK_DISCONNECTION&&wifiAdmin.isNetworkOnline()==true){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvConnectState.setVisibility(View.GONE);
                                        layoutWifiSpeed.setVisibility(View.VISIBLE);
                                    }
                                });
                            }else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvConnectState.setVisibility(View.VISIBLE);
                                        tvConnectState.setText("不能上网");
                                        layoutWifiSpeed.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    }.start();

                }
            }

        }

    }

    //在打开网络的情况下且没有连接网络如果存在已经配置好的WIFi则自动连接其中信号最好的Wifi
    private void autoConnectConfigurationWifi() {
        if (isOpen){
            configWifiInfo.clear();
            //获取已经配置的WIFi
            configWifiInfo=getConfigWifi();
            Log.i("configWifiInfo",configWifiInfo.size()+"");
            int maxLevel = getMaxLevelInConfigWifi();
            for (int i =0;i<configWifiInfo.size();i++){
                if (configWifiInfo.get(i).getLevel()==maxLevel){
                    temp = "configwifi";
                    int wcgID = wManager.addNetwork(configWifiInfo.get(i).getExistConfig());
                    boolean b = wManager.enableNetwork(wcgID, true);
                    autoConnectwifiName = configWifiInfo.get(i).getExistConfig().SSID.substring(1,configWifiInfo.get(i).getExistConfig().SSID.length()-1);
                    tvWifiName.setText(autoConnectwifiName);
                    layoutWifiSpeed.setVisibility(View.GONE);
                    tvConnectState.setVisibility(View.VISIBLE);
                    tvConnectState.setText("开始连接...");
                    //开启计时器，在20s内没有连接成功则连接失败，断开连接
                    startTimer(wifiName);
                }
            }
        }
    }

    //从配置好的wifi集合中获取到信号等级最高的配置wifi的下标
    private int getMaxLevelInConfigWifi() {
        int maxLevel =0;
        for (int i=0;i<configWifiInfo.size();i++){
           if (configWifiInfo.get(i).getLevel()>=maxLevel){
               maxLevel = configWifiInfo.get(i).getLevel();
           }
        }
        return maxLevel;
    }

    //获取配置好的WIFi
    private List<ConfigWifiInfoBean> getConfigWifi() {
        for (int i = 0; i < priviatewifiInfo.size(); i++) {
            WifiConfiguration existConfig = wifiAdmin.IsExsits(priviatewifiInfo.get(i).getWifiName());
            if (existConfig!=null){
                configWifiInfo.add(new ConfigWifiInfoBean(existConfig,priviatewifiInfo.get(i).getLevel()));
            }
        }

        for (int i = 0; i < publicwifiInfo.size(); i++) {
            WifiConfiguration existConfig = wifiAdmin.IsExsits(publicwifiInfo.get(i).getWifiName());
            if (existConfig!=null){
                configWifiInfo.add(new ConfigWifiInfoBean(existConfig,publicwifiInfo.get(i).getLevel()));
            }
        }
        return configWifiInfo;
    }

    private void setListviewItemListener() {
        //需要密码的wifi设置点击事件
        lvPrivateWifi.setOnItemClickListener(new PriviteWifiItemListener(priviatewifiInfo, WifiActivity.this,privateWifiAdapter));

        //公共wifi设置点击事件
        lvPublicWifi.setOnItemClickListener(new PublicWifiItemListener(publicwifiInfo, WifiActivity.this,publicWifiAdapter));

    }

    private void registBroadCast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NETWORK_STATE_CHANGED_ACTION);//监听网络连接的广播
        filter.addAction(WIFI_STATE_CHANGED_ACTION);//监听wifi打开的广播
        filter.addAction(SUPPLICANT_STATE_CHANGED_ACTION);  //是不是正在获得IP地址
        filter.addAction(CONNECTIVITY_ACTION);//连上与否
        filter.addAction(RSSI_CHANGED_ACTION);//信号变化
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
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(WifiActivity.this,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE},124);
        }else {
            List<ScanResult> scanResults = wManager.getScanResults();
            if (scanResults != null) {
                for (ScanResult result : scanResults) {
                    String wifiName = result.SSID;
                    int level = WifiManager.calculateSignalLevel(result.level, 100);
                    String capabilities = result.capabilities;//包含WPA都是需要密码
                    if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                        type = WifiAdmin.TYPE_WPA;
                    } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                        type = WifiAdmin.TYPE_WEP;
                    } else {
                        type = WifiAdmin.TYPE_NO_PASSWD;
                    }
                    if (type == WifiAdmin.TYPE_WPA || type == WifiAdmin.TYPE_WEP) {
                        if (priviatewifiInfo != null&& !TextUtils.isEmpty(wifiName)) {

                            priviatewifiInfo.add(new WifiInfoBean(wifiName, level, type));
                            privateWifiAdapter.notifyDataSetChanged();
                        }

                    } else if (type == WifiAdmin.TYPE_NO_PASSWD) {
                        if (priviatewifiInfo != null&& !TextUtils.isEmpty(wifiName)) {
                            publicwifiInfo.add(new WifiInfoBean(wifiName, level, type));
                            publicWifiAdapter.notifyDataSetChanged();
                        }

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
                ivLine.setVisibility(View.GONE);
            } else {
                layoutPublicWifi.setVisibility(View.VISIBLE);
                publicwifiInfo = removeTheSameNameWifi(publicwifiInfo);
                Collections.sort(publicwifiInfo);
                Collections.reverse(publicwifiInfo);
                publicWifiAdapter.notifyDataSetChanged();

            }

            //如果需要密码的WIFi和公共WIFI都没有时
            if (priviatewifiInfo.size() <= 0 && publicwifiInfo.size() <= 0) {
                layoutWifiList.setVisibility(View.GONE);
                layoutNoWifi.setVisibility(View.VISIBLE);
            } else {
                layoutWifiList.setVisibility(View.VISIBLE);
                layoutNoWifi.setVisibility(View.GONE);
            }

            getCurrentWifi();
            setConfigWifiState();
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
        EventBus.getDefault().unregister(this);
    }

    //eventbus接受点击连接wifi后改变Ui的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void WifiConnect(WifiConnectEvent event) {
        switch (event.getTemp()) {
            case "PublicWifiInfo":
                //初始化其他WIFi的连接状态，设置点击的wifi的连接状态
                setConnectState(event.getPosition(),event.getTemp());
                publicWifiAdapter.notifyDataSetChanged();
                temp = event.getTemp();
                wifiName = event.getWifiName();
                tvWifiName.setText(wifiName);
                layoutWifiSpeed.setVisibility(View.GONE);
                tvConnectState.setVisibility(View.VISIBLE);
                tvConnectState.setText("开始连接...");
                //开启计时器，在20s内没有连接成功则连接失败，断开连接
                startTimer(wifiName);
                break;
            case "priviatewifiInfo":
                //初始化其他WIFi的连接状态，设置点击的wifi的连接状态
                setConnectState(event.getPosition(),event.getTemp());
                privateWifiAdapter.notifyDataSetChanged();
                temp = event.getTemp();
                wifiName = event.getWifiName();
                tvWifiName.setText(wifiName);
                layoutWifiSpeed.setVisibility(View.GONE);
                tvConnectState.setVisibility(View.VISIBLE);
                tvConnectState.setText("开始连接...");
                startTimer(wifiName);
                break;
        }

    }

    ////初始化其他WIFi的连接状态，设置点击的wifi的连接状态
    private void setConnectState(int position, String temp) {
        if (temp.equals("PublicWifiInfo")){
            for (int i = 0;i<priviatewifiInfo.size();i++){
                priviatewifiInfo.get(i).setConnected(false);
                priviatewifiInfo.get(i).setConnecting(false);
                privateWifiAdapter.notifyDataSetChanged();
            }
            for (int i=0;i<publicwifiInfo.size();i++){
                if (i==position){
                    publicwifiInfo.get(i).setConnected(false);
                    publicwifiInfo.get(i).setConnecting(true);
                }else {
                    publicwifiInfo.get(i).setConnected(false);
                    publicwifiInfo.get(i).setConnecting(false);
                }
                publicWifiAdapter.notifyDataSetChanged();
            }



        }
        if (temp.equals("priviatewifiInfo")){
            for (int i = 0;i<publicwifiInfo.size();i++){
                publicwifiInfo.get(i).setConnected(false);
                publicwifiInfo.get(i).setConnecting(false);
                publicWifiAdapter.notifyDataSetChanged();
            }
            for (int i=0;i<priviatewifiInfo.size();i++){
                if (i==position){
                    priviatewifiInfo.get(i).setConnected(false);
                    priviatewifiInfo.get(i).setConnecting(true);
                }else {
                    priviatewifiInfo.get(i).setConnected(false);
                    priviatewifiInfo.get(i).setConnecting(false);
                }
                privateWifiAdapter.notifyDataSetChanged();
            }

        }
    }


    //eventbus接受时候停止刷新（点击wifi后停止扫描WIFi，关闭对话框或成功连接后开始扫描）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void scanWifi(ScanWifiEvent event) {
        switch (event.getTemp()) {
            case "stop":
                isStopScanWifi = true;
                break;
            case "start":
                isStopScanWifi = false;
                break;
        }

    }


    //eventbus接受断开网络时的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void WifiDisConnect(WifiDisconnectEvent event) {
        switch (event.getTemp()) {
            case "privatewifi":
                if (event.getI()==0){
                    tvWifiName.setText("当前没有网络连接");
                }
                isStopScanWifi=false;
                break;
            case "publicWifi":
                if (event.getI()==0){
                    tvWifiName.setText("当前没有网络连接");
                }
                isStopScanWifi = false;
                break;
        }

    }


    //开始计时
    private Timer timer = null;
    private boolean isConnectSuccess;
    public  void startTimer(final String wifiName){
        if (timer!=null){
            stopTimer();
        }
       isConnectSuccess=false;
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //在20s内没有连接成功则连接失败，断开连接
                if (!isConnectSuccess){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvConnectState.setText("连接失败");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //断开网络初始化所有网络的连接状态
                            for (int i = 0; i < publicwifiInfo.size(); i++) {
                                publicwifiInfo.get(i).setConnected(false);
                                publicwifiInfo.get(i).setConnecting(false);
                                if (publicwifiInfo.get(i).getWifiName()==wifiName){
                                    WifiConfiguration tempConfig = wifiAdmin.IsExsits(wifiName);
                                    if (tempConfig != null) {
                                        wManager.removeNetwork(tempConfig.networkId);
                                    }
                                }
                                publicWifiAdapter.notifyDataSetChanged();
                            }
                            for (int i = 0; i < priviatewifiInfo.size(); i++) {
                                priviatewifiInfo.get(i).setConnected(false);
                                priviatewifiInfo.get(i).setConnecting(false);
                                if (priviatewifiInfo.get(i).getWifiName()==wifiName){
                                    WifiConfiguration tempConfig = wifiAdmin.IsExsits(wifiName);
                                    if (tempConfig != null) {
                                        wManager.removeNetwork(tempConfig.networkId);
                                    }
                                }
                                privateWifiAdapter.notifyDataSetChanged();
                            }
                            isStopScanWifi=false;
                            tvConnectState.setVisibility(View.GONE);
                            tvWifiName.setText("当前没有网络连接");
                            Toast.makeText(WifiActivity.this,"连接失败，请重新连接",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        },20*1000);
    }

    //停止计时
    private void stopTimer() {
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
    }


    public void onRequestPermissionsResult(final int requestCode, String[] permissions, final int[] grantResults) {
        if (requestCode==124){
            List<ScanResult> scanResults = wManager.getScanResults();
            if (scanResults != null) {
                for (ScanResult result : scanResults) {
                    String wifiName = result.SSID;
                    int level = WifiManager.calculateSignalLevel(result.level, 100);
                    String capabilities = result.capabilities;//包含WPA都是需要密码
                    if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                        type = WifiAdmin.TYPE_WPA;
                    } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                        type = WifiAdmin.TYPE_WEP;
                    } else {
                        type = WifiAdmin.TYPE_NO_PASSWD;
                    }
                    if (type == WifiAdmin.TYPE_WPA || type == WifiAdmin.TYPE_WEP) {
                        if (priviatewifiInfo != null&& !TextUtils.isEmpty(wifiName)) {

                            priviatewifiInfo.add(new WifiInfoBean(wifiName, level, type));
                            privateWifiAdapter.notifyDataSetChanged();
                        }

                    } else if (type == WifiAdmin.TYPE_NO_PASSWD) {
                        if (priviatewifiInfo != null&& !TextUtils.isEmpty(wifiName)) {
                            publicwifiInfo.add(new WifiInfoBean(wifiName, level, type));
                            publicWifiAdapter.notifyDataSetChanged();
                        }

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
                ivLine.setVisibility(View.GONE);
            } else {
                layoutPublicWifi.setVisibility(View.VISIBLE);
                publicwifiInfo = removeTheSameNameWifi(publicwifiInfo);
                Collections.sort(publicwifiInfo);
                Collections.reverse(publicwifiInfo);
                publicWifiAdapter.notifyDataSetChanged();

            }

            //如果需要密码的WIFi和公共WIFI都没有时
            if (priviatewifiInfo.size() <= 0 && publicwifiInfo.size() <= 0) {
                layoutWifiList.setVisibility(View.GONE);
                layoutNoWifi.setVisibility(View.VISIBLE);
            } else {
                layoutWifiList.setVisibility(View.VISIBLE);
                layoutNoWifi.setVisibility(View.GONE);
            }

            getCurrentWifi();
            setConfigWifiState();
        }
    }
}
