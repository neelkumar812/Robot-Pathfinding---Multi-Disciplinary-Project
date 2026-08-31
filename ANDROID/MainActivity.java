package com.example.mdp_android_grp21;

import java.util.concurrent.TimeUnit;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import androidx.viewpager.widget.ViewPager;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;
import com.example.mdp_android_grp21.ui.main.MapTabFragmentController;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.mdp_android_grp21.ui.main.SectionsStateAdapterController;
import com.example.mdp_android_grp21.ui.main.SettingFragmentView;
import com.google.android.material.tabs.TabLayout;
import com.example.mdp_android_grp21.ui.main.BluetoothConnectServiceController;
import com.example.mdp_android_grp21.ui.main.CommunicationFragmentController;
//import com.example.mdp_android_grp21.ui.main.ManualFragmentController;
import com.example.mdp_android_grp21.ui.main.GridMapView;
import com.google.android.material.tabs.TabLayoutMediator;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private static GridMapView gridMapViewDescriptor;
    static TextView xAxisTextView, yAxisTextView, directionTextView, roboStatusTextView;

    // Declaration Variables
    private static SharedPreferences sharedPreferencesInterface;
    private static SharedPreferences.Editor editorSharedPreferences;
    private static Context context;
    private static ImageView raceCar;

    BluetoothDevice btDevice;
    ProgressDialog progressDialogBox;
    private static final String TAG = "Main Activity";
    private int[] tabIcons = {
            R.drawable.tab_comm_image,
            R.drawable.tab_map_image,
            R.drawable.tab_automation_image,
            R.drawable.tab_bluetooth_image
    };
    private TabLayout tabLayouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_main);
        SectionsStateAdapterController SectionsStateAdapterController = new SectionsStateAdapterController(this, getSupportFragmentManager(), getLifecycle());
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        ((SectionsStateAdapterController) SectionsStateAdapterController).addFragment(new CommunicationFragmentController(), "Command");
        ((SectionsStateAdapterController) SectionsStateAdapterController).addFragment(new MapTabFragmentController(), "Map");
        ((SectionsStateAdapterController) SectionsStateAdapterController).addFragment(new SettingFragmentView(), "Bluetooth");
        viewPager.setAdapter(SectionsStateAdapterController);
        viewPager.setOffscreenPageLimit(9999);
        tabLayouts = findViewById(R.id.tabs);
        new TabLayoutMediator(tabLayouts,viewPager,
                new TabLayoutMediator.TabConfigurationStrategy(){
                    @Override
                    public  void onConfigureTab(@NonNull TabLayout.Tab tab, int position){
                    };
                            }).attach();
        tabLayouts.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() { //tab elements
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                super.onTabSelected(tab);
                switch(tab.getPosition()){
                    case 0:     tabLayouts.getTabAt(0).setIcon(R.drawable.tab_comm_selected);
                                break;
                    case 1:     tabLayouts.getTabAt(1).setIcon(R.drawable.tab_map_selected);
                                break;
                    case 2:     tabLayouts.getTabAt(2).setIcon(R.drawable.tab_automation_selected);
                                break;
                    case 3:     tabLayouts.getTabAt(3).setIcon(R.drawable.tab_bluetooth_selected);
                                break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
//                super.onTabUnselected(tab);
                switch(tab.getPosition()){
                    case 0:     tabLayouts.getTabAt(0).setIcon(R.drawable.tab_comm_image);
                                break;
                    case 1:     tabLayouts.getTabAt(1).setIcon(R.drawable.tab_map_image);
                                break;
                    case 2:     tabLayouts.getTabAt(2).setIcon(R.drawable.tab_automation_image);
                                break;
                    case 3:     tabLayouts.getTabAt(3).setIcon(R.drawable.tab_bluetooth_image);
                                break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
//                super.onTabReselected(tab);
            }
        });


        setupTabIcons();

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        MainActivity.context = getApplicationContext();
        this.sharedPreferences();
        editorSharedPreferences.putString("message", "");
        editorSharedPreferences.putString("direction","None");
        editorSharedPreferences.putString("connStatus", "Disconnected");
        editorSharedPreferences.commit();
        gridMapViewDescriptor = new GridMapView(this);
        gridMapViewDescriptor = findViewById(R.id.mapView);
        raceCar = findViewById(R.id.racecar);
        raceCar.setVisibility(View.GONE);
        xAxisTextView = findViewById(R.id.xAxisTextView);
        yAxisTextView = findViewById(R.id.yAxisTextView);
        directionTextView = findViewById(R.id.directionAxisTextView);

        roboStatusTextView = findViewById(R.id.robotStatusTextView);

        progressDialogBox = new ProgressDialog(MainActivity.this);
        progressDialogBox.setMessage("Reconnecting");
        progressDialogBox.setCancelable(false);
        progressDialogBox.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


    }

    private void setupTabIcons() {
        tabLayouts.getTabAt(0).setIcon(R.drawable.tab_comm_selected);
        tabLayouts.getTabAt(1).setIcon(tabIcons[1]);
        tabLayouts.getTabAt(2).setIcon(tabIcons[3]);
        tabLayouts.getTabAt(0).setText("Command");
        tabLayouts.getTabAt(1).setText("Map");
        tabLayouts.getTabAt(2).setText("Bluetooth");
    }



    public static TextView getRoboStatusTextView() {  return roboStatusTextView; }

    public static void sharedPreferences() {
        sharedPreferencesInterface = MainActivity.getSharedPreferences(MainActivity.context);
        editorSharedPreferences = sharedPreferencesInterface.edit();
    }

    public static ImageView getRaceCar(){return raceCar;}

    public static void updateRaceCar(int col, int row,String direction){
        float sizeOfCell = gridMapViewDescriptor.getCellSize();
        switch(direction){
            case "up":
                raceCar.animate().x((col-1) * sizeOfCell).y((float) (row-1 ) * sizeOfCell).rotation(0).start();
                break;
            case "down":
                raceCar.animate().x((col-1) * sizeOfCell).y((float) (row-1 ) * sizeOfCell).rotation(180).start();
                break;
            case"left":
                raceCar.animate().x((col-1) * sizeOfCell).y((float) (row-1 ) * sizeOfCell).rotation(270).start();
                break;
            case "right":
                raceCar.animate().x((col-1) * sizeOfCell).y((float) (row-1 ) * sizeOfCell).rotation(-270).start();
                break;
            default: raceCar.animate().x((col-1) * sizeOfCell).y((float) (row-1 ) * sizeOfCell).rotation(0).start();;
        }
    }

    // Send message to bluetooth
    public static void sendMessageToBlueTooth(String message) {
        showLog("Entering sendMessageToBlueTooth");
        editorSharedPreferences = sharedPreferencesInterface.edit();

        if (BluetoothConnectServiceController.BluetoothConnectionStatus) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothConnectServiceController.write(bytes);
        }
        showLog(message);
        editorSharedPreferences.putString("message", CommunicationFragmentController.getMessage().getText() + "\n" + message);
        editorSharedPreferences.apply();
        refreshBlueToothMessage();
    }

    public static void sendMessageToBlueTooth(String message, int x, int y) throws JSONException {
        sharedPreferences();

        JSONObject jsonObjects = new JSONObject();
        String message1;

        if ("waypoint".equals(message)) {
            jsonObjects.put(message, message);
            jsonObjects.put("x", x);
            jsonObjects.put("y", y);
            message1 = "ALG|" + message + " (" + x + "," + y + ")";
        } else {
            message1 = "Unexpected message: " + message;
        }
        editorSharedPreferences.putString("message", CommunicationFragmentController.getMessage().getText() + "\n" + message1);
        editorSharedPreferences.commit();
        if (BluetoothConnectServiceController.BluetoothConnectionStatus) {
            byte[] bytes = message1.getBytes(Charset.defaultCharset());
            BluetoothConnectServiceController.write(bytes);
        }
        showLog("Exiting sendMessageToBlueTooth");
    }

    public static void refreshBlueToothMessage() {
        CommunicationFragmentController.getMessage().setText(sharedPreferencesInterface.getString("message", ""));
        CommunicationFragmentController.getMessage().append(" ");
    }


    public void updateDirection(String direction) {
        gridMapViewDescriptor.setDirectionOfRobot(direction);
        directionTextView.setText(sharedPreferencesInterface.getString("direction",""));
        sendMessageToBlueTooth("Direction is set to " + direction);
    }

    public static void updateText() {
        xAxisTextView.setText(String.valueOf(gridMapViewDescriptor.getCurrentCoordinates()[0]-1));
        yAxisTextView.setText(String.valueOf(gridMapViewDescriptor.getCurrentCoordinates()[1]-1));
        directionTextView.setText(sharedPreferencesInterface.getString("direction",""));
    }

    public static void receiveMessage(String message) {
        sharedPreferences();
        editorSharedPreferences.putString("message", sharedPreferencesInterface.getString("message", "") + "\n" + message);
        editorSharedPreferences.commit();
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    private BroadcastReceiver FifthBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences();

            if(status.equals("connected")){
                try {
                    progressDialogBox.dismiss();
                } catch(NullPointerException e){
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "Device now connected to "+mDevice.getName(), Toast.LENGTH_LONG).show();
                editorSharedPreferences.putString("connStatus", "Connected to " + mDevice.getName());
            }
            else if(status.equals("disconnected")){
                Toast.makeText(MainActivity.this, "Disconnected from "+mDevice.getName(), Toast.LENGTH_LONG).show();

                editorSharedPreferences.putString("connStatus", "Disconnected");
                progressDialogBox.show();
            }
            editorSharedPreferences.commit();
        }
    };
    public static boolean isNumeric(String string) {
        int intValue;

        System.out.println(String.format("Parsing string: \"%s\"", string));

        if(string == null || string.equals("")) {
            System.out.println("String cannot be parsed, it is null or empty.");
            return false;
        }

        try {
            intValue = Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Input String cannot be parsed to Integer.");
        }
        return false;
    }

    private void moveRobot(GridMapView robot, int steps, boolean forward){
        String dir;
        if (forward)
            dir = "forward";
        else
            dir = "back";

        for (int i=0; i<steps;i+=10){
            robot.moveRobot(dir);
/*            TimeUnit.SECONDS.sleep((long)0.5);*/
        }
    }

    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("receivedMessage");
            showLog("MESSAGEMESSAGE "+message);
            GridMapView gridMapView = MainActivity.getGridMapDescriptor();
            if (gridMapView.getCanDrawRobot()){
                int steps;
                switch(message.charAt(0)){
                    case 'q': //left
                        moveRobot(gridMapView, 20,true);
                        gridMapView.moveRobot("left");
                        moveRobot(gridMapView, 30,true);
                        break;
                    case 'e':// right
                        moveRobot(gridMapView, 20,true);
                        gridMapView.moveRobot("right");
                        moveRobot(gridMapView, 30,true);
                        break;
                    case 'w':// forward
                        steps = Integer.valueOf(message.substring(1,4));
                        moveRobot(gridMapView, steps,true);
                        break;
                    case 's':// back
                        steps = Integer.valueOf(message.substring(1,4));
                        moveRobot(gridMapView, steps,false);
                        break;
                    case 'd': //reverse right
                        moveRobot(gridMapView, 30,false);
                        gridMapView.moveRobot("left");
                        moveRobot(gridMapView, 20,false);
                        break;
                    case 'a': //reverse right
                        moveRobot(gridMapView, 30,false);
                        gridMapView.moveRobot("right");
                        moveRobot(gridMapView, 20,false);
                        break;
                    default:break;
                }
            }
            try {
                if (message.substring(0,18).equals("STATUS LOOKINGFOR ")){
                    roboStatusTextView.setText("Looking For Target "+message.substring(18,19));
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            // From AMDTOOL
            try{
               if (message.substring(0,6).equals("ENDRUN")){
                   roboStatusTextView.setText("Run Ended");
               }
            }catch (Exception e) {
                e.printStackTrace();
            }
            try{
                if (message.substring(0,5).equals("ROBOT")){
                    if (message.length()==9){
                        if(isNumeric(message.substring(6,7))&&isNumeric(message.substring(8,9))) {
                            sendMessageToBlueTooth("Updating Target ID");
                            gridMapViewDescriptor.setObstacleText(message.substring(6,7), message.substring(8,9));
                        }
                    }
                    if (message.length()==11){
                        if(isNumeric(message.substring(6,8))&&isNumeric(message.substring(9,11))) {
                            sendMessageToBlueTooth("Updating Target ID");
                            gridMapViewDescriptor.setObstacleText(message.substring(6,8), message.substring(9,11));
                        }
                    }
                    if (message.length()==10){
                        if(isNumeric(message.substring(6,8))&&isNumeric(message.substring(9,10))) {
                            sendMessageToBlueTooth("Updating Target ID");
                            gridMapViewDescriptor.setObstacleText(message.substring(6,8), message.substring(9,10));
                        }
                    }
                    if (message.length()==10){
                        if(isNumeric(message.substring(6,7))&&isNumeric(message.substring(8,10))) {
                            sendMessageToBlueTooth("Updating Target ID");
                            gridMapViewDescriptor.setObstacleText(message.substring(6,7), message.substring(8,10));;
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            try {
                String direction = "";
                Boolean flag = false;
                if (message.substring(message.length()-1).equals("N")){
                    flag = true;
                    direction = "up";
                }
                if (message.substring(message.length()-1).equals("S")){
                    flag = true;
                    direction = "down";
                }
                if (message.substring(message.length()-1).equals("W")){
                    flag = true;
                    direction = "left";
                }
                if (message.substring(message.length()-1).equals("E")){
                    flag = true;
                    direction = "right";
                }
                if (message.substring(0,5).equals("ROBOT")&&flag){

                    if (message.length()==11){
                        if(isNumeric(message.substring(6,7))&&isNumeric(message.substring(8,9))){
                            int x1 = Integer.parseInt(message.substring(6,7));
                            int y1 = Integer.parseInt(message.substring(8,9));
                            if (x1>1 && y1>1 && x1<20 && y1<20){
                                gridMapViewDescriptor.setCurrentCoordinates(x1,y1,direction);
                                String text123 = gridMapViewDescriptor.getRobotStatus();
                                roboStatusTextView.setText("Updating Position");
                                new java.util.Timer().schedule(
                                        new java.util.TimerTask() {
                                            @Override
                                            public void run() {
                                                // your code here
                                                roboStatusTextView.setText(text123);
                                            }
                                        },
                                        2500
                                );
                            }
                        }
                    }

                    if (message.length()==13){
                        if(isNumeric(message.substring(6,8))&&isNumeric(message.substring(9,11))){
                            int x1 = Integer.parseInt(message.substring(6,8));
                            int y1 = Integer.parseInt(message.substring(9,11));
                            if (x1>1 && y1>1 && x1<20 && y1<20){
                                gridMapViewDescriptor.setCurrentCoordinates(x1,y1,direction);
                                String text123 = gridMapViewDescriptor.getRobotStatus();
                                roboStatusTextView.setText("Updating Position");
                                new java.util.Timer().schedule(
                                        new java.util.TimerTask() {
                                            @Override
                                            public void run() {
                                                // your code here
                                                roboStatusTextView.setText(text123);
                                            }
                                        },
                                        2500
                                );
                            }
                        }
                    }

                    if (message.length()==12){
                        if(isNumeric(message.substring(6,7))&&isNumeric(message.substring(8,10))){
                            int x1 = Integer.parseInt(message.substring(6,7));
                            int y1 = Integer.parseInt(message.substring(8,10));
                            if (x1>1 && y1>1 && x1<20 && y1<20){
                                gridMapViewDescriptor.setCurrentCoordinates(x1,y1,direction);
                                String text123 = gridMapViewDescriptor.getRobotStatus();
                                roboStatusTextView.setText("Updating Position");
                                new java.util.Timer().schedule(
                                        new java.util.TimerTask() {
                                            @Override
                                            public void run() {
                                                // your code here
                                                roboStatusTextView.setText(text123);
                                            }
                                        },
                                        2500
                                );
                            }
                        }
                    }

                    if (message.length()==12){
                        if(isNumeric(message.substring(6,8))&&isNumeric(message.substring(9,10))){
                            int x1 = Integer.parseInt(message.substring(6,8));
                            int y1 = Integer.parseInt(message.substring(9,10));
                            if (x1>1 && y1>1 && x1<20 && y1<20){
                                gridMapViewDescriptor.setCurrentCoordinates(x1,y1,direction);
                                String text123 = gridMapViewDescriptor.getRobotStatus();
                                roboStatusTextView.setText("Updating Position");
                                new java.util.Timer().schedule(
                                        new java.util.TimerTask() {
                                            @Override
                                            public void run() {
                                                // your code here
                                                roboStatusTextView.setText(text123);
                                            }
                                        },
                                        2500
                                );
                            }
                        }
                    }

                }
                flag = false;
            } catch (Exception e) {
                showLog("Updating Position Failed");
            }
            try {
                if (message.length() > 7 && message.substring(2,6).equals("grid")) {
                    StringBuilder resultString = new StringBuilder();
                    String amdString = message.substring(11,message.length()-2);
                     BigInteger hexBigIntegerExplored = new BigInteger(amdString, 16);
                    StringBuilder exploredString = new StringBuilder(hexBigIntegerExplored.toString(2));


                    for(int i = exploredString.length() ; i < 300; i++)
                    {
                        exploredString.insert(0, "0");
                    }


                    int i = 0;
                    while(i<exploredString.length())
                    {

                        StringBuilder subString = new StringBuilder();
                        for(int j=0; j<15; j++)
                        {
                            subString.append(exploredString.charAt(j + i));
                        }
                        resultString.insert(0, subString.toString());
                        i = i+15;
                    }

                    hexBigIntegerExplored = new BigInteger(resultString.toString(), 2);
                    resultString = new StringBuilder(hexBigIntegerExplored.toString(16));

                    JSONObject amd = new JSONObject();
                    amd.put("explored", "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
                    amd.put("length", amdString.length()*4);
                    amd.put("obstacle", resultString.toString());
                    JSONArray amdArray = new JSONArray();
                    amdArray.put(amd);
                    JSONObject amdM = new JSONObject();
                    amdM.put("map", amdArray);
                    message = String.valueOf(amdM);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // IMAGE
            try {

                String regexpStr = "\\[(.*?)\\]";
                Pattern pattern = Pattern.compile(regexpStr);
                Matcher m = pattern.matcher(message);
                String imageString ="{imageID,x,y} = ";
                boolean hasImage = false;
                while(m.find()) {
                    String found = m.group();
                    found = found.replaceAll("[^\\d]", " ");
                    found = found.trim();
                    found = found.replaceAll("\\s+", " ");
                    String[] arrOfDigits = found.split(" ");
                    int x = 1, y = 1, id = 1;
                    if(arrOfDigits.length==3){
                        x = Integer.parseInt(arrOfDigits[0]);
                        y = Integer.parseInt(arrOfDigits[1]);
                        id = Integer.parseInt(arrOfDigits[2]);
                        gridMapViewDescriptor.displayNumberInCell(x,y,id);
                        showLog("Image Added for index: " + x + "," +y);
                        imageString = imageString + "("+id+","+x+","+y+")," ;
                        hasImage=true;
                    }
                }
                if(hasImage){
                    editorSharedPreferences = sharedPreferencesInterface.edit();
                    editorSharedPreferences.putString("message", CommunicationFragmentController.getMessage().getText() + "\n" + imageString);
                    editorSharedPreferences.apply();
                    refreshBlueToothMessage();
                }

            } catch (Exception e) {
                showLog("Adding Image Failed");
            }


            sharedPreferences();
            String receivedText = sharedPreferencesInterface.getString("message", "") + "\n" + message;
            editorSharedPreferences.putString("message", receivedText);
            editorSharedPreferences.commit();
            refreshBlueToothMessage();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                btDevice = (BluetoothDevice) data.getExtras().getParcelable("btDevice");
                UUID myUUID = (UUID) data.getSerializableExtra("myUUID");
            }
        }
    }
    public static GridMapView getGridMapDescriptor() {
        return gridMapViewDescriptor;
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(FifthBroadcastReceiver);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(FifthBroadcastReceiver);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            IntentFilter filter2 = new IntentFilter("ConnectionStatus");
            LocalBroadcastManager.getInstance(this).registerReceiver(FifthBroadcastReceiver, filter2);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        showLog("Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(TAG, "onSaveInstanceState");
        showLog("Exiting onSaveInstanceState");
    }
}