package com.nilepoint.monitorevaluatemobile.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.SettingsListAdapter;

import java.util.Arrays;
import java.util.List;


public class SettingsActivity extends AppCompatActivity {

    //TODO make this a settings object, make each list, build out  dynamic settings pattern

    /*Create new settings here, and load them into the list. The IDs correspond to a title (as a key val pair)*/

    //variables to store and manage the list of settings
    private RecyclerView settingsListRecycler;
    protected RecyclerView.LayoutManager layoutManager;
    protected RecyclerView.Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //get array of the settings
        List<String> availableSettings = Arrays.asList(getResources().getStringArray(R.array.settings_options));
        
        //set up recyclerview and adapter for list
        settingsListRecycler = (RecyclerView) findViewById(R.id.setting_list_recyclerView);
        layoutManager = new LinearLayoutManager(this);
        adapter = new SettingsListAdapter(availableSettings, this);
        settingsListRecycler.setAdapter(adapter);
        settingsListRecycler.setLayoutManager(layoutManager);


        Log.d("SettingsActivity", "item count: " + adapter.getItemCount());


        //inflate and set the action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        //TODO change the title depending on the setting chosen
        getSupportActionBar().setTitle(R.string.action_settings);

    }

    //Open up the appropriate

    //Navigate home from up arrow in the action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
