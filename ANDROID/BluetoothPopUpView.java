package com.example.mdp_android_grp21.ui.main;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.mdp_android_grp21.R;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothPopUpView extends AppCompatActivity {
    private static final String TAG = "BluetoothPopUp";
    BluetoothAdapter btAdapter;
    public ArrayList<BluetoothDevice> newBTDevices;
    public ArrayList<BluetoothDevice> btDeviceArrayList;
    public BluetoothDeviceAdapterListView btDeviceAdapterListView;
    public BluetoothDeviceAdapterListView pairedDeviceListAdapter;
    TextView textView1;
    ListView deviceListView;
    ListView pairedDeviceListView;
    Button connectBtn;
    ProgressDialog progressDialogBox;

    SharedPreferences sharedPreferencesInterface;
    SharedPreferences.Editor editorSharedPreferencesInterface;

    BluetoothConnectServiceController btConnectServiceController;
    private static final UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothDevice btDevice;

    boolean retry = false;
    Handler reconnectHandler = new Handler();

    Runnable reconnectionRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (!BluetoothConnectServiceController.BluetoothConnectionStatus) {
                    startConnectionService(btDevice, deviceUUID);
                    Toast.makeText(BluetoothPopUpView.this, "Success", Toast.LENGTH_SHORT).show();
                }
                reconnectHandler.removeCallbacks(reconnectionRunnable);
                retry = false;
            } catch (Exception e) {
                Toast.makeText(BluetoothPopUpView.this, "Trying to reconnect", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_pop_up_window);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);


        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Switch aSwitch = (Switch) findViewById(R.id.bluetoothSwitch);
        if(btAdapter.isEnabled()){
            aSwitch.setChecked(true);
            aSwitch.setText(getResources().getString(R.string.on));
        }

        deviceListView = (ListView) findViewById(R.id.dListView);
        pairedDeviceListView = (ListView) findViewById(R.id.pairedDevicesListView);
        newBTDevices = new ArrayList<>();
        btDeviceArrayList = new ArrayList<>();

        connectBtn = (Button) findViewById(R.id.connectBtn);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(FourthBroadcastReceiver, filter);

        IntentFilter filter2 = new IntentFilter("ConnectionStatus");
        LocalBroadcastManager.getInstance(this).registerReceiver(FifthBroadcastReceiver, filter2);

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                btAdapter.cancelDiscovery();
                pairedDeviceListView.setAdapter(pairedDeviceListAdapter);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    newBTDevices.get(i).createBond();
                    btConnectServiceController = new BluetoothConnectServiceController(BluetoothPopUpView.this);
                    btDevice = newBTDevices.get(i);
                }
            }
        });

        pairedDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                btAdapter.cancelDiscovery();
                deviceListView.setAdapter(btDeviceAdapterListView);
                btConnectServiceController = new BluetoothConnectServiceController(BluetoothPopUpView.this);
                btDevice = btDeviceArrayList.get(i);
            }
        });

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    compoundButton.setText(getResources().getString(R.string.on));
                }else
                {
                    compoundButton.setText(getResources().getString(R.string.off));
                }

                if(btAdapter ==null){
                    Toast.makeText(BluetoothPopUpView.this, "Device Does Not Support Bluetooth capabilities!", Toast.LENGTH_LONG).show();
                    compoundButton.setChecked(false);
                }
                else {
                    if (!btAdapter.isEnabled()) {
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
                        startActivity(discoverableIntent);

                        compoundButton.setChecked(true);

                        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                        registerReceiver(FirstBroadcastReceiver, BTIntent);

                        IntentFilter discoverIntent = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                        registerReceiver(SecondBroadcastReceiver, discoverIntent);
                    }
                    if (btAdapter.isEnabled()) {
                        btAdapter.disable();
                        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                        registerReceiver(FirstBroadcastReceiver, BTIntent);
                    }
                }
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btDevice == null)
                {
                    Toast.makeText(BluetoothPopUpView.this, "Please Select a Device before connecting.", Toast.LENGTH_LONG).show();
                }
                else {
                    startConnection();
                }
            }
        });


        Button backBtn = findViewById(R.id.backBtn);

        textView1 = (TextView) findViewById(R.id.TextView1);
        String connStatus = "Disconnected";
        sharedPreferencesInterface = getApplicationContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
        if (sharedPreferencesInterface.contains("connStatus"))
            connStatus = sharedPreferencesInterface.getString("connStatus", "");

        textView1.setText(connStatus);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorSharedPreferencesInterface = sharedPreferencesInterface.edit();
                editorSharedPreferencesInterface.putString("connStatus", textView1.getText().toString());
                editorSharedPreferencesInterface.apply();
                finish();
            }
        });

        progressDialogBox = new ProgressDialog(BluetoothPopUpView.this);
        progressDialogBox.setMessage("Reconnecting");
        progressDialogBox.setCancelable(false);
        progressDialogBox.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void toggleButtonScan(View view){
        newBTDevices.clear();
        if(btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                Toast.makeText(BluetoothPopUpView.this, "Please turn on your Bluetooth", Toast.LENGTH_SHORT).show();
            }
            if (btAdapter.isDiscovering()) {
                btAdapter.cancelDiscovery();
                checkPermissions();
                btAdapter.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(ThirdBroadcastReceiver, discoverDevicesIntent);
            } else if (!btAdapter.isDiscovering()) {
                checkPermissions();

                btAdapter.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(ThirdBroadcastReceiver, discoverDevicesIntent);
            }
            btDeviceArrayList.clear();
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            Log.d(TAG, "toggleButton: Number of paired devices found: "+ pairedDevices.size());
            for(BluetoothDevice device : pairedDevices){
                Log.d(TAG, "Paired Device: "+ device.getName() +" : " + device.getAddress());
                btDeviceArrayList.add(device);
                pairedDeviceListAdapter = new BluetoothDeviceAdapterListView(this, R.layout.device_adapter_view, btDeviceArrayList);
                pairedDeviceListView.setAdapter(pairedDeviceListAdapter);
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        } else {
            Log.d(TAG, "checkPermissions: No need to check permissions. SDK version < LOLLIPOP.");

        }
    }
    private final BroadcastReceiver FirstBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(btAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "FirstBroadcastReceiver: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "FirstBroadcastReceiver: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "FirstBroadcastReceiver: STATE ON");

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "FirstBroadcastReceiver: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver SecondBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(btAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);


            }
        }
    };

    private BroadcastReceiver ThirdBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice deviceOne = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                newBTDevices.add(deviceOne);
                Log.d(TAG, "onReceive: "+ deviceOne.getAddress() +" : " + deviceOne.getName());
                btDeviceAdapterListView = new BluetoothDeviceAdapterListView(context, R.layout.device_adapter_view, newBTDevices);
                deviceListView.setAdapter(btDeviceAdapterListView);

            }
        }
    };

    private BroadcastReceiver FourthBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                    Toast.makeText(BluetoothPopUpView.this, "Successfully " + device.getName(), Toast.LENGTH_SHORT).show();
                    btDevice = device;
                }
            }
        }
    };

    private BroadcastReceiver FifthBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferencesInterface = getApplicationContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
            editorSharedPreferencesInterface = sharedPreferencesInterface.edit();

            if(status.equals("connected")){
                try {
                    progressDialogBox.dismiss();
                } catch(NullPointerException e){
                    e.printStackTrace();
                }

                Log.d(TAG, "FifthBroadcastReceiver: Device now connected to "+ device.getName());
                Toast.makeText(BluetoothPopUpView.this, "Device now connected to "+ device.getName(), Toast.LENGTH_LONG).show();
                editorSharedPreferencesInterface.putString("connStatus", "Connected to " + device.getName());
                textView1.setText("Connected to " + device.getName());
            }
            else if(status.equals("disconnected") && !retry){
                Toast.makeText(BluetoothPopUpView.this, "Disconnected from "+ device.getName(), Toast.LENGTH_LONG).show();
                btConnectServiceController = new BluetoothConnectServiceController(BluetoothPopUpView.this);

                sharedPreferencesInterface = getApplicationContext().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
                editorSharedPreferencesInterface = sharedPreferencesInterface.edit();
                editorSharedPreferencesInterface.putString("connStatus", "Disconnected");
                TextView TextView1 = findViewById(R.id.TextView1);
                TextView1.setText("Disconnected");
                editorSharedPreferencesInterface.apply();

                try {
                    progressDialogBox.show();
                }catch (Exception e){
                    Log.d(TAG, "BluetoothPopUp: FifthBroadcastReceiver Dialog show failure");
                }
                retry = true;
                reconnectHandler.postDelayed(reconnectionRunnable, 5000);

            }
            editorSharedPreferencesInterface.commit();
        }
    };

    public void startConnection(){
        startConnectionService(btDevice,deviceUUID);
    }

    public void startConnectionService(BluetoothDevice device, UUID uuid){
        btConnectServiceController.startClientThread(device, uuid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(FirstBroadcastReceiver);
            unregisterReceiver(SecondBroadcastReceiver);
            unregisterReceiver(ThirdBroadcastReceiver);
            unregisterReceiver(FourthBroadcastReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(FifthBroadcastReceiver);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(FirstBroadcastReceiver);
            unregisterReceiver(SecondBroadcastReceiver);
            unregisterReceiver(ThirdBroadcastReceiver);
            unregisterReceiver(FourthBroadcastReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(FifthBroadcastReceiver);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("bluetoothDevice", btDevice);
        data.putExtra("deviceUUID",deviceUUID);
        setResult(RESULT_OK, data);
        super.finish();
    }
}
