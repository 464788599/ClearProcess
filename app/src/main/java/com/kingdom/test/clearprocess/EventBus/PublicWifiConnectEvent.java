package com.kingdom.test.clearprocess.EventBus;

import android.view.View;

import com.kingdom.test.clearprocess.javabean.WifiInfoBean;

/**
 * Created by admin on 2016/9/28.
 */
public class PublicWifiConnectEvent {
    String temp;
    WifiInfoBean wifiInfoBean;
    View view;
    public PublicWifiConnectEvent(String temp, WifiInfoBean wifiInfoBean, View view) {
        this.temp =temp;
        this.wifiInfoBean = wifiInfoBean;
        this.view = view;
    }

    public View getView() {
        return view;
    }

    public String getTemp() {
        return temp;
    }

    public WifiInfoBean getWifiInfoBean() {
        return wifiInfoBean;
    }
}
