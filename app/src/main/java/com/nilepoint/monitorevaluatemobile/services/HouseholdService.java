package com.nilepoint.monitorevaluatemobile.services;

import android.util.Log;

import com.google.gson.Gson;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.dtn.Block;
import com.nilepoint.dtn.Bundle;
import com.nilepoint.dtn.DTN;
import com.nilepoint.dtn.EndpointId;
import com.nilepoint.dtn.convergence.ProtostuffPayload;
import com.nilepoint.model.Household;
import com.nilepoint.model.HouseholdRelationship;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.logging.RemoteLogger;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

/**
 * Created by ashaw on 6/25/17.
 */

public class HouseholdService implements Runnable {
    public static String TAG = "HouseholdService";
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

    final private BlockingQueue<UpdateEvent> householdQueue = new LinkedBlockingDeque<>();

    final private DTNService dtnService = WLTrackApp.dtnService;

    final private RemoteLogger logger = new RemoteLogger();

    private boolean started = true;

    public void addHouseholdEvent(UpdateEvent event){
        householdQueue.add(event);
    }


    public HouseholdService() {
        scheduledExecutorService.execute(this);
    }

    @Override
    public void run() {
        DTN dtn = dtnService.getDTN();

        while(started){
            try {
                UpdateEvent event = householdQueue.poll(1, TimeUnit.SECONDS);


                if (event != null) {
                    Bundle bundle = event.getBundle();
                    MapMessage msg = event.toMapMessage();

                    msg.put("className", "Household");

                    switch(event.getType()){
                        case CREATED:
                            if (bundle.getDestination().getUri().getHost().equals(dtn.getLocalNodeInfo().getId())){
                                // this was sent to me, we'll drop it.
                                logger.debug(TAG, "Got a bundle destined for us, not resending");
                            } else {
                                logger.debug(TAG, "Got Household CREATED event, sending to DTN and AMQP Msg: " + msg.getMap());

                                if (WLTrackApp.dtnService.amqpConvergenceLayer != null) {
                                    WLTrackApp.dtnService.amqpConvergenceLayer.sendToAllNeighbors(bundle);
                                }

                                if (bundle.getSource().getUri().getHost().equals(dtn.getLocalNodeInfo().getId())) {
                                    Log.d(TAG,"enqueuing bundle " + bundle.getUuid() + " to DTN");
                                    dtn.enqueue(bundle);
                                } else {
                                    Log.d(TAG,"inserting bundle " + bundle.getUuid() + " to DTN Database");

                                    dtn.getDatabase().insertBundle(bundle);
                                }
                            }

                            break;

                        case UPDATED:
                            if (bundle.getDestination().getUri().getHost().equals(dtn.getLocalNodeInfo().getId())){
                                // this was sent to me, we'll drop it.
                                Log.d(TAG,"Got a bundle destined for us, not resending");
                            } else {
                                Log.d(TAG,"Got Household UPDATED event, sending to DTN and AMQP");

                                //Log.d(TAG,"Sending: " + event.toMapMessage().getMap());

                                if (WLTrackApp.dtnService.amqpConvergenceLayer != null) {
                                    WLTrackApp.dtnService.amqpConvergenceLayer.sendToAllNeighbors(bundle);
                                }

                                if (bundle.getSource().getUri().getHost().equals(dtn.getLocalNodeInfo().getId())) {
                                    Log.d(TAG,"enqueuing bundle " + bundle.getUuid() + " to DTN");
                                    dtn.enqueue(bundle); // enqueue to be sent.
                                } else {
                                    Log.d(TAG,"inserting bundle " + bundle.getUuid() + " to DTN Database");
                                    dtn.getDatabase().insertBundle(bundle);
                                }
                            }
                            break;
                        case CREATE:
                            Log.d(TAG,"Got Household CREATE event, creating update then sending to DTN and AMQP");

                            createHousehold(bundle);
                            break;
                        case UPDATE:
                            Log.d(TAG,"Got Household UPDATE event, updating update.");

                            updateHousehold(bundle);
                            break;
                        case DELETE:
                            break;
                    }

                }

                //Thread.sleep(100); // small delay so we don't loop a ton TODO: Rethink this (limits us to 10 a second)
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private com.nilepoint.dtn.Bundle createBundle(MapMessage msg){
        return  new com.nilepoint.dtn.Bundle(new EndpointId("dtn://" + dtnService.getDTN().getLocalNodeInfo().getId()),
                new EndpointId("dtn://MQ"), msg);
    }

    /**
     * Create a household from a map message. If the head of household cannot be found,
     * queue this create message up until it is.
     *
     * @param bundle
     * @return
     */
    public Household createHousehold(final Bundle bundle) {
        final Household household = new Household();

        Block payload = bundle.getPayload();

        ProtostuffPayload pl = ProtostuffPayload.get(payload.getData());

        final MapMessage mapMessage = pl.unpack();

        Realm realm = null;

        StoredParticipant hoh = realm.where(StoredParticipant.class)
                .equalTo("id", mapMessage.getMap().get("headOfHousehold.id"))
                .findFirst();

        household.setHeadOfHousehold(mapMessage.getMap().get("headOfHousehold.id"));

        Gson gson = new Gson();

        for (Map.Entry<String,String> entry : mapMessage.getMap().entrySet()){
            if (entry.getKey().startsWith("member")){
                HouseholdRelationship relationship = realm
                        .where(HouseholdRelationship.class)
                        .equalTo("participant", entry.getValue()).findFirst();

                if (relationship == null) {
                    household.addMember("member", entry.getValue());
                }
            }
        }

        try {
            realm = Realm.getDefaultInstance();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealm(household);
                }
            });

            return household;
        }  finally {
            if (realm != null){
                realm.close();
            }
        }
    }

    public Household updateHousehold(final Bundle bundle){
        Realm realm = null;

        Block payload = bundle.getPayload();

        ProtostuffPayload pl = ProtostuffPayload.get(payload.getData());

        final MapMessage mapMessage = pl.unpack();

        try {
            realm = Realm.getDefaultInstance();


            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Household household = realm.where(Household.class)
                            .equalTo("id", mapMessage.getId()).findFirst();


                    if (household == null){
                        Log.d(TAG,"HouseholdService.updateHousehold: could not find household with id " + mapMessage.getId() + " creating");

                        household = new Household();

                        household.setId(mapMessage.getId());

                        household.setHeadOfHousehold(mapMessage.getMap().get("headOfHousehold.id"));

                        realm.copyToRealm(household);
                    }

                    Log.d(TAG,"HouseholdService: Household message of version " +mapMessage.getVersion()
                            + " local household version: " + household.getVersion());

                    if (household.getVersion() < mapMessage.getVersion()) {
                        for (Map.Entry<String, String> entry : mapMessage.getMap().entrySet()) {
                            if (entry.getKey().startsWith("member")) {
                                HouseholdRelationship relationship = realm.where(HouseholdRelationship.class)
                                        .equalTo("household", household.getId())
                                        .equalTo("participant", entry.getValue())
                                        .findFirst();

                                if (relationship == null) {
                                    household.addMember("member", entry.getValue());
                                }
                            }
                        }
                    }
                }
            });

            return realm.where(Household.class).equalTo("id", mapMessage.getId()).findFirst();

        }  finally {
            if (realm != null){
                realm.close();
            }
        }
    }

    public void updateHouseholds(final Collection<Bundle> bundles){

        Log.i(TAG, "Batch Updating " + bundles.size() + " households");

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (Bundle bundle : bundles) {

                        Block payload = bundle.getPayload();

                        ProtostuffPayload pl = ProtostuffPayload.get(payload.getData());

                        final MapMessage mapMessage = pl.unpack();

                        if (!mapMessage.getMap().get("className").equals("Household")){
                            continue;
                        }

                        Household household = realm.where(Household.class)
                                .equalTo("id", mapMessage.getId()).findFirst();


                        if (household == null) {
                            Log.d(TAG, "HouseholdService.updateHousehold: could not find household with id " + mapMessage.getId() + " creating");

                            household = new Household();

                            household.setId(mapMessage.getId());

                            household.setHeadOfHousehold(mapMessage.getMap().get("headOfHousehold.id"));

                            realm.copyToRealm(household);
                        }

                        Log.d(TAG, "HouseholdService: Household message of version " + mapMessage.getVersion()
                                + " local household version: " + household.getVersion());

                        if (household.getVersion() < mapMessage.getVersion()) {
                            for (Map.Entry<String, String> entry : mapMessage.getMap().entrySet()) {
                                if (entry.getKey().startsWith("member")) {
                                    HouseholdRelationship relationship = realm.where(HouseholdRelationship.class)
                                            .equalTo("household", household.getId())
                                            .equalTo("participant", entry.getValue())
                                            .findFirst();

                                    if (relationship == null) {
                                        household.addMember("member", entry.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            });

        }  finally {
            if (realm != null){
                realm.close();
            }
        }
    }
}
