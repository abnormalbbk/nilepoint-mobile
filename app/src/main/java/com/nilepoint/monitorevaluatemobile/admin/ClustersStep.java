package com.nilepoint.monitorevaluatemobile.admin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.dkharrat.nexusdialog.FormController;
import com.nilepoint.monitorevaluatemobile.R;

import org.codepond.wizardroid.WizardStep;

/**
 * Created by ashaw on 5/21/17.
 */

public class ClustersStep extends WizardStep {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.form_activity, container, false);


        FormController formController = new FormController(this.getContext());



        return v;
    }
}
