package com.example.mdp_android_grp21.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
//import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsStateAdapterController extends FragmentStateAdapter {

    private final List<Fragment> fragmentsArray =  new ArrayList<>();
//    private final List<String> fragmentsTitle =  new ArrayList<>();

    public SectionsStateAdapterController(Context context, @NonNull FragmentManager fm, @NonNull Lifecycle lc) {
        super(fm,lc);
    }
    public void addFragment (Fragment fragment, String Title){
        fragmentsArray.add(fragment);
//        fragmentsTitle.add(Title);

    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CommunicationFragmentController();
            case 1:
                return new MapTabFragmentController();
            case 2:
                return new SettingFragmentView();
        }
        return null;
    }
    public Fragment getItem(int position) {
        return fragmentsArray.get(position);

    }
//    @Override
//    public CharSequence getPageTitle(int position) {
//        return fragmentsTitle.get(position);
//    }

    @Override
    public int getItemCount() {
        return fragmentsArray.size();
    }
//    public int getCount() {
//        return fragmentsArray.size();
//    }

}