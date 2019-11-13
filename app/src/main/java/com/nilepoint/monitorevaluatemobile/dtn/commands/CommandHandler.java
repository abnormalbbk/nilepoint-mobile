package com.nilepoint.monitorevaluatemobile.dtn.commands;

import com.nilepoint.api.MobileDevice;
import com.nilepoint.dtn.discovery.Node;

/**
 * Created by ashaw on 11/21/17.
 */

public abstract class CommandHandler <E extends Command> {
    protected Class commandClass;

    protected MobileDevice deviceFilter;

    public CommandHandler(Class commandClass) {
        this.commandClass = commandClass;
    }

    public abstract void handleCommand(E cmd);

    public Class getCommandClass() {
        return commandClass;
    }

    public void setCommandClass(Class commandClass) {
        this.commandClass = commandClass;
    }

    public MobileDevice getDeviceFilter() {
        return deviceFilter;
    }

    public void setDeviceFilter(MobileDevice deviceFilter) {
        this.deviceFilter = deviceFilter;
    }

    @Override
    public String toString() {
        return "CommandHandler: (" + commandClass + ")";
    }

    transient ResponseCallback callback;

    public ResponseCallback getCallback() {
        return callback;
    }

    protected void msg(String message){
        if (callback != null){
            callback.infoMessage(message);
        }
    }
    protected void err(String message){
        if (callback != null){
            callback.errorMessage(message);
        }
    }

    public void setCallback(ResponseCallback callback) {
        this.callback = callback;
    }
}
