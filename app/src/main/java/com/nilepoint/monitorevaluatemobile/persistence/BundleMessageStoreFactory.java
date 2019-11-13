package com.nilepoint.monitorevaluatemobile.persistence;

import android.content.Context;

import com.nilepoint.amqp.RecoveryQueueFactory;
import com.nilepoint.dtn.Bundle;

import java.util.List;

/**
 * Created by ashaw on 3/3/17.
 */

public class BundleMessageStoreFactory implements RecoveryQueueFactory <Bundle> {

    Context context;
    String name;

    public BundleMessageStoreFactory(String name, Context context) {
        this.context = context;
        this.name = name;
    }

    public List<Bundle> create() {
        return new PersistentStorageList<>(name, context);
    }

}
