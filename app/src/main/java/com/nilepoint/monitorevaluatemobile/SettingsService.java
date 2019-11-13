package com.nilepoint.monitorevaluatemobile;

import java.io.Serializable;

import io.paperdb.Paper;

/**
 * Created by ashaw on 11/14/17.
 *
 *  Synchronized settings service.
 */

public class SettingsService {
    private static SettingsService instance;

    static {
        instance = new SettingsService();
    }

    public synchronized void put(String key, Serializable o){
        Paper.book().write(key, o);
    }

    public synchronized <E> E get (String key){
        return Paper.book().read(key);
    }

    public static SettingsService getInstance() {
        return instance;
    }
}
