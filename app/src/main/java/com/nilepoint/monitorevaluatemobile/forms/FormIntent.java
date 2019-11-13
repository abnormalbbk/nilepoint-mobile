package com.nilepoint.monitorevaluatemobile.forms;

import android.content.Context;
import android.content.Intent;

import com.nilepoint.monitorevaluatemobile.participant.ParticipantHouseholdFormActivity;

/**
 * Created by ashaw on 3/1/17.
 */

public class FormIntent extends Intent {
    private String formName;

    private FormListener formListener;

    public FormIntent(Context context, String formName) {
        super(context, ParticipantHouseholdFormActivity.class);

        this.putExtra("formName",formName);
    }

    public FormListener getFormListener() {
        return formListener;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public void setFormListener(FormListener listener){
        this.formListener = listener;
    }
}
