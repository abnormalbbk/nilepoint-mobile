package com.nilepoint.monitorevaluatemobile.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.dtn.DTN;
import com.nilepoint.dtn.discovery.NeighborRegistration;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;

import java.util.UUID;

/**
 * Server
 */

public class BluetoothServer extends Thread {

    boolean started = true;

    private BluetoothServerListener listener;

    public final static String TAG = "BluetoothServer";

    public UUID uuid = UUID.fromString("aeeb5480-1c74-45e2-bfd0-f592958cba2a");

    private BluetoothServerSocket btServerSocket;

    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    private DTN dtn;
    private BluetoothConvergenceLayer layer;

    public BluetoothServer(BluetoothConvergenceLayer layer) {

        this.dtn = layer.getDTN();
        this.layer = layer;

        Log.i(TAG,"BluetoothServer: Listening on for UUID " + uuid);
    }

    public void run() {

        while (started) {
            if (btServerSocket == null){
                try {
                    Thread.sleep(500);

                    btServerSocket = adapter.listenUsingRfcommWithServiceRecord(dtn.getLocalNodeInfo().getName(),
                            uuid);

                } catch (Exception e){
                    Crashlytics.logException(e);

                }

                Log.d(TAG,"btServerSocket not created, not accepting");

                continue;
            }



            try {
                Log.d(TAG,"BluetoothServer: Waiting for bluetooth connection.");

                final BluetoothSocket socket = btServerSocket.accept();

                layer.getLock().lock();

                try {
                    final BluetoothDevice remoteDevice = socket.getRemoteDevice();

                    layer.createOrUpdateRegistration(remoteDevice);

                    final NeighborRegistration reg = layer.getNeighborRegistry().findRegistrationByLocalAddress(socket
                            .getRemoteDevice().getAddress());

                    BluetoothSocketSender sender = new BluetoothSocketSender(socket, dtn);

                    System.out.println("BluetoothServer.run: Accepted a connection from " + socket.getRemoteDevice());

                    final BluetoothSocketReceiver receiver = BluetoothSocketReceiverFactory.get(socket);

                    layer.getBluetoothRegistrations().put(remoteDevice.getAddress(), new BluetoothRegistration(remoteDevice, sender, receiver));

                    receiver.start();

                    WLTrackApp.customToast("Peer " + remoteDevice.getName() + " connected.");
                } finally {
                    layer.getLock().unlock();
                }

            } catch (Exception e) {
                Log.e(TAG, "BluetoothAcceptThread: Could not create accept socket connection - creating new socket", e);
                try {
                    btServerSocket.close();
                    btServerSocket = null;
                } catch (Exception ex){
                    Crashlytics.logException(ex);
                }
            }
        }
    }

    public BluetoothServerListener getListener() {
        return listener;
    }

    public void setListener(BluetoothServerListener listener) {
        this.listener = listener;
    }
}
