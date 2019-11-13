package com.nilepoint.monitorevaluatemobile.tracking;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.nilepoint.monitorevaluatemobile.R;

/**
 * Created by ashaw on 9/17/17.
 */

public class SelectGroupHostActivity extends AppCompatActivity {
    private static final String TAG = "specify_activity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_find_group_host);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.select_group);

        SelectParticipantFromGroupFragment fragment = new SelectParticipantFromGroupFragment();

        startFragment(fragment);
    }

    public void startFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.find_group_fragment_container, fragment, TAG)
                .commit();
    }
}
