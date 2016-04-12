package it.playfellas.tenbuslib.events;

import android.bluetooth.BluetoothAdapter;

import java.io.Serializable;

public abstract class NetEvent implements Serializable {
    public final String deviceName = BluetoothAdapter.getDefaultAdapter().getName();
    public final String deviceAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
}
