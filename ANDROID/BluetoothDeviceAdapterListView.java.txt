package com.example.mdp_android_grp21.ui.main;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mdp_android_grp21.R;

import java.util.ArrayList;

public class BluetoothDeviceAdapterListView extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater layoutInflater;
    private ArrayList<BluetoothDevice> btDeviceArrayList;
    private int i;

    public BluetoothDeviceAdapterListView(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices) {
        super(context, tvResourceId, devices);
        this.btDeviceArrayList = devices;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        i = tvResourceId;
    }

    public View getView(int p, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(i, null);

        BluetoothDevice btDevice = btDeviceArrayList.get(p);

        if (btDevice != null) {
            TextView name = (TextView) convertView.findViewById(R.id.deviceName);
            TextView address = (TextView) convertView.findViewById(R.id.deviceAddress);

            if (name != null) {
                name.setText(btDevice.getName());
            }
            if (address != null) {
                address.setText(btDevice.getAddress());
            }
        }

        return convertView;
    }
}