package com.nilepoint.monitorevaluatemobile.new_user;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.nilepoint.monitorevaluatemobile.R;

import org.codepond.wizardroid.WizardStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by claudiatrafton on 3/23/17.
 * First step in new user set up
 */

public class LanguageStep extends WizardStep {

    //required empty constructor
    public LanguageStep(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.step_spinner_layout, container, false);


        //Set up header text
        TextView header = (TextView) v.findViewById(R.id.title_text_new_user_spinner);
        header.setText(R.string.language_wizard);

        //set up spinner content from realm
        Spinner spinner = (Spinner) v.findViewById(R.id.spinner_new_user);
        //spinner.setAdapter();
        // you need to have a list of data that you want the spinner to display
        List<String> spinnerArray =  new ArrayList<String>();

        spinnerArray.add("English");
        spinnerArray.add("Spanish");
        spinnerArray.add("Amharic");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,spinnerArray);

        spinner.setAdapter(adapter);

        spinner.setBackgroundResource(R.drawable.spinner_background);

        Button oauthButton = (Button) v.findViewById(R.id.oauth_button);

        oauthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), OAuthActivity.class);
                startActivity(intent);
            }
        });

        return v;

        //return super.onCreateView(inflater, container, savedInstanceState);
    }
}
