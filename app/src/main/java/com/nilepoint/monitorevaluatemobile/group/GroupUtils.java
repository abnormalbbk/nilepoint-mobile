package com.nilepoint.monitorevaluatemobile.group;

import android.util.Log;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.Group;
import com.nilepoint.model.StoredParticipant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by claudiatrafton on 8/20/17.
 */

public class GroupUtils {

    /**
     * get the group by its name
     * @param name
     * @return the group with the name entered
     */
    public static Group getGroupByName(String name) {
        Realm realm = null;
        Group group;
        try {
            realm = Realm.getDefaultInstance();
            group = realm.where(Group.class)
                    .beginsWith("name", name)
                    .findFirst();

        } finally {
            if (realm != null) {
                realm.close();
            }
        }
        Log.d("GroupUtils", "Group found! Name is: " + group.getName());
        return group;
    }


    /**
     * get the group by its id
     * @param id
     * @return the group with the name entered
     */
    public static Group getGroupById(String id) {
        Realm realm = null;
        Group group;
        try {
            realm = Realm.getDefaultInstance();
            group = realm.where(Group.class)
                    .beginsWith("id", id)
                    .findFirst();

        } finally {
            if (realm != null) {
                realm.close();
            }
        }
        Log.d("GroupUtils", "Group found! Name is: " + group.getName());
        return group;
    }
    public static MapMessage getGroupMap(String id) {
        Realm realm = null;
        Group group;
        try {
            realm = Realm.getDefaultInstance();
            group = realm.where(Group.class)
                    .beginsWith("id", id)
                    .findFirst();

        } finally {
            if (realm != null) {
                realm.close();
            }
        }
        Log.d("GroupUtils", "Group found! Name is: " + group.getName());
        return group.toMessage();
    }

    /**
     * get all of the participants not in the current group
     * @param groupId
     * @return
     */
    public static ArrayList<StoredParticipant> getParticipantsNotInCurrentGroup(String groupId){
        Realm realm = null;
        ArrayList<StoredParticipant> participants = new ArrayList<>();
        Group group = getGroupById(groupId);
        RealmResults<StoredParticipant> res;
        try {
            realm = Realm.getDefaultInstance();
            res = realm.where(StoredParticipant.class)
                    .findAll();

        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        for(StoredParticipant sp: res) {
            if(!group.getMembers().contains(sp)){
                participants.add(sp);
            }
        }
        return participants;
    }

    /**
     * Adds a participant to a group
     * @param id group ID
     * @param sp the participant to add
     */
    public static void addParticipantToGroup(final String id, final StoredParticipant sp){
        Realm realm = null;
       try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction(){
                @Override
                public void execute(Realm realm) {
                    Group group = realm.where(Group.class)
                            .beginsWith("id", id)
                            .findFirst();

                    if (group != null){
                        group.addMember(sp);
                        Log.d("GroupUtils", sp.getFirstName() + "added to the group!");
                    }


                }
            });
        } finally {
           if (realm != null) {
               realm.close();
           }

       }


    }

    /**
     * add a list of storedParticipants to a Group at once
     * @param sps
     * @param id
     */
    public static void addMembersListToGroup(final List<StoredParticipant> sps, final String id){
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction(){
                @Override
                public void execute(Realm realm) {
                Group group = realm.where(Group.class)
                        .beginsWith("id", id)
                        .findFirst();

                if (group != null){
                    for(StoredParticipant sp: sps){
                        group.getMembers().add(sp);
                        Log.d("GroupUtils", sp.getFirstName() + "added to the group!");
                    }
                }
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }

        }


    }

    /**
     * removes a list of stored participants from the list of members
     * @param sps
     * @param id
     */
    public static void removeMembersListFromGroup(final List<StoredParticipant> sps, final String id){
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction(){
                @Override
                public void execute(Realm realm) {
                    Group group = realm.where(Group.class)
                            .beginsWith("id", id)
                            .findFirst();

                    if (group != null){
                        for(StoredParticipant sp: sps){
                            group.getMembers().remove(sp);
                            Log.d("GroupUtils", sp.getFirstName() + "removed from the group!");
                        }

                    }


                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }

        }


    }
    public static void removeLeadersListFromGroup(final List<StoredParticipant> sps, final String id){
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction(){
                @Override
                public void execute(Realm realm) {
                    Group group = realm.where(Group.class)
                            .beginsWith("id", id)
                            .findFirst();

                    if (group != null) {
                        for (StoredParticipant sp : sps) {
                            group.getLeaders().remove(sp);
                            Log.d("GroupUtils", sp.getFirstName() + "removed from the leaders!");
                        }

                    }
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }

        }


    }



    public static void removeParticipantFromGroup(final String id, final StoredParticipant sp){
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction(){
                @Override
                public void execute(Realm realm) {
                    Group group = realm.where(Group.class)
                            .beginsWith("id", id)
                            .findFirst();

                    if (group != null && group.getMembers().contains(sp)){
                        group.getMembers().remove(sp);
                        Log.d("GroupUtils", sp.getFirstName() + "added to the group!");
                    }

                    else {
                        Log.d("GroupUtils", "Cannot remove a participant from a list they dont belong to..." );
                    }


                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }

        }


    }

    /**
     * Searches an arraylist for participants by name
     * @param list
     * @param str
     * @return
     */
    public static ArrayList<StoredParticipant> searchParticipantsByName(List<StoredParticipant> list, String str){
        ArrayList<StoredParticipant> results = new ArrayList<>();
        for(StoredParticipant sp: list){
            if(sp.getFirstName().contains(str) || sp.getLastName().contains(str) || sp.getId().contains(str))
                results.add(sp);
        }
        return results;
    }



    /**
     * Set the information to be displayed in the about tab
     * TODO set the values to be coming from the group model
     */
    public static void setGroupInfo(ArrayList<InfoPair> list, Group group){
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

        list.add(new InfoPair("Group type", group.getType().replaceAll("GT-", "")));
        list.add(new InfoPair("Cluster", group.getCluster()));
        list.add(new InfoPair("Community", group.getCommunity()));
        if (group.getBeginDate() != null) {
            list.add(new InfoPair("Start date", fmt.format(group.getBeginDate())));
        }
        if (group.getEndDate() != null) {
            list.add(new InfoPair("End date", fmt.format(group.getEndDate())));
        }
    }

    /**
     * gets an arraylist of participants from the Realm List
     * @param realmList
     * @return all of the members stored in an array list
     */
    public static ArrayList<StoredParticipant> membersToArrayList(RealmList<StoredParticipant> realmList){
        ArrayList<StoredParticipant> list = new ArrayList<>();
        for(StoredParticipant sp: realmList){
            list.add(sp);
            Log.d("GroupUtils", sp.getFirstName() + "added to the list!");
        }
        return list;
    }

}
