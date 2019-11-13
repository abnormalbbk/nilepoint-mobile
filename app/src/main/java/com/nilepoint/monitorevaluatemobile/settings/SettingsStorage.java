package com.nilepoint.monitorevaluatemobile.settings;

import com.nilepoint.model.Setting;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;

import io.realm.Realm;

/**
 * Created by ashaw on 6/18/17.
 */

public class SettingsStorage {

    /**
     * Get a setting from realm
     *
     * @param setting
     * @return
     */
    public Setting getSetting(String setting){
        Realm realm = Realm.getDefaultInstance();

        return realm.where(Setting.class).equalTo("name", setting).findFirst();
    }

    /**
     * Store a setting in realm
     *
     * @param setting
     * @param value
     */
    public void setSetting(final String setting, final String value){
        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Setting set = realm.where(Setting.class).equalTo("name", setting).findFirst();

                    if (set == null) {
                        set = new Setting(setting, value);
                        realm.copyToRealm(set);
                    } else {
                        set.setValue(value);
                    }
                }
            });

        } finally {
            if( realm != null) {
                realm.close();
            }
        }
    }

    public Boolean getBoolean(String setting){
        Setting s = getSetting(setting);
        if (s == null){
            return false;
        }

        return Boolean.valueOf(s.getValue());
    }
}
