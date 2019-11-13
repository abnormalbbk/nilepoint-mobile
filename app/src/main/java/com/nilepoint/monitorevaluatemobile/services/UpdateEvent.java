package com.nilepoint.monitorevaluatemobile.services;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.dtn.Block;
import com.nilepoint.dtn.Bundle;
import com.nilepoint.dtn.convergence.ProtostuffPayload;

/**
 * Created by ashaw on 6/18/17.
 */

public class UpdateEvent {
    UpdateEventType type;

    Bundle bundle;

    boolean force;

    public UpdateEvent(UpdateEventType type, Bundle bundle) {
        this.bundle = bundle;
        this.type = type;
    }


    public UpdateEvent(UpdateEventType type, Bundle bundle, boolean force) {
        this.bundle = bundle;
        this.type = type;
        this.force = force;
    }

    public UpdateEventType getType() {
        return type;
    }

    public void setType(UpdateEventType type) {
        this.type = type;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public MapMessage toMapMessage(){
        Block payload = bundle.getPayload();

        ProtostuffPayload pl = ProtostuffPayload.get(payload.getData());

        if (pl.getClassOfPayload().equals(MapMessage.class)){
            return pl.unpack();
        }

        return null;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
