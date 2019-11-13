package com.nilepoint.monitorevaluatemobile.dtn.commands;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.model.Household;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.BluetoothConvergenceLayer;

import java.util.ArrayList;
import java.util.List;

import io.protostuff.Tag;
import io.realm.Realm;

/**
 * Created by ashaw on 2/5/18.
 */

public class SyncDataCommand extends Command {

    transient Node node;

    @Tag(1)
    Boolean isClient = false;
    @Tag(2)
    Boolean isInit = false;

    public SyncDataCommand() {
    }

    public SyncDataCommand(Node node, Boolean isClient) {
        this.node = node;
        this.isClient = isClient;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Boolean getClient() {
        return isClient;
    }

    public void setClient(Boolean client) {
        isClient = client;
    }

    public Boolean getInit() {
        return isInit;
    }

    public void setInit(Boolean init) {
        isInit = init;
    }
}
