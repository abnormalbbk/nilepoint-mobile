package com.nilepoint.monitorevaluatemobile.persistence;

import android.content.Context;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.persistence.Datastore;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by ashaw on 4/18/17.
 */

public class BatchParticipantFactory {

    /**
     * Create test participants in a background thread. Will create them in
     * batches of 500. The param n will determine how many batches are created.
     *
     *
     * @param n this method will create 500 * n participents
     */
    public static void createTestParticipants(final Context context, final int n){

    }
}
