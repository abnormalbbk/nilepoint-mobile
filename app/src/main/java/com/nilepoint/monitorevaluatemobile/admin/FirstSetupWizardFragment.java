package com.nilepoint.monitorevaluatemobile.admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.nilepoint.monitorevaluatemobile.LoginActivity;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.new_user.AddPhotoStep;
import com.nilepoint.monitorevaluatemobile.new_user.CompleteStep;
import com.nilepoint.monitorevaluatemobile.new_user.PersonalizeStep;
import com.nilepoint.monitorevaluatemobile.user.UserSession;

import org.codepond.wizardroid.WizardFlow;
import org.codepond.wizardroid.WizardFragment;
import org.codepond.wizardroid.WizardStep;

import io.paperdb.Paper;
import io.realm.Realm;

/**
 * Created by claudiatrafton on 3/22/17.
 */

public class FirstSetupWizardFragment extends WizardFragment implements View.OnClickListener {

    public static String TAG = "FirstSetupWizardF";

    private Button nextButton;

    //empty constructor
    public FirstSetupWizardFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View wizardLayout = inflater.inflate(R.layout.fragment_wizard_first_setup, container, false);
        nextButton = (Button) wizardLayout.findViewById(R.id.wizard_next_button);
        nextButton.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) wizardLayout.findViewById(R.id.toolbar_wizard);
        TextView toolbarTitle = (TextView) wizardLayout.findViewById(R.id.toolbar_wizard_title);

        toolbarTitle.setText("Administrative Setup");

        Boolean isInit = Paper.book().read("data.init");

        if (!isInit) {
            nextButton.setVisibility(View.GONE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
        }

        wizardLayout.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK){
                    wizard.goBack();
                }
                return false;
            }
        });

        return wizardLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Boolean isInit = Paper.book().read("data.init");

        if (!isInit) {
            nextButton.setVisibility(View.GONE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    //Creates the different steps, easy to add new steps, just use the same method
    @Override
    public WizardFlow onSetup() {
        WizardFlow flow = new WizardFlow.Builder()
                .addStep(AdminLanguageStep.class)
                //.addStep(PersonalizeStep.class)
                .addStep(AddPhotoStep.class)
                //.addStep(CountriesStep.class)
                //.addStep(ClustersStep.class)
                //.addStep(CommunitiesStep.class)
                //.addStep(DownloadStep.class)
                .addStep(CompleteStep.class)
                .create();

        return flow;
    }

    //what happens when buttons are clicked, goes to the next step...is this redundant..?
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.wizard_next_button && !wizard.isLastStep()){

            if (wizard.getCurrentStep() instanceof PersonalizeStep){
                final PersonalizeStep step = (PersonalizeStep) wizard.getCurrentStep();

                Realm realm = null;

                try {
                    realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            // set this user as the active one for this session
                            UserSession.userId = realm.copyToRealm(step.getUser()).getId();

                            Log.d(TAG,"Active user is " + UserSession.userId);
                        }
                    });
                } finally {
                    if (realm != null){
                        realm.close();
                    }
                }
            }

            wizard.goNext();

        }
        else if (v.getId() == R.id.wizard_next_button && wizard.isLastStep()){
            //complete wizard
            Intent intent = new Intent(getContext(), LoginActivity.class);
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
