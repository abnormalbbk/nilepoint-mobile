package com.nilepoint.monitorevaluatemobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.persistence.SettingHostActivity;
import com.nilepoint.monitorevaluatemobile.settings.ProfileSettingsActivity;
import com.nilepoint.monitorevaluatemobile.settings.RegionsSettingsActivity;
import com.nilepoint.monitorevaluatemobile.settings.SettingsInfo;
import com.nilepoint.monitorevaluatemobile.settings.UsersShownAtLoginSettingsActivity;

import java.util.List;

/**
 * Created by claudiatrafton on 3/6/17.
 */

public class SettingsListAdapter extends RecyclerView.Adapter<SettingsListAdapter.ViewHolder> {


    // inner class to hold a reference to each item of RecyclerView
    //Context is needed for intents to accept or decline a challenge
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        protected String settingItem;
        protected TextView settingCardText;
        protected Context context;

        public ViewHolder(View settingListLayoutView, Context c) {
            super(settingListLayoutView);
            settingListLayoutView.setOnClickListener(this);
            settingCardText = (TextView) settingListLayoutView.findViewById(R.id.setting_card_text);
            context = c;

        }
        @Override
        public void onClick(View v){
            int pos = getLayoutPosition();
            openSetting(pos,context);
            Log.d("SettingsActivity", pos + "InternalSetting clicked!");
        }

        /*
            *Open up the fragment in the view container and pass it the position of the item in the settings string array
        */
        public void openSetting (int pos, Context context){
            /*Fragment settingFragment = new SettingFragment();
            Bundle args = new Bundle();
            args.putInt("settingSelected",pos);
            settingFragment.setArguments(args);
            FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.pref_container, settingFragment);
            fragmentTransaction.commit(); */
            if(pos == 0) {
                Intent intent = new Intent(context, ProfileSettingsActivity.class);
                context.startActivity(intent);
            }
            else if(pos == 2){
                Intent intent = new Intent(context, RegionsSettingsActivity.class);
                context.startActivity(intent);

            }

            else if((pos == 3)){
                Intent intent = new Intent(context, UsersShownAtLoginSettingsActivity.class);
                context.startActivity(intent);
            }

            else {
                Intent intent = new Intent(context, SettingHostActivity.class);
                intent.putExtra("setting_position", pos);
                context.startActivity(intent);
            }
        }

    } //end of inner ViewHolder inner class

    //Beginning of code for SettingListAdapter

    protected List<String> settingsKeys;
    protected Context mContext;
    protected String SETTING_KEY_POS = "position";

    //constructor pulls in an array of settings
    public SettingsListAdapter(List<String> settingsList, Context context){
        settingsKeys = settingsList;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.setting_title_card, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, mContext);
        return viewHolder;
    }

    /*
     *Method pulls in data and binds it to the card
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //on bind, get the information from db

        holder.settingCardText.setText(settingsKeys.get(position));
        Log.d("SettingsActivity", "Text: " + settingsKeys.get(position) );
        Log.d("SettingsActivity",settingsKeys.get(position));
    }




    //method needed
    @Override
    public int getItemCount(){
        return settingsKeys.size();
    }

}

