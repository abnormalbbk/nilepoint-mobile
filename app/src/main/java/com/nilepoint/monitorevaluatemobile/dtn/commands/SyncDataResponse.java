package com.nilepoint.monitorevaluatemobile.dtn.commands;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.fh.bridge.model.FHArea;
import com.nilepoint.fh.bridge.model.FHProject;
import com.nilepoint.model.Group;
import com.nilepoint.model.Household;
import com.nilepoint.model.Photo;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.BluetoothConvergenceLayer;
import com.nilepoint.monitorevaluatemobile.persistence.SystemStorage;
import com.nilepoint.monitorevaluatemobile.user.UserSession;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import io.protostuff.Tag;
import io.realm.Realm;

/**
 * Created by ashaw on 2/6/18.
 */

public class SyncDataResponse extends Command {
    @Tag(1)
    List<MapMessage> households   = new ArrayList<>();

    @Tag(2)
    List<MapMessage> participants = new ArrayList<>();

    @Tag(3)
    List<MapMessage> activities   = new ArrayList<>();

    @Tag(4)
    List<MapMessage> groups   = new ArrayList<>();

    @Tag(5)
    Boolean syncDone = false;

    @Tag(6)
    FHArea area;

    @Tag(7)
    List<FHProject> projects = new ArrayList<>();

    @Tag(8)
    List<MapMessage> users = new ArrayList<>();

    @Tag(9)
    List<byte[]> photos = new ArrayList<>();


    public SyncDataResponse() {
    }


    /**
     * Factory method to get household data responses in chunks of a certain size.
     * @param size
     * @return
     */
    public static List<SyncDataResponse> getHouseholdsInChunks(int size){
        List<SyncDataResponse> responses = new ArrayList<>();

        Realm realm = Realm.getDefaultInstance();

        int householdCount = Long.valueOf(realm
                .where(Household.class).count()).intValue();

        try {
            for (int i = 0; i < householdCount ; i += size) {
                System.out.println("Getting households " + i + " through " + (i+size));
                SyncDataResponse response = new SyncDataResponse();

                List<Household> households = realm
                        .where(Household.class)
                        .findAll().subList(i, Math.min(i+size, householdCount));

                for (Household household : households) {
                    response.households.add(household.toMessage());
                }

                responses.add(response);
            }

        } catch (Exception e){
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            realm.close();
        }
        return responses;
    }

    public static List<SyncDataResponse> getParticipantsInChunks(int size){
        List<SyncDataResponse> responses = new ArrayList<>();

        Realm realm = Realm.getDefaultInstance();

        int householdCount = Long.valueOf(realm
                .where(StoredParticipant.class).count()).intValue();

        try {
            for (int i =0; i < householdCount ; i += size) {
                SyncDataResponse response = new SyncDataResponse();

                List<StoredParticipant> participants = realm
                        .where(StoredParticipant.class)
                        .findAll();

                for (StoredParticipant household : participants.subList(i, Math.min(i+size, householdCount))) {
                    response.participants.add(household.toMessage());
                }

                responses.add(response);
            }

        } catch (Exception e){
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            realm.close();
        }

        return responses;
    }

    private void prepareHouseholds(){
        Realm realm = Realm.getDefaultInstance();

        try {
            for (Household household : realm
                    .where(Household.class)
                    .findAll()){
                households.add(household.toMessage());
            }

        } catch (Exception e){
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            realm.close();
        }
    }
    private void prepareParticipants(){
        Realm realm = Realm.getDefaultInstance();

        try {
            for (StoredParticipant participant : realm
                    .where(StoredParticipant.class)
                    .findAll()){
                participants.add(participant.toMessage());
            }

        } catch (Exception e){
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            realm.close();
        }
    }
    public void prepareGroups(){
        Realm realm = Realm.getDefaultInstance();

        try {
            for (Group group : realm
                    .where(Group.class)
                    .findAll()){
                groups.add(group.toMessage());
            }

        } catch (Exception e){
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            realm.close();
        }
    }

    public void prepareSystemData(){
        SystemStorage storage = new SystemStorage();

        this.area = storage.getBaseArea();
        this.projects = storage.getProjects();
        this.users = storage.getUsersAsMapMessages();

    }

    public void prepareActivities(){
        Realm realm = Realm.getDefaultInstance();

        try {
            for (TrackedActivity activity : realm
                    .where(TrackedActivity.class)
                    .findAll()){
                activities.add(activity.toMessage());
            }
        } catch (Exception e){
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            realm.close();
        }
    }

    public void preparePhotos(){
        Realm realm = Realm.getDefaultInstance();

        try {
            for (Photo photo : realm
                    .where(Photo.class)
                    .findAll()){
                photos.add(photo.getBytes());
            }
        } catch (Exception e){
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            realm.close();
        }
    }


    public List<MapMessage> getHouseholds() {
        return households;
    }

    public void setHouseholds(List<MapMessage> households) {
        this.households = households;
    }

    public List<MapMessage> getGroups() {
        return groups;
    }

    public void setGroups(List<MapMessage> groups) {
        this.groups = groups;
    }

    public List<MapMessage> getActivities() {
        return activities;
    }

    public void setActivities(List<MapMessage> activities) {
        this.activities = activities;
    }

    public List<MapMessage> getParticipants() {

        return participants;
    }

    public void setParticipants(List<MapMessage> participants) {
        this.participants = participants;
    }

    public Boolean getSyncDone() {
        return syncDone;
    }

    public void setSyncDone(Boolean syncDone) {
        this.syncDone = syncDone;
    }

    public List<MapMessage> getUsers() {
        return users;
    }

    public void setUsers(List<MapMessage> users) {
        this.users = users;
    }

    public FHArea getArea() {
        return area;
    }

    public void setArea(FHArea area) {
        this.area = area;
    }

    public List<FHProject> getProjects() {
        return projects;
    }

    public void setProjects(List<FHProject> projects) {
        this.projects = projects;
    }

    public List<byte[]> getPhotos() {
        return photos;
    }

    public void setPhotos(List<byte[]> photos) {
        this.photos = photos;
    }
}
