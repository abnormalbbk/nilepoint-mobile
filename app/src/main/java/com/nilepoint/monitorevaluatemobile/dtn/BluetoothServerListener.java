package com.nilepoint.monitorevaluatemobile.dtn;

import android.bluetooth.BluetoothDevice;

/**
 * Created by ashaw on 7/14/17.
 */

public interface BluetoothServerListener {
    /**
     * Fired when a device is disconnected from the server
     *
     * @param device
     */
    public void onDeviceDisconnect(BluetoothDevice device);
}
