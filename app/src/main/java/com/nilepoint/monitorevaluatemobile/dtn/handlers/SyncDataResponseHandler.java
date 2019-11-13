package com.nilepoint.monitorevaluatemobile.dtn.handlers;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.Group;
import com.nilepoint.model.Household;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.dtn.commands.CommandHandler;
import com.nilepoint.monitorevaluatemobile.dtn.commands.ResponseCallback;
import com.nilepoint.monitorevaluatemobile.dtn.commands.SyncDataCommand;
import com.nilepoint.monitorevaluatemobile.dtn.commands.SyncDataResponse;
import com.nilepoint.monitorevaluatemobile.integration.SystemDataProcessor;
import com.nilepoint.monitorevaluatemobile.user.UserSession;

import java.util.List;

import io.paperdb.Paper;
import io.realm.Realm;

/**
 * Created by ashaw on 2/5/18.
 */

public class SyncDataResponseHandler extends CommandHandler<SyncDataResponse> {
    private int newParticipants = 0;
    private int newHouseholds   = 0;
    private int updatedHouseholds = 0;
    private int newActivities   = 0;
    private int updatedParticipants = 0;

    public SyncDataResponseHandler() {
        super(SyncDataResponse.class);
    }

    @Override
    public void handleCommand(final SyncDataResponse cmd) {
        //msg("Got new data from peer. Adding participants....");
        handleParticipants(cmd.getParticipants());
        //msg("Got new data from peer. Adding households....");
        handleHouseholds(cmd.getHouseholds());
        //msg("Got new data from peer. Adding activities....");
        handleActivities(cmd.getActivities());
        //msg("Got new data from peer. Adding groups....");
        handleGroups(cmd.getGroups());
        //msg("Got new data from peer. Adding groups....");

        // handle system data if it's in the response.

        handleSystemData(cmd);

        if (getCallback() != null && cmd.getSyncDone() == true) {
            getCallback().isDone();
        }

    }


    private void handleSystemData(final SyncDataResponse cmd){
        SystemDataProcessor processor = new SystemDataProcessor();

        Realm realm = Realm.getDefaultInstance();

        try {
            if (cmd.getArea() != null) {
                msg("Got system data from remote host: Installing Area " + cmd.getArea());
                try {
                    processor.processBaseArea(realm, cmd.getArea());
                } catch (Exception e){
                    Crashlytics.logException(e);
                    e.printStackTrace();
                }
            }

            if (!cmd.getProjects().isEmpty()){
                try {
                    msg("Got system data from remote host: Installing projects " + cmd.getProjects());
                    processor.processProjects(cmd.getProjects());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            if (!cmd.getUsers().isEmpty()){
                msg("Got system data from remote host: Installing users " + cmd.getUsers());

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (final MapMessage user : cmd.getUsers()) {
                            System.out.println("SystemDataResponseHandler: Adding user " + user.getMap());

                            User sUser = User.fromMessage(user);

                            sUser.setLoginEnabled(true);

                            realm.copyToRealm(sUser);

                            UserSession.userId = user.getId();
                        }
                    }
                });

                Paper.book().write("data.init", true);
            }
        } finally {
            realm.close();
        }
    }

    private void handleGroups(final List<MapMessage> groups){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (MapMessage group : groups){
                        Group sgroup = realm.where(Group.class)
                                .equalTo("id", group.getId()).findFirst();

                        Group cGroup = Group.fromMessage(group);

                        if (sgroup == null){
                            realm.copyToRealm(cGroup);
                        }  else {
                            if (sgroup.getLastUpdated() == null || sgroup.getLastUpdated().before(cGroup.getLastUpdated()))
                            {
                                sgroup.setMembers(cGroup.getMembers());
                                sgroup.setLeaders(cGroup.getLeaders());
                                sgroup.setLastUpdated(cGroup.getLastUpdated());
                            }
                        }
                    }
                    //msg("New participants after sync: " + newParticipants);
                    //msg("Updated participants after sync: " + updatedParticipants);
                }
            });
        } finally {
            realm.close();
        }
    }
    private void handleParticipants(final List<MapMessage> participants){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (MapMessage participant : participants){
                        StoredParticipant sparticipant = realm.where(StoredParticipant.class)
                                .equalTo("id", participant.getId()).findFirst();

                        if (sparticipant == null){
                            realm.copyToRealm(new StoredParticipant(participant));

                            newParticipants++;
                        }  else {
                            if (sparticipant.getVersion() < participant.getVersion())
                            {
                                updatedParticipants+=1;
                                sparticipant.setMessage(participant);
                            }

                        }
                    }
                    //msg("New participants after sync: " + newParticipants);
                    //msg("Updated participants after sync: " + updatedParticipants);
                }
            });
        } finally {
            realm.close();
        }

    }

    private void handleHouseholds(final List<MapMessage> households){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (MapMessage household : households){
                        Household shousehold = realm.where(Household.class)
                                .equalTo("id", household.getId()).findFirst();

                        if (shousehold == null){
                            realm.copyToRealm(Household.fromMessage(household));

                            newHouseholds++;
                        } else if (household.getVersion() > shousehold.getVersion()){
                            Household updatedHousehold = Household.fromMessage(household);

                            if (updatedHousehold.getLastUpdated().after(shousehold.getLastUpdated())){
                                realm.copyToRealmOrUpdate(updatedHousehold);
                                updatedHouseholds++;
                            }
                        }
                    }

                    //msg("New households after sync: " + newHouseholds);
                    //msg("Updated households after sync: " + updatedHouseholds);
                }
            });
        } finally {
            realm.close();
        }
    }

    private void handleActivities(final List<MapMessage> activities){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (MapMessage activity : activities){
                        TrackedActivity sactivity = realm.where(TrackedActivity.class)
                                .equalTo("id", activity.getId()).findFirst();

                        if (sactivity == null){
                            realm.copyToRealm(TrackedActivity.fromMessage(activity));

                            newActivities++;
                        }
                    }

                    //msg("New activities after sync: " + newActivities);
                }
            });
        } finally {
            realm.close();
        }
    }
}
