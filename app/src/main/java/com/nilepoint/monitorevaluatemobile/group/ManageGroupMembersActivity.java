package com.nilepoint.monitorevaluatemobile.group;

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
import android.widget.SearchView;
import android.widget.Toast;

import com.nilepoint.model.Group;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.tracking.ChooseAddParticipantTypeActivity;
import com.nilepoint.monitorevaluatemobile.participant.BarcodeScannerButtonFragment;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class ManageGroupMembersActivity extends AppCompatActivity implements BarcodeScannerButtonFragment.OnFragmentInteractionListener {

    private static final int ADD_PARTICIPANTS = 1;
    private static final int REMOVE_PARTICIPANTS = 2;
    private static final String SPECIFY = "specify_activity";
    private static final String DELETE = "DELETE";
    private static final String ADD = "ADD";


    private Group group;
    private Button button;
    private SearchView searchView;
    private boolean isSearchViewActive;
    private SelectableParticipantListFragment fragment;
    private String listAction;
    private String memberType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member_to_group);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        isSearchViewActive = false;


        listAction = getIntent().getStringExtra("listAction");
        memberType = getIntent().getStringExtra("memberType");

        final String groupId = getIntent().getStringExtra("group.id");

        group = GroupUtils.getGroupById(groupId);

        //startBarcodeScannerFragment();

        if(listAction.equals(ADD))
            getSupportActionBar().setTitle(R.string.find_members_to_add);
        else if(listAction.equals(DELETE)) {
            getSupportActionBar().setTitle("Remove Group Participants");
        }

        button = (Button) findViewById(R.id.group_member_button);
        searchView = (SearchView) findViewById(R.id.group_add__activity_searchview);
        searchView.clearFocus();
        isSearchViewActive = false;
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    //startFragmentByFlag(listAction);
                    isSearchViewActive = true;

                    //setButtonClickListener();
                    //setSearchTextListener();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listAction.equals(ADD)){
                    startActivityForResult(new Intent(ManageGroupMembersActivity.this,
                            ChooseAddParticipantTypeActivity.class), ADD_PARTICIPANTS);
                }
                else if(listAction.equals(DELETE)){

                    if ("leaders".equals(memberType)) {
                        GroupUtils.removeLeadersListFromGroup(fragment.getSelectedList(), groupId);
                    } else {
                        GroupUtils.removeMembersListFromGroup(fragment.getSelectedList(), groupId);
                    }

                    com.nilepoint.dtn.Bundle b = WLTrackApp.dtnService.createBundle(GroupUtils.getGroupMap(groupId));

                    WLTrackApp.dtnService.getDTN().enqueue(b);

                    if (WLTrackApp.dtnService.amqpConvergenceLayer != null){
                        WLTrackApp.dtnService.amqpConvergenceLayer.sendToAllNeighbors(b);
                    }

                    finish();
                }
            }
        });
        startFragmentByFlag(listAction);


    }

    //-----compartmentalizing listener creation for readability-------//
    /**
     * Set the listeners for the add button
     */
    public void setButtonClickListener() {

        String groupId = getIntent().getStringExtra("group.id");

        group = GroupUtils.getGroupById(groupId);

        Log.d("AddMember", "Group in addMember activity: " + group.getName());
        //startFragment(new SelectableParticipantListFragment());
        startFragmentByFlag(listAction);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case ADD_PARTICIPANTS:
                List<StoredParticipant> pList = new ArrayList<>();

                if (data.getStringExtra("participant.id") != null){
                    pList.add(getParticipantById(data.getStringExtra("participant.id")));
                } else {
                    List<String> ids = data.getStringArrayListExtra("participant.ids");

                    pList.addAll(getParticipantsByIds(ids));
                }

                addMembersToGroup(pList);

                // reload the group

                group = GroupUtils.getGroupById(group.getId());

                startFragmentByFlag(ADD);

                break;
            case REMOVE_PARTICIPANTS:
                startFragmentByFlag(ADD);
                break;
        }
    }

    public Group getGroup(){
        return group;
    }

    private StoredParticipant getParticipantById(String id){
        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            return realm.where(StoredParticipant.class).equalTo("id", id).findFirst();
        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }

    private List<StoredParticipant> getParticipantsByIds(List<String> ids){
        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            return realm.where(StoredParticipant.class).in("id",
                    ids.toArray(new String[]{}))
                    .findAll();
        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }

    public ArrayList<StoredParticipant> getGroupMembers(){
        return GroupUtils.membersToArrayList(group.getMembers());
    }

    /**
     * Starts the appropriate fragment based on the flag that was passed in from calling activity
     * @param listAction
     */
    private SelectableParticipantListFragment startFragmentByFlag(String listAction){
        Bundle bundle = new Bundle();

        fragment = new SelectableParticipantListFragment();

        switch(listAction){
            case(DELETE):
                button.setText("Remove");
                break;

            case(ADD):
                button.setText("Add");
                break;
        }

        bundle.putString("action", listAction);
        bundle.putString("memberType", memberType);

        fragment.setArguments(bundle);

        startFragment(fragment);
        return fragment;

    }

    /**
     * remove members list from the realm object
     * @param list
     */
    private void removeMembersFromGroup(List<StoredParticipant> list){
        GroupUtils.removeMembersListFromGroup(list, group.getId());
    }

    /**
     * add members to the list in the realm object
     * @param list
     */
    private void addMembersToGroup(List<StoredParticipant> list){
        GroupUtils.addMembersListToGroup(list, group.getId());
    }


    public void startFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.add_group_member_fragment_container, fragment, SPECIFY)
                .commit();
    }

    public SelectableParticipantListFragment getSelectableParticipantListFragment(){
        SelectableParticipantListFragment fragment = (SelectableParticipantListFragment)
                getSupportFragmentManager().findFragmentById(R.id.add_group_member_fragment_container);
        return fragment;
    }
  
    public void startBarcodeScannerFragment(){
        Bundle args = new Bundle();
        args.putString("activityStartFlag", "groupMembers");
        args.putString("groupId", group.getId());
        BarcodeScannerButtonFragment fragment = new BarcodeScannerButtonFragment();
        fragment.setArguments(args);
        startFragment(fragment);
    }

    /**
     * returns the current fragment in the container
     * @return
     */
    public Fragment getCurrentFragment(){
        return getSupportFragmentManager()
                .findFragmentById(R.id.add_group_member_fragment_container).getParentFragment();
    }

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
    public void onFragmentInteraction(Uri uri) {
        //Do a thing
    }
}
