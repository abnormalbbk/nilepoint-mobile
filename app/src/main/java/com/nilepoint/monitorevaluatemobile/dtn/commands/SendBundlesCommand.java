package com.nilepoint.monitorevaluatemobile.dtn.commands;

import com.nilepoint.utils.ModelUtilities;

import io.protostuff.Tag;

/**
 * Created by ashaw on 6/29/17.
 */

public class SendBundlesCommand extends Command {
    @Tag(1)
    String uuid = ModelUtilities.randomUUID();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
