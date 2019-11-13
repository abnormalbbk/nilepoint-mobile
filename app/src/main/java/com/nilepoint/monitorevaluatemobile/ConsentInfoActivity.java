package com.nilepoint.monitorevaluatemobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nilepoint.monitorevaluatemobile.forms.FormIntent;

public class ConsentInfoActivity extends AppCompatActivity {

    private Button signButton;
    private CheckBox consentObtainedCheckbox;
    private boolean consentObtained = false;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.form_consent_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        signButton = (Button) findViewById(R.id.consent_button);
        consentObtainedCheckbox = (CheckBox) findViewById(R.id.consent_obtained_checkbox);
        errorText = (TextView) findViewById(R.id.consent_error);
        consentObtainedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    consentObtained = true;
                    errorText.setVisibility(View.INVISIBLE);
                }
            }
        });

        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO start activity where a checkbox is for consent
                Intent intent = new FormIntent(getBaseContext(),"Household");
                if(consentObtained) {
                    startActivity(intent);
                }
                else {
                    errorText.setVisibility(View.VISIBLE);
                }
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
