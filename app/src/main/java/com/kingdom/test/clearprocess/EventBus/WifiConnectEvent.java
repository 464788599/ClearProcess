package com.kingdom.test.clearprocess.EventBus;

/**
 * Created by admin on 2016/9/28.
 */
public class WifiConnectEvent {
    String temp;
    String wifiName;
    int position;
    public WifiConnectEvent(String temp, String wifiName, int position) {
        this.temp =temp;
        this.wifiName = wifiName;
        this.position =position;

    }

    public int getPosition() {
        return position;
    }

    public String getTemp() {
        return temp;
    }

    public String getWifiName() {
        return wifiName;
    }
}
