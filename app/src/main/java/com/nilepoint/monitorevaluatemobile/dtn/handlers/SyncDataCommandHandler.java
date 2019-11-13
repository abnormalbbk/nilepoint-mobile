package com.nilepoint.monitorevaluatemobile.dtn.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.nilepoint.api.MobileDevice;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.BluetoothConvergenceLayer;
import com.nilepoint.monitorevaluatemobile.dtn.MobileDeviceRegistry;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.commands.ResponseMessageCommand;
import com.nilepoint.monitorevaluatemobile.dtn.commands.SyncDataCommand;
import com.nilepoint.monitorevaluatemobile.dtn.commands.SyncDataResponse;
import com.nilepoint.monitorevaluatemobile.init.ConnectedPeerActivity;

/**
 * Created by ashaw on 2/6/18.
 */

public class SyncDataCommandHandler extends CommandHandler<SyncDataCommand> {

    transient BluetoothConvergenceLayer net = WLTrackApp.dtnService.btlayer;

    Context context;

    public SyncDataCommandHandler(Context context) {
        super(SyncDataCommand.class);
        this.context = context;
    }

    @Override
    public void handleCommand(SyncDataCommand cmd) {
        // if this command is coming back from the other device we don't have to start up the activity.
        if (!cmd.getClient()) {

            System.out.println("Got SyncDataCommand - starting activity");

            MobileDeviceRegistry reg = MobileDeviceRegistry.getInstance();

            Intent intent = new Intent(context, ConnectedPeerActivity.class);

            MobileDevice device = cmd.getSourceDevice();

            if (reg.findNodeByDevice(device) == null) {
                System.out.println("Can't find node for device (before Hello?) " +
                        "- Tracking device " + cmd.getSourceDevice());

                device = cmd.getSourceDevice();

                reg.track(device, cmd.getSourceNode());
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("device.id", device.getId());
            intent.putExtra("isClient", Boolean.TRUE);

            context.startActivity(intent);
        }

        // send the response back to the requesting client.

        sendChunkedSyncData(cmd, 500);
    }


    private void sendChunkedSyncData(SyncDataCommand cmd, int size){
        Node node =  cmd.getSourceNode();

        net.sendRaw(new ResponseMessageCommand("Receiving households...")
                , node);

        int i = 0;
        for (SyncDataResponse data : SyncDataResponse.getHouseholdsInChunks(size)) {
            net.sendRaw(data
                    , node);

            net.sendRaw(new ResponseMessageCommand("Receiving households " + (i+1) + "-" + (i+size))
                    , node);

            i+= size;
        }

        net.sendRaw(new ResponseMessageCommand("Receiving participants...")
                , node);

        i = 0;

        for (SyncDataResponse data : SyncDataResponse.getParticipantsInChunks(size)) {
            net.sendRaw(data
                    , node);
            net.sendRaw(new ResponseMessageCommand("Receiving participants " + (i+1) + "-" + (i+size))
                    , node);
            i+= size;
        }

        SyncDataResponse activities = new SyncDataResponse();

        net.sendRaw(new ResponseMessageCommand("Building activity list...")
                , node);

        activities.prepareActivities();

        net.sendRaw(activities
                , node);

        SyncDataResponse groups = new SyncDataResponse();

        net.sendRaw(new ResponseMessageCommand("Building group list...")
                , node);

        groups.prepareGroups();

        if (cmd.getInit() == true) {
            SyncDataResponse systemData = new SyncDataResponse();

            net.sendRaw(new ResponseMessageCommand("New device initialization, Sending system data (projects, area)")
                    , node);

            net.sendRaw(groups
                    , node);

            systemData.prepareSystemData();

            systemData.setSyncDone(true);

            net.sendRaw(systemData
                    , node);
        } else {
            // if not, groups are the last message to send.
            groups.setSyncDone(true);
            net.sendRaw(groups
                    , node);
        }

        net.sendRaw(new ResponseMessageCommand(String.format("Peer is done syncing data."))
                , node);

        //net.sendRaw(new SyncDataResponse(cmd.getSourceNode()), cmd.getSourceNode());
    }
}
