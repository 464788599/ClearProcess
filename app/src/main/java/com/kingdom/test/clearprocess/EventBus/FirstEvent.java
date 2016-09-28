package com.kingdom.test.clearprocess.EventBus;

/**
 * Created by admin on 2016/9/23.
 */
public class FirstEvent {
    String temp;
    long size;

    public FirstEvent( String temp,long size) {
        this.size = size;
        this.temp = temp;
    }

    public String getTemp() {
        return temp;
    }

    public long getSize() {
        return size;
    }
}
