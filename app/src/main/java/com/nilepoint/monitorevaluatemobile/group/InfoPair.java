package com.nilepoint.monitorevaluatemobile.group;

/**
 * Created by claudiatrafton on 8/20/17.
 */

public class InfoPair {

    private String title;
    private String value;

    public InfoPair(String title, String value) {
        this.value = value;
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
