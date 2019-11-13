package com.nilepoint.monitorevaluatemobile.dtn.commands;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import io.protostuff.Tag;

/**
 * Created by ashaw on 11/26/17.
 */

public class ResponseMessageCommand extends Command {
    @Tag(1)
    String message;

    @Tag(2)
    int progress;

    public ResponseMessageCommand() {
    }

    public ResponseMessageCommand(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
