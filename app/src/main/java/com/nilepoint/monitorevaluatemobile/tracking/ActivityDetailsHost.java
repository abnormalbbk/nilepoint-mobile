package com.nilepoint.monitorevaluatemobile.tracking;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nilepoint.model.Photo;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.Project;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.camera.PhotoListFragment;
import com.nilepoint.monitorevaluatemobile.camera.PhotoListener;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;
import com.nilepoint.monitorevaluatemobile.group.InfoPair;
import com.nilepoint.monitorevaluatemobile.participant.ParticipantDataSource;
import com.nilepoint.monitorevaluatemobile.participant.ParticipantListFragment;
import com.nilepoint.monitorevaluatemobile.participant.SelectMultipleParticipantHostActivity;
import com.nilepoint.monitorevaluatemobile.viewpager.ViewPagerAdapter;
import com.nilepoint.model.Distribution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.Sort;

public class ActivityDetailsHost extends AppCompatActivity implements ParticipantListFragment.OnFragmentInteractionListener {

    public final static String TAG = "ActivityDetailsHost";

    public final static int ADD_PARTICIPANT = 1;
    public final static int REMOVE_PARTICIPANT = 2;

    private ViewPager viewPager;
    private FragmentPagerAdapter pagerAdapter;
    private PhotoListFragment photoFragment;
    private TabLayout tabLayout;
    private String activityId;
    private String type; //used to determine UI, comes from the PlannedActivity
    private Toolbar toolbar;
    private PlannedActivity plannedActivity;
    private String trackedActivityId;

    private boolean removeMode = false;

    //other info for fragments
    ArrayList<InfoPair> infoList = new ArrayList<>();

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_host);

        realm = Realm.getDefaultInstance();

        Intent intent = getIntent();

        final TrackedActivity trackedActivity = intent.getStringExtra("activity.id") != null
                ? realm.where(TrackedActivity.class).equalTo("id",
                intent.getStringExtra("activity.id")).findFirst() :
                new TrackedActivity();

        for (String key : getIntent().getExtras().keySet()) {
            if (key != null){
                Object value = getIntent().getExtras().get(key);
            Log.d(TAG, String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
            }
        }

        if (intent.getStringExtra("activity.id") == null) {
            trackedActivity.setProject(TrackingUtils.getProjectById(intent.getStringExtra("project.id")));

            if (intent.getStringExtra("training.id") != null) {
                PlannedActivity pActivity = TrackingUtils.getPlannedActivityById(intent.getStringExtra("training.id"));
                trackedActivity.setTraining(pActivity);
                plannedActivity = pActivity;
            }

            if (intent.getStringExtra("module.id") != null) {
                PlannedActivity pActivity = TrackingUtils.getPlannedActivityById(intent.getStringExtra("module.id"));
                trackedActivity.setModule(pActivity);
                plannedActivity = pActivity;
            }

            if (intent.getStringExtra("lesson.id") != null) {
                PlannedActivity pActivity = TrackingUtils.getPlannedActivityById(intent.getStringExtra("lesson.id"));
                trackedActivity.setLesson(pActivity);
                plannedActivity = pActivity;
            }

            if (intent.getStringExtra("distribution.id") != null) {
                PlannedActivity pActivity = TrackingUtils.getPlannedActivityById(intent.getStringExtra("distribution.id"));
                trackedActivity.setCategory(pActivity);
                plannedActivity = pActivity;

                System.out.println("Distribution, planned activity " + plannedActivity);
            }



            String clusterName = intent.getStringExtra(FormKeyIDs.CLUSTER_ID);
            String communityName = intent.getStringExtra(FormKeyIDs.COMMUNITY_ID);

            trackedActivity.setCluster(TrackingUtils.getAreaByName(clusterName));
            trackedActivity.setCommunity(TrackingUtils.getAreaByName(communityName));
        }

        if (trackedActivity.getTraining() != null){
            infoList.add(new InfoPair("Training", trackedActivity.getTraining().getName()));
        }

        if (trackedActivity.getModule() != null){
            infoList.add(new InfoPair("Module", trackedActivity.getModule().getName()));
        }

        if (trackedActivity.getLesson() != null){
            infoList.add(new InfoPair("Lesson", trackedActivity.getLesson().getName()));
        }

        if (trackedActivity.getCategory() != null){
            infoList.add(new InfoPair("Distribution", trackedActivity.getCategory().getName()));
        }

        if (trackedActivity.getCluster() != null) {
            infoList.add(new InfoPair("Cluster", trackedActivity.getCluster().getName()));
        }

        if (trackedActivity.getCommunity() != null) {
            infoList.add(new InfoPair("Community", trackedActivity.getCommunity().getName()));
        }
        //Manage fragments and tabs
        viewPager = (ViewPager) findViewById(R.id.activity_viewPager);
        pagerAdapter = new ActivityViewPagerAdapter(getSupportFragmentManager(), type, trackedActivity, this);
        viewPager.setAdapter(pagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.activity_sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Color.GRAY, Color.WHITE);


        //manage toolbars
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.tracking_menu);

        Menu options = toolbar.getMenu();

        toolbar.setTitle(R.string.activity_details);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Fragment currentFragment = getSupportFragmentManager().
                        findFragmentByTag("android:switcher:" + R.id.activity_viewPager + ":" + viewPager.getCurrentItem());
                switch (item.getItemId()){
                    case R.id.add_participant:
                        startActivityForResult(new Intent(ActivityDetailsHost.this,
                                ChooseAddParticipantTypeActivity.class), ADD_PARTICIPANT);
                        return true;

                    case R.id.remove_participant:

                        ArrayList<String> participantIds = new ArrayList<String>();

                        Intent intent = new Intent(ActivityDetailsHost.this,
                                SelectMultipleParticipantHostActivity.class);

                        for (StoredParticipant p : trackedActivity.getParticipantList()){
                            System.out.println(p.getExternalId());
                            participantIds.add(p.getId());
                        }

                        intent.putStringArrayListExtra("participant.ids",participantIds);

                        startActivityForResult(intent, REMOVE_PARTICIPANT);

                        return true;
                    case R.id.tracking_done:
                        Realm realm = Realm.getDefaultInstance();

                        final TrackedActivity trackedActivity = realm.where(TrackedActivity.class)
                                .equalTo("id", trackedActivityId)
                                .findFirst();

                        Intent confirmIntent = null;

                        if (trackedActivity.getCategory() != null) {
                            // This is a distribution, show the confirm distribution.
                            try {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        if (trackedActivity.getCategory() != null) {
                                            trackedActivity.setStatus("Completed");
                                        }
                                    }
                                });
                                Log.d(TAG, "Sending activity to DTN " + trackedActivity.toMessage());

                                com.nilepoint.dtn.Bundle b = WLTrackApp.dtnService.createBundle(trackedActivity.toMessage());

                                if (WLTrackApp.dtnService.amqpConvergenceLayer != null){
                                    WLTrackApp.dtnService.amqpConvergenceLayer.sendToAllNeighbors(b);
                                }

                                WLTrackApp.dtnService.getDTN().enqueue(b);
                            } finally {
                                realm.close();
                            }
                        } else {
                            try {

                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        if (trackedActivity.getCategory() != null) {
                                            trackedActivity.setStatus("Completed");
                                        }
                                    }
                                });
                                Log.d(TAG, "Sending activity to DTN " + trackedActivity.toMessage());

                                com.nilepoint.dtn.Bundle b = WLTrackApp.dtnService.createBundle(trackedActivity.toMessage());

                                if (WLTrackApp.dtnService.amqpConvergenceLayer != null){
                                    WLTrackApp.dtnService.amqpConvergenceLayer.sendToAllNeighbors(b);
                                }

                                WLTrackApp.dtnService.getDTN().enqueue(b);

                            } catch (Exception e){
                                Log.e(TAG, "Error saving activity ", e);
                            } finally {
                                if (realm != null){
                                    realm.close();
                                }
                            }

                            confirmIntent = new Intent(ActivityDetailsHost.this, ConfirmActivityActivity.class);

                            confirmIntent.putExtra("activity.id", trackedActivity.getId());

                        }

                        if (confirmIntent != null) {
                            startActivityForResult(confirmIntent, 1);
                        }

                        finish();

                        return true;

                }
                return true;
            }
        });

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    // save the tracked activity to realm
                    realm.copyToRealm(trackedActivity);
                    trackedActivityId = trackedActivity.getId();
                }
            });
        } catch (Exception e){
            Log.e(TAG, "Error saving activity ", e);
        } finally {
            if (realm != null){
                realm.close();
            }
        }

    }

    //Methods to talk to the fragments
    public ArrayList<InfoPair> getInfoList(){
        return infoList;
    }

    public PlannedActivity getPlannedActivity() {
        return plannedActivity;
    }

    /**
     * Handles tabs and fragments
     */
    public class ActivityViewPagerAdapter extends ViewPagerAdapter {

        private String[] tabTitles = {"About", "Participants", "Photos"};
        private String type;
        private HashMap<Integer, String> fragmentTags;
        private ActivityDetailsHost context;

        TrackedActivity trackedActivity;

        ActivityViewPagerAdapter(FragmentManager fragmentManager, String type, TrackedActivity trackedActivity, ActivityDetailsHost context){
            super(fragmentManager);

            setTabTitles(tabTitles);

            this.type = type;

            fragmentTags = new HashMap<Integer, String>();

            this.trackedActivity = trackedActivity;

            this.context = context;

        }

        public HashMap<Integer, String> getFragmentTags(){
            return fragmentTags;
        }


        @Override
        public Fragment getItem(int position){
            Fragment fragment;

            switch(position){
                case 0:
                    fragment = TrackingAboutFragment.newInstance();
                    break;
                case 1:
                    fragment = ParticipantListFragment.newInstance(new ParticipantDataSource() {
                        @Override
                        public List<StoredParticipant> getParticipants() {
                            Realm realm = Realm.getDefaultInstance();
                            try {
                                final TrackedActivity trackedActivity = realm.where(TrackedActivity.class)
                                        .equalTo("id", trackedActivityId)
                                        .findFirst();
                                return trackedActivity.getParticipantList().where()
                                        .findAllSorted("firstName", Sort.ASCENDING,
                                                "lastName", Sort.ASCENDING);
                            } finally {
                                realm.close();
                            }
                        }

                        @Override
                        public List<StoredParticipant> getParticipants(String search) {
                            Realm realm = Realm.getDefaultInstance();
                            try {
                                final TrackedActivity trackedActivity = realm.where(TrackedActivity.class)
                                        .equalTo("id", trackedActivityId)
                                        .findFirst();
                                return trackedActivity.getParticipantList().where()
                                        .beginsWith("lastName", search)
                                        .or()
                                        .beginsWith("firstName", search)
                                        .findAllSorted("firstName", Sort.ASCENDING,
                                                "lastName", Sort.ASCENDING);
                            } finally {
                                realm.close();
                            }
                        }

                        @Override
                        public List<StoredParticipant> getParticipants(String search, String sort) {
                            Realm realm = Realm.getDefaultInstance();
                            try {
                                final TrackedActivity trackedActivity = realm.where(TrackedActivity.class)
                                        .equalTo("id", trackedActivityId)
                                        .findFirst();
                                return trackedActivity.getParticipantList().where()
                                    .beginsWith("lastName", search)
                                    .or()
                                    .beginsWith("firstName", search).findAll()
                                    .sort(sort);
                            } finally {
                                realm.close();
                            }
                        }
                    }, true, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            context.startActivityForResult(new Intent(context,
                                    ChooseAddParticipantTypeActivity.class), ADD_PARTICIPANT);
                        }
                    });
                    break;
                    //fragment = TrackingAboutFragment.newInstance();
                case 2:
                    photoFragment = PhotoListFragment.newInstance(trackedActivity.getPhotos());
                    fragment = photoFragment;
                    break;
                    //return GroupMembersFragment.newInstance();
                default:
                    fragment = TrackingAboutFragment.newInstance();
            }

            fragmentTags.put(position,fragment.getTag());

            return fragment;
        }

    } //eoc

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null){
            return;
        }

        final String pId = data.getStringExtra("participant.id");

        final Collection<String> participantIds = pId != null ? Collections.singleton(pId)
                : data.getStringArrayListExtra("participant.ids");

        Realm realm = null;

        switch (requestCode){
            case ADD_PARTICIPANT:
                if (participantIds != null){
                    try {
                        realm = Realm.getDefaultInstance();
                        final TrackedActivity trackedActivity = realm.where(TrackedActivity.class)
                                .equalTo("id", trackedActivityId)
                                .findFirst();

                        realm = Realm.getDefaultInstance();

                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                for (String pId : participantIds) {
                                    StoredParticipant participant =
                                            realm.where(StoredParticipant.class).equalTo("id", pId).findFirst();

                                    trackedActivity.getParticipantList().add(participant);
                                    // hack for now
                                    /*if ( !ActivityDetailsHost.this.trackedActivity.getParticipantList().contains(participant)) {
                                        ActivityDetailsHost.this.trackedActivity
                                                .getParticipantList().add(participant);
                                    }*/
                                }
                            }
                        });

                        if (trackedActivity.getCategory() != null) {
                            // This is a distribution, show the confirm distribution.
                            Intent confirmIntent = new Intent(ActivityDetailsHost.this, ConfirmDistributionActivity.class);

                            final ArrayList<String> distributionIds = new ArrayList<>();

                            try {
                                realm = Realm.getDefaultInstance();
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        final TrackedActivity trackedActivity = realm.where(TrackedActivity.class)
                                                .equalTo("id", trackedActivityId)
                                                .findFirst();

                                        List<StoredParticipant> participants =
                                                realm.where(StoredParticipant.class)
                                                        .in("id",
                                                                participantIds.toArray(new String[]{})).findAll();

                                        System.out.println("Participants: " + participants);

                                        for (StoredParticipant p : participants) {

                                            System.out.println("Adding distribution for " + p + " planned:" + trackedActivity.getCategory());

                                            Distribution d = new Distribution();

                                            d.setParticipant(p);

                                            d.setProject(realm.where(Project.class).equalTo("id"
                                                    ,trackedActivity.getProject().getId()).findFirst());
                                            d.setDistribution(trackedActivity.getCategory());
                                            d.setCluster(trackedActivity.getCluster());
                                            d.setActivityDate(new Date());
                                            d.setPhotos(trackedActivity.getPhotos());

                                            realm.copyToRealm(d);

                                            distributionIds.add(d.getId());

                                            System.out.println("Created distribution " + d.getId());
                                        }
                                    }
                                });
                            } catch (Exception e){
                              Log.e(TAG, "Error creating distribution",e);
                            } finally {
                                if (realm != null){
                                    realm.close();
                                }
                            }

                            if (!trackedActivity.getParticipantList().isEmpty()) {
                                confirmIntent.putExtra("participant.id", trackedActivity.getParticipantList()
                                        .get(0).getId());
                            }

                            System.out.println("Sending " + distributionIds + " to confirm activity");
                            confirmIntent.putStringArrayListExtra("distribution.ids",distributionIds);

                            startActivity(confirmIntent);
                        }

                    } finally {
                        if (realm != null) {
                            realm.close();
                        }
                    }
                }
            break;

            case REMOVE_PARTICIPANT:
                if (participantIds != null){
                    try {
                        realm = Realm.getDefaultInstance();
                        final TrackedActivity trackedActivity = realm.where(TrackedActivity.class)
                                .equalTo("id", trackedActivityId)
                                .findFirst();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                for (String pId : participantIds) {
                                    StoredParticipant participant =
                                            realm.where(StoredParticipant.class).equalTo("id", pId).findFirst();

                                    System.out.println("Removing " + participant + " from tracked activity");

                                    trackedActivity.getParticipantList().remove(participant);

                                    /*ActivityDetailsHost.this.trackedActivity
                                            .getParticipantList().remove(participant);*/
                                }
                            }
                        });
                    } finally {
                        if (realm != null) {
                            realm.close();
                        }
                    }
                }
                break;

            case PhotoListFragment.PHOTO_TAKEN_REQUEST_CODE:
                Log.d(TAG,"Got photo taken request code");
                final Bitmap photo = (Bitmap) data.getExtras().get("data");

                final Photo photoRealm = new Photo();

                photoRealm.setPhoto(photo);

                try {
                    realm = Realm.getDefaultInstance();

                    final TrackedActivity ta = realm.where(TrackedActivity.class)
                            .equalTo("id", trackedActivityId)
                            .findFirst();

                    if (!ta.getPhotos().contains(photoRealm)) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                ta.getPhotos().add(photoRealm);
                            }
                        });
                    }

                    photoFragment.resetAdapter();

                } catch (Exception e) {
                    Log.e(TAG, "Exception", e);
                }finally {
                    if (realm != null){
                        realm.close();
                    }
                }
                break;
        }
    }

    /*public TrackedActivity getTrackedActivity() {
        return trackedActivity;
    }

    public void setTrackedActivity(TrackedActivity trackedActivity) {
        this.trackedActivity = trackedActivity;
    }*/

    public String getTrackedActivityId() {
        return trackedActivityId;
    }

    public void setTrackedActivityId(String trackedActivityId) {
        this.trackedActivityId = trackedActivityId;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        System.out.println(uri);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        realm.close();
    }
}
