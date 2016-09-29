package com.kingdom.test.clearprocess.EventBus;

/**
 * Created by admin on 2016/9/28.
 */
public class WifiConnectEvent {
    String temp;
    String wifiName;
    public WifiConnectEvent(String temp, String wifiName) {
        this.temp =temp;
        this.wifiName = wifiName;

    }

    public String getTemp() {
        return temp;
    }

    public String getWifiName() {
        return wifiName;
    }
}
