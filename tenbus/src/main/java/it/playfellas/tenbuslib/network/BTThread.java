package it.playfellas.tenbuslib.network;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import it.playfellas.tenbuslib.events.EventFactory;
import it.playfellas.tenbuslib.events.NetEvent;

/**
 * Created by affo on 28/07/15.
 */
abstract class BTThread extends Thread {

    private static final String TAG = "BTThread";

    protected BluetoothSocket mmSocket = null;
    private ObjectInputStream mmIn = null;
    private ObjectOutputStream mmOut = null;
    private TenBus bus = TenBus.get();
    private boolean active = true;

    public abstract BluetoothSocket pair() throws IOException;

    private synchronized boolean isActive() {
        return active;
    }

    public synchronized void deactivate() {
        this.active = false;
        try {
            // Closing the input stream to break the while and stop the thread
            mmIn.close();
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "Cannot close input stream on deactivate()", e);
        }
    }

    public void run() {
        try {
            mmSocket = pair();
            mmOut = new ObjectOutputStream(mmSocket.getOutputStream());
            mmOut.flush();
            mmIn = new ObjectInputStream(mmSocket.getInputStream());
        } catch (IOException e) {
            TenBus.get().post(EventFactory.btError(null, "Cannot pair devices"));
            cancel();
            return;
        }
        // Setting the thread name
        setName(TAG + ":" + mmSocket.getRemoteDevice().getName());

        while (isActive()) {
            try {
                NetEvent netEvent = (NetEvent) mmIn.readObject();
                // NOTE: the only case in which a NetEvent is posted
                // as internal. If not, an infinite loop would start!
                bus.postInternal(netEvent);
            } catch (IOException e) {
                Log.e(TAG, "Disconnected while listening", e);
                TenBus.get().post(EventFactory.btError(null, "Disconnected while listening"));
                break;
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Cannot deserialize incoming event", e);
                TenBus.get().post(EventFactory.btError(null, "Cannot deserialize incoming event"));
                break;
            }
        }
        cancel();
    }

    public void write(NetEvent netEvent) throws IOException {
        mmOut.writeObject(netEvent);
        mmOut.flush();
    }

    private void cancel() {
        try {
            if (mmSocket.isConnected()) {
                mmOut.close();
                mmSocket.close();
                mmIn.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Cannot close socket connection", e);
        } finally {
            TenBus.get().post(EventFactory.btDisconnected(mmSocket.getRemoteDevice()));
            mmSocket = null;
            mmIn = null;
            mmOut = null;
        }
    }
}
