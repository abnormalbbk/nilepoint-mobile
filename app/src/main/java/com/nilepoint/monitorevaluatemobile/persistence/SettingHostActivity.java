package com.nilepoint.monitorevaluatemobile.persistence;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.fragment.SettingFragment;

public class SettingHostActivity extends AppCompatActivity {

    protected final static String TAG = "SettingHostActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_host);

        //inflate and set the action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //TODO change the title depending on the setting chosen
        getSupportActionBar().setTitle(R.string.settings_title);

        //get the position of the element clicked
        final Intent intent = getIntent();
        int position = intent.getIntExtra("setting_position",-5);
        Log.d(TAG, "POS: " + position);
        openSetting(position);
    }

    //close the fragment when clicking the back arrow
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    //Open the selected setting in a new fragment instant
    public void openSetting(int pos){
        Fragment settingFragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putInt("settingSelected",pos);
        settingFragment.setArguments(args);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.setting_container, settingFragment);

        /*TO DO: get settings from Realm persistance*/
        String[] titles = getResources().getStringArray(R.array.settings_options);
        getSupportActionBar().setTitle(titles[pos]);
        fragmentTransaction.commit();
    }
}
