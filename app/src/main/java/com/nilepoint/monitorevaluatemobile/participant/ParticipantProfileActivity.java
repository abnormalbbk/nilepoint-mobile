package com.nilepoint.monitorevaluatemobile.participant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nilepoint.model.Household;
import com.nilepoint.model.HouseholdRelationship;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.camera.SelectPhotoActivity;
import com.nilepoint.monitorevaluatemobile.forms.FormIntent;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;
import com.nilepoint.persistence.Datastore;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Activity that shows the participant details and allows for some minor edits
 * @author ctrafton
 */
public class ParticipantProfileActivity extends AppCompatActivity {

    public static final String TAG = "ProfileActivity";
    private static final String SPECIFY = "specify_activity";

    private TextView participantName;
    private TextView participantId;
    private TextView addPhotoText;
    private ImageView participantProfilePictureAdd;
    private LinearLayout participantProfilePhotoContainer;
    private ImageView participantProfilePhoto;
    private FloatingActionButton editButton;
    private FragmentPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private Context context = this;
    private Realm realm;
    private ArrayList<TrackedActivity> participantTrackedActivities = new ArrayList<>();

    //contains the information about the participant, formatted for adapter
    private ArrayList<Map.Entry<String, String>> participantInfo = new ArrayList<>();

    //members f the current household
    private ArrayList<StoredParticipant> hhMembers = new ArrayList<>();

    private Household hh;

    private StoredParticipant hhHead;


    private Datastore ds = Datastore.init(this);
    private StoredParticipant participant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_participant_profile);

        //Set up toolbar, replaces old ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_participant_profile);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.participant_profile_viewPager);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.participant_profile_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);


        //get the participant that was clicked
        final Intent intent = getIntent();
        final String id = intent.getStringExtra("spId");
        Log.d(TAG, "extra id is : " + id);

        //get participant information
        participant = getParticipantById(id);

        Log.d(TAG,"Profile for " + participant.getFirstName() + " " + participant.getId());

        getActivitiesForParticipant();

        setParticipantInfo();

        getHouseholdAndHeadForParticipant();

        hhMembers.addAll(getMembers());

        if (hh != null) {
            hhHead = getParticipantById(hh.getHeadOfHousehold());
        }

        //hhMembers.add(new StoredParticipant());

        Log.d(TAG, participant.toString());

        Map<String, String> pMap = participant.toMessage().getMap();

        Log.d(TAG,"Profile for " + id);



        //Set up content in views
        participantName = (TextView) findViewById(R.id.participant_profile_name);
        participantProfilePhotoContainer = (LinearLayout) findViewById(R.id.participant_profile_header);
        participantProfilePhoto = (ImageView) findViewById(R.id.participant_profile_picture);
        addPhotoText = (TextView) findViewById(R.id.participant_profile_add_photo_text);
        participantProfilePictureAdd = (ImageView) findViewById(R.id.participant_profile_picture_add);
        editButton = (FloatingActionButton) findViewById(R.id.participant_profile_edit_button);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isHoH = realm.where(Household.class).equalTo("headOfHousehold", id).findFirst() != null;

                Intent intent = new FormIntent(ParticipantProfileActivity.this, isHoH ? "Household" : "Participant");

                intent.putExtra("participant.id", id);

                if (isHoH) {
                    intent.putExtra("headOfHouseholdId", id);
                }

                startActivity(intent);
            }
        });

        participantProfilePictureAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SelectPhotoActivity.class);
                intent.putExtra("participant.id", participant.getId());
                startActivity(intent);

            }
        });


    }

    private StoredParticipant getParticipantById(String id){
        realm = Realm.getDefaultInstance();
        return realm.where(StoredParticipant.class).equalTo("id", id).findFirst();
    }


    public ArrayList<TrackedActivity> getTrackedActivties(){
        return participantTrackedActivities;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //get the participant that was clicked
        final Intent intent = getIntent();
        final String id = intent.getStringExtra("spId");

        //get participant information
        participant = ds.findParticipantById(id);

        Map<String, String> pMap = participant.toMessage().getMap();

        String givenName = pMap.get(FormKeyIDs.GIVEN_NAME_ID);
        String fatherName = pMap.get(FormKeyIDs.FATHER_NAME_ID);


        StringBuilder name = new StringBuilder();

        name.append(givenName);

        if (fatherName != null){
            name.append(" ").append(fatherName);
        }

        participantName.setText(name.toString());

        participantId = (TextView) findViewById(R.id.participant_profile_ID);

        Map<String,String> participantMap = participant.toMessage().getMap();

        participantId.setText(participantMap.get(FormKeyIDs.PARTICIPANT_CODE) == null
                ? "Participant Code Pending" : participantMap.get(FormKeyIDs.PARTICIPANT_CODE));

        setParticipantInfo();

        viewPager.setCurrentItem(0);

        if (participant.getPhoto() != null){
            participantProfilePictureAdd.setVisibility(View.GONE);
            addPhotoText.setVisibility(View.GONE);
            participantProfilePhoto.setImageBitmap(participant.getPhoto().getBitmap());
            participantProfilePhoto.setVisibility(View.VISIBLE);
        }

        if (realm == null){
            realm = Realm.getDefaultInstance();
        }

    }

    public void setParticipantInfo(){ //TODO make string resources for translations later
        Map<String,String> participantMap = participant.toMessage().getMap();

        participantInfo.clear();

        String deceasedString = (participant.toMessage().get("isDeceased", false) == false) ? "No" : "Yes";

        participantInfo.add(new AbstractMap.SimpleEntry<String, String>("Phone", participant.getPhoneNumber()));
        participantInfo.add(new AbstractMap.SimpleEntry<String, String>("Birthday", participantMap.get(FormKeyIDs.BIRTHDAY_ID)));
        participantInfo.add(new AbstractMap.SimpleEntry<String, String>("Gender", participantMap.get(FormKeyIDs.GENDER_ID)));
        if (participantMap.get(FormKeyIDs.PSNP_NUMBER) != null){
            participantInfo.add(new AbstractMap.SimpleEntry<String, String>("PSNP Number", participantMap.get(FormKeyIDs.PSNP_NUMBER)));
        }
        participantInfo.add(new AbstractMap.SimpleEntry<String, String>("Is Deceased?", deceasedString));
    }

    public ArrayList<Map.Entry<String, String>> getParticipantInfo(){
        return participantInfo;
    }


    /**
     * get the activities for this participant
     */
    public void getActivitiesForParticipant(){
        realm.executeTransaction(new Realm.Transaction(){
            @Override
            public void execute(Realm realm){
                RealmResults<TrackedActivity> allTrackedActivities = realm.where(TrackedActivity.class)
                        .findAll();
                for(TrackedActivity ta : allTrackedActivities){
                    if(ta.getParticipantList().contains(participant)){
                        participantTrackedActivities.add(ta);
                        Log.d(TAG, "Activity found: " + ta.getId());
                    }
                }

            }
        });
    }

    /**
     * I know this is literally the worst thing ever but it seemed to be the only working solution
     */
    public void getHouseholdAndHeadForParticipant(){
        if(!isHeadOfHousehold()) {

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmQuery<HouseholdRelationship> queryHhr = realm.where(HouseholdRelationship.class)
                            .equalTo("participant", participant.getId());

                    if (queryHhr.findFirst() == null) {
                        Log.d(TAG, "Couldn't find hh for participant :(");
                        return;

                    }

                    String hhId = queryHhr.findFirst().getHousehold();
                    if (hhId != null) {
                        hh = realm.where(Household.class).equalTo("id", hhId).findFirst();

                        Log.d(TAG, "Found hh! It is: " + hh.getId());

                        hhHead = realm.where(StoredParticipant.class)
                                .equalTo("id", hh.getHeadOfHousehold())
                                .findFirst();
                    }
                }
            });
        }
        else
            hhHead = participant;

        if (hhHead != null) {
            hh = ds.findHouseholdForHeadOfHousehold(hhHead);
        }
    }

    /**
     * get all members currently entered into the household - including the head
     */
    public ArrayList<StoredParticipant> getMembers(){
        ArrayList<StoredParticipant> membersTemp = new ArrayList<>();
        if (hh == null){
            return new ArrayList<>();
        }

        for(HouseholdRelationship hhRel: hh.getMembers()) {
            StoredParticipant p = realm.where(StoredParticipant.class)
                    .equalTo("id", hhRel.getParticipantId())
                    .findFirst();

            if (p != null) {
                membersTemp.add(p);
            } else {
                Log.d(TAG,"Could not find member with Id: " + hhRel.getParticipantId());
            }
        }

        if(!participant.equals(hhHead)){
            membersTemp.add(hhHead);
        }

        membersTemp.remove(participant); //I feel like there has to be a way to make realm qery for this, but it kept crashing

        Log.d(TAG, "Members in household: " + membersTemp.size());

        return membersTemp;
    }

    public boolean isHeadOfHousehold(){
        return realm
                .where(Household.class)
                .equalTo("headOfHousehold", participant.getId())
                .findFirst() != null;
    }


    public StoredParticipant getParticipant(){
        return participant;
    }

    public Household getHousehold(){
        return hh;
    }

    public ArrayList<StoredParticipant> getHhMembers() {return hhMembers;}

    public StoredParticipant getHhHead(){return hhHead;}

    /* Household member information methods*/
    public String getResidence(){
        return null;
    }

    @Override
    public void onBackPressed() {
        //go back home when done
        finish();
    }

    /**
     * Handles tabs and fragments
     */
    public static class ViewPagerAdapter extends FragmentPagerAdapter {

        private static String tabTitles[] = new String[] { "Profile", "Household Info", "Activities"}; //TODO change to resources
        private static final int NUM_PAGES = tabTitles.length;

        public ViewPagerAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }

        @Override
        public int getCount(){
            return NUM_PAGES;
        }

        @Override
        public Fragment getItem(int position){
            switch(position){
                case 0:
                    Log.d(TAG, "first one clicked!");
                    return ParticipantProfileInfoFragment.newInstance();
                case 1:
                    return ParticipantProfileHouseholdFragment.newInstance();
                case 2:
                    return ParticipantProfileActivityFragment.newInstance();
                default:
                    return ParticipantProfileInfoFragment.newInstance();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }


    } //eoc


    @Override
    protected void onDestroy() {
        super.onDestroy();

        realm.close();
    }

}
