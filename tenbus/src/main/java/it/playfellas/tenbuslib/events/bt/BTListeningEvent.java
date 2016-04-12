package it.playfellas.tenbuslib.events.bt;

import android.bluetooth.BluetoothDevice;

public class BTListeningEvent extends BTEvent {

    public BTListeningEvent(BluetoothDevice device) {
        super(device);
        this.message = "Listening";
    }
}
