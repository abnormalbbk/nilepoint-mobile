package com.nilepoint.monitorevaluatemobile.new_user;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.nilepoint.monitorevaluatemobile.R;

import org.codepond.wizardroid.WizardStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by claudiatrafton on 4/4/17.
 * NOTE: TODO Refactor these with the spinner to make them extend a dropDownStep class
 */

public class CountryStep extends WizardStep {

    public CountryStep(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.step_spinner_layout, container, false);


        //Set up header text
        TextView header = (TextView) v.findViewById(R.id.title_text_new_user_spinner);
        header.setVisibility(View.GONE);

        //set up spinner content from realm
        Spinner spinner = (Spinner) v.findViewById(R.id.spinner_new_user);
        //spinner.setAdapter();
        // you need to have a list of data that you want the spinner to display
        List<String> spinnerArray =  new ArrayList<String>();
        spinnerArray.add("Country 1");
        spinnerArray.add("Country 2");
        spinnerArray.add("Country 2");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,spinnerArray);
        spinner.setAdapter(adapter);
        spinner.setBackgroundResource(R.drawable.spinner_background);
        //TODO set options

        return v;

        //return super.onCreateView(inflater, container, savedInstanceState);
    }
}
