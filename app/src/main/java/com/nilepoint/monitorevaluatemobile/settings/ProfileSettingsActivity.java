package com.nilepoint.monitorevaluatemobile.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.camera.SelectPhotoActivity;
import com.nilepoint.monitorevaluatemobile.user.UserSession;

import io.realm.Realm;

public class ProfileSettingsActivity extends AppCompatActivity {

    //needed to get the signed in user?
    ImageButton userPhoto;
    TextView addUserPhotoText;
    Context context = this;
    RecyclerView profileInfoRecycler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        getSupportActionBar().setTitle("User Profile");




        TextView username =  (TextView) findViewById(R.id.user_profile_settings_name);

        userPhoto = (ImageButton) findViewById(R.id.user_settings_add_photo);
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SelectPhotoActivity.class);

                intent.putExtra("user.id", UserSession.userId);

                startActivity(intent);
            }
        });

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            User user = realm.where(User.class).equalTo("id",UserSession.userId).findFirst();

            if (user != null){
                if (user.getPhoto() != null) {
                    userPhoto.setImageBitmap(user.getPhoto().getBitmap());
                }

                username.setText(user.getFirstName() + " " + user.getLastName());
            }
        } catch (Exception e){

        } finally {
            if (realm != null) {
                realm.close();
            }
        }



        /*if(userPhoto != null){
            GET AND REPLACE DEFAULT IMAGE, make not clickable
        }
        else {

        } */

    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView username =  (TextView) findViewById(R.id.user_profile_settings_name);

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            User user = realm.where(User.class).equalTo("id",UserSession.userId).findFirst();

            if (user != null){
                if (user.getPhoto() != null) {
                    userPhoto.setImageBitmap(user.getPhoto().getBitmap());
                }

                username.setText(user.getFirstName() + " " + user.getLastName());
            }
        } catch (Exception e){

        } finally {
            if (realm != null) {
                realm.close();
            }
        }

    }

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
