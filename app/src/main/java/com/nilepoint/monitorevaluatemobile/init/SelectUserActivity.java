package com.nilepoint.monitorevaluatemobile.init;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.kishan.askpermission.AskPermission;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.HomeActivity;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.UserOption;
import com.nilepoint.monitorevaluatemobile.adapter.UserButtonsAdapter;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by ashaw on 2/7/18.
 */

public class SelectUserActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<UserOption> userOptionList = new ArrayList<>(); //initialize empty list
    private RecyclerView.Adapter adapter;
    private Button doneBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO Realm storage of if first user or not
        setContentView(R.layout.activity_select_user);

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            for (User user : realm.where(User.class).findAll()) {
                Intent intent = new Intent(getBaseContext(), HomeActivity.class);

                intent.putExtra("user.id", user.getId());

                userOptionList.add(new UserOption(user, R.color.colorWhite,
                        null));
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
        //userOptionList.add(new UserOption(this, R.color.colorHomeCard2)); //TODO get icon from aynne. temporary placeholder/

        recyclerView = (RecyclerView) findViewById(R.id.user_options_recyclerView);
        doneBtn = (Button) findViewById(R.id.user_select_done);

        layoutManager = new LinearLayoutManager(this);

        adapter = new UserButtonsAdapter(userOptionList, this.getBaseContext(), true);

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(layoutManager);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
