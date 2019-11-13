package com.nilepoint.monitorevaluatemobile.dtn;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.dtn.DTN;
import com.nilepoint.dtn.convergence.Payload;
import com.nilepoint.dtn.convergence.ProtostuffPayload;
import com.nilepoint.dtn.convergence.Sender;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CopyDatabaseCommand;
import com.nilepoint.monitorevaluatemobile.dtn.commands.SendBundlesCommand;
import com.nilepoint.monitorevaluatemobile.stats.StatisticsManager;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.UUID;

/**
 * Created by ashaw on 2/19/17.
 */
public class BluetoothSocketSender extends Sender {
    public UUID uuid = UUID.fromString("aeeb5480-1c74-45e2-bfd0-f592958cba2a");

    public final static String TAG = "BluetoothSocketSnd";
    private final DTN dtn;

    private BluetoothSocket socket;
        private ObjectOutputStream oos;
        private BluetoothCallback callback;

    public BluetoothSocketSender(BluetoothSocket socket, DTN dtn) {
        super();

        this.dtn = dtn;
        this.socket = socket;

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception ex){
            Crashlytics.logException(ex);
        }

        connected = true;

        start();
    }

    public boolean connect(){
        return true;
    }

    /**
     * Opens up a socket to the remote host and sends the payload.
     *
     * @param payload
     */
    @Override
    public synchronized boolean send(Payload payload) {
        try {

            if (oos != null) {
                oos.writeObject(payload);

                oos.flush();

                return true;
            }

            System.out.println("Could not send payload");

            return false;
        } catch (IOException ex) {
            System.out.println("BluetoothSocketSender: Exception thrown trying to write to BluetoothSocketSender socket, closing it. " + ex);

            try {
                socket.close();

                socket = null;
            } catch (Exception e){

            }

            if (getCallback() != null){
                getCallback().onError(socket.getRemoteDevice());
            }

            return false;
        }

    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public void setSocket(BluetoothSocket socket) {
        this.socket = socket;
    }

    public void disconnect() {
        try {
            if (oos != null) {
                oos.close();
                socket.close();
            }
            if (getCallback() != null){
                getCallback().onDisconnect(socket.getRemoteDevice());
            }

        } catch (Exception e){
            Crashlytics.logException(e);
            System.out.println("Could not close, socket may already be closed");
        }

        connected = false;
    }

    public BluetoothCallback getCallback() {
        return callback;
    }

    public void setCallback(BluetoothCallback callback) {
        this.callback = callback;
    }

    public interface BluetoothCallback {
        void onConnect(BluetoothDevice device);
        void onDisconnect(BluetoothDevice device);
        void onError(BluetoothDevice device);
    }
}
