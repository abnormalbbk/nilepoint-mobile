package com.nilepoint.monitorevaluatemobile.logging;

import android.util.Log;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.api.MobileDevice;
import com.nilepoint.dtn.convergence.ConvergenceLayer;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.paperdb.Paper;

/**
 * Created by ashaw on 8/24/17.
 *
 * Class to use the bundle layer to send log messages to the exchnge.
 */

public class RemoteLogger {
    MobileDevice device;

    ExecutorService executor =  Executors.newFixedThreadPool(10);

    public RemoteLogger(){

        if (Paper.book().contains("device.api")){
            device = Paper.book().read("device.api");
        }
    }
    public RemoteLogger(MobileDevice device) {
        this.device = device;
    }

    public void debug(String tag, String message){
        //Log.d(tag, message);
        System.out.println(tag + ":" + message);
        sendLogAsMessage(tag, "DEBUG", message);
    }

    public void info(String tag, String message){

        //Log.i(tag, message);
        System.out.println(tag + ":" + message);
        sendLogAsMessage(tag, "INFO", message);
    }

    public void error(String tag, String message){
       // Log.e(tag, message);
        System.out.println(tag + ":" + message);
        sendLogAsMessage(tag, "ERROR", message);
    }

    public void error(String tag, String message, Throwable ex){
       // Log.e(tag, message, ex);
        sendLogAsMessage(tag, "ERROR", message + " exception: " + ExceptionUtils.getStackTrace(ex));
    }

    private void sendLogAsMessage(String tag, String severity, String message){
        final MapMessage msg = new MapMessage();

        msg.put("className", "Log");

        if (device != null) {
            msg.put("device.id", device.getId());
        }

        msg.put("message", message);

        msg.put("severity", severity);

        msg.put("tag", tag);

        try {
            final ConvergenceLayer layer = WLTrackApp
                    .dtnService
                    .amqpConvergenceLayer;

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (layer != null) {
                        layer.sendToAllNeighbors(WLTrackApp.dtnService.createBundle(msg));
                    }
                }
            });
        } catch (Throwable tr){
            // ignore
        }



    }
}
