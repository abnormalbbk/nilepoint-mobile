package com.nilepoint.monitorevaluatemobile.dtn.handlers;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.api.MobileDevice;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.model.Photo;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.MobileDeviceRegistry;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.commands.HelloCommand;
import com.nilepoint.monitorevaluatemobile.user.UserSession;


/**
 * Created by ashaw on 11/21/17.
 */

public class HelloCommandHandler extends CommandHandler<HelloCommand> {
    public static String TAG = "HelloCommandHandler";

    boolean dbAsk = false;

    public HelloCommandHandler() {
        super(HelloCommand.class);
    }

    @Override
    public void handleCommand(HelloCommand cmd) {
        try {

            Node node = cmd.getSourceNode();
            MobileDevice device = cmd.getSourceDevice();

            MobileDeviceRegistry mobileDeviceRegistry = MobileDeviceRegistry.getInstance();

            // first contact

            if (!mobileDeviceRegistry.isTracked(device)) {
                System.out.println("Tracking device " + cmd.getSourceDevice());

                mobileDeviceRegistry.track(device, node);

                if (cmd.getUserPhoto() != null) {

                    Log.i(TAG, "Got user photo from device " + cmd.getDeviceId());

                    Photo photo = Photo.fromBase64(cmd.getUserPhoto());

                    mobileDeviceRegistry.addPhoto(device, photo);
                }

            }

            WLTrackApp.dtnService.btlayer.getNeighborRegistry().addOrUpdateRegistration(node);

        } catch (Exception ex){
            Crashlytics.logException(ex);
            ex.printStackTrace();
        }

    }
}
