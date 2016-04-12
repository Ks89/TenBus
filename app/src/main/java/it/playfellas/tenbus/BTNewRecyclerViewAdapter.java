package it.playfellas.tenbus;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BTNewRecyclerViewAdapter extends RecyclerView.Adapter<BTNewRecyclerViewAdapter.ViewHolder> {
    private ItemClickListener itemClickListener;

    public ItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    public List<BluetoothDevice> getNewDiscoveredDevices() {
        return newDiscoveredDevices;
    }

    private List<BluetoothDevice> newDiscoveredDevices;

    public BTNewRecyclerViewAdapter(@NonNull ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        setHasStableIds(true);
        newDiscoveredDevices = new ArrayList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View parent;

        @Bind(R.id.connectedDeviceImageView)
        ImageView connectedDeviceImageView;
        @Bind(R.id.nameTextView)
        TextView nameTextView;
        @Bind(R.id.addressTextView)
        TextView addressTextView;

        public ViewHolder(View view) {
            super(view);
            this.parent = view;
            ButterKnife.bind(this, view);
        }

        public void setOnClickListener(OnClickListener listener) {
            parent.setOnClickListener(listener);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.master_bt_new_cardview, viewGroup, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final BluetoothDevice device = newDiscoveredDevices.get(position);

        viewHolder.nameTextView.setText(device.getName());
        viewHolder.addressTextView.setText(device.getAddress());

        viewHolder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.connectToPaired(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newDiscoveredDevices.size();
    }

    public interface ItemClickListener {
        void connectToPaired(final BluetoothDevice device);
    }
}