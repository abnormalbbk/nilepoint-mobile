package com.nilepoint.monitorevaluatemobile.new_user;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.R;

import org.codepond.wizardroid.WizardStep;

/**
 * Created by claudiatrafton on 4/1/17.
 */

public class PersonalizeStep extends WizardStep{

    public static String TAG = "PersonalizeStep";

    View view;

    public PersonalizeStep(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.step_personalize_layout, container, false);

        //header
        TextView title = (TextView) view.findViewById(R.id.title_text_new_user_personalize);

        title.setText(R.string.personal_info_wizard);

        //set up input fields
        EditText firstName = (EditText) view.findViewById(R.id.new_user_first_name);
        EditText lastName = (EditText) view.findViewById(R.id.new_user_last_name);
        EditText emailAdd = (EditText) view.findViewById(R.id.new_user_email_address);

        return view;

        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(TAG,"Destroy Personalize Step View");
    }

    public User getUser(){

        EditText firstName = (EditText) view.findViewById(R.id.new_user_first_name);
        EditText lastName = (EditText) view.findViewById(R.id.new_user_last_name);
        EditText emailAdd = (EditText) view.findViewById(R.id.new_user_email_address);

        User user = new User();

        user.setFirstName(firstName.getText().toString());
        user.setLastName(lastName.getText().toString());
        user.setEmail(emailAdd.getText().toString());

        return user;
    }
}
