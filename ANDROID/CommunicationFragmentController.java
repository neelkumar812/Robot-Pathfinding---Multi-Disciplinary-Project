package com.example.mdp_android_grp21.ui.main;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.mdp_android_grp21.MainActivity;
import com.example.mdp_android_grp21.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.nio.charset.Charset;

/**
 * A placeholder fragment containing a simple view.
 */

public class CommunicationFragmentController extends Fragment implements SensorEventListener {

    private static final String ARG_SECTION_NUMBER = "sectionNumber";
    private static final String TAG = "communicationFragment";

    private ViewModel1 viewModel1;

    // Declaration Variable
    // Shared Preferences
    SharedPreferences sharedPreferences;

    FloatingActionButton sendButton;
    private static TextView textView;
    private EditText editText;

    ImageButton forwardButton;
    ImageButton rightButton;
    ImageButton backButton;
    ImageButton leftButton;
    TextView robotStatistics;
    Switch tiltSwitch;
    private Sensor sensor;
    private SensorManager sensorManager;

    private static GridMapView gridMapView;

    public static CommunicationFragmentController newInstance(int index) {
        CommunicationFragmentController fragment = new CommunicationFragmentController();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel1 = ViewModelProviders.of(this).get(ViewModel1.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        viewModel1.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_comms, container, false);

        sendButton = (FloatingActionButton) root.findViewById(R.id.messageButton);

        // Message Box
        textView = (TextView) root.findViewById(R.id.messageReceivedTextView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        editText = (EditText) root.findViewById(R.id.typeBoxEditText);

        //Variable Declaration
        forwardButton = root.findViewById(R.id.forwardImageBtn);
        rightButton = root.findViewById(R.id.rightImageBtn);
        backButton = root.findViewById(R.id.backImageBtn);
        leftButton = root.findViewById(R.id.leftImageBtn);
        tiltSwitch = root.findViewById(R.id.phoneTiltSwitch);

        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // get shared preferences
        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);

        robotStatistics = MainActivity.getRoboStatusTextView();

        gridMapView = MainActivity.getGridMapDescriptor();

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked moveForwardImageBtn");
                if (gridMapView.getCanDrawRobot()) {
                    gridMapView.moveRobot("forward");
                    MainActivity.updateText();
                    if (gridMapView.getValidPosition())
                        updaterobotStatus("moving forward");
                    else
                        updaterobotStatus("Unable to move forward");
                    MainActivity.sendMessageToBlueTooth("w010");
                }
                else
                    updaterobotStatus("Please press 'STARTING POINT'");
                showLog("Exiting moveForwardImageBtn");
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked turnRightImageBtn");
                if (gridMapView.getCanDrawRobot()) {
                    gridMapView.moveRobot("right");
                    updaterobotStatus("turning right");
                    MainActivity.updateText();
                    MainActivity.sendMessageToBlueTooth("d090");
                }
                else
                    updaterobotStatus("Please set startConnectionService point");
                showLog("Exiting turnRightImageBtn");
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked backButton");
                if (gridMapView.getCanDrawRobot()) {
                    gridMapView.moveRobot("back");
                    MainActivity.updateText();
                    if (gridMapView.getValidPosition())
                        updaterobotStatus("moving backward");
                    else
                        updaterobotStatus("Unable to move backward");
                    MainActivity.sendMessageToBlueTooth("s010");
                }
                else
                    updaterobotStatus("Please press 'STARTING POINT'");
                showLog("Exiting moveBackwardImageBtn");
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked turnLeftImageBtn");
                if (gridMapView.getCanDrawRobot()) {
                    gridMapView.moveRobot("left");
                    MainActivity.updateText();
                    updaterobotStatus("turning left");
                    MainActivity.sendMessageToBlueTooth("d090");
                }
                else
                    updaterobotStatus("Please press 'STARTING POINT'");
                showLog("Exiting turnLeftImageBtn");
            }
        });

        tiltSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (gridMapView.getCanDrawRobot()) {
                    if(tiltSwitch.isChecked()){
                        showToast("Tilt motion control: ON");
                        tiltSwitch.setPressed(true);

                        sensorManager.registerListener(CommunicationFragmentController.this, sensor, sensorManager.SENSOR_DELAY_NORMAL);
                        sensorHandler.post(sensorDelay);
                    }else{
                        showToast("Tilt motion control: OFF");
                        try {
                            sensorManager.unregisterListener(CommunicationFragmentController.this);
                        }catch(IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                        sensorHandler.removeCallbacks(sensorDelay);
                    }
                } else {
                    updaterobotStatus("Set startConnectionService point");

                    tiltSwitch.setChecked(false);
                }
                if(tiltSwitch.isChecked()){
                    compoundButton.setText(getResources().getString(R.string.tilt_on));
                }else
                {
                    compoundButton.setText(getResources().getString(R.string.tilt_off));
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stext = "" + editText.getText().toString();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("message", sharedPreferences.getString("message", "") + '\n' + stext);
                editor.apply();
                textView.setText(sharedPreferences.getString("message", ""));
                editText.setText("");

                if (BluetoothConnectServiceController.BluetoothConnectionStatus) {
                    byte[] bytes = stext.getBytes(Charset.defaultCharset());
                    BluetoothConnectServiceController.write(bytes);
                }
            }
        });

        return root;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        if(sensorFlag) {
            if (y < -2) {
                gridMapView.moveRobot("forward");
                MainActivity.updateText();
                MainActivity.sendMessageToBlueTooth("f");
            } else if (y > 2) {
                gridMapView.moveRobot("back");
                MainActivity.updateText();
                MainActivity.sendMessageToBlueTooth("b");
            } else if (x > 2) {
                gridMapView.moveRobot("left");
                MainActivity.updateText();
                MainActivity.sendMessageToBlueTooth("l");
            } else if (x < -2) {
                gridMapView.moveRobot("right");
                MainActivity.updateText();
                MainActivity.sendMessageToBlueTooth("r");
            }
        }
        sensorFlag = false;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try{
            sensorManager.unregisterListener(CommunicationFragmentController.this);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    private void updaterobotStatus(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP,0, 0);
        toast.show();
    }
    private static void showLog(String message) {
        Log.d(TAG, message);
    }
    Handler sensorHandler = new Handler();
    boolean sensorFlag= false;
    private final Runnable sensorDelay = new Runnable() {
        @Override
        public void run() {
            sensorFlag = true;
            sensorHandler.postDelayed(this,1000);
        }
    };

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    public static TextView getMessage() {
        return textView;
    }
}