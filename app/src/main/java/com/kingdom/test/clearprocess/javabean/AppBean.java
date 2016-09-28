package com.kingdom.test.clearprocess.javabean;

import android.graphics.drawable.Drawable;

/**
 * Created by admin on 2016/9/14.
 */
public class AppBean implements Comparable<AppBean> {
    String appName;
    Drawable appICon;
    boolean isChecked;
    float memory;
    String processName;
    int importance;
    public AppBean(String appName, Drawable appICon, float memory, String processName, int importance) {
        this.appName = appName;
        this.appICon = appICon;
        this.memory = memory;
        this.processName = processName;
        this.importance = importance;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppICon() {
        return appICon;
    }

    public void setAppICon(Drawable appICon) {
        this.appICon = appICon;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    //取反
    public void setTag(boolean isChecked) {
        this.isChecked = !isChecked;
    }

    public float getMemory() {
        return memory;
    }

    public void setMemory(float memory) {
        this.memory = memory;
    }

    @Override
    public int compareTo(AppBean another) {
        int r= 0;
        if (this.importance != 0 && another.importance != 0) {
             r = this.importance - another.importance;
        }
        return r;
    }
}
