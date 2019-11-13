package com.nilepoint.monitorevaluatemobile.activity_tracking;

import com.nilepoint.model.Form;
import com.nilepoint.model.FormElement;
import com.nilepoint.model.FormElementType;
import com.nilepoint.model.FormSection;

/**
 * Created by ashaw on 6/11/17.
 */

public class ActivityTrackingFormFactory {

    private static final String FORM_SECTION_TRAINING = "Training";
    private static final String FORM_SECTION_ACTIVITY_PROFILE = "Activity Profile";
    private static final String FORM_SECTION_TRAINING_PROFILE = "Specify Training Profile";

    public static Form getActivityTrackingForm(){
        Form form = new Form("Add Training");

        form.setVersion(4L);

        FormSection profileSection = new FormSection(FORM_SECTION_TRAINING_PROFILE);
        FormSection activityProfile = new FormSection(FORM_SECTION_ACTIVITY_PROFILE);

        activityProfile.setName("");

        form.addSection(activityProfile).addSection(profileSection);

        return form;
    }
}
