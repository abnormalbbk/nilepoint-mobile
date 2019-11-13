package com.nilepoint.monitorevaluatemobile.new_user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.fh.bridge.NPExchangeAPIManager;
import com.nilepoint.fh.bridge.WorldlinkAPIManager;
import com.nilepoint.fh.bridge.model.FHActivity;
import com.nilepoint.fh.bridge.model.FHActivityDetail;
import com.nilepoint.fh.bridge.model.FHAddress;
import com.nilepoint.fh.bridge.model.FHArea;
import com.nilepoint.fh.bridge.model.FHAreaType;
import com.nilepoint.fh.bridge.model.FHGroup;
import com.nilepoint.fh.bridge.model.FHGroupRole;
import com.nilepoint.fh.bridge.model.FHHousehold;
import com.nilepoint.fh.bridge.model.FHHouseholdParticipant;
import com.nilepoint.fh.bridge.model.FHHouseholdRelationship;
import com.nilepoint.fh.bridge.model.FHParticipant;
import com.nilepoint.fh.bridge.model.FHPlannedActivity;
import com.nilepoint.fh.bridge.model.FHProject;
import com.nilepoint.fh.bridge.model.FHUser;
import com.nilepoint.model.Area;
import com.nilepoint.model.Form;
import com.nilepoint.model.Group;
import com.nilepoint.model.Household;
import com.nilepoint.model.HouseholdRelationship;
import com.nilepoint.model.Photo;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.PlannedDistribution;
import com.nilepoint.model.Project;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.admin.FirstSetupWizardHostActivity;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;
import com.nilepoint.monitorevaluatemobile.init.SelectUserActivity;
import com.nilepoint.monitorevaluatemobile.logging.RemoteLogger;
import com.nilepoint.monitorevaluatemobile.persistence.Environment;
import com.nilepoint.monitorevaluatemobile.user.UserSession;
import com.nilepoint.xindicate.Participant;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.paperdb.Paper;
import io.realm.Realm;

//import com.nilepoint.fh.bridge.model.FHPlannedActivity;

/**
 * Created by ashaw on 6/5/17.
 */

public class OAuthActivity extends AppCompatActivity {

    public static final String TAG = "OAuthActivity";

    private String oauthClientID = "548038281131-u21j35ne6crra8dbjeumon304njpt9m8.apps.googleusercontent.com";

    private RemoteLogger log = new RemoteLogger();
    private NPExchangeAPIManager api;

    private Handler mainHandler ;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private ProgressBar progressBar;

    EditText errorTxt;
    // Keep this enabled -- things broke in weird ways without it
    private boolean disableOauth = false;


    public void appendToStatusText(final String text){
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                errorTxt.append(text + "\n");
            }
        };

        mainHandler.post(myRunnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_oauth);

        mainHandler = new Handler(getMainLooper());

        final TextView view = (TextView) findViewById(R.id.oauth_msg);
        final Button button = (Button) findViewById(R.id.oauth_done_button);
        errorTxt = (EditText) findViewById(R.id.errorText);

        Environment environment = Paper.book().read("environment");

        System.out.println("Authenticating through " + environment.getAuthHostname() + " api: " + environment.getApiHostname());
        api = new NPExchangeAPIManager(environment.getAuthHostname(),environment.getApiHostname());

        //api = new WorldlinkAPIManager(environment.getAuthHostname(), environment.getApiHostname());

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Paper.book().write("data.init", true);

                finish();

                Intent intent = new Intent(getBaseContext(), SelectUserActivity.class);

                startActivity(intent);
            }
        });

        if (getIntent().getData() != null) {
            log.info(TAG, "Intent data " + getIntent().getData().toString());

            // To re-enable Oauth2 Flow
            final String authzCode = getIntent().getData().getQueryParameter("code");

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //log.debug(TAG,"Authenticating oauth with auth code " + authzCode);

                        // HACK:
                        if (!api.authenticate("admin@nilepoint.com","abc123!")){
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    errorTxt.append("There was an error in authentication");
                                    button.setVisibility(View.VISIBLE);
                                    progressBar.setProgress(100);
                                }
                            };
                            mainHandler.post(myRunnable);
                            return;
                        }
//                        if (!api.authenticate(authzCode)){
//                            Runnable myRunnable = new Runnable() {
//                                @Override
//                                public void run() {
//                                    errorTxt.append("There was an error in authentication, please try again later:\n " +
//                                            api.getErrors().get(0).substring(0, 255));
//
//                                    log.error(TAG, "Could not authenticate with FH API: " + api.getErrors().get(0));
//
//                                    button.setVisibility(View.VISIBLE);
//
//                                    progressBar.setProgress(100);
//                                } // This is your code
//                            };
//
//                            mainHandler.post(myRunnable);
//
//                            return;
//                        }

                        //appendToStatusText("Getting user information from Exchange API");



                        Realm realm = null;
                        try {
                            realm = Realm.getDefaultInstance();
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    User user = new User();
                                    user.setFirstName("Nilepoint");
                                    user.setLastName("User");
                                    user.setLoginEnabled(true);
                                    realm.copyToRealm(user);

                                }
                            });



//                            realm.executeTransaction(new Realm.Transaction() {
//                                @Override
//                                public void execute(Realm realm) {
//                                    User user = new User();
//
//                                    user.setFirstName(api.getFirstNameFromToken());
//                                    user.setLastName(api.getLastNameFromToken());
//                                    user.setEmail(api.getEmailFromToken());
//                                    user.setLoginEnabled(true);
//
//                                    realm.copyToRealm(user);
//
//                                    UserSession.userId = user.getId();
//
//                                    //log.info(TAG, "Created user " + user );
//
//                                    appendToStatusText("Created user " + user );
//
//                                    // download workers if admin.
//                                }
//                            });

                        } catch (Exception ex){
                            ex.printStackTrace();
                        } finally {
                            if (realm != null){
                                realm.close();
                            }
                        }

                        List<Callable<Long>> tasks = new ArrayList<>();
                        tasks.add(new PopulateParticipantsTask());
//                        tasks.add(new PopulateDummyProjectsTask());
//                        tasks.add(new PopulateDummyGroupsTask());


                      /*  tasks.add(new PopulateAreasTask());
                        tasks.add(new PopulateProjectsTask());
                        tasks.add(new PopulateHouseholdTask());
                        tasks.add(new PopulateGroupsTask());
                      */
                        // wait for all tasks to finish.
                        executorService.invokeAll(tasks);

                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                view.setText("Finished Downloading.");

                                button.setVisibility(View.VISIBLE);
                            } // This is your code
                        };

                        mainHandler.post(myRunnable);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);

                        appendToStatusText("Failed to download information from Exchange API");

                        Paper.book().write("data.init", false);

                        log.error(TAG,"Error thrown trying to download information from FH API: " + ExceptionUtils.getStackTrace(e), e);
                    }
                }
            });

            t.start();

        } else {

            if (!disableOauth) {
                log.debug(TAG, "Starting browser for OAUTH");

                Uri authzUrl = Uri.parse("https://accounts.google.com/o/oauth2/auth?client_id=" + oauthClientID + "&redirect_uri=com.nilepoint.monitorevaluatemobile%3A%2Foauth.callback&response_type=code&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email");

                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, authzUrl);

                startActivity(launchBrowser);


                log.debug(TAG, "Done launching browser for OAUTH");
            }
            finish();
        }
    }

    public void increaseProgressBar(final int amount){
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progressBar.getProgress() + amount);
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    class PopulateParticipantsTask implements Callable<Long>{

        @Override
        public Long call() throws Exception {
            final List<Participant> participants = api.getParticipants();
            Log.i("PopulateParticipants", String.format("Got %d participants", participants.size()));
            appendToStatusText(String.format("Got %d participants", participants.size()));
            appendToStatusText("Downloaded " + participants.size() + " participants");

            Realm realm = null;
            try {
                realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        // get rid of the participants and assorted other things
                        realm.where(StoredParticipant.class).findAll().deleteAllFromRealm();
                        realm.where(HouseholdRelationship.class).findAll().deleteAllFromRealm();
                        realm.where(Household.class).findAll().deleteAllFromRealm();
                        realm.where(Photo.class).findAll().deleteAllFromRealm();

                        for (Participant participant: participants){
                            MapMessage message = new MapMessage();
                            StoredParticipant stored = realm.where(StoredParticipant.class)
                                    .equalTo("externalId", participant.getParticipantCode()).findFirst();

                            message.put(FormKeyIDs.GIVEN_NAME_ID, participant.getGivenName());
                            message.put(FormKeyIDs.FATHER_NAME_ID, participant.getFatherName());
                            message.put(FormKeyIDs.PREFERRED_NAME_ID, participant.getPreferredName());
                            message.put(FormKeyIDs.PHONE_NUMBER_ID, participant.getPhoneNumber());
                            message.put(FormKeyIDs.BIRTHDAY_ID,participant.getBirthday());
                            message.put(FormKeyIDs.GENDER_ID,participant.getGender());
                            message.put(FormKeyIDs.PARTICIPANT_CODE,participant.getParticipantCode());
                            message.put(FormKeyIDs.CLUSTER_ID, participant.getCluster());
                            message.put(FormKeyIDs.COMMUNITY_ID, participant.getCommunity());


                            if (stored == null ){
                                stored = new StoredParticipant();
                                stored.setExternalId(participant.getParticipantCode());
                                stored.setId(participant.getId());
                                message.setId(participant.getId());
                            }

                            stored.setMessage(message);

                            realm.copyToRealm(stored);

                        }


                    }
                });
            }catch (Exception e) {
                log.error(TAG, e.getMessage());
                e.printStackTrace();
            } finally {
                log.error(TAG, "Closing Realm in PopulateParticipantsTask");
                if (realm != null) {
                    realm.close();
                }
            }
            return 0L;
        }
    }

    class PopulateDummyProjectsTask implements Callable<Long>{

        @Override
        public Long call() throws Exception {
            Realm realm = null;
            try {
                appendToStatusText("Creating Projects...");
                realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            realm.where(Project.class).findAll().deleteAllFromRealm();
                            realm.where(PlannedActivity.class).findAll().deleteAllFromRealm();
                            realm.where(PlannedDistribution.class).findAll().deleteAllFromRealm();

                            List<FHProject> projects = api.getDummyProjectActivities("1");

                            // store the projects for syncing
                            Paper.book().write("projects.fh", projects);

                            for (FHProject project : projects) {
                                log.info(TAG, "Adding project " + project.getName());

                                appendToStatusText("Adding project " + project.getName());

                                Project realmProject = processProject(realm, project);

                                realm.copyToRealm(realmProject);

                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            appendToStatusText("Error creating all projects " + e.getMessage());
                            log.error(TAG, "Exception thrown attempting to populate projects", e);
                        }
                    }
                });
                increaseProgressBar(25);

                appendToStatusText("Done creating "+realm.where(PlannedActivity.class).count()+" activities in " + realm.where(Project.class).count() + " projects.");

                return realm.where(PlannedActivity.class).count();
            }catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
            return 0L;
    }

    class PopulateProjectsTask implements Callable<Long> {
        @Override
        public Long call() throws Exception {
            /*Realm realm = null;
            try {

                appendToStatusText("Creating Projects...");

                realm = Realm.getDefaultInstance();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            realm.where(Project.class).findAll().deleteAllFromRealm();
                            realm.where(PlannedActivity.class).findAll().deleteAllFromRealm();
                            realm.where(PlannedDistribution.class).findAll().deleteAllFromRealm();

                            final FHArea myArea = api.getMyArea();


                            List<FHProject> projects = api.getProjectActivities(myArea.getId().toString());

                            // store the projects for syncing
                            Paper.book().write("projects.fh", projects);

                            for (FHProject project : projects) {
                                log.info(TAG, "Adding project " + project.getName());

                                appendToStatusText("Adding project " + project.getName());

                                Project realmProject = processProject(realm, project);

                                realm.copyToRealm(realmProject);

                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            appendToStatusText("Error creating all projects " + e.getMessage());
                            log.error(TAG, "Exception thrown attempting to populate projects", e);
                        }
                    }
                });

                increaseProgressBar(25);

                appendToStatusText("Done creating "+realm.where(PlannedActivity.class).count()+" activities in " + realm.where(Project.class).count() + " projects.");

                return realm.where(PlannedActivity.class).count();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }*/

            return 0L;
        }
    }

        /**
         * Convert to a realm area and persist.
         * //@param project
         *
         * @return
         */
        private Project processProject(Realm realm, FHProject project) {
            log.debug(TAG,"Processing project " + project.getName());

            Project realmProject = new Project();

            realmProject.setId(project.getId());
            realmProject.setName(project.getName() == null ? "Project " + project.getId() : project.getName());

            if (project.getPlannedActivities().size() > 0) {
                for (FHPlannedActivity plannedActivity : project.getPlannedActivities()) {
                    PlannedActivity pa = processPlannedActivity(realm, null, plannedActivity,0);

                    realmProject.getActivities().add(pa);

                    Log.d(TAG, "Got planned activity "+ plannedActivity.getName());
                }

            }

            return realmProject;
        }


        private PlannedActivity processPlannedActivity(Realm realm, FHPlannedActivity parent, FHPlannedActivity activity, int level) {
            PlannedActivity realmActivity = new PlannedActivity();

            Log.d(TAG, level + ". Processing activity " + activity.getName() + " id: " + activity.getId());

            realmActivity.setId(activity.getId());
            realmActivity.setName(activity.getName());
            realmActivity.setType(activity.getType());
            realmActivity.setProjectId(activity.getProjectId());
            realmActivity.setCategory(activity.getCategory());
            realmActivity.setLevelCode(activity.getLevelCode());

            activityCache.put(activity.getId(), realmActivity);

            if (activity.getActivities() != null && !activity.getActivities().isEmpty()){
                for (FHActivity childActivity : activity.getActivities()) {
                    processActivity(realm, realmActivity, childActivity);

                    try {
                        if (childActivity.getPlannedActivity().getPlannedDetails() != null
                                && realmActivity.getPlannedDistributions().isEmpty()) {
                            for (FHActivityDetail detail : childActivity.getPlannedActivity().getPlannedDetails()) {
                                System.out.println("Got detail " + detail.getUnitCode() + " for planned activity "
                                        + childActivity.getPlannedActivity().getId());
                                PlannedDistribution dist = new PlannedDistribution();

                                dist.setType(detail.getType());
                                dist.setParent(realmActivity);
                                dist.setQuantity(detail.getQuantity());
                                // do lookup instead of raw code
                                dist.setUnit(detail.getUnitCode());

                                //realm.copyToRealm(dist);

                                realmActivity.getPlannedDistributions().add(dist);
                            }
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    if (childActivity.getArea() != null) {

                        Area area = realm.where(Area.class)
                                .equalTo("id", childActivity.getAreaId()).findFirst();

                        if (area != null) {
                            realmActivity.getAreas().add(area);

                            if (FHAreaType.Community.equals(childActivity.getArea().getTypeName())){
                                // add the cluster as well
                                FHArea fhArea = childActivity.getArea().getCluster();
                                if (fhArea != null) {
                                    Area cluster = realm.where(Area.class).equalTo("name", fhArea.getAreaName()).findFirst();

                                    if (cluster != null && !realmActivity.getAreas().contains(cluster)) {
                                        realmActivity.getAreas().add(cluster);
                                    } else {
                                        System.out.println("There was an issue finding area name " + childActivity.getArea()
                                                .getCluster().getAreaName());
                                    }
                                }
                            }
                        }
                    }
                }
            }


            if (activity.getParentId() != null){
                System.out.println("Activity parent: " + activity.getParentId());

                PlannedActivity p = activityCache.get(activity.getParentId());

                if (p != null) {
                    p.getChildren().add(realmActivity);
                } else {
                    System.out.println("Could not find planned activity with ID " + activity.getParentId());
                }
            }

            return realmActivity;
        }
    }

   private void processActivity (Realm realm, PlannedActivity realmActivity, FHActivity activity){
        if (activity.getActivityDetails() != null && !activity.getActivityDetails().isEmpty()) {
            for (FHActivityDetail detail : activity.getActivityDetails()) {
                Log.d(TAG,"Adding activity detail " + detail);

                if (activity.getPlannedActivity().getType() != null && activity.getPlannedActivity().getType().equals("AT-Distribution")){
                    Log.d(TAG,"Got distribution, creating planned distribution.");

                    if (detail.getBeneficiary() != null && detail.getBeneficiary().getUuid() != null) {
                        System.out.println("Adding planned distribution for " + detail.getBeneficiary().getUuid());
                        PlannedDistribution dist = new PlannedDistribution();

                        dist.setType(detail.getType());
                        dist.setBeneficiaryUuid(detail.getBeneficiary().getUuid());
                        dist.setParent(activityCache.get(activity.getPlannedActivity().getId()));
                        dist.setQuantity(detail.getQuantity());
                        // do lookup instead of raw code
                        dist.setUnit(detail.getUnitCode());

                        //realm.copyToRealm(dist);

                        realmActivity.getPlannedDistributions().add(dist);
                    }
                }
            }
        }
    }

    Map<String, PlannedActivity> activityCache = new HashMap<String, PlannedActivity>();

    class PopulateAreasTask implements Callable<Long>  {
        @Override
        public Long call() {
           /* Realm realm = null;
            try {

                appendToStatusText("Creating Areas...");
                log.info(TAG, "Creating areas.");

                realm = Realm.getDefaultInstance();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        // get rid of all of our areas

                        try {
                            realm.where(Area.class).findAll().deleteAllFromRealm();

                            final FHArea myArea = api.getMyArea();
                            final FHArea theArea = api.getArea(myArea.getId().toString());

                            Paper.book().write("area.fh", theArea );

                            Area area = processArea(null, theArea);

                            realm.copyToRealm(area);
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error(TAG,"Exception thrown processing area " , e);
                        }
                    }
                });

                increaseProgressBar(25);

                log.info(TAG, "Created " + realm.where(Area.class).count() + " areas.");

                appendToStatusText("Created " + realm.where(Area.class).count() + " areas.");

                return realm.where(Area.class).count();
            } catch (Exception e) {
                log.error(TAG, "Exception thrown when trying to add area " + ExceptionUtils.getStackTrace(e));
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }*/

            return 0L;
        }


    }

    class PopulateHouseholdTask implements Callable<Long> {
//        @Override
        public Long call() {
//            Realm realm = null;
//            try {
//
//                appendToStatusText("Creating Households/Participants...");
//
//                realm = Realm.getDefaultInstance();
//
//                FHArea myArea = api.getMyArea();
//
//                final List<FHHousehold> households = api.getHouseholdsInChunks(myArea.getLeftIndex().toString(), myArea.getRightIndex().toString(), 100, new WorldlinkAPIManager.ChunksCallback() {
//                    @Override
//                    public void onDownloadDone(int i, int total) {
//                        appendToStatusText("Downloaded " + i + " households out of " + total);
//                    }
//                });
//
//                //final List<FHHousehold> households = api.getHouseholds(myArea.getLeftIndex().toString(), myArea.getRightIndex().toString(), 100,0);
//
//                log.debug(TAG,"Downloaded " + households.size() + " households");
//
//                appendToStatusText("Downloaded " + households.size() + " households");
//
//                realm.executeTransaction(new Realm.Transaction() {
//                    @Override
//                    public void execute(Realm realm) {
//                        // get rid of the participants and assorted other things
//                        realm.where(StoredParticipant.class).findAll().deleteAllFromRealm();
//                        realm.where(HouseholdRelationship.class).findAll().deleteAllFromRealm();
//                        realm.where(Household.class).findAll().deleteAllFromRealm();
//                        realm.where(Photo.class).findAll().deleteAllFromRealm();
//
//                        for (FHHousehold household : households){
//                            Household hh = new Household();
//
//                            //log.debug(TAG,"Got household: " + household);
//                            for (FHHouseholdParticipant hr : household.getRelations()){
//                                FHParticipant participant = hr.getParticipant();
//
//                                /*log.debug(TAG,"Adding participant: " +
//                                        participant.getFirstName()+ " " +
//                                        participant.getLastName());*/
//
//                                try {
//                                    MapMessage message = new MapMessage();
//
//                                    StoredParticipant stored = realm.where(StoredParticipant.class)
//                                            .equalTo("externalId", participant.getParticipantCode()).findFirst();
//
//                                    message.put(FormKeyIDs.GIVEN_NAME_ID, participant.getFirstName());
//                                    message.put(FormKeyIDs.FATHER_NAME_ID, participant.getLastName());
//                                    message.put(FormKeyIDs.PREFERRED_NAME_ID, participant.getNickname());
//                                    message.put(FormKeyIDs.PHONE_NUMBER_ID, participant.getTelephoneNumber());
//                                    message.put(FormKeyIDs.PREFERRED_NAME_ID, participant.getNickname());
//
//                                    DateFormat fromdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
//
//                                    DateFormat todf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
//
//                                    if (participant.getBirthDate() != null){
//                                        try {
//                                            Date birthdate = fromdf.parse(participant.getBirthDate());
//
//                                            message.put(FormKeyIDs.BIRTHDAY_ID, todf.format(birthdate));
//                                        } catch (Exception ex){
//                                            log.debug(TAG,"Can't parse birthdate " + participant.getBirthDate());
//                                        }
//                                    }
//
//                                    message.put(FormKeyIDs.GENDER_ID, participant.getGender());
//                                    message.put(FormKeyIDs.PARTICIPANT_CODE, participant.getParticipantCode());
//
//
//                                   // log.debug(TAG,"Setting addresses: " + new Gson().toJson(household.getAddresses()));
//
//                                    if (participant.getHouseholds() != null) {
//                                        for (FHHouseholdRelationship hr2 : participant.getHouseholds()) {
//                                            if (hr2.getHousehold() != null && hr2.getHousehold().getAddresses() != null) {
//                                                for (FHAddress address : hr2.getHousehold().getAddresses()) {
//                                                    if (address.getCluster() != null) {
//                                                        message.put(FormKeyIDs.CLUSTER_ID, address.getCluster());
//                                                    }
//                                                    if (address.getCommunity() != null) {
//                                                        message.put(FormKeyIDs.COMMUNITY_ID, address.getCommunity());
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    if (stored == null) {
//                                        stored = new StoredParticipant();
//                                        stored.setExternalId(participant.getParticipantCode());
//                                        stored.setId(participant.getUuid());
//                                        message.setId(participant.getUuid());
//                                        /*log.info(TAG,"setting UUID to "+ participant.getUuid()
//                                                + " and external id to " + participant.getParticipantCode());*/
//                                    }
//
//                                    if (hh.getHeadOfHousehold() == null){
//
//                                        hh.setHeadOfHousehold(stored);
//                                        message.put("isHeadOfHousehold", "true");
//
//                                    } else {
//
//                                        hh.addMember("type", stored.getId());
//                                    }
//
//                                    stored.setMessage(message);
//
//                                    realm.copyToRealm(stored);
//
//                                } catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            realm.copyToRealm(hh);
//                        }
//                    }
//                });
//
//            } catch (Exception e) {
//                log.error(TAG, "Error downloading households ", e);
//            } finally {
//                if (realm != null) {
//                    realm.close();
//                }
//            }
//
//            increaseProgressBar(25);
//
            return 1L;
        }
    }

    class PopulateDummyGroupsTask implements Callable<Long> {
        @Override
        public Long call() {
           Realm realm = null;
            try {

                appendToStatusText("Creating Groups...");

                realm = Realm.getDefaultInstance();

                final List<FHGroup> groups = api.getDummyGroups("1");

                if (groups == null){
                    appendToStatusText("Could not download groups, something is wrong with the API call");
                    return 0L;
                }

                log.debug(TAG,"Oauth: Number of groups downloaded: " + groups.size());

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(Group.class).findAll().deleteAllFromRealm();

                    for (FHGroup group : groups){
                        Group g = new Group();
                        g.setBeginDate(group.getStartDate());
                        g.setEndDate(group.getEndDate());

                        log.debug(TAG,"OAuth: Creating new group: " +group.getName() + "with " + group.getMembers() + " members");

                        if (group.getAreaId() != null) {
                            System.out.println("Getting area " + group.getAreaId());

                            Area groupArea = realm.where(Area.class).equalTo("id", group.getAreaId()).findFirst();

                            if (groupArea != null) {
                                System.out.println(groupArea.getName() + " type:" + groupArea.getType());

                                if (groupArea.getType().equals("Community")) {
                                    g.setCommunity(groupArea.getName());
                                    g.setCluster(groupArea.getParent().getName());
                                }
                                if (groupArea.getType().equals("Cluster")) {
                                    g.setCluster(groupArea.getName());
                                }
                            }
                        }

                        g.setName(group.getName());
                        g.setType(group.getType());

                        g.setId(group.getUuid());

                        for (FHGroupRole gr : group.getMembers()) {
                            FHParticipant participant = gr.getParticipant();

                            try {
                                MapMessage message = new MapMessage();

                                message.setId(participant.getUuid());

                                StoredParticipant stored = realm.where(StoredParticipant.class)
                                        .equalTo("externalId", participant.getParticipantCode())
                                        .or()
                                        .equalTo("id", participant.getUuid())
                                        .findFirst();

                                message.put(FormKeyIDs.GIVEN_NAME_ID, participant.getFirstName());
                                message.put(FormKeyIDs.FATHER_NAME_ID, participant.getLastName());
                                message.put(FormKeyIDs.PREFERRED_NAME_ID, participant.getNickname());
                                message.put(FormKeyIDs.PHONE_NUMBER_ID, participant.getTelephoneNumber());
                                message.put(FormKeyIDs.GENDER_ID, participant.getGender());
                                message.put(FormKeyIDs.PARTICIPANT_CODE, participant.getParticipantCode());
                                message.put(FormKeyIDs.ESTIMATED_AGE, participant.isBirthdayEstimated().toString());

                                DateFormat fromdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                                DateFormat todf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);

                                if (participant.getBirthDate() != null){
                                    try {
                                        Date birthdate = fromdf.parse(participant.getBirthDate());

                                        message.put(FormKeyIDs.BIRTHDAY_ID, todf.format(birthdate));
                                    } catch (Exception ex){
                                        log.debug(TAG,"Can't parse birthdate " + participant.getBirthDate());
                                    }
                                }

                                for (FHHouseholdRelationship householdRelationship : participant.getHouseholds()) {
                                    FHHousehold household = householdRelationship.getHousehold();

                                    for (FHAddress address : household.getAddresses()) {
                                        if (address.getCluster() != null) {
                                            message.put(FormKeyIDs.CLUSTER_ID, address.getCluster());
                                        }
                                        if (address.getCommunity() != null) {
                                            message.put(FormKeyIDs.COMMUNITY_ID, address.getCommunity());
                                        }
                                        if (address.getSubCommunity() != null) {
                                            message.put(FormKeyIDs.VILLAGE_ID, address.getSubCommunity());
                                        }
                                    }
                                }


                                if (stored == null) {
                                    stored = new StoredParticipant();
                                    stored.setExternalId(participant.getParticipantCode());
                                    stored.setId(participant.getUuid());
                                }

                                log.debug(TAG,"OAuth: Added participant to group (" + group.getName() + ") " + message.getMap());

                                stored.setMessage(message);

                                if (gr.getTypeCode() == null) {
                                    g.getMembers().add(stored);
                                } else if (gr.getTypeCode().contains("Leader")){
                                    g.getLeaders().add(stored);
                                } else {
                                    g.getMembers().add(stored);
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        realm.copyToRealm(g);
                    }
                    }
                });


                Paper.book().write("groups.fh", groups);

                increaseProgressBar(25);

                appendToStatusText("Done creating groups");

                return realm.where(Group.class).count();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }

            return 0L;
        }
    }

    class PopulateGroupsTask implements Callable<Long> {
        @Override
        public Long call() {
           /* Realm realm = null;
            try {

                appendToStatusText("Creating Groups...");

                realm = Realm.getDefaultInstance();

                final List<FHGroup> groups = api.getGroups(api.getMyArea().getId().toString());

                if (groups == null){
                    appendToStatusText("Could not download groups, something is wrong with the API call");
                    return 0L;
                }

                log.debug(TAG,"Oauth: Number of groups downloaded: " + groups.size());

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(Group.class).findAll().deleteAllFromRealm();

                    for (FHGroup group : groups){
                        Group g = new Group();
                        g.setBeginDate(group.getStartDate());
                        g.setEndDate(group.getEndDate());

                        log.debug(TAG,"OAuth: Creating new group: " +group.getName() + "with " + group.getMembers() + " members");

                        if (group.getAreaId() != null) {
                            System.out.println("Getting area " + group.getAreaId());

                            Area groupArea = realm.where(Area.class).equalTo("id", group.getAreaId()).findFirst();

                            if (groupArea != null) {
                                System.out.println(groupArea.getName() + " type:" + groupArea.getType());

                                if (groupArea.getType().equals("Community")) {
                                    g.setCommunity(groupArea.getName());
                                    g.setCluster(groupArea.getParent().getName());
                                }
                                if (groupArea.getType().equals("Cluster")) {
                                    g.setCluster(groupArea.getName());
                                }
                            }
                        }

                        g.setName(group.getName());
                        g.setType(group.getType());

                        g.setId(group.getUuid());

                        for (FHGroupRole gr : group.getMembers()) {
                            FHParticipant participant = gr.getParticipant();

                            try {
                                MapMessage message = new MapMessage();

                                message.setId(participant.getUuid());

                                StoredParticipant stored = realm.where(StoredParticipant.class)
                                        .equalTo("externalId", participant.getParticipantCode())
                                        .or()
                                        .equalTo("id", participant.getUuid())
                                        .findFirst();

                                message.put(FormKeyIDs.GIVEN_NAME_ID, participant.getFirstName());
                                message.put(FormKeyIDs.FATHER_NAME_ID, participant.getLastName());
                                message.put(FormKeyIDs.PREFERRED_NAME_ID, participant.getNickname());
                                message.put(FormKeyIDs.PHONE_NUMBER_ID, participant.getTelephoneNumber());
                                message.put(FormKeyIDs.GENDER_ID, participant.getGender());
                                message.put(FormKeyIDs.PARTICIPANT_CODE, participant.getParticipantCode());
                                message.put(FormKeyIDs.ESTIMATED_AGE, participant.isBirthdayEstimated().toString());

                                DateFormat fromdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                                DateFormat todf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);

                                if (participant.getBirthDate() != null){
                                    try {
                                        Date birthdate = fromdf.parse(participant.getBirthDate());

                                        message.put(FormKeyIDs.BIRTHDAY_ID, todf.format(birthdate));
                                    } catch (Exception ex){
                                        log.debug(TAG,"Can't parse birthdate " + participant.getBirthDate());
                                    }
                                }

                                for (FHHouseholdRelationship householdRelationship : participant.getHouseholds()) {
                                    FHHousehold household = householdRelationship.getHousehold();

                                    for (FHAddress address : household.getAddresses()) {
                                        if (address.getCluster() != null) {
                                            message.put(FormKeyIDs.CLUSTER_ID, address.getCluster());
                                        }
                                        if (address.getCommunity() != null) {
                                            message.put(FormKeyIDs.COMMUNITY_ID, address.getCommunity());
                                        }
                                        if (address.getSubCommunity() != null) {
                                            message.put(FormKeyIDs.VILLAGE_ID, address.getSubCommunity());
                                        }
                                    }
                                }


                                if (stored == null) {
                                    stored = new StoredParticipant();
                                    stored.setExternalId(participant.getParticipantCode());
                                    stored.setId(participant.getUuid());
                                }

                                log.debug(TAG,"OAuth: Added participant to group (" + group.getName() + ") " + message.getMap());

                                stored.setMessage(message);

                                if (gr.getTypeCode() == null) {
                                    g.getMembers().add(stored);
                                } else if (gr.getTypeCode().contains("Leader")){
                                    g.getLeaders().add(stored);
                                } else {
                                    g.getMembers().add(stored);
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        realm.copyToRealm(g);
                    }
                    }
                });


                Paper.book().write("groups.fh", groups);

                increaseProgressBar(25);

                appendToStatusText("Done creating groups");

                return realm.where(Group.class).count();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }*/

            return 0L;
        }
    }
}
