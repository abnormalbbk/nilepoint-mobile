package com.nilepoint.monitorevaluatemobile.new_user;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.camera.SelectPhotoActivity;
import com.nilepoint.monitorevaluatemobile.user.UserSession;

import org.codepond.wizardroid.WizardStep;

import io.realm.Realm;

/**
 * Created by claudiatrafton on 4/1/17.
 */

public class AddPhotoStep extends WizardStep {

    View v;

    public AddPhotoStep(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.step_add_photo_layout, container, false);

        //Set up header text
        TextView header = (TextView) v.findViewById(R.id.title_text_new_user);

        header.setText(R.string.photo_wizard);

        ///set up button
        ImageButton cameraButton = (ImageButton) v.findViewById(R.id.new_user_add_photo_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SelectPhotoActivity.class);

                intent.putExtra("user.id", UserSession.userId);

                startActivityForResult(intent, 1);
            }
        });


        return v;
        
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageButton cameraButton = (ImageButton) v.findViewById(R.id.new_user_add_photo_button);

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            if (UserSession.userId != null) {
                User user = realm.where(User.class).equalTo("id", UserSession.userId).findFirst();

                if (user != null && user.getPhoto() != null) {
                    cameraButton.setImageBitmap(user.getPhoto().getBitmap());
                }
            }

        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }
}
