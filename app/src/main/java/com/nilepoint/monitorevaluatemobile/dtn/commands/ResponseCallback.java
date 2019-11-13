package com.nilepoint.monitorevaluatemobile.dtn.commands;

/**
 * Created by ashaw on 2/6/18.
 */

public interface ResponseCallback {
    void infoMessage(String message);
    void errorMessage(String error);
    void isDone();
}
