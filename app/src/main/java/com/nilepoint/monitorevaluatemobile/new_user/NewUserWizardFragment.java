package com.nilepoint.monitorevaluatemobile.new_user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.HomeActivity;
import com.nilepoint.monitorevaluatemobile.R;

import org.codepond.wizardroid.WizardFlow;
import org.codepond.wizardroid.WizardFragment;

/**
 * Created by claudiatrafton on 3/22/17.
 */

public class NewUserWizardFragment extends WizardFragment implements View.OnClickListener {

    // the user that is being setup.
    User user;

    //empty constructor
    public NewUserWizardFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View wizardLayout = inflater.inflate(R.layout.fragment_wizard_new_user, container, false);
         final Button nextButton = (Button) wizardLayout.findViewById(R.id.wizard_next_button);
        nextButton.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) wizardLayout.findViewById(R.id.toolbar_wizard);
        TextView toolbarTitle = (TextView) wizardLayout.findViewById(R.id.toolbar_wizard_title);
        toolbarTitle.setText("Set Up");


        wizardLayout.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK){
                    wizard.goBack();
                }
                return false;
            }
        });

        //correct behavior when back softkey pressed

        return wizardLayout;
    }

    //Creates the different steps, easy to add new steps, just use the same method
    @Override
    public WizardFlow onSetup() {
        return new WizardFlow.Builder()
                .addStep(LanguageStep.class)
                .addStep(PersonalizeStep.class)
                .addStep(AddPhotoStep.class)
                .addStep(ClusterStep.class)
                .addStep(CountryStep.class)
                .addStep(CompleteStep.class)
                /*TODO add subsequent steps*/
                .create();
    }

    //what happens when buttons are clicked, goes to the next step...is this redundant..?
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.wizard_next_button && !wizard.isLastStep()){
            wizard.goNext();
        }
        else if (v.getId() == R.id.wizard_next_button && wizard.isLastStep()){
            //complete wizard
            Intent intent = new Intent(getContext(), HomeActivity.class);
            startActivity(intent);
        }
        else {
           // wizard.goBack();
            updateWizardControls(v);
        }
        /*TODO update for the last step of the wizard to save all of the data in Realm*/
        updateWizardControls(v);
    }

    /*Set the Realm persistance 1st time user variable to be false and save information*/
    @Override
    public void onWizardComplete() {
        super.onWizardComplete();

        //TODO save all of the users data
    }


    /**
     * Updates the UI according to current step position
     */
    private void updateWizardControls(View v) {
        Button b = (Button) v.findViewById(v.getId());
        if(wizard.isLastStep()){
            b.setText("Get Started");
        }
        else {
            b.setText("Next");
        }
    }
}
