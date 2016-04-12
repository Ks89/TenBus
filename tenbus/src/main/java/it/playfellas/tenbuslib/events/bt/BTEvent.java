package it.playfellas.tenbuslib.events.bt;

import android.bluetooth.BluetoothDevice;

import it.playfellas.tenbuslib.events.InternalEvent;

public abstract class BTEvent extends InternalEvent {
    protected String message = "BTEvent";
    private BluetoothDevice device;

    public BTEvent(BluetoothDevice device) {
        super();
        this.device = device;
    }

    @Override
    public String toString() {
        String prefix = "me: ";
        if (device != null) {
            prefix = device.getName() + ": ";
        }
        return prefix + message;
    }

    public BluetoothDevice getDevice() {
        return device;
    }
}
