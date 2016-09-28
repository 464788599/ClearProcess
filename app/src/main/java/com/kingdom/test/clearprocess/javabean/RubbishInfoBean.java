package com.kingdom.test.clearprocess.javabean;

import android.graphics.drawable.Drawable;

/**
 * Created by admin on 2016/9/23.
 */
public class RubbishInfoBean implements Comparable<RubbishInfoBean> {
    String name;
    Drawable icon;
    boolean isChecked;
    long size;
    String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public RubbishInfoBean(String name, Drawable icon, long size, String path) {
        this.name = name;
        this.icon = icon;
        this.size = size;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        icon = icon;
    }

    public boolean isChecked() {
        return isChecked;
    }
    //取反
    public void setTag(boolean isChecked) {
        this.isChecked = !isChecked;
    }
    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public int compareTo(RubbishInfoBean another) {
        int r = 0;
        r = (int) (this.size - another.size);
        return 0;
    }
}
