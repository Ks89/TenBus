package it.playfellas.tenbuslib.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.io.IOException;

import it.playfellas.tenbuslib.InternalConfig;
import it.playfellas.tenbuslib.events.EventFactory;
import it.playfellas.tenbuslib.events.InternalEvent;
import it.playfellas.tenbuslib.events.NetEvent;


/**
 * Wraps Otto Bus to extend it with the ability to post on the main thread
 */
public class TenBus {
    private static final String TAG = TenBus.class.getSimpleName();
    private static TenBus bus;
    private final Handler mainThread = new Handler(Looper.getMainLooper());
    private Bus ottoBus;
    private Peer peer;

    private TenBus(ThreadEnforcer t) {
        ottoBus = new Bus(t);
        peer = null;
    }

    public synchronized static TenBus get() {
        if (bus == null) {
            bus = new TenBus(ThreadEnforcer.ANY);
        }
        return bus;
    }

    public void attach(BluetoothDevice device) {
        if (peer == null) {
            // need to instantiate a new peer
            if (device == null) {
                peer = new SlavePeer();
            } else {
                peer = new MasterPeer();
            }
        }
        peer.obtainConnection(device);
    }

    /**
     * Method to disconnect a device from this device.
     * To disconnect a slave peer, simply call detach(null).
     * To disconnect a slave from a master, pass a `device` in.
     *
     * @param device can be `null` in case of slave
     */
    public void detach(BluetoothDevice device) {
        peer.closeConnection(device);
    }

    public void detach() {
        if (peer == null) {
            Log.e(TAG, "Cannot detach if not attached!");
            return;
        }

        peer.close();
        peer = null;
        Log.i(TAG, "Detached from TenBus");
    }

    public void register(final Object subscriber) {
        Log.d(TAG, subscriber.getClass().getSimpleName() + " registered");
        ottoBus.register(subscriber);
    }

    public void unregister(final Object subscribedObj) {
        Log.d(TAG, subscribedObj.getClass().getSimpleName() + " unregistered");
        ottoBus.unregister(subscribedObj);
    }

    private void logEvent(final Object event) {
        if (InternalConfig.BT_DEBUG) {
            Log.d(event.getClass().getSimpleName(), event.toString());
        }
    }

    void postInternal(final Object event) {
        logEvent(event);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            ottoBus.post(event);
        } else {
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    ottoBus.post(event);
                }
            });
        }
    }

    private void postNet(final NetEvent event) throws IOException {
        logEvent(event);

        if (peer == null) {
            Log.e(TAG, "Cannot send NetEvent if no device is attached!");
            throw new IOException();
        }
        peer.sendMessage(event);
    }

    public void post(final InternalEvent e) {
        postInternal(e);
    }

    public void post(final NetEvent e) {
        try {
            postNet(e);
        } catch (IOException ex) {
            String msg = "IO error on posting event " + e.toString();
            Log.e(TAG, msg);
            postInternal(EventFactory.btError(null, msg));
        }
    }

    /**
     * Use this method to get the next `BluetoothDevice` connected to this device.
     * Note that devices are inspected circularly: there is always a next one.
     *
     * @return the next `BluetoothDevice` connected to this device.
     * If no device is connected, it returns `null`.
     */
    public BluetoothDevice nextDevice() {
        return peer.next();
    }

    public String myBTAddress() {
        return BluetoothAdapter.getDefaultAdapter().getAddress();
    }

    public String myBTName() {
        return BluetoothAdapter.getDefaultAdapter().getName();
    }

    /**
     * Note that the number of devices can change during iteration.
     * Re-invoke this method to update the number.
     *
     * @return the actual number of devices connected.
     * 0 in case of a slave device.
     */
    public int noDevices() {
        return peer.noDevices();
    }
}


