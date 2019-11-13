package com.nilepoint.monitorevaluatemobile.dtn;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.nilepoint.dtn.Bundle;
import com.nilepoint.dtn.DTN;
import com.nilepoint.dtn.convergence.FilteringReceiverListener;
import com.nilepoint.dtn.convergence.ProtostuffPayload;
import com.nilepoint.dtn.discovery.NeighborRegistration;
import com.nilepoint.model.Area;
import com.nilepoint.model.Household;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.Project;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.commands.Command;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CommandCenter;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CopyDatabaseCommand;
import com.nilepoint.monitorevaluatemobile.dtn.commands.FullDatabaseResponse;
import com.nilepoint.monitorevaluatemobile.stats.StatisticsManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

/**
 * Created by ashaw on 7/16/17.
 */

public class BluetoothSocketReceiverFactory {

    public static String TAG = "BtSocketRecF";

    static BluetoothConvergenceLayer layer = WLTrackApp.dtnService.btlayer;
    static DTN dtn = WLTrackApp.dtnService.getDTN();

    static BluetoothSocketReceiver get(final BluetoothSocket socket){
        final BluetoothSocketReceiver receiver = new BluetoothSocketReceiver(socket);

        // TODO move this out
        receiver.addListener(new FilteringReceiverListener<Command>(Command.class) {
            @Override
            public void filteredObjectReceived(Command o) {
                Log.d(TAG,"BluetoothSocketReceiver: GOT COMMAND: " + o);

                NeighborRegistration reg = layer.getNeighborRegistry().findRegistrationByLocalAddress(socket
                        .getRemoteDevice().getAddress());

                if (reg != null){
                    o.setSourceNode(reg.getNode());
                }

                // new method for handling commands

                CommandCenter.getInstance().commandReceived(o);
            }
            @Override
            public void objectRejected(Object o) {

                Log.d(TAG,"BluetoothServer: Object rejected for command listener: " + o);
            }

            @Override
            public void receiverShutdown() {

                receiver.stopReceiver();
            }
        });

        receiver.addListener(new FilteringReceiverListener<Bundle>(Bundle.class) {
            @Override
            public void receiverShutdown() {
                Log.d(TAG,"BluetoothAcceptThread: Receiver error");
                /*if (listener != null){
                    listener.onDeviceDisconnect(socket.getRemoteDevice());
                }*/
            }

            @Override
            public void objectRejected(Object o) {
                // unknown object
            }

            @Override
            public void filteredObjectReceived(Bundle bundle) {
                String remoteAddress = socket.getRemoteDevice()
                        .getAddress();

                NeighborRegistration reg = layer.getNeighborRegistry()
                        .findRegistrationByLocalAddress(remoteAddress);

                // the ID will be the device ID until it gets the first bundle,
                // this will update it to the UUID of the device

                if (reg != null && !reg.getNode().getId().equals(bundle.getSource().getUri().getHost())){
                    reg.getNode().setId(bundle.getSource().getUri().getHost());

                    Log.d(TAG,"BluetoothAcceptThread: Updating node from "
                            + socket.getRemoteDevice().getAddress()
                            + " to " + reg.getNode().getId() );
                }

                String sourceId = bundle.getSource().getUri().getHost();

                List<String> bundles = layer.getBundlesReceived().get(sourceId);

                if (bundles == null){
                    bundles = new ArrayList<>();

                    layer.getBundlesReceived().put(sourceId, bundles);
                }

                bundles.add(bundle.getUuid());

                layer.handleBundle(bundle);
            }
        });

        return receiver;
    }
}
