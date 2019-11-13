package com.nilepoint.monitorevaluatemobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.nilepoint.monitorevaluatemobile.forms.FormIntent;

public class ObtainConsentActivity extends AppCompatActivity {

    private static final String TAG = "ObtainConsentActivity";

    private Button confirmConsentButton;
    private CheckBox consentCheck;
    private boolean consented;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obtain_consent);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.home_activity_add_household);
        getSupportActionBar().setSubtitle(R.string.form_consent_secondary_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        consentCheck = (CheckBox) findViewById(R.id.obtain_consent_checkbox);
        consentCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    consented = true;
                }
                else {
                    consented = false;
                }
                Log.d(TAG, "consented: " + consented);
            }
        });

        //enable when this is complete
        confirmConsentButton = (Button) findViewById(R.id.confirm_consent_button);

        confirmConsentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create a new participant form if consented
                if(consented){
                    Intent intent = new FormIntent(getBaseContext(), "Participant");
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getBaseContext(), "You must get consent to continue", Toast.LENGTH_SHORT);
                }
                //TODO start activity where a checkbox is for consent

            }
        });

    }


    //Navigate home from up arrow in the action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
