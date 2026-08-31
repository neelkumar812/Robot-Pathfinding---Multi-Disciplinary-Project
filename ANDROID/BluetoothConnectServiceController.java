package com.example.mdp_android_grp21.ui.main;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectServiceController {

    private static final String TAG = "BluetoothConnection";

    private static final String group_name = "MDP_Group_21";
    public static final UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter btAdapter;
    Context context;

    private AcceptThread acceptThread;

    private ConnectThread connectThread;
    private BluetoothDevice btDevice;
    private UUID uuid;
    ProgressDialog progressDialog;
    Intent intent;

    public static boolean BluetoothConnectionStatus=false;
    private static ConnectedThread connectedThread;

    public BluetoothConnectServiceController(Context context) {
        this.btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
        startAcceptThread();
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket ServerSocket;

        public AcceptThread() {
            BluetoothServerSocket t = null;

            try {
                t = btAdapter.listenUsingInsecureRfcommWithServiceRecord(group_name, deviceUUID);
                Log.d(TAG, "Thread Accepted" + deviceUUID);
            }catch(IOException e){
                Log.e(TAG, "Thread ERROR: IOException: " + e.getMessage());
            }
            ServerSocket = t;
        }
        public void run(){
            BluetoothSocket sock =null;
            try {
                Log.d(TAG, "run: ...");

                sock = ServerSocket.accept();
                Log.d(TAG, "run");
            }catch (IOException e){
                Log.e(TAG, e.getMessage());
            }
            if(sock!=null){
                connected(sock, sock.getRemoteDevice());
            }
            Log.i(TAG, "END");
        }
        public void cancel(){
            Log.d(TAG, "cancel");
            try{
                ServerSocket.close();
            } catch(IOException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread{
        private BluetoothSocket btSocket;

        public ConnectThread(BluetoothDevice device, UUID u){
            uuid = u;
            btDevice = device;
        }

        public void run(){
            BluetoothSocket tmpSocket = null;
            Log.d(TAG, "RUN: mConnectThread");

            try {
                Log.d(TAG, "ConnectThread: InsecureRfcommSocket  " + deviceUUID);
                tmpSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            btSocket = tmpSocket;
            btAdapter.cancelDiscovery();

            try {
                btSocket.connect();
                connected(btSocket, btDevice);

            } catch (IOException e) {
                try {
                    btSocket.close();
                    Log.d(TAG, "socket is close.");
                } catch (IOException e1) {
                    Log.e(TAG, e.getMessage());
                }
                Log.d(TAG, "UUID."+ deviceUUID);
                try {
                    BluetoothPopUpView mBluetoothPopUpViewActivity = (BluetoothPopUpView) context;
                    mBluetoothPopUpViewActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Failed to connect to the Device.", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception z) {
                    z.printStackTrace();
                }

            }
            try {
                progressDialog.dismiss();
            } catch(NullPointerException e){
                e.printStackTrace();
            }
        }

        public void cancel(){
            try{
                btSocket.close();
            } catch(IOException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public synchronized void startAcceptThread(){
        Log.d(TAG, "startConnectionService");

        if(connectThread !=null){
            connectThread.cancel();
            connectThread =null;
        }
        if(acceptThread == null){
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public void startClientThread(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient: Started.");

        try {
            progressDialog = ProgressDialog.show(context, "Connecting Bluetooth", "Please Wait...", true);
        } catch (Exception e) {
            Log.d(TAG, "StartClientThread Dialog show failure");
        }


        connectThread = new ConnectThread(device, uuid);
        connectThread.start();
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mSocket;
        private final InputStream inStream;
        private final OutputStream outStream;

        public ConnectedThread(BluetoothSocket socket) {
            intent = new Intent("ConnectionStatus");
            intent.putExtra("Status", "connected");
            intent.putExtra("Device", btDevice);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            BluetoothConnectionStatus = true;

            this.mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            while(true){
                try {
                    bytes = inStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "Input: "+ incomingMessage);

                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("receivedMessage", incomingMessage);

                    LocalBroadcastManager.getInstance(context).sendBroadcast(incomingMessageIntent);
                } catch (IOException e) {
                    Log.e(TAG, "Error input. "+e.getMessage());

                    intent = new Intent("ConnectionStatus");
                    intent.putExtra("Status", "disconnected");
                    intent.putExtra("Device", btDevice);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    BluetoothConnectionStatus = false;

                    break;
                }
            }
        }

        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write to output : "+text);
            try {
                outStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error writing output . "+e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket mSocket, BluetoothDevice device) {
        Log.d(TAG, "connected: Starting.");
        this.btDevice =  device;
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        connectedThread = new ConnectedThread(mSocket);
        connectedThread.start();
    }

    public static void write(byte[] out){
        connectedThread.write(out);
    }
}


