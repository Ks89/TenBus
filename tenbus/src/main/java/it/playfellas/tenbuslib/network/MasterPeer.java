package it.playfellas.tenbuslib.network;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import it.playfellas.tenbuslib.events.NetEvent;
import it.playfellas.tenbuslib.events.bt.BTDisconnectedEvent;

/**
 * Created by affo on 28/07/15.
 */
class MasterPeer extends Peer {

    private static final String TAG = MasterPeer.class.getSimpleName();
    private Map<String, BTMasterThread> threadMap;
    private int iterationStep;

    public MasterPeer() {
        super();
        threadMap = new LinkedHashMap<>(); // preserve insert order
        iterationStep = 0;
        TenBus.get().register(this);
    }

    @Subscribe
    public synchronized void onDeviceDisconnected(BTDisconnectedEvent event) {
        closeConnection(event.getDevice());
    }

    @Override
    public synchronized void obtainConnection(BluetoothDevice device) {
        if (threadMap.containsKey(device.getAddress())) {
            Log.w(TAG, "Already connected to " + device.getName());
            return;
        }

        try {
            BTMasterThread btMasterThread = new BTMasterThread(device);
            btMasterThread.start();
            threadMap.put(device.getAddress(), btMasterThread);
        } catch (IOException e) {
            Log.e(TAG, "Error in instantiating master thread", e);
        }
    }

    @Override
    public synchronized void closeConnection(BluetoothDevice device) {
        BTMasterThread removed = threadMap.remove(device.getAddress());
        if (removed != null) {
            removed.deactivate();
        } else {
            Log.w(TAG, "Non existing thread removed: " + device.getName());
        }

        int size = threadMap.size();
        if (size > 0 && iterationStep >= size) {
            iterationStep = size - 1;
        }
    }


    @Override
    public synchronized void close() {
        for (Map.Entry<String, BTMasterThread> btMasterThread : threadMap.entrySet()) {
            btMasterThread.getValue().deactivate();
        }
    }

    @Override
    public synchronized void sendMessage(NetEvent netEvent) throws IOException {
        for (Map.Entry<String, BTMasterThread> btMasterThread : threadMap.entrySet()) {
            btMasterThread.getValue().write(netEvent);
        }
    }

    @Override
    public synchronized int noDevices() {
        return threadMap.size();
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public synchronized BluetoothDevice next() {
        BluetoothDevice dev = (new ArrayList<>(threadMap.values())).get(iterationStep).getDevice();
        iterationStep++;
        int size = threadMap.size();
        if (iterationStep >= size) {
            iterationStep = 0; // circularity
        }
        return dev;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
