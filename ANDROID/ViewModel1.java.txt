package com.example.mdp_android_grp21.ui.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class ViewModel1 extends androidx.lifecycle.ViewModel {

    private MutableLiveData<Integer> mutableIndex = new MutableLiveData<>();
    private LiveData<String> mutableText = Transformations.map(mutableIndex, new Function<Integer, String>() {
        @Override
        public String apply(Integer input) {
            return "Hello world: " + input;
        }
    });

    public void setIndex(int index) {

        mutableIndex.setValue(index);


    }

    public LiveData<String> getText() {
        return mutableText;
    }
}