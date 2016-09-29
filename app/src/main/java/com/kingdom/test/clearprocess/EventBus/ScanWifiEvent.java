package com.kingdom.test.clearprocess.EventBus;

/**
 * Created by admin on 2016/9/29.
 */
public class ScanWifiEvent {
    String temp;
    boolean isStop;

    public ScanWifiEvent(String temp, boolean isStop) {
        this.temp = temp;
        this.isStop = isStop;
    }

    public String getTemp() {
        return temp;
    }

    public boolean isStop() {
        return isStop;
    }
}
