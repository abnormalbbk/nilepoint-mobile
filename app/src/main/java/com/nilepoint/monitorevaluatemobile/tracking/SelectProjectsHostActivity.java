package com.nilepoint.monitorevaluatemobile.tracking;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.nilepoint.monitorevaluatemobile.R;

import io.realm.Realm;

public class SelectProjectsHostActivity extends AppCompatActivity {

    private static final String SPECIFY = "specify_activity";

    Realm realm = Realm.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_projects_host);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.select_project);
        SelectProjectFragment fragment = new SelectProjectFragment();

        fragment.setRealm(realm);

        startFragment(fragment);
    }


    public void startFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.select_project_activity_fragment_container, fragment, SPECIFY)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        realm.close();
    }
}
