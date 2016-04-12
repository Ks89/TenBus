package it.playfellas.tenbuslib.events;

import android.bluetooth.BluetoothDevice;

import it.playfellas.tenbuslib.events.bt.BTConnectedEvent;
import it.playfellas.tenbuslib.events.bt.BTConnectingEvent;
import it.playfellas.tenbuslib.events.bt.BTDisconnectedEvent;
import it.playfellas.tenbuslib.events.bt.BTErrorEvent;
import it.playfellas.tenbuslib.events.bt.BTListeningEvent;

public class EventFactory {
    public static StringNetEvent stringEvent(String body) {
        return new StringNetEvent(body);
    }

    public static BTDisconnectedEvent btDisconnected(BluetoothDevice device) {
        return new BTDisconnectedEvent(device);
    }

    public static BTConnectedEvent btConnected(BluetoothDevice device) {
        return new BTConnectedEvent(device);
    }

    public static BTListeningEvent btListening(BluetoothDevice device) {
        return new BTListeningEvent(device);
    }

    public static BTConnectingEvent btConnecting(BluetoothDevice device) {
        return new BTConnectingEvent(device);
    }

    public static BTErrorEvent btError(BluetoothDevice device, String msg) {
        return new BTErrorEvent(device, msg);
    }


}

