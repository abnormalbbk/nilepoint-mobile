package com.nilepoint.monitorevaluatemobile.admin;

import android.os.Build;

import com.nilepoint.api.MobileDevice;
import com.nilepoint.model.Device;
import com.nilepoint.model.Setting;
import com.nilepoint.monitorevaluatemobile.persistence.Environment;
import com.nilepoint.monitorevaluatemobile.settings.DTNSettingsActivity;
import com.nilepoint.monitorevaluatemobile.settings.SettingsMixin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.paperdb.Paper;

/**
 * Class that will handle all first startup workflow for the app.
 *
 */

public class FirstStartupRunner extends SettingsMixin {

    public boolean isFirstStartup(){
        // if there's no device ID this is the first boot.
        return Paper.book().contains("device.id") == false;
    }

    public void startup(){
        createDefaults();
    }

    public void startupTest(){
        startup();
    }

    public void createDefaults(){
        set(DTNSettingsActivity.AMQP_LAYER_ACTIVE_LABEL, "true");
        set(DTNSettingsActivity.TCP_LAYER_ACTIVE_LABEL, "false");
        set(DTNSettingsActivity.BT_LAYER_ACTIVE_LABEL, "true");
    }

}
