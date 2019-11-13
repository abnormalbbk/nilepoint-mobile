package com.nilepoint.monitorevaluatemobile.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

/**
 * Created by claudiatrafton on 3/9/17.
 */
//TODO build out settings model to be extensible so that new settings can be easily added. Settings will be created on the fly and handled by a single fragment and intent
public class InternalSetting extends Preference {

    protected String settingID;
    protected String settingTitle;
    //protected PreferenceCategory settingType;

    public InternalSetting(Context context, String id, String title){
        super(context);
        settingID = id;
        settingTitle = title;
        this.setTitle(settingTitle);
        this.setKey(settingID);
        this.setSummary(settingTitle);

    }

    //Constructor for dropdown TODO change list passed in to be key value pairs
    /*public InternalSetting(Context context, DropDownPreference dropDown, ArrayList<String> options){
        this(context, id, title);

    } */

    /*public InternalSetting(Context context,String id, String title, SwitchPreferenceCompat switchPref){
        super(context);
        settingID = id;
        settingTitle = title;
        this.setTitle(settingTitle);
        this.setKey(settingID);
    } */

    /*So that we don't repeat ourselves when adding new settings, handles nitty gritties*/

    //Display the setting
    private void addToScreen(PreferenceScreen screen){
        screen.addPreference(this);
    }

    public String getSettingID() {
        return settingID;
    }

    public void setSettingID(String settingID) {
        this.settingID = settingID;
    }

    public String getSettingTitle() {
        return settingTitle;
    }

    public void setSettingTitle(String settingTitle) {
        this.settingTitle = settingTitle;
    }
}
