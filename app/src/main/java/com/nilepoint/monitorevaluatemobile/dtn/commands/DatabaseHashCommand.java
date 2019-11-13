package com.nilepoint.monitorevaluatemobile.dtn.commands;

import io.protostuff.Tag;

/**
 * Created by ashaw on 8/24/17.
 */

public class DatabaseHashCommand extends Command {
    @Tag(1)
    String hash;

    public DatabaseHashCommand(String hash) {
        this.hash = hash;
    }
}
