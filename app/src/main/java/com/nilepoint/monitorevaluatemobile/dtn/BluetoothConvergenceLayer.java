package com.nilepoint.monitorevaluatemobile.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.api.MobileDevice;
import com.nilepoint.dtn.Bundle;
import com.nilepoint.dtn.DTN;
import com.nilepoint.dtn.DTNEvent;
import com.nilepoint.dtn.DTNEventListener;
import com.nilepoint.dtn.DTNEventType;
import com.nilepoint.dtn.convergence.ConvergenceLayer;
import com.nilepoint.dtn.convergence.ProtostuffPayload;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.dtn.discovery.NeighborRegistration;
import com.nilepoint.dtn.discovery.NeighborRegistry;
import com.nilepoint.model.Area;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.Project;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.commands.HeartbeatCommand;
import com.nilepoint.monitorevaluatemobile.dtn.commands.HelloCommand;
import com.nilepoint.monitorevaluatemobile.logging.RemoteLogger;
import com.nilepoint.monitorevaluatemobile.user.UserSession;
import com.nilepoint.utils.ModelUtilities;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import io.paperdb.Paper;
import io.realm.Realm;

/**
 * This DTN convergence layer will allow bundles to be sent through bluetooth.
 */

public class BluetoothConvergenceLayer implements ConvergenceLayer, Runnable {
    public UUID uuid = UUID.fromString("aeeb5480-1c74-45e2-bfd0-f592958cba2a");

    public final static String TAG = "BluetoothConvergence";

    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    RemoteLogger logger = new RemoteLogger();

    /* Device lookups - key is the device address */

    private final Map<String, BluetoothRegistration> bluetoothRegistrations = new ConcurrentHashMap<>();

    private final Map<String, List<String>> bundlesReceived = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService senderExecutorService = Executors.newScheduledThreadPool(10);

    private DTN dtn;

    private volatile boolean started = true;

    private volatile NeighborRegistry neighborRegistry;

    private volatile BluetoothDiscoveryService bluetoothDiscoveryService;
    private volatile BluetoothServer server;

    private final ReentrantLock lock = new ReentrantLock();


    /**
     *
     * @param ctx A context so that we can lookup the bluetooth services
     * @param dtn The DTN that this convergence layer belongs to.
     */
    public BluetoothConvergenceLayer(Context ctx, DTN dtn) {
        this.dtn = dtn;

        if (adapter != null) {
            this.neighborRegistry = new NeighborRegistry(dtn, this);

            // move this logic out, it's a bit leaky
            dtn.addListener(new DTNEventListener() {
                @Override
                public void onDTNEvent(DTNEvent dtnEvent) {
                    if (dtnEvent.getType() == DTNEventType.NEIGHBOR_REMOVED){

                        System.out.println(dtnEvent.getSource() + " removed. Removing bluetooth registration. ");

                        NeighborRegistration reg1 = (NeighborRegistration) dtnEvent.getSource();

                        BluetoothRegistration reg = bluetoothRegistrations.get(reg1.getNode()
                                .getLocalAddress());

                        if (reg != null){
                            reg.getSender().disconnect();
                        }

                        bluetoothRegistrations.remove(reg1.getNode().getLocalAddress());

                        MobileDeviceRegistry.getInstance().untrack(reg1.getNode());

                    }
                }
            });


           // bluetoothDiscoveryService = new BluetoothDiscoveryService(ctx, this);

            scheduledExecutorService.schedule(this, 0, TimeUnit.SECONDS);

            server = new BluetoothServer(this);

            server.setListener(new BluetoothServerListener() {
                @Override
                public void onDeviceDisconnect(BluetoothDevice device) {
                    logger.debug(TAG, "BluetoothConvergenceLayer: Device disconnected, removing neighbor " + device);

                    bluetoothRegistrations.remove(device.getAddress());

                    removeRegistration(device);

                }
            });

            server.start();
        }
    }

    public void handleBundle(Bundle bundle){
        dtn.fire(new DTNEvent(DTNEventType.BUNDLE_RECEIVED, bundle));
    }

    public synchronized void sendRaw (final Object object, final Node neighbor){
        senderExecutorService.schedule(new Runnable(){
            @Override
            public void run() {
                String address = neighbor.getLocalAddress();

                BluetoothRegistration registration =  bluetoothRegistrations.get(address);

                if (registration != null) {
                    BluetoothSocketSender sender = registration.getSender();

                    try {
                        sender.send(new ProtostuffPayload(object));
                    } catch (Exception e){
                        Crashlytics.logException(e);
                        e.printStackTrace();
                    }
                }
            }
        }, 0, TimeUnit.SECONDS);
    }

    @Override
    /**
     * Send a bundle
     */
    public synchronized boolean send(final Bundle bundle, final Node neighbor) {
        logger.debug(TAG,"BluetoothConvergenceLayer: send() " + bundle + " to " + neighbor);

        try {
            if (bundle.getSource().isDevice(neighbor.getId())){
                // don't send things back to their source.
                return true;
            }

            List<String> bundleList = bundlesReceived.get(neighbor.getId());

            if (bundleList != null && bundleList.contains(bundle.getUuid())){

                // do not send this bundle back if we've recevied from this session.
                return true;
            }

            String address = neighbor.getLocalAddress();

            BluetoothRegistration registration =  bluetoothRegistrations.get(address);

            if (registration != null) {
                BluetoothSocketSender sender = registration.getSender();

                if (sender != null) {
                    boolean sent = sender.send(new ProtostuffPayload(bundle));

                    if (!sent) {
                        logger.debug(TAG,"BluetoothConvergenceLayer: Failed sending " + bundle + " to " + sender);

                        bluetoothRegistrations.remove(address);

                        dtn.enqueue(bundle);
                    }

                    return sent;
                } else {
                    dtn.enqueue(bundle);

                    return false;
                }
            }

        } catch (Exception e) {
            Crashlytics.logException(e);
            logger.error(TAG, "BluetoothConvergenceLayer: Exception thrown sending a bundle.", e);
        }
        return false;
    }

    public boolean testSender(BluetoothSocketSender sender){
        if (sender == null || sender.getSocket() == null || !sender.getSocket().isConnected()){
            return false;
        }

        return true;
    }

    @Override
    public void start() throws IOException {

    }

    public void stop() {
        bluetoothRegistrations.clear();
    }

    @Override
    public void sendToAllNeighbors(Bundle bundle) {
        logger.debug(TAG,"BluetoothConvergenceLayer: Send to All Neighbors bundle:" + bundle
                + " bluetoothRegistrations: "+ getNeighborRegistry().getRegistrations());

        for (NeighborRegistration registration : getNeighborRegistry().getRegistrations()){
            send (bundle, registration.getNode());
        }
    }

    public DTN getDTN() {
        return dtn;
    }

    public NeighborRegistry getNeighborRegistry() {
        return neighborRegistry;
    }

    /**
     * Task to run every 60 seconds
     */
    public void run() {

        try {
            //logger.debug(TAG,"BluetoothConvergenceLayer: Bonded Devices: " + adapter.getBondedDevices());

            //adapter.startDiscovery();

            BluetoothSocket socket = null;

            for (BluetoothDevice device : adapter.getBondedDevices()) {
                lock.lock();

                try {
                    boolean listening = false;

                    String address = device.getAddress();

                    BluetoothRegistration registration = bluetoothRegistrations.get(address);

                    if (registration != null) {

                        BluetoothSocketSender sender = registration.getSender();

                        if (sender.getSocket() == null || !sender.getSocket().isConnected()) {
                            WLTrackApp.customToast("Disconnected from " + device.getName());

                            removeRegistration(device);

                            continue;
                        }

                        sendHelloCommand(sender);

                        continue;
                    } else {
                        // no registration, create a new one and link up the server / sender.
                        try {

                            socket = device.createRfcommSocketToServiceRecord(uuid);

                            socket.connect();

                            logger.debug(TAG, "Sender not found for " + address + " , created one.");

                            WLTrackApp.customToast("Connected to " + device.getName());

                            logger.debug(TAG, " Socket connected - this phone will be a client.");

                            BluetoothSocketReceiver receiver = BluetoothSocketReceiverFactory.get(socket);

                            receiver.start();

                            logger.debug(TAG, " BtReciever Started ");

                            BluetoothSocketSender sender = new BluetoothSocketSender(socket, dtn);

                            sender.setCallback(new BluetoothSocketSender.BluetoothCallback() {
                                @Override
                                public void onConnect(BluetoothDevice device) {

                                }

                                @Override
                                public void onDisconnect(BluetoothDevice device) {
                                    logger.debug(TAG, " Disconnect in sender, removing registration. ");
                                    removeRegistration(device);
                                }

                                @Override
                                public void onError(BluetoothDevice device) {
                                    logger.debug(TAG, " Error in sender, removing registration. ");
                                    removeRegistration(device);
                                }
                            });

                            logger.debug(TAG, " Bluetooth Sender created ");

                            registration = new BluetoothRegistration(device, sender, receiver);

                            bluetoothRegistrations.put(address, registration);

                            logger.debug(TAG, " Sending hello command. ");

                            sendHelloCommand(sender);

                            createOrUpdateRegistration(device);

                            registration.setLastContact(new Date());

                        } catch (IOException ex) {
                            //logger.error(TAG, "Could not create socket for " + device.getAddress() + " Removing registrations. ", ex);

                            removeRegistration(device);

                            bluetoothRegistrations.remove(device.getAddress());

                            if (socket != null) {
                                socket.close();
                                // System.out.println("Closed socket for device " + device.getAddress());
                            }
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
        } catch (Throwable e){
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            //adapter.cancelDiscovery();
        }

        // schedule another run of this

        scheduledExecutorService.schedule(this, 10, TimeUnit.SECONDS);
    }

    public void createOrUpdateRegistration(BluetoothDevice device){
        NeighborRegistration registrationForDeviceAddress = getNeighborRegistry()
                .findRegistrationByLocalAddress(device.getAddress());

        Node neighbor = registrationForDeviceAddress != null ? registrationForDeviceAddress.getNode() : new Node(
                ModelUtilities.randomUUID(),
                device.getName(),
                device.getAddress(),
                device.getAddress()
        );

        neighbor.setLayer(this);

        getNeighborRegistry().addOrUpdateRegistration(neighbor);
    }

    public void removeRegistration(BluetoothDevice device){
        NeighborRegistration registrationForDeviceAddress = getNeighborRegistry()
                .findRegistrationByLocalAddress(device.getAddress());

        if (registrationForDeviceAddress != null) {
            getNeighborRegistry().removeRegistration(registrationForDeviceAddress.getNode());
        }

        BluetoothRegistration reg = bluetoothRegistrations.get(device.getAddress());

        try {
            if (reg != null) {
                reg.getSender().disconnect();
            }
        } catch (Exception e){
            System.out.println("Could not disconnect from " + device.getName());
        }

        bluetoothRegistrations.remove(device.getAddress());
    }

    public Map<String, BluetoothRegistration> getBluetoothRegistrations() {
        return bluetoothRegistrations;
    }

    private void sendHelloCommand(BluetoothSocketSender sender){

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            MobileDevice device = Paper.book().read("device");

            HelloCommand hello = new HelloCommand();

            hello.setSourceDevice(device);

            hello.setNumberOfParticipants(realm.where(StoredParticipant.class).count());
            hello.setNumberOfAreas(realm.where(Area.class).count());
            hello.setNumberOfProjects(realm.where(Project.class).count());
            hello.setNumberOfPlannedActivities(realm.where(PlannedActivity.class).count());

            if (UserSession.userId != null){
                User user = realm.where(User.class)
                        .equalTo("id", UserSession.userId )
                        .findFirst();

                if (user != null) {
                    if (user.getPhoto() != null) {
                        hello.setUserPhoto(user.getPhoto().toBase64());
                    }
                }
            }

            sender.send(new ProtostuffPayload(hello));
        } catch (Exception e){
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public Map<String, List<String>> getBundlesReceived() {
        return bundlesReceived;
    }
}
