package com.nilepoint.monitorevaluatemobile.dtn;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.util.TimeFormatException;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.dtn.convergence.ProtostuffPayload;
import com.nilepoint.dtn.convergence.Receiver;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.stats.StatisticsManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;

/**
 * A simple receiver that will receive a ProtostuffPayload and fire listeners.
 *
 * @TODO add disconnection
 *
 * @author ashaw
 */
public class BluetoothSocketReceiver extends Receiver {

    public final static String TAG = "BluetoothSocketR";
    /**
     * The socket used for IO
     */
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private ObjectInputStream ois;

    int errors = 0;

    public BluetoothSocketReceiver(BluetoothSocket socket) {
        Log.d(TAG,"Creating BluetoothSocketReceiver for " + socket.getRemoteDevice().getAddress());

        this.socket = socket;
        this.device = socket.getRemoteDevice();
    }

    @Override
    public void run() {
        try {

            started = true;

            this.ois = new ObjectInputStream(socket.getInputStream());

        } catch (IOException ioe) {
            Log.d(TAG,"BluetoothSocketReceiver: An exception was thrown trying to receive an object", ioe);
        }

        try {
            while (started) {
                if (ois == null) {
                    Log.d(TAG,"BluetoothSocketReceiver: output stream is null");

                    fireError();

                    continue;
                }

                if (!socket.isConnected()) {
                    Log.d(TAG,"BluetoothSocketReceiver: Socket isn't connected, stopping");
                    socket = null;
                    ois = null;
                    break;
                }

                try {
                    ProtostuffPayload payload = (ProtostuffPayload) ois.readObject();

                    System.out.println(String.format("BluetoothSocketReceiver: Payload received class: %s length:" +
                            " %s socket: %s", payload.getClassOfPayload(), payload.getBytes().length, socket));


                    fire(payload.unpack());

                    StatisticsManager.getStatistics().setLastContactWithPeer(new Date());

                } catch (IOException | ClassNotFoundException ioe) {
                    Log.d(TAG,"BluetoothSocketReceiver: Error on socket: " + socket);
                    fireError();
                }

            }

            Log.d(TAG,"BluetoothSocketReceiver: Receiver ended ");
        } catch (Exception e){
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    public void stopReceiver(){
        System.out.println("Stopping receiver, removing registration for " + device);
        started = false;

        WLTrackApp.dtnService.btlayer.removeRegistration(device);

        try {
            socket.close();
            ois.close();
        } catch (Exception e){
            Crashlytics.logException(e);
            e.printStackTrace();
        }

        socket = null;
    }
}
