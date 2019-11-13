package com.nilepoint.monitorevaluatemobile.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Service that will be used to discover other bluetooth devices and create neighbors.
 */

public class BluetoothDiscoveryService {
    public final static String TAG = "BluetoothDiscoverySrv";
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    private Context ctx;
    private BluetoothConvergenceLayer layer;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.i(TAG, "Got Action: " + action);

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.i(TAG, "Found device " + device.getName());
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.i(TAG, "Action Connection state changed " + device);
            }
        }
    };

    public BluetoothDiscoveryService(Context ctx, BluetoothConvergenceLayer layer) {
        this.ctx = ctx;
        this.layer = layer;
    }

    public void start(){
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        ctx.registerReceiver(mReceiver, filter);

        adapter.startDiscovery();


    }

    public void stop(){
        ctx.unregisterReceiver(mReceiver);
        adapter.cancelDiscovery();
    }
}
