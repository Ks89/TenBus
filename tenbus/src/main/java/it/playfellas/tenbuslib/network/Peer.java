package it.playfellas.tenbuslib.network;

import android.bluetooth.BluetoothDevice;

import java.io.IOException;
import java.util.Iterator;

import it.playfellas.tenbuslib.events.NetEvent;

/**
 * Created by affo on 28/07/15.
 */
abstract class Peer implements Iterator<BluetoothDevice> {
    /**
     * @param device can be `null` in case of slave
     * @throws IOException
     */
    public abstract void obtainConnection(BluetoothDevice device);

    /**
     * @param device can be `null` in case of slave
     * @throws IOException
     */
    public abstract void closeConnection(BluetoothDevice device);

    public abstract void close();

    public abstract void sendMessage(NetEvent netEvent) throws IOException;

    public abstract int noDevices();
}
