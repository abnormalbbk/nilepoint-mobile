package com.nilepoint.monitorevaluatemobile.dtn;

import android.bluetooth.BluetoothDevice;

import java.util.Date;

/**
 * Created by ashaw on 7/16/17.
 */

public class BluetoothRegistration {
    BluetoothDevice device;
    BluetoothSocketReceiver receiver;
    BluetoothSocketSender sender;

    Date lastContact;

    public BluetoothRegistration(BluetoothDevice device,
                                 BluetoothSocketSender sender,
                                 BluetoothSocketReceiver receiver) {
        this.device = device;
        this.receiver = receiver;
        this.sender = sender;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothSocketReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(BluetoothSocketReceiver receiver) {
        this.receiver = receiver;
    }

    public BluetoothSocketSender getSender() {
        return sender;
    }

    public void setSender(BluetoothSocketSender sender) {
        this.sender = sender;
    }

    public Date getLastContact() {
        return lastContact;
    }

    public void setLastContact(Date lastContact) {
        this.lastContact = lastContact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BluetoothRegistration that = (BluetoothRegistration) o;

        return device != null ? device.equals(that.device) : that.device == null;

    }

    @Override
    public int hashCode() {
        return device != null ? device.hashCode() : 0;
    }
}
