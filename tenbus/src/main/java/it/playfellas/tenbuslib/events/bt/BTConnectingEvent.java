package it.playfellas.tenbuslib.events.bt;

import android.bluetooth.BluetoothDevice;

public class BTConnectingEvent extends BTEvent {

    public BTConnectingEvent(BluetoothDevice device) {
        super(device);
        this.message = "Connecting";
    }
}
