package com.nilepoint.monitorevaluatemobile.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.settings.SettingsInfo;
import com.nilepoint.monitorevaluatemobile.settings.SettingsMixin;
import com.viralypatel.sharedpreferenceshelper.lib.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Arrays;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener{

    public static final String TAG = "SettingsFragment";

    //handles global preferences
    SharedPreferencesHelper preferencesHelper;

    SettingsMixin settings = new SettingsMixin();

    public SettingFragment() {
        // Required empty public constructor
    }

    //Method that grabs the preference modules for use. Uses
    @Override
    public void onCreatePreferences(Bundle bundle, String s){
        addPreferencesFromResource(R.xml.preferences_root_layout);

        //get info from host activity
        Bundle argsBundle = this.getArguments();
        int selectedSetting = argsBundle.getInt("settingSelected");

        //returns null until populated
        PreferenceScreen settingsScreen = this.getPreferenceScreen();
        Context context = settingsScreen.getContext();

        //Instnatiate helper to use default sharedPreferences (See library docs)
        preferencesHelper = new SharedPreferencesHelper(getActivity());
        loadSelectedSetting(selectedSetting,context,settingsScreen);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "A setting was changed!");

        Preference preference = findPreference(key);

        try {
            if (!(getSetting(key) instanceof Boolean)) {
                preference.setTitle(getSetting(key).toString());
            }

            String value = getSetting(key).toString();

            System.out.println("Setting " + key + " to " + value);


            if (SettingsInfo.ENVIRONMENT_SETTINGS_KEY.equals(key)){
                String environment = settings.get(key);

                if (!value.equals(environment)) {
                    settings.set(key, getSetting(key).toString());
/*
                    Toast toast = Toast.makeText(getPreferenceScreen().getContext(), R.string.env_restart, Toast.LENGTH_LONG);
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View layouttoast = inflater.inflate(R.layout.toast, (ViewGroup) getActivity().findViewById(R.id.toastcustom));
                    ((TextView) layouttoast.findViewById(R.id.texttoast)).setText(getActivity().getString( R.string.env_restart ));
                    toast.setView(layouttoast);
                            toast.show();*/
                    WLTrackApp.customToast(getActivity(), getActivity().getString( R.string.env_restart ));
                }
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*So that we don't repeat ourselves when adding new settings, handles nitty gritties*/
    private void setPref(String title, Preference pref, PreferenceScreen screen, Object key){
        pref.setKey(key.toString());
        pref.setTitle(getSetting(key).toString());
        pref.setSummary(title);
        screen.addPreference(pref);
    }

    //Just a filler method for now
    private Object getSetting(Object key){
        if (preferencesHelper.getAll().get(key) != null) {
            return preferencesHelper.getAll().get(key);
        }
        else {
            return "Choose Office";
        }

    }

    /*
    *display the setting to change based on the position in the string array (see strings.xml)
    * TODO break out the cases into variables in case we need to change the order
     */
    private void loadSelectedSetting(int pos, Context context, PreferenceScreen screen){
        switch(pos){
            case 0:
               //profile
                break;
            case 1:
                DropDownPreference languages = new DropDownPreference(context);
                setPref("Select a Language",languages,screen, SettingsInfo.LANGUAGE_SETTINGS_KEY);
                String[] languagesList = getResources().getStringArray(R.array.language_options);
                languages.setEntries(languagesList);
                languages.setEntryValues(languagesList);

                break;
            case 2:
                //regions
                break;
            case 3:
                //users at login
                break;
            case 4:
                String environmentName = Paper.book().read("environmentName");

                DropDownPreference environments = new DropDownPreference(context);

                setPref("Select an Environment ",environments,screen, SettingsInfo.ENVIRONMENT_SETTINGS_KEY);

                String[] environmentsList = new String []{"Development","Alpha","Beta", "Beta-NP", "PG-Dev"};

                environments.setEntries(environmentsList);

                environments.setEntryValues(environmentsList);

                environments.setValueIndex(Arrays.asList(environmentsList).indexOf(environmentName));

                break;
            default:
                //do nothing
                break;
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

}
