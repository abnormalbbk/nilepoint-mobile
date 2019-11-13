package com.nilepoint.monitorevaluatemobile.group;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.nilepoint.model.Group;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.HomeActivity;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.participant.ParticipantDataSource;
import com.nilepoint.monitorevaluatemobile.viewpager.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.Sort;

public class GroupInfoHostActivity extends AppCompatActivity {

    //TODO temporary variables, the group color will be set by the user
    private static final int GROUP_COLOR = R.color.purple;
    private static final String GROUP_NAME = "Group 1";

    private ViewPager viewPager;
    private FragmentPagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private ActionBar actionBar;

    private Group group;
    private ArrayList<StoredParticipant> groupMembers;
    private ArrayList<InfoPair> groupInfo = new ArrayList<>();

    public final static int ADD_MEMBERS_TO_GROUP = 1;
    public final static int ADD_LEADERS_TO_GROUP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info_host);

        //get the group ID clicked
        final Intent intent = getIntent();

        final String groupId = intent.getStringExtra("group.id");

        group = GroupUtils.getGroupById(groupId);
        GroupUtils.setGroupInfo(groupInfo, group);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_groups);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundResource(R.color.purple);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle(group.getName()); //TODO get the group name by the ID
        //actionBar = getSupportActionBar();

        viewPager = (ViewPager) findViewById(R.id.group_viewPager);
        pagerAdapter = new GroupViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.group_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Color.GRAY, Color.WHITE);

    }

    public ArrayList<InfoPair> getGroupInfo() {
        return groupInfo;
    }

    public Group getGroup() {
        return group;
    }

    public ArrayList<StoredParticipant> getGroupMembers(){
        Log.d("GroupHost", "Number of members: " + group.getMembers().size());
        return GroupUtils.membersToArrayList(group.getMembers());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent goHome = new Intent(this, HomeActivity.class);
                startActivity(goHome);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles tabs and fragments
     */
    public class GroupViewPagerAdapter extends ViewPagerAdapter {

        private String[] tabTitles = {"Profile", "Leaders", "Members"};

        GroupViewPagerAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
            setTabTitles(tabTitles);

        }

        @Override
        public Fragment getItem(int position){

            switch(position){
                /*case 0:
                    return GroupAboutFragment.newInstance();*/
                case 0:
                    return GroupAboutFragment.newInstance();
                case 1:
                    return GroupMembersFragment.newInstance(new ParticipantDataSource() {
                        @Override
                        public List<StoredParticipant> getParticipants() {
                            return group.getLeaders()

                                    .sort("firstName", Sort.ASCENDING, "lastName", Sort.ASCENDING);
                        }

                        @Override
                        public List<StoredParticipant> getParticipants(String search) {
                            return group.getLeaders()
                                    .where()
                                    .beginsWith("cluster", search,Case.INSENSITIVE)
                                    .or()
                                    .contains("externalId", search,Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("firstName", search,Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("lastName", search, Case.INSENSITIVE)
                                    .findAllSorted("firstName", Sort.ASCENDING, "lastName", Sort.ASCENDING);
                        }

                        @Override
                        public List<StoredParticipant> getParticipants(String search, String sort) {
                            return group.getLeaders()
                                    .where()
                                    .beginsWith("cluster", search,Case.INSENSITIVE)
                                    .or()
                                    .contains("externalId", search,Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("firstName", search,Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("lastName", search, Case.INSENSITIVE)
                                    .findAllSorted("firstName", Sort.ASCENDING, "lastName", Sort.ASCENDING);
                        }
                    },ADD_LEADERS_TO_GROUP);
                case 2:
                    return GroupMembersFragment.newInstance(new ParticipantDataSource() {
                        @Override
                        public List<StoredParticipant> getParticipants() {
                            return group.getMembers()
                                    .sort("firstName", Sort.ASCENDING, "lastName", Sort.ASCENDING);
                        }

                        @Override
                        public List<StoredParticipant> getParticipants(String search) {
                            return group.getMembers()
                                    .where()
                                    .beginsWith("cluster", search,Case.INSENSITIVE)
                                    .or()
                                    .contains("externalId", search,Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("firstName", search,Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("lastName", search, Case.INSENSITIVE)
                                    .findAllSorted("firstName", Sort.ASCENDING, "lastName", Sort.ASCENDING);
                        }

                        @Override
                        public List<StoredParticipant> getParticipants(String search, String sort) {
                            return group.getMembers().where()
                                    .beginsWith("cluster", search,Case.INSENSITIVE)
                                    .or()
                                    .contains("externalId", search,Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("firstName", search,Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("lastName", search, Case.INSENSITIVE)
                                    .findAllSorted("firstName", Sort.ASCENDING, "lastName", Sort.ASCENDING);

                        }
                    },ADD_MEMBERS_TO_GROUP);
                default:
                    return null;
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Realm realm = null;

        if (data == null){
            return;
        }

        final ArrayList<String> participantIds = data.getStringArrayListExtra("participant.ids");

        switch (requestCode) {
            case ADD_MEMBERS_TO_GROUP:
                if (participantIds != null){
                    try {
                        realm = Realm.getDefaultInstance();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                for (String pId : participantIds) {
                                    StoredParticipant participant =
                                            realm.where(StoredParticipant.class).equalTo("id", pId).findFirst();

                                    group.addMember(participant);

                                }
                            }
                        });


                        com.nilepoint.dtn.Bundle b = WLTrackApp.dtnService.createBundle(group.toMessage());

                        WLTrackApp.dtnService.getDTN().enqueue(b);

                        if (WLTrackApp.dtnService.amqpConvergenceLayer != null){
                            WLTrackApp.dtnService.amqpConvergenceLayer.sendToAllNeighbors(b);
                        }
                    } finally {
                        if (realm != null) {
                            realm.close();
                        }
                    }
                }
                break;
            case ADD_LEADERS_TO_GROUP:
                if (participantIds != null){
                    try {
                        realm = Realm.getDefaultInstance();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                for (String pId : participantIds) {
                                    StoredParticipant participant =
                                            realm.where(StoredParticipant.class)
                                                    .equalTo("id", pId).findFirst();

                                    group.addLeader(participant);
                                }
                            }
                        });

                        com.nilepoint.dtn.Bundle b = WLTrackApp.dtnService.createBundle(group.toMessage());

                        WLTrackApp.dtnService.getDTN().enqueue(b);

                        if (WLTrackApp.dtnService.amqpConvergenceLayer != null){
                            WLTrackApp.dtnService.amqpConvergenceLayer.sendToAllNeighbors(b);
                        }
                    } finally {
                        if (realm != null) {
                            realm.close();
                        }
                    }
                }
                break;
        }

    }


}
