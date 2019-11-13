package com.nilepoint.monitorevaluatemobile.activity_tracking;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.dkharrat.nexusdialog.FormController;
import com.github.dkharrat.nexusdialog.FormFragment;
import com.nilepoint.model.Group;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.HomeActivity;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WorkflowDialog;
import com.nilepoint.monitorevaluatemobile.participant.SelectParticipantFragment;
import com.nilepoint.persistence.Datastore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class ActivityTrackingHostActivity extends AppCompatActivity implements
        SpecifyTrackingActivityFragment.OnFragmentInteractionListener,
        SpecifyTrainingFragment.OnFragmentInteractionListener{

    private static String TAG = "ActivityHostActivity";

    private static final String SPECIFY = "specify_activity";

    private static final int DONE_FRAGMENT_INDEX = 2;
    private static final int CONFIRM_FRAGMENT_INDEX = 1;
    private static final int SELECT_PARTICIPANT_OR_GROUP_INDEX = 0;
    private static final int SELECT_PARTICIPANT_FROM_GROUP_INDEX = 9; //arbitrary since its optional

    private Button nextButton;
    private int stepIndex; //what step are we on
    private final Context context = this;
    private Intent intent;
    protected Datastore ds = Datastore.init(getBaseContext());
    private ArrayList<StoredParticipant> attendees = new ArrayList<>();
    private ArrayList<StoredParticipant> participants = new ArrayList<>();
    private ArrayList<Group> groups = new ArrayList<>();
    private LinkedHashMap<String, String> formElements;
    private TrackedActivity trackedActivity = new TrackedActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_host);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        intent = getIntent();
        HashMap<String, String> formMap = (HashMap<String, String>) intent.getSerializableExtra("activity.elements"); //get form elements from Activity

        //TODO comment back in after testing
       formElements = new LinkedHashMap<>(formMap);

        //TODO comment back in after testing
        Log.d("HostActivity", formElements.toString());

        //TODO comment back in after testing
        trackedActivity = MapToTrackedActivityFactory.mapFieldsToTrackedActivity(formElements);  //set form fields to tracked info data

        participants.addAll(ds.findParticipants("", "lastName", 100));

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            groups.addAll(realm.where(Group.class).findAll());
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        stepIndex = 0; //on initialization, we are on step 0

        goToStep(stepIndex);

        nextButton = (Button) findViewById(R.id.activity_tracking_next_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stepIndex < CONFIRM_FRAGMENT_INDEX){
                    stepIndex++;
                    goToStep(stepIndex);
                }
                else {
                    trackedActivity.setParticipantList(getRealmList(attendees));
                    //Log.d("ActivityHost", trackedActivity.getParticipantList().get(0).toString());
                    saveActivityToRealm(); //save on done click
                    Intent intent = new Intent(context, HomeActivity.class);
                    context.startActivity(intent);
                }

            }
        });
    }


    /**
     * Check if nexus form fragments are valid
     * @param fragment
     */
    private void setSubmitValidation(final FormFragment fragment){
        nextButton.setOnClickListener(new View.OnClickListener() {
            FormController controller = fragment.getFormController();
            @Override
            public void onClick(View v) {
                if(!controller.isValidInput()){
                    controller.showValidationErrors();
                }
                else {
                    stepIndex++;
                    goToStep(stepIndex);
                }

            }
        });

    }

    public void goToStep(int stepIndex){
        Fragment fragment;
        //switch(stepIndex){
            if(stepIndex == SELECT_PARTICIPANT_OR_GROUP_INDEX){
                fragment = new SelectParticipantFragment();
                startFragment(fragment);
            }
        else if (stepIndex == CONFIRM_FRAGMENT_INDEX){
                fragment = new ConfirmActivityAdditionFragment();
                startFragment(fragment);
            }

        else if (stepIndex == SELECT_PARTICIPANT_FROM_GROUP_INDEX){
                fragment = new SelectParticipantsFromGroupFragment();
                startFragment(fragment);
            }

    }

    public void startFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.activity_tracking_fragment_container, fragment, SPECIFY)
                .commit();
    }

    /**
     * Start a fragment with some integer extra
     * @param fragment
     * @param groupPos
     * @param extraIntName
     */
    public void startFragment(Fragment fragment,String groupName, int groupPos, String extraIntName, String extraStringName){
        Bundle args = new Bundle();
        args.putInt(extraIntName,  groupPos);
        args.putString(extraStringName, groupName);
        fragment.setArguments(args);
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.activity_tracking_fragment_container, fragment, SPECIFY)
                .commit();
    }

    public void setToolbarTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    public void setNextButtonText(String text) {nextButton.setText(text);}

    /**
     * add an attendee to the lesson
     * @param sp
     */
    public void addAttendee(StoredParticipant sp){
        attendees.add(sp);
        Log.d("HostActivity", sp.getFirstName() + " added to the list!");
    }

    /**
     * remove an attendee from the lesson
     * @param sp
     */
    public void removeAttendee(StoredParticipant sp){
        attendees.remove(sp);
        Log.d("HostActivity", sp.getFirstName() + " removed from the list!");
    }

    public LinkedHashMap<String, String> getFormElements(){
        return formElements;
    }

    public ArrayList<StoredParticipant> getAttendees(){
        return attendees;
    }

    public ArrayList<StoredParticipant> getParticipants(){
        return participants;
    }

    public void setParticipants(ArrayList<StoredParticipant> spList){
        participants = spList;
    }

    public void searchParticipants(String str){
        List<StoredParticipant> results = ds.findParticipants(str);
        participants.clear();
        participants.addAll(results);
    }


    /* group methods */
    public void addGroup(Group group) {groups.add(group);}

    public void removeGroup(Group group) {groups.remove(group);}

    public ArrayList getGroups() {return groups;}

    public void setGroups(ArrayList groupList) {groups = groupList;}

    public void searchGroups(String str){
        //List<Group> results = ds.find
    }

    public ArrayList<StoredParticipant> getGroupMemberList(int index){
        RealmList<StoredParticipant> rlMembers = new RealmList<StoredParticipant>();
        rlMembers.addAll(groups.get(index).getMembers());
        ArrayList<StoredParticipant> members = new ArrayList<>();
        for(StoredParticipant sp: rlMembers){
            members.add(sp);
            Log.d(TAG, sp.getFirstName());
        }
        return members;
    }

    public String getGroupName(int index){
        return groups.get(index).getName();
    }



    /**
     * gets all elements from arraylist and adds to RealmList
     * @param list
     * @return
     */
    public RealmList<StoredParticipant> getRealmList(ArrayList<StoredParticipant> list){
        RealmList<StoredParticipant> realmList = new RealmList<>();
      for(StoredParticipant sp: list){
          realmList.add(sp);
      }
        return realmList;
    }

    public void saveActivityToRealm(){
        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealm(trackedActivity);
                    Log.d(TAG, "stored trackedActivity " + trackedActivity.getId().toString());
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /*Get the participant by ID from Realm TODO figure out why this isnt working...*/
    public StoredParticipant findParticipantById(String id){
        Realm realm = null;
        StoredParticipant scannedParticipant;

        try {
            realm = Realm.getDefaultInstance();

            scannedParticipant = realm.where(StoredParticipant.class)
                    .equalTo("id", id)
                    .or()
                    .equalTo("externalId",id)
                    .findFirst();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
        return scannedParticipant;
    }

    @Override
    protected void onNewIntent(Intent data) {
        super.onNewIntent(data);
        if(data != null && data.hasExtra("spIdScan")){
            setIntent(data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent data = getIntent();
        String spId = data.getStringExtra("spIdScan");
        StoredParticipant sp = ds.findParticipantById(spId);

        if(sp != null) {
            Log.d(TAG, spId);
            if (!attendees.contains(sp)) {
                attendees.add(sp);
                Log.d(TAG, sp.getFirstName() + " added to the list!");
                Toast.makeText(this, "Successfully added " + sp.getLastName() + ", " + sp.getFirstName() +
                        " to the activity.", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "item already exists in list or is null");
            }
        }
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


    @Override
    public void onBackPressed() {
        if (stepIndex != 0 && stepIndex != CONFIRM_FRAGMENT_INDEX) {
            stepIndex--;
            attendees.clear();
            goToStep(stepIndex);
        }

         else {
            WorkflowDialog confirmCancel = new WorkflowDialog(this, this, true);
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
        //do something
    }
}
