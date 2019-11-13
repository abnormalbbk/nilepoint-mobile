package com.nilepoint.monitorevaluatemobile.new_user;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nilepoint.monitorevaluatemobile.R;

import org.codepond.wizardroid.WizardStep;

/**
 * Created by claudiatrafton on 4/3/17.
 */

public class CompleteStep extends WizardStep {

    public CompleteStep(){

    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.step_complete_layout, container, false);

        TextView header = (TextView) v.findViewById(R.id.title_text_new_user_spinner);

        return v;
    }


}
