package it.playfellas.tenbuslib.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import it.playfellas.tenbuslib.InternalConfig;
import it.playfellas.tenbuslib.events.EventFactory;

/**
 * Created by affo on 28/07/15.
 */
class BTSlaveThread extends BTThread {

    private static final String TAG = BTSlaveThread.class.getSimpleName();
    private BluetoothServerSocket mmServerSocket;

    @Override
    public BluetoothSocket pair() throws IOException {
        String address = BluetoothAdapter.getDefaultAdapter().getAddress();
        mmServerSocket = BluetoothAdapter.getDefaultAdapter()
                .listenUsingRfcommWithServiceRecord(InternalConfig.BT_APP_NAME_SECURE,
                        UUID.fromString(InternalConfig.BT_MY_SALT_SECURE + address.replace(":", "")));
        TenBus.get().post(EventFactory.btListening(null));
        // Blocking call
        BluetoothSocket s = mmServerSocket.accept();
        destroySocket();
        TenBus.get().post(EventFactory.btConnected(s.getRemoteDevice()));
        return s;
    }

    private void destroySocket() {
        if (mmServerSocket != null) {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Cannot close ServerSocket", e);
            }
            mmServerSocket = null;
        }
    }
}
