package com.nilepoint.monitorevaluatemobile.viewpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by claudiatrafton on 7/26/17.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private FragmentManager fragmentManager;
    private String[] tabTitles;

    public ViewPagerAdapter(FragmentManager fragmentManager){
        super(fragmentManager);
    }

    @Override
    public int getCount(){
        return tabTitles.length;
    }

    //this was made to be overridden!!!!
    @Override
    public Fragment getItem(int position){
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    public String[] getTabTitles() {
        return tabTitles;
    }

    public void setTabTitles(String[] tabTitles) {
        this.tabTitles = tabTitles;
    }

}
