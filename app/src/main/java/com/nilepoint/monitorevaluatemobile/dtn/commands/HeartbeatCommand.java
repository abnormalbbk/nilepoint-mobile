package com.nilepoint.monitorevaluatemobile.dtn.commands;

import com.nilepoint.utils.ModelUtilities;

import java.util.Date;

import io.protostuff.Tag;

/**
 * Created by ashaw on
 * 7/4/17.
 */
public class HeartbeatCommand extends Command {
    @Tag(1)
    String uuid = ModelUtilities.randomUUID();

    @Tag(2)
    Date dateCreated = new Date();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
