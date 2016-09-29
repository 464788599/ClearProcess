package com.kingdom.test.clearprocess.EventBus;

/**
 * Created by admin on 2016/9/29.
 */
public class WifiDisconnectEvent {
    String temp;
    int i;

    public WifiDisconnectEvent(String temp, int i) {
        this.temp = temp;
        this.i = i;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }
}
