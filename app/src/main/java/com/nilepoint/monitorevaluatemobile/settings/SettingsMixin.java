package com.nilepoint.monitorevaluatemobile.settings;

import android.util.Log;

import com.nilepoint.model.Setting;

import java.io.Serializable;

import io.paperdb.Paper;
import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by ashaw on 6/19/17.
 */

public class SettingsMixin {

    public static String TAG = "SettingsMixin";

    public void store(final Serializable o){

    }

    public <E> E get(String name){
        return Paper.book().read(name);
    }

    public void set(String name, Serializable value){
        Paper.book().write(name, value);
    }


}
