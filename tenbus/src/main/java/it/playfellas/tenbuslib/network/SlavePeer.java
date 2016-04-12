package it.playfellas.tenbuslib.network;

import android.bluetooth.BluetoothDevice;

import java.io.IOException;

import it.playfellas.tenbuslib.events.NetEvent;


/**
 * Created by affo on 28/07/15.
 */
class SlavePeer extends Peer {

    private BTSlaveThread btSlaveThread;

    public SlavePeer() {
        super();
    }

    /**
     * In this case device is not used. It can be passed as `null`.
     *
     * @param device can be `null` in case of slave
     * @throws IOException
     */
    @Override
    public void obtainConnection(BluetoothDevice device) {
        btSlaveThread = new BTSlaveThread();
        btSlaveThread.start();
    }

    /**
     * In this case device is not used. It can be passed as `null`.
     *
     * @param device can be `null` in case of slave
     */
    @Override
    public void closeConnection(BluetoothDevice device) {
        this.btSlaveThread.deactivate();
    }

    @Override
    public void close() {
        if (btSlaveThread != null) {
            btSlaveThread.deactivate();
        }
    }

    @Override
    public void sendMessage(NetEvent netEvent) throws IOException {
        btSlaveThread.write(netEvent);
    }

    @Override
    public int noDevices() {
        return 0;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public BluetoothDevice next() {
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
