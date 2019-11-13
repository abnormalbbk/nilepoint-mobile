package com.nilepoint.monitorevaluatemobile.services;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.amqp.AMQPConfiguration;
import com.nilepoint.amqp.Endpoint;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.dtn.*;
import com.nilepoint.dtn.convergence.AMQPConvergenceLayer;
import com.nilepoint.dtn.convergence.ProtostuffPayload;
import com.nilepoint.dtn.convergence.TCPConvergenceLayer;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.model.Area;
import com.nilepoint.model.Group;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.Project;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.BluetoothConvergenceLayer;
import com.nilepoint.monitorevaluatemobile.dtn.commands.FullDatabaseResponse;
import com.nilepoint.monitorevaluatemobile.logging.RemoteLogger;
import com.nilepoint.monitorevaluatemobile.persistence.BundleMessageStoreFactory;
import com.nilepoint.monitorevaluatemobile.persistence.Environment;
import com.nilepoint.monitorevaluatemobile.persistence.PaperStorage;
import com.nilepoint.monitorevaluatemobile.settings.DTNSettingsActivity;
import com.nilepoint.monitorevaluatemobile.settings.SettingsMixin;
import com.nilepoint.monitorevaluatemobile.settings.SettingsStorage;
import com.nilepoint.monitorevaluatemobile.stats.StatisticsManager;
import com.nilepoint.persistence.RealmBundleQueueFactory;
import com.nilepoint.serialization.ProtostuffManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import io.paperdb.Book;
import io.paperdb.Paper;
import io.realm.Realm;

/**
 * Created by ashaw on 3/3/17
 *
 * TODO: Clean up this class, it's a mess
 *
 */

public class DTNService extends SettingsMixin {
    public static final String TAG = "DTNService";

    BluetoothAdapter mBluetoothAdapter;

    public AMQPConvergenceLayer amqpConvergenceLayer;
    public BluetoothConvergenceLayer btlayer;
    public TCPConvergenceLayer tcp;

    private RemoteLogger logger = new RemoteLogger();

    private final SettingsStorage settingsStorage = new SettingsStorage();

    private DTN dtn;

    public final static String TERMINAL_ENDPOINT = "MQ";

    ActivityService activityService = new ActivityService();
    GroupService groupService = new GroupService();



    public DTNService(Context ctx) {


        Book book = Paper.book();

        DTNConfiguration config = new DTNConfiguration();

        config.setDiscoveryPort(15559);

        config.setListenPort(15560);

        config.setSharedSecret("012345678901234567890123456789!!");

        config.setBundleStorageAdapter(new PaperStorage(ctx));

        dtn = new DTN(config);

        String ip = getLocalIpAddress();
        String id = Paper.book().read("device.id");
        String name = Paper.book().read("device.name");

        dtn.setLocalNodeInfo(new Node(id, name, ip, ip));

        /**
         * Add the bluetooth convergence layer.
         */
        ProtostuffManager<Bundle> psManager = new ProtostuffManager<>(Bundle.class);

        RealmBundleQueueFactory queueFactory = new RealmBundleQueueFactory("main-bundles");

        Environment environment = Paper.book().read("environment");

        AMQPConfiguration<Bundle> dtnAMQP = new AMQPConfiguration<>()
                .endpoint(new Endpoint(environment.getAmqpHostname()))
                .username(environment.getAmqpUsername())
                .password(environment.getAmqpPassword())
                .exchangeType("direct")
                .deserializer(psManager)
                .serializer(psManager)
                .ack(true)
                .secure(true)
                .exchange("nilepoint-bundle")
                .queue("bundles")
                .routingKey("")
                .retryStorage(new BundleMessageStoreFactory("bundle-restore-queue",ctx));

        final Boolean amqpEnabled = book.read(DTNSettingsActivity.AMQP_LAYER_ACTIVE_LABEL);
        final Boolean tcpEnabled = book.read(DTNSettingsActivity.TCP_LAYER_ACTIVE_LABEL);
        final Boolean btEnabled = book.read(DTNSettingsActivity.BT_LAYER_ACTIVE_LABEL);

        if (amqpEnabled) {
            amqpConvergenceLayer = new AMQPConvergenceLayer(dtnAMQP, dtn);
            // add our server to the convergence layer
            amqpConvergenceLayer.getNeighborRegistry().addPermanentRegistration(new Node(environment.getAmqpHostname(),
                    environment.getAmqpHostname(), environment.getAmqpHostname(), environment.getAmqpHostname()));
        }


        if (tcpEnabled) {
            tcp = new TCPConvergenceLayer(dtn);

            dtn.addConvergenceLayer(tcp);
        } else {
            logger.info(TAG,"TCPConvergenceLayer Disabled");
        }

        if (btEnabled) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter != null){
                btlayer = new BluetoothConvergenceLayer(ctx, dtn);

                dtn.addConvergenceLayer(btlayer);
            } else {
                logger.info(TAG,"Cannot get bluetooth adapter, possibly not supported");
            }
        } else {
            logger.info(TAG,"BluetoothConvergenceLayer Disabled");
        }

        logger.debug(TAG, String.format("Conergence Layers: tcp: %s | bt: %s | amqp: %s ",
                book.read(DTNSettingsActivity.TCP_LAYER_ACTIVE_LABEL),
                book.read(DTNSettingsActivity.BT_LAYER_ACTIVE_LABEL),
                book.read(DTNSettingsActivity.AMQP_LAYER_ACTIVE_LABEL)));

        dtn.addListener(new DTNEventListener() {
            @Override
            public void onDTNEvent(DTNEvent dtnEvent) {

                Realm realm = null;

                try {
                    realm = Realm.getDefaultInstance();;
                    switch (dtnEvent.getType()) {
                        case BUNDLE_RECEIVED:
                            // we get a bundle, we try and persist the bundle
                            Bundle bundle = (Bundle) dtnEvent.getSource();

                            if (dtn.getDatabase().get(bundle.getUuid()) != null) {
                                // this bundle is already in our database, drop it

                                return;
                            }

                            // if the bundle isn't destined for us, put it in the DB so other neighbors
                            // will get it when they connect.

                            if (!bundle.getDestination().isDevice(dtn.getLocalNodeInfo().getId())) {
                                dtn.getDatabase().insertBundle(bundle);
                            }

                            processBundle(bundle);

                            dtn.getStatistics().inc("Bundles Processed", 1.0);

                            StatisticsManager.getStatistics()
                                    .setLastContactWithPeer(new Date())
                                    .store();

                            break;
                        case NEIGHBOR_ADDED:
                            // when a neighbor is added, replicate the current
                            // bundles
                            dtn.getStatistics().inc("Neighbors Added", 1.0);

                            break;
                    }
                } finally {
                    if (realm != null){
                        realm.close();
                    }
                }
            }
        });

        new Thread(new Runnable() { public void run(){
            try {
                dtn.startConvergenceLayers();
                if (amqpEnabled) {
                    amqpConvergenceLayer.start();
                }
            } catch (Exception e){
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }}).start();


    }

    /**
     * Process a bundle that has been received by the DTN.
     * @param bundle
     */
    private void processBundle(Bundle bundle){
      Block payload = bundle.getPayload();

        Log.d(TAG,"Got bundle from dtn: " + bundle);

        ProtostuffPayload pl = ProtostuffPayload.get(payload.getData());

        Log.d(TAG,"got payload of class " + pl.getClassOfPayload());

        try {
            if (pl.getClassOfPayload().equals(MapMessage.class)) {
                MapMessage mapMessage = pl.unpack();

                if ("Participant".equals(mapMessage.getMap().get("className"))) {
                    WLTrackApp.participantService.addParticipantEvent(
                            new UpdateEvent(UpdateEventType.UPDATE, bundle)
                    );
                } else if ("Household".equals(mapMessage.getMap().get("className"))) {
                    WLTrackApp.householdService.addHouseholdEvent(
                            new UpdateEvent(UpdateEventType.UPDATE, bundle)
                    );
                } else if ("Group".equals(mapMessage.getMap().get("className"))) {
                    System.out.println("Got group bundle");
                    groupService.updateOrAddGroup(bundle, mapMessage);
                } else  if ("Activity".equals(mapMessage.getMap().get("className"))){
                    activityService.updateOrAddActivity(bundle, mapMessage);

                } else {
                    logger.error(TAG,"Unknown map message className: " + mapMessage.getMap().get("className") + " " + mapMessage.getMap());
                }
            } else if (pl.getClassOfPayload().equals(FullDatabaseResponse.class)) {
                FullDatabaseResponse response = pl.unpack();

                logger.error(TAG,"DTNService: Got list of objects, hopefully bundles: " + response.getBundles());

                WLTrackApp.participantService.updateParticipants(response.getBundles());
                WLTrackApp.householdService.updateHouseholds(response.getBundles());
            } else {
                logger.error(TAG,"DTNService: Got bundle of unknown class "  + pl.getClassOfPayload());
            }
        } catch (Exception e){
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    /**
     * If we get bundles that are system
     * @param bundles
     */
    public void processSystemBundles(final List<Bundle> bundles){

        // TODO refactor this into it's own class
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (Bundle bundle : bundles) {
                        ProtostuffPayload payload = ProtostuffPayload.get(bundle
                                .getPayload().getData());

                        switch (payload.getClassOfPayload().getSimpleName()) {
                            case "Area":
                                Area area = payload.unpack();

                                Area storedArea = realm.where(Area.class)
                                        .equalTo("id",area.getId())
                                        .findFirst();

                                if (storedArea == null){
                                    realm.copyToRealm(area);
                                }

                                break;
                            case "Project":
                                Project project = payload.unpack();

                                Project storedProject = realm.where(Project.class)
                                        .equalTo("id",project.getId())
                                        .findFirst();

                                if (storedProject == null){
                                    realm.copyToRealm(project);
                                }
                                break;
                            case "PlannedActivity":
                                PlannedActivity plannedActivity = payload.unpack();

                                PlannedActivity storedPlannedActivity = realm.where(PlannedActivity.class)
                                        .equalTo("id",plannedActivity.getId())
                                        .findFirst();

                                if (storedPlannedActivity == null){
                                    realm.copyToRealm(plannedActivity);
                                }

                                break;
                            case "Group":
                                Group group = payload.unpack();

                                Group storedGroup = realm.where(Group.class)
                                        .equalTo("id",group.getId())
                                        .findFirst();

                                if (storedGroup == null){
                                    realm.copyToRealm(storedGroup);
                                }

                                break;
                        }
                    }
                }
            });
        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        String ip = inetAddress.getHostAddress();
                        Log.i(TAG, "***** IP="+ ip);
                        return ip;
                    }
                }
            }
        } catch (SocketException ex) {
            logger.error(TAG, "Error in looking up local IP Address", ex);
        }
        return null;
    }

    public com.nilepoint.dtn.Bundle createBundle(MapMessage msg){
        return  new com.nilepoint.dtn.Bundle(new EndpointId("dtn://" + getDTN().getLocalNodeInfo().getId()),
                new EndpointId("dtn://" + TERMINAL_ENDPOINT), msg);
    }
    public com.nilepoint.dtn.Bundle createBundle(MapMessage msg, String destination){
        return  new com.nilepoint.dtn.Bundle(new EndpointId("dtn://" + getDTN().getLocalNodeInfo().getId()),
                new EndpointId("dtn://" + destination), msg);
    }
    public com.nilepoint.dtn.Bundle createBundle(Object object, String destination){
        return  new com.nilepoint.dtn.Bundle(new EndpointId("dtn://" + getDTN().getLocalNodeInfo().getId()),
                new EndpointId("dtn://" + destination), object);
    }

    public DTN getDTN() {
        return dtn;
    }

    public void sendToAll(Bundle bundle){
        getDTN().enqueue(bundle);
        // send to AMQP, which is seperate for now
        amqpConvergenceLayer.sendToAllNeighbors(bundle);
    }
}
