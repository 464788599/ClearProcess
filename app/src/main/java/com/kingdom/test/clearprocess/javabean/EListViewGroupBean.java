package com.kingdom.test.clearprocess.javabean;

/**
 * Created by admin on 2016/9/20.
 */
public class EListViewGroupBean {
    String rubbishCategory;
    long rubbishSize;

    public EListViewGroupBean(String rubbishCategory, long rubbishSize) {
        this.rubbishCategory = rubbishCategory;
        this.rubbishSize = rubbishSize;
    }

    public String getRubbishCategory() {
        return rubbishCategory;
    }

    public void setRubbishCategory(String rubbishCategory) {
        this.rubbishCategory = rubbishCategory;
    }

    public long getRubbishSize() {
        return rubbishSize;
    }

    public void setRubbishSize(long rubbishSize) {
        this.rubbishSize = rubbishSize;
    }
}
