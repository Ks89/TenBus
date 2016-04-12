package it.playfellas.tenbuslib.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import it.playfellas.tenbuslib.InternalConfig;
import it.playfellas.tenbuslib.events.EventFactory;

/**
 * Created by affo on 28/07/15.
 */
class BTMasterThread extends BTThread {
    private static final String TAG = BTMasterThread.class.getSimpleName();
    private BluetoothDevice device;

    public BTMasterThread(BluetoothDevice device) throws IOException {
        this.device = device;
        mmSocket = device.createRfcommSocketToServiceRecord(
                UUID.fromString(InternalConfig.BT_MY_SALT_SECURE + device.getAddress().replace(":", "")));
    }

    @Override
    public BluetoothSocket pair() throws IOException {
        Log.i(TAG, "Pairing devices");
        // Always cancel discovery because it will slow down a connection
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        // Make a connection to the BluetoothSocket:
        // This is a blocking call and will only return on a
        // successful connection or an exception
        TenBus.get().post(EventFactory.btConnecting(mmSocket.getRemoteDevice()));
        mmSocket.connect();
        TenBus.get().post(EventFactory.btConnected(mmSocket.getRemoteDevice()));
        return mmSocket;
    }

    public BluetoothDevice getDevice() {
        return device;
    }
}
