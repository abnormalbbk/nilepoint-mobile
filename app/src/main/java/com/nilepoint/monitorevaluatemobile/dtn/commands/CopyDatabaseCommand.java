package com.nilepoint.monitorevaluatemobile.dtn.commands;

import io.protostuff.Tag;

/**
 * Created by ashaw on 7/16/17.
 */

public class CopyDatabaseCommand extends Command {
    @Tag(1)
    boolean askForSend;

    @Tag(2)
    String hash;

    public CopyDatabaseCommand(){

    }

    public CopyDatabaseCommand(boolean askForSend, String hash) {
        this.askForSend = askForSend;
        this.hash = hash;
    }

    public CopyDatabaseCommand(boolean askForSend) {
        this.askForSend = askForSend;
    }

    public boolean isAskForSend() {
        return askForSend;
    }

    public void setAskForSend(boolean askForSend) {
        this.askForSend = askForSend;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
