package com.nilepoint.monitorevaluatemobile.selector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.HomeActivity;
import com.nilepoint.monitorevaluatemobile.LoginActivity;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.UserOption;
import com.nilepoint.monitorevaluatemobile.adapter.UserButtonsAdapter;

import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by ashaw on 9/14/17.
 */

public abstract class GenericSelectorActivity extends AppCompatActivity {

    public static final String TAG = GenericSelectorActivity.class.getName();

    protected ArrayList<Selection> selections = getSelections();
    protected RecyclerView recyclerView;
    protected SelectorAdapter adapter;
    protected LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (selections == null){
            Log.e(TAG, "Selections property not overrided");
        }

        //TODO Realm storage of if first user or not
        setContentView(R.layout.activity_login);

        recyclerView = (RecyclerView) findViewById(R.id.user_options_recyclerView);

        layoutManager = new LinearLayoutManager(this);

        adapter = new SelectorAdapter(selections, this);

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(layoutManager);

    }

    protected abstract ArrayList<Selection> getSelections();
}
