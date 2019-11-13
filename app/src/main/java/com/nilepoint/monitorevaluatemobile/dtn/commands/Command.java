package com.nilepoint.monitorevaluatemobile.dtn.commands;

import com.nilepoint.api.MobileDevice;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.monitorevaluatemobile.logging.RemoteLogger;


import java.io.Serializable;

import io.protostuff.Tag;

/**
 * Created by ashaw on 6/29/17.
 */

public class Command implements Serializable {

    public transient  String TAG = getClass().getSimpleName();

    protected transient RemoteLogger logger = new RemoteLogger();

    @Tag(50)
    private Node sourceNode;

    @Tag(51)
    private Node destinationNode;

    @Tag(52)
    private MobileDevice sourceDevice;

    public Node getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    public Node getDestinationNode() {
        return destinationNode;
    }

    public void setDestinationNode(Node destinationNode) {
        this.destinationNode = destinationNode;
    }

    public MobileDevice getSourceDevice() {
        return sourceDevice;
    }

    public void setSourceDevice(MobileDevice sourceDevice) {
        this.sourceDevice = sourceDevice;
    }
}
