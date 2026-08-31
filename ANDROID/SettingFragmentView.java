package com.example.mdp_android_grp21.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.mdp_android_grp21.R;

public class SettingFragmentView extends Fragment {
    // Init
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ViewModel1 vm1;
    private static SharedPreferences.Editor preferencesEditor;

    SharedPreferences sharedPreferencesInterface;


@Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        vm1 = ViewModelProviders.of(this).get(ViewModel1.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }

        vm1.setIndex(index);

    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // inflate
        View root = inflater.inflate(R.layout.activity_setting, container, false);
        // get shared preferences

        sharedPreferencesInterface = getActivity().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
        preferencesEditor = sharedPreferencesInterface.edit();

        // Toolbar
        Button bluetoothButton = (Button) root.findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popup = new Intent(getActivity(), BluetoothPopUpView.class);
                startActivity(popup);
            }
        });



        return root;
    }



}
