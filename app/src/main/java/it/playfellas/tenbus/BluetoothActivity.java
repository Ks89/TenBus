package it.playfellas.tenbus;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.playfellas.tenbuslib.events.bt.BTConnectedEvent;
import it.playfellas.tenbuslib.events.bt.BTDisconnectedEvent;
import it.playfellas.tenbuslib.network.TenBus;

public class BluetoothActivity extends AppCompatActivity implements
        BTPairedRecyclerViewAdapter.ItemClickListener,
        BTNewRecyclerViewAdapter.ItemClickListener {

    private static final String TAG = BluetoothActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 2;


    @Bind(R.id.pairedDevicesRecyclerView)
    RecyclerView pairedDevicesRecyclerView;
    @Bind(R.id.newDevicesDevicesRecyclerView)
    RecyclerView newDevicesDevicesRecyclerView;
    @Bind(R.id.scanButton)
    Button scanButton;

    private BluetoothAdapter mBtAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    private BTNewRecyclerViewAdapter newAdapter;
    private BTPairedRecyclerViewAdapter pairedAdapter;

    private List<BluetoothDevice> connectedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_activity);

        ButterKnife.bind(this);

        checkBluetooth();


        connectedDevices = new ArrayList<>();

        //i created mBluetoothAdapter in MainActivity, but i need also here this object.
        //Indeed, i get the same instance with this static call.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //init recyclerviews and adapters
        newDevicesDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        newDevicesDevicesRecyclerView.setHasFixedSize(true);
        newAdapter = new BTNewRecyclerViewAdapter(this);
        newDevicesDevicesRecyclerView.setAdapter(newAdapter);
        newDevicesDevicesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        pairedDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        pairedDevicesRecyclerView.setHasFixedSize(true);
        pairedAdapter = new BTPairedRecyclerViewAdapter(this);
        pairedDevicesRecyclerView.setAdapter(pairedAdapter);
        pairedDevicesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedAdapter.getPairedDevices().add(device);
            }
            pairedAdapter.notifyDataSetChanged();
        }
    }

    private void saveDevice(BluetoothDevice device) {
    }


    @Override
    protected void onStart() {
        super.onStart();
        TenBus.get().register(this);

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // destroy any possible connection created before
        TenBus.get().detach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "BlueTooth enabled -> everything is ok!");
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BlueTooth not enabled");
                Toast.makeText(this, "BT not enabled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void checkBluetooth() {
        // If the adapter is null, then Bluetooth is not supported
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth non disponibile", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        TenBus.get().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {
        this.goBack();
    }


    @OnClick(R.id.scanButton)
    public void scanClick(View v) {
        doDiscovery();
        v.setVisibility(View.GONE);
    }

    private void goBack() {
        //reset preferences and go back to the MainActivity
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void connectToPaired(BluetoothDevice device) {
        mBtAdapter.cancelDiscovery();
        connectDevice(device.getAddress());
    }

    /**
     * The BroadcastReceiver that listens for discovered devices
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                // When discovery finds a device
                case BluetoothDevice.ACTION_FOUND:
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // If it's already paired, skip it, because it's been listed already
                    //Indicates the remote device is bonded (paired).
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        int positionToAdd = newAdapter.getNewDiscoveredDevices().size();
                        newAdapter.getNewDiscoveredDevices().add(device);
                        newAdapter.notifyItemInserted(positionToAdd);
                    }
                    break;
                //When discovery completes
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "Discovery completed!");
                    break;
            }
        }
    };

    private void doDiscovery() {
        Log.d(TAG, "Discovery started!");

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    /**
     * Establish connection with a device
     */
    private void connectDevice(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        Log.d(TAG, "Connecting...");
        TenBus.get().attach(device);
    }

    @Subscribe
    public void onBTConnectedEvent(BTConnectedEvent event) {
        //remove from the paired devices
        int positionToRemove = pairedAdapter.getPairedDevices().indexOf(event.getDevice());
        pairedAdapter.getPairedDevices().remove(event.getDevice());
        pairedAdapter.notifyItemRemoved(positionToRemove);

        //add device to the connected lists
        this.connectedDevices.add(event.getDevice());

        //save into preferences
        this.saveDevice(event.getDevice());

        //set the correct border-button up/down/left or right.
        //not only the visibility but also the device name inside
//        this.updatedBorderButtonState(event.getDevice().getName());
    }

    @Subscribe
    public void onBTDisconnectedEvent(BTDisconnectedEvent event) {
        //remove a device from the connected list
        this.connectedDevices.remove(event.getDevice());
    }
}
