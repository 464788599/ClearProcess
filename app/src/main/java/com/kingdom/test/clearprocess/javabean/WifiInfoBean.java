package com.kingdom.test.clearprocess.javabean;

/**
 * Created by admin on 2016/9/26.
 */

public class WifiInfoBean implements Comparable<WifiInfoBean> {
    String wifiName;
    int level;
    int type;
    boolean isConnecting;
    boolean isConnected;
    boolean isSave;

    public boolean isSave() {
        return isSave;
    }

    public void setSave(boolean save) {
        isSave = save;
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setConnecting(boolean connecting) {
        isConnecting = connecting;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public WifiInfoBean(String wifiName, int level, int type) {
        this.wifiName = wifiName;
        this.level = level;
        this.type = type;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int compareTo(WifiInfoBean another) {
        int r=this.level-another.level;
        return r;
    }
}
