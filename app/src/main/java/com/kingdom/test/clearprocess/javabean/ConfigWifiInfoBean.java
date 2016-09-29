package com.kingdom.test.clearprocess.javabean;

import android.net.wifi.WifiConfiguration;

/**
 * Created by admin on 2016/9/29.
 */

public class ConfigWifiInfoBean {
    WifiConfiguration existConfig;
    int level;

    public ConfigWifiInfoBean(WifiConfiguration existConfig, int level) {
        this.existConfig = existConfig;
        this.level = level;
    }

    public WifiConfiguration getExistConfig() {
        return existConfig;
    }

    public void setExistConfig(WifiConfiguration existConfig) {
        this.existConfig = existConfig;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
