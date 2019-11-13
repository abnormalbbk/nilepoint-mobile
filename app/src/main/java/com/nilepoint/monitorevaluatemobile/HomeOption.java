package com.nilepoint.monitorevaluatemobile;

import android.content.Intent;

/***
 *
 * Created by claudiatrafton on 5/8/17.
 * Class that stores information about an option that can be on the Homescreen. It was created this way to make it easy to add and remove options
 */

public class HomeOption {

    private String text;
    private int iconId; //get icon from resource file
    private int colorId; //get color from resource file
    private Intent intent; //an intent for the activity that should be opened on click

    public HomeOption(String text, int iconId, int colorId, Intent intent){
        this.text = text;
        this.iconId = iconId;
        this.colorId = colorId;
        this.intent = intent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

}
