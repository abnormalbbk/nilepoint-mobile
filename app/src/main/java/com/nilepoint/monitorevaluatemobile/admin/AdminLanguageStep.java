package com.nilepoint.monitorevaluatemobile.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.init.ConnectedPeerActivity;
import com.nilepoint.monitorevaluatemobile.init.DataSyncActivity;
import com.nilepoint.monitorevaluatemobile.new_user.OAuthActivity;
import com.nilepoint.monitorevaluatemobile.settings.SettingsInfo;

import org.codepond.wizardroid.WizardStep;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Book;
import io.paperdb.Paper;

/**
 * Created by claudiatrafton on 3/23/17.
 * First step in new user set up
 */

public class AdminLanguageStep extends WizardStep {

    //required empty constructor
    public AdminLanguageStep(){

    }

    Button nextButtonFromParent;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.step_spinner_layout, container, false);


        //Set up header text
        TextView header = (TextView) v.findViewById(R.id.title_text_new_user_spinner);
        header.setText( R.string.language_wizard);

        //set up spinner content from realm
        Spinner spinner = (Spinner) v.findViewById(R.id.spinner_new_user);
        //spinner.setAdapter();
        // you need to have a list of data that you want the spinner to display
        List<String> spinnerArray =  new ArrayList<String>();

        spinnerArray.add("English");
        //spinnerArray.add("Spanish");
        //spinnerArray.add("Amharic");

        Spinner spinnerEnv = (Spinner) v.findViewById(R.id.spinner_env);
        //spinner.setAdapter();
        // you need to have a list of data that you want the spinner to display
        final List<String> spinnerEnvArray =  new ArrayList<String>();

        spinnerEnvArray.add("Select an Environment...");
        spinnerEnvArray.add("Development");
        spinnerEnvArray.add("Alpha");
        spinnerEnvArray.add("Beta");
        spinnerEnvArray.add("Beta-NP");
        spinnerEnvArray.add("PG-Dev");

        /*Button oauthButton = (Button) v.findViewById(R.id.oauth_button);

        oauthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), OAuthActivity.class);
                startActivity(intent);
            }
        });*/

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item,spinnerArray);

        ArrayAdapter<String> adapter2 = new HideFirstItemAdapter(getContext(),android.R.layout.simple_spinner_item,spinnerEnvArray);

        spinner.setAdapter(adapter);

        spinner.setBackgroundResource(R.drawable.spinner_background);

        spinnerEnv.setAdapter(adapter2);

        spinnerEnv.setBackgroundResource(R.drawable.spinner_background);

        final Button oauthButton = (Button) v.findViewById(R.id.oauth_button);

        oauthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), OAuthActivity.class);
                startActivity(intent);
            }
        });

        final Button peerButton = (Button) v.findViewById(R.id.peer_init_btn);

        if (Paper.book().contains("environmentName")){
            spinnerEnv.setVisibility(View.GONE);
            peerButton.setVisibility(View.VISIBLE);
            oauthButton.setVisibility(View.VISIBLE);
        }

        peerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), DataSyncActivity.class);

                intent.putExtra("isInit",true);

                startActivity(intent);
            }
        });


        // hack because we can't get the button

        spinnerEnv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Book book = Paper.book();

                if (position > 0) {
                    book.write(SettingsInfo.ENVIRONMENT_SETTINGS_KEY, spinnerEnvArray.get(position));

                    WLTrackApp app = (WLTrackApp) getActivity().getApplicationContext();

                    // trigger the connection to the MQs and API

                    app.setEnvironment();
                    app.afterEnvironmentSelection();

                    peerButton.setVisibility(View.VISIBLE);
                    oauthButton.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return v;

        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    private class HideFirstItemAdapter extends ArrayAdapter<String> {

        public HideFirstItemAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            View v = null;

            // If this is the initial dummy entry, make it hidden
            if (position == 0) {
                TextView tv = new TextView(getContext());
                tv.setHeight(0);
                tv.setVisibility(View.GONE);
                v = tv;
            }
            else {
                // Pass convertView as null to prevent reuse of special case views
                v = super.getDropDownView(position, null, parent);
            }

            // Hide scroll bar because it appears sometimes unnecessarily, this does not prevent scrolling
            parent.setVerticalScrollBarEnabled(false);
            return v;
        }

    }
}
