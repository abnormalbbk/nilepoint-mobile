package com.nilepoint.monitorevaluatemobile.dtn.handlers;

import android.content.Context;

import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.commands.ResponseMessageCommand;

/**
 * Created by ashaw on 11/26/17.
 */

public class ResponseMessageHandler extends CommandHandler<ResponseMessageCommand> {
    Context context;
    public ResponseMessageHandler(Context context) {
        super(ResponseMessageCommand.class);
        this.context = context;
    }

    @Override
    public void handleCommand(ResponseMessageCommand cmd) {
        System.out.println("Got response message: " + cmd.getMessage());

        WLTrackApp application = (WLTrackApp) context.getApplicationContext();

        if (application.getCurrentActivity() != null) {
            //WLTrackApp.customToast(application.getCurrentActivity(), cmd.getMessage());
        }
    }
}
