package com.nilepoint.monitorevaluatemobile.participant;

import com.github.dkharrat.nexusdialog.validations.InputValidator;
import com.nilepoint.formbuilder.FormService;
import com.nilepoint.formbuilder.RegExInputValidator;
import com.nilepoint.model.ElementValidator;
import com.nilepoint.model.Form;
import com.nilepoint.model.FormElement;
import com.nilepoint.model.FormElementType;
import com.nilepoint.model.FormSection;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ashaw on 11/19/17.
 */

public class ParticipantFormFactory {
    FormService formService;

    public static Long DEFAULT_FORM_VERSION = 27L;

    public ParticipantFormFactory(FormService formService) {
        this.formService = formService;
    }


    public void createParticipantForm(){
        Form form = new Form("Participant");
        form.setVersion(DEFAULT_FORM_VERSION);

        //FormSection sectionPrimaryInfo = new FormSection("Primary Information");

        //The section that will go on the first page. It specifies who the participant is

        FormSection step1 = new FormSection("");
        step1.setLabel("");
        form.addSection(step1);

        List<ElementValidator> validators = new ArrayList<>();

        validators.add(new ElementValidator(".*"));

        //step1.addElement(new FormElement(FormElementType.CHECK, "isHeadOfHousehold", "Head of Household?"));
        //step1.addElement(new FormElement(FormElementType.CHOICE, "isHeadOfHousehold", "Head of Household?").addChoices("Yes","No"));

        step1.addElement(new FormElement(FormElementType.TEXT, "givenName", "Given Name")
                .setValidators(validators));
        step1.addElement(new FormElement(FormElementType.TEXT, "fatherName", "Family Name (optional)"));
        step1.addElement(new FormElement(FormElementType.TEXT, "preferredName","Preferred Name (optional)"));

        /*FormElement headRelation = new FormElement(FormElementType.CHOICE, "houseHeadRealation", "Relation to head of household");
        headRelation.setChoices(Arrays.asList(new String[] {"Husband","Wife", "Son", "Daughter", "Other Adult", "Other Child"})); //just a sample for now
        step1.addElement(headRelation);*/

        step1.addElement(new FormElement(FormElementType.CHECK, FormKeyIDs.ESTIMATED_AGE, "Is Age Estimated?"));
        step1.addElement(new FormElement(FormElementType.TEXT, "estimated_months", "Months").setHidden(true, FormKeyIDs.ESTIMATED_AGE));
        step1.addElement(new FormElement(FormElementType.TEXT, "estimated_years", "Years").setHidden(true,FormKeyIDs.ESTIMATED_AGE));
        step1.addElement(new FormElement(FormElementType.DATE, "birthday", "Birthday").setValidators(validators).setHidden(false,FormKeyIDs.ESTIMATED_AGE));


        FormElement genderChoice = new FormElement(FormElementType.CHOICE, "gender", "Gender");
        genderChoice.setChoices(Arrays.asList(new String[] {"Male","Female"}));
        step1.addElement(genderChoice.setValidators(validators));


        step1.addElement(new FormElement(FormElementType.TEXT, "phoneNumber", "Phone Number (optional)"));
        //step1.addElement(new FormElement(FormElementType.TEXT, "psnpNumber", "PSNP Number (optional)"));


        step1.addElement(new FormElement(FormElementType.CHECK, "isDeceased", "Is deceased?"));

        //Location information

        /**
         * Send it through the formService to be stored.
         */
        formService.messageReceived(form);
    }

    /***
     * Create a new household form - needed before making participants
     */
    public void createHouseholdForm(){
        Form form = new Form("Household");
        form.setVersion(DEFAULT_FORM_VERSION);

        List<ElementValidator> validators = new ArrayList<>();

        validators.add(new ElementValidator(".*"));

        //Basic household information
        FormSection step1 = new FormSection("");
        step1.setLabel("");
        form.addSection(step1);

        step1.addElement(new FormElement(FormElementType.TEXT, "givenName", "Given Name").setValidators(validators));
        step1.addElement(new FormElement(FormElementType.TEXT, "fatherName", "Family Name (optional)"));
        step1.addElement(new FormElement(FormElementType.TEXT, "preferredName","Preferred Name (optional)"));

        //birthday info TODO add approximate age slider
        step1.addElement(new FormElement(FormElementType.CHECK, FormKeyIDs.ESTIMATED_AGE, "Is Birthdate Estimated?"));

        step1.addElement(new FormElement(FormElementType.TEXT, "estimated_months", "Months").setHidden(true, FormKeyIDs.ESTIMATED_AGE));
        step1.addElement(new FormElement(FormElementType.TEXT, "estimated_years", "Years").setHidden(true,FormKeyIDs.ESTIMATED_AGE));

        step1.addElement(new FormElement(FormElementType.DATE, "birthday", "Birthday").setHidden(false,FormKeyIDs.ESTIMATED_AGE));

        FormElement genderChoice = new FormElement(FormElementType.CHOICE, "gender", "Gender")
                .addChoices("Male","Female").setValidators(validators);

        step1.addElement(genderChoice);

        step1.addElement(new FormElement(FormElementType.TEXT, "phoneNumber", "Phone Number (optional)"));
        step1.addElement(new FormElement(FormElementType.TEXT, "psnpNumber", "PSNP Number (optional)"));

        step1.addElement(new FormElement(FormElementType.CHECK, "isDeceased", "Is deceased?"));

        FormSection step2 = new FormSection("Location");
        step2.setLabel("Specify Household Location");
        form.addSection(step2);

        formService.messageReceived(form);

    }

}
