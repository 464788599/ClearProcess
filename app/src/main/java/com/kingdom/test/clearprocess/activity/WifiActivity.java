package com.kingdom.test.clearprocess.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kingdom.test.clearprocess.EventBus.PublicWifiConnectEvent;
import com.kingdom.test.clearprocess.R;
import com.kingdom.test.clearprocess.adapter.PrivateWifiAdapter;
import com.kingdom.test.clearprocess.adapter.PublicWifiAdapter;
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

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY;
import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION;
import static com.kingdom.test.clearprocess.utils.WifiAdmin.WIFI_CONNECTED;
import static com.kingdom.test.clearprocess.utils.WifiAdmin.WIFI_CONNECT_FAILED;

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
    private ImageView ivConnented;
    private ProgressBar proConnenting;
    private LinearLayout layoutWifiSpeed;
    private TextView tvConnectState;
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
                    boolean b = intent.getBooleanExtra(EXTRA_NO_CONNECTIVITY,false);
                    Log.i("qqweqeqe",b+"");
                    if (b==false){//网络变化，连接网络完成时(不一定连接成功)
                        Log.i("没有发生变化",b+"");
                        //判断网络是否连接成功
                       int connetStated= wifiAdmin.isWifiContected(WifiActivity.this);
                        switch (connetStated){
                            case WIFI_CONNECTED://连接成功
                                Log.i("是否连接成功","连接成功");
                                break;
                            case WIFI_CONNECT_FAILED://连接失败
                                Log.i("是否连接成功","连接失败");
                                break;
                        }
                    }else {//网络变化，正在连接网络时

                    }
                    break;
            }
        }
    };
    private WifiInfo mWifiInfo;
    private WifiAdmin wifiAdmin;

    private void changeWithWifiConnectState(Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {//网络连接断开


        } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {//已经连接网络或连接上网络
            //获取当前连接的wifi
            getCurrentWifi();
            if (tvConnectState != null) {

                tvConnectState.setVisibility(View.GONE);
            }

            layoutWifiSpeed.setVisibility(View.VISIBLE);
            if (proConnenting != null){
                Toast.makeText(WifiActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                proConnenting.setVisibility(View.GONE);
            }

            if (ivConnented != null) ivConnented.setVisibility(View.VISIBLE);
        } else if (info.getState().equals(NetworkInfo.State.CONNECTING)) {//网络正在连接
            tvConnectState.setText("开始连接...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tvConnectState.setText("正在获取IP地址...");
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

    private void getCurrentWifi() {
        if (isOpen==true){//wifi已经打开
            mWifiInfo = wManager.getConnectionInfo();
            String ssid = mWifiInfo.getSSID();
            String wifiName= ssid.substring(1,ssid.length()-1);
            tvWifiName.setText(wifiName);
        }

    }

    private void setListviewItemListener() {
        //需要密码的wifi设置点击事件
        lvPrivateWifi.setOnItemClickListener(new PriviteWifiItemListener(priviatewifiInfo, WifiActivity.this));

        //公共wifi设置点击事件
        lvPublicWifi.setOnItemClickListener(new PublicWifiItemListener(publicwifiInfo, WifiActivity.this));

    }

    private void registBroadCast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NETWORK_STATE_CHANGED_ACTION);//监听网络连接的广播
        filter.addAction(WIFI_STATE_CHANGED_ACTION);//监听wifi打开的广播
        filter.addAction(SUPPLICANT_STATE_CHANGED_ACTION);  //是不是正在获得IP地址
        filter.addAction(CONNECTIVITY_ACTION);//连上与否
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
                int level =WifiManager.calculateSignalLevel(result.level,100);
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

                        priviatewifiInfo.add(new WifiInfoBean(wifiName, level, type,false,false));
                        privateWifiAdapter.notifyDataSetChanged();
                    }

                } else if (type == WifiAdmin.TYPE_NO_PASSWD) {
                    publicwifiInfo.add(new WifiInfoBean(wifiName, level, type,false,false));
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
        EventBus.getDefault().unregister(this);
    }


    //eventbus接受连接wifi后改变Ui的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void addSize(PublicWifiConnectEvent event) {
        switch (event.getTemp()) {
            case "PublicWifiInfo":
                View pubView = event.getView();
                ivConnented = (ImageView) pubView.findViewById(R.id.iv_select);
                proConnenting = (ProgressBar) pubView.findViewById(R.id.pro_connect);
                ivConnented.setVisibility(View.GONE);
                proConnenting.setVisibility(View.VISIBLE);
                tvWifiName.setText(event.getWifiInfoBean().getWifiName());
                layoutWifiSpeed.setVisibility(View.GONE);
                tvConnectState.setVisibility(View.VISIBLE);
                break;
            case "priviatewifiInfo":
                View priView = event.getView();
                ivConnented = (ImageView) priView.findViewById(R.id.iv_select);
                proConnenting = (ProgressBar) priView.findViewById(R.id.pro_connect);
                ivConnented.setVisibility(View.GONE);
                proConnenting.setVisibility(View.VISIBLE);
                tvWifiName.setText(event.getWifiInfoBean().getWifiName());
                layoutWifiSpeed.setVisibility(View.GONE);
                tvConnectState.setVisibility(View.VISIBLE);
                break;
        }

    }


}
