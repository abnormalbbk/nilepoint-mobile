package com.nilepoint.monitorevaluatemobile.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.UserOption;
import com.nilepoint.monitorevaluatemobile.adapter.UserButtonsAdapter;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class UsersShownAtLoginSettingsActivity extends AppCompatActivity {

    private List<UserOption> userOptionList = new ArrayList<>(); //initialize empty list
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_shown_at_login_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Users shown at login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);

        //get currently available users
        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();
            for (User user : realm.where(User.class).findAll()) {
                //TODO create stub activity that will bring the user to the edit screen (use fragments?)
                userOptionList.add(new UserOption(user, R.color.colorWhite, null));
            }
        } finally {
            if (realm != null){
                realm.close();
            }
        }
        //userOptionList.add(new UserOption(this, R.color.colorWhite)); //TODO get icon from aynne. temporary placeholder/

        recyclerView = (RecyclerView) findViewById(R.id.users_login_settings_recyclerView);
        layoutManager = new LinearLayoutManager(this);
        adapter = new UserButtonsAdapter(userOptionList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
    }

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
