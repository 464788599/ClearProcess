package com.kingdom.test.clearprocess.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kingdom.test.clearprocess.R;
import com.kingdom.test.clearprocess.view.WaveProgressView;


public class BatteryActivity extends AppCompatActivity {
    WaveProgressView mWaveProgressView;
    private TextView tvElectricityNum;
    private TextView tvVoltageNum;
    private TextView tvTemperatureNum;
    private TextView tvCheckBtn;
    private RelativeLayout batteryBar;
    private ImageView ivback;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra("status", 0);
            int health = intent.getIntExtra("health", 0);
            boolean present = intent.getBooleanExtra("present", false);
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 0);
            int icon_small = intent.getIntExtra("icon-small", 0);
            int plugged = intent.getIntExtra("plugged", 0);
            int voltage = intent.getIntExtra("voltage", 0);
            int temperature = intent.getIntExtra("temperature", 0);
            String technology = intent.getStringExtra("technology");

            Log.i("statusdadadada",status+"");
            //电池的状态
            switch (status){
                case BatteryManager.BATTERY_STATUS_UNKNOWN://未知状态
                    break;
                case BatteryManager.BATTERY_STATUS_CHARGING://充电状态
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING://放电中
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING://未充电
                    break;
                case BatteryManager.BATTERY_STATUS_FULL://满电状态
                    break;
            }

            //电池健康的状态5564654564654564564564444444444444444444444444444444444444444444444444899999999999999999999999999999
            switch (status){
                case BatteryManager.BATTERY_HEALTH_UNKNOWN://未知状态
                    break;
                case BatteryManager.BATTERY_HEALTH_GOOD://良好
                    break;
                case BatteryManager.BATTERY_HEALTH_OVERHEAT://
                    break;
                case BatteryManager.BATTERY_HEALTH_DEAD://没电
                    break;
                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE://过电压
                    break;
                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE://未知错误
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        tvElectricityNum = (TextView) findViewById(R.id.tv_electricity_num);
        tvVoltageNum = (TextView) findViewById(R.id.tv_voltage_num);
        tvTemperatureNum = (TextView) findViewById(R.id.tv_temperature_num);
        tvCheckBtn= (TextView) findViewById(R.id.textView);
        batteryBar = (RelativeLayout) findViewById(R.id.battery_bar);
        ivback = (ImageView) findViewById(R.id.iv_back);
        mWaveProgressView = (WaveProgressView) findViewById(R.id.wave_view_bar);

        //注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
