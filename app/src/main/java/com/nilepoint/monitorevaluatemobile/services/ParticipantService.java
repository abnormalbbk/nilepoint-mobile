package com.nilepoint.monitorevaluatemobile.services;

import android.support.annotation.Nullable;
import android.util.Log;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.dtn.Block;
import com.nilepoint.dtn.Bundle;
import com.nilepoint.dtn.DTN;
import com.nilepoint.dtn.EndpointId;
import com.nilepoint.dtn.convergence.ProtostuffPayload;
import com.nilepoint.model.Household;
import com.nilepoint.model.Photo;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.logging.RemoteLogger;
import com.nilepoint.monitorevaluatemobile.stats.StatisticsManager;
import com.nilepoint.utils.DateUtilities;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

/**
 * Single service where participants are updated / created / etc.
 */

public class ParticipantService implements Runnable {
    public static String TAG = "ParticipantService";

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

    final private BlockingQueue<UpdateEvent> participantQueue = new LinkedBlockingDeque<>();

    final private DTNService dtnService = WLTrackApp.dtnService;

    private boolean started = true;

    private RemoteLogger logger = new RemoteLogger();

    public void addParticipantEvent(UpdateEvent event) {
        participantQueue.add(event);
    }


    public ParticipantService() {
        scheduledExecutorService.execute(this);
    }

    @Override
    public void run() {
        DTN dtn = dtnService.getDTN();

        while (started) {
            try {
                UpdateEvent event = participantQueue.poll(1, TimeUnit.SECONDS);


                if (event != null) {
                    Bundle bundle = event.getBundle();
                    MapMessage msg = event.toMapMessage();

                    String sourceHost = bundle.getSource().getUri().getHost();
                    String destinationHost = bundle.getDestination().getUri().getHost();

                    String localHost = dtn.getLocalNodeInfo().getId();

                    msg.put("className", "Participant");

                    switch (event.getType()) {
                        case CREATED:


                            if (destinationHost.equals(localHost)) {
                                // this was sent to me, we'll drop it.
                                Log.d(TAG, "Got a bundle destined for us, not resending");
                            } else {
                                Log.d(TAG, "Got Participant CREATED event, not destined for this device" +
                                        ", sending to DTN and AMQP");

                                // TODO: 10/22/19 Find how this works
                                if (WLTrackApp.dtnService.amqpConvergenceLayer != null) {
                                    WLTrackApp.dtnService.amqpConvergenceLayer.sendToAllNeighbors(bundle);
                                    StatisticsManager.getStatistics()
                                            .setLastContactWithMessageQueue(new Date())
                                            .store();
                                }

                                if (sourceHost.equals(dtn.getLocalNodeInfo().getId())) {
                                    Log.d(TAG, "enqueuing bundle " + bundle.getUuid() + " to DTN");
                                    dtn.enqueue(bundle);
                                } else {
                                    Log.d(TAG, "inserting bundle " + bundle.getUuid() + " to DTN Database");

                                    dtn.getDatabase().insertBundle(bundle);
                                }
                            }

                            break;

                        case UPDATED:
                            if (destinationHost.equals(localHost)) {
                                // this was sent to me, we'll drop it.
                                Log.d(TAG, "Got a bundle destined for us, not resending");
                            } else {
                                Log.d(TAG, "Got Participant UPDATED event, sending to DTN and AMQP");
                                //Log.d(TAG,"Sending: " + event.toMapMessage().getMap());

                                if (WLTrackApp.dtnService.amqpConvergenceLayer != null) {
                                    WLTrackApp.dtnService.amqpConvergenceLayer.sendToAllNeighbors(bundle);
                                    StatisticsManager
                                            .getStatistics()
                                            .setLastContactWithMessageQueue(new Date()).store();
                                } else {
                                    System.out.println("WLTrackApp.dtnService.amqpConvergenceLayer is null");
                                }

                                if (bundle.getSource().getUri().getHost().equals(dtn.getLocalNodeInfo().getId())) {
                                    Log.d(TAG, "enqueuing bundle " + bundle.getUuid() + " to DTN");
                                    dtn.enqueue(bundle); // enqueue to be sent.
                                } else {
                                    Log.d(TAG, "inserting bundle " + bundle.getUuid() + " to DTN Database");
                                    dtn.getDatabase().insertBundle(bundle);
                                }
                            }

                            break;
                        case CREATE:
                            Log.d(TAG, "Got Participant CREATE event, creating update then sending to DTN and AMQP");

                            createParticipant(null, event.toMapMessage());
                            break;
                        case UPDATE:
                            Log.d(TAG, "Got Participant UPDATE event.");

                            updateParticipant(event.toMapMessage());
                            break;
                        case DELETE:
                            break;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private com.nilepoint.dtn.Bundle createBundle(MapMessage msg) {
        return new com.nilepoint.dtn.Bundle(new EndpointId("dtn://" + dtnService.getDTN().getLocalNodeInfo().getId()),
                new EndpointId("dtn://MQ"), msg);
    }

    public StoredParticipant createParticipant(@Nullable final String headOfHouseholdId, final MapMessage mapMessage) {
        return createParticipant(headOfHouseholdId, createBundle(mapMessage));
    }

    public StoredParticipant createParticipant(@Nullable final String headOfHouseholdId, final Bundle bundle) {

        Block payload = bundle.getPayload();

        ProtostuffPayload pl = ProtostuffPayload.get(payload.getData());

        final MapMessage mapMessage = pl.unpack();

        mapMessage.put("className", "Participant");

        final StoredParticipant participant = new StoredParticipant(mapMessage);

        if (mapMessage.getMap().containsKey("photo")) {
            participant.setPhoto(Photo.fromBase64(mapMessage.getMap().get("photo")));
        }

        Realm realm = null;

        try {

            realm = Realm.getDefaultInstance();

            addParticipantEvent(new UpdateEvent(UpdateEventType.CREATED, createBundle(mapMessage)));

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealm(participant);

                    if (headOfHouseholdId == null) {
                        final Household hh = new Household();

                        hh.setHeadOfHousehold(participant);

                        realm.copyToRealmOrUpdate(hh);

                        WLTrackApp.householdService.addHouseholdEvent(
                                new UpdateEvent(UpdateEventType.CREATED,
                                        createBundle(hh.toMessage())));

                    } else {
                        Log.d(TAG, "Adding participant to household " + headOfHouseholdId);

                        Household hh = realm.where(Household.class)
                                .equalTo("headOfHousehold", headOfHouseholdId)
                                .findFirst();

                        hh.addMember("member", participant.getId());

                        WLTrackApp.householdService.addHouseholdEvent(
                                new UpdateEvent(UpdateEventType.UPDATED,
                                        createBundle(hh.toMessage())));


                        Log.d(TAG, "Household Message: " + hh.toMessage());
                    }

                }
            });

            Log.d("createParticipant()", participant.toString());

            // send an event to tell that this has been created
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return participant;
    }

    public StoredParticipant updateParticipant(final MapMessage mapMessage) {
        return updateParticipant(createBundle(mapMessage));
    }

    public StoredParticipant updateParticipant(final Bundle bundle) {
        Realm realm = null;

        Block payload = bundle.getPayload();

        ProtostuffPayload pl = ProtostuffPayload.get(payload.getData());

        final MapMessage mapMessage = pl.unpack();

        try {
            realm = Realm.getDefaultInstance();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    StoredParticipant participant = realm.where(StoredParticipant.class).equalTo("id",
                            mapMessage.getId()).findFirst();

                    // we dont have it locally, create it.
                    if (participant == null) {
                        participant = new StoredParticipant(mapMessage);

                        if (mapMessage.getMap().containsKey("photo")) {
                            participant.setPhoto(Photo.fromBase64(mapMessage.getMap().get("photo")));
                        }

                        realm.copyToRealm(participant);

                        addParticipantEvent(new UpdateEvent(UpdateEventType.UPDATED, bundle));
                    } else {

                        SimpleDateFormat fmt = new SimpleDateFormat(DateUtilities.DATE_AND_TIME_FORMAT);
                        Date date = new Date();

                        try {
                            date = mapMessage.getMap().get("lastUpdated") == null ? new Date() : fmt.parse(mapMessage.getMap().get("lastUpdated"));
                        } catch (Exception e) {
                            Log.d(TAG, "Could not parse date " + mapMessage.getMap().get("lastUpdated"));
                        }

                        Log.d(TAG, String.format("New update was modified %s stored update last updated %s - Message Version: %s Participant Version: %s", date,
                                participant.getLastUpdated(), mapMessage.getVersion(), participant.getVersion()));

                        if (participant.getLastUpdated().before(date) && (mapMessage.getVersion() > participant.getVersion())) {
                            Log.d(TAG, "Timestamp and version is newer, updating existing update.");

                            // from the server - master record, overwrite everything
                            if (mapMessage.getVersion() == Integer.MAX_VALUE){
                                participant.setVersion(participant.getVersion()+1);
                            } else {
                                participant.setVersion(mapMessage.getVersion());
                            }

                            participant.setMessage(mapMessage);

                            if (mapMessage.getMap().containsKey("photo")) {
                                Photo p = Photo.fromBase64(mapMessage.getMap().get("photo"));

                                p = realm.copyToRealmOrUpdate(p);

                                participant.setPhoto(p);
                            }

                            addParticipantEvent(new UpdateEvent(UpdateEventType.UPDATED, bundle));
                        } else {
                            Log.d(TAG, "Not updating existing update.");
                        }
                    }
                }
            });

            return realm.where(StoredParticipant.class).equalTo("id", mapMessage.getId()).findFirst();

        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public void updateParticipants(final Collection<Bundle> bundles) {
        Realm realm = null;

        Log.i(TAG, "Batch Updating " + bundles.size() + " participants");


        try {
            realm = Realm.getDefaultInstance();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (Bundle bundle : bundles) {

                        Block payload = bundle.getPayload();

                        ProtostuffPayload pl = ProtostuffPayload.get(payload.getData());

                        final MapMessage mapMessage = pl.unpack();

                        if (!mapMessage.getMap().get("className").equals("Participant")){
                            continue;
                        }

                        StoredParticipant participant = realm.where(StoredParticipant.class).equalTo("id",
                                mapMessage.getId()).findFirst();

                        // we dont have it locally, create it.
                        if (participant == null) {
                            participant = new StoredParticipant(mapMessage);

                            if (mapMessage.getMap().containsKey("photo")) {
                                participant.setPhoto(Photo.fromBase64(mapMessage.getMap().get("photo")));
                            }

                            realm.copyToRealm(participant);
                        } else {

                            SimpleDateFormat fmt = new SimpleDateFormat(DateUtilities.DATE_AND_TIME_FORMAT);
                            Date date = new Date();

                            try {
                                date = mapMessage.getMap().get("lastUpdated") == null ? new Date() : fmt.parse(mapMessage.getMap().get("lastUpdated"));
                            } catch (Exception e) {
                                Log.d(TAG, "Could not parse date " + mapMessage.getMap().get("lastUpdated"));
                            }

                            Log.d(TAG, String.format("New update was modified %s stored update last updated %s - Message Version: %s Participant Version: %s", date,
                                    participant.getLastUpdated(), mapMessage.getVersion(), participant.getVersion()));

                            if (participant.getLastUpdated().before(date) && mapMessage.getVersion() > participant.getVersion()) {
                                Log.d(TAG, "Timestamp and version is newer, updating existing update.");

                                participant.setMessage(mapMessage);

                                if (mapMessage.getMap().containsKey("photo")) {
                                    Photo p = Photo.fromBase64(mapMessage.getMap().get("photo"));

                                    p = realm.copyToRealmOrUpdate(p);

                                    participant.setPhoto(p);
                                }

                                addParticipantEvent(new UpdateEvent(UpdateEventType.UPDATED, bundle));
                            } else {
                                Log.d(TAG, "Not updating existing update.");
                            }
                        }
                    }
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }
}
