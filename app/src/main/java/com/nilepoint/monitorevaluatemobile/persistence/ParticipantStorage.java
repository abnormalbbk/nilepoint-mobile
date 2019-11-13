package com.nilepoint.monitorevaluatemobile.persistence;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.Household;
import com.nilepoint.model.StoredParticipant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

/**
 * Created by ashaw on 11/21/17.
 *
 * DAO class for participants.
 */

public class ParticipantStorage {
    public Map<String, Integer> getAllParticipantIdsAndVersions(){
        Map<String, Integer> participantMap = new HashMap<>();

        Realm realm = null;

        try {

            realm = Realm.getDefaultInstance();

            for (StoredParticipant participant : realm.where(StoredParticipant.class).findAll()) {
                participantMap.put(participant.getId(), participant.getVersion());
            }

        } finally {
            if (realm != null){
                realm.close();
            }
        }

        return participantMap;
    }

    public List<StoredParticipant> getAllParticipants(){
        List<StoredParticipant> participantMap = new ArrayList<>();

        Realm realm = null;

        try {

            realm = Realm.getDefaultInstance();

            for (StoredParticipant participant : realm.where(StoredParticipant.class).findAll()) {
                participantMap.add(participant);
            }

        } finally {
            if (realm != null){
                realm.close();
            }
        }

        return participantMap;
    }

    public List<StoredParticipant> getAllParticipants(Map<String,Integer> excluding){
        List<StoredParticipant> participantMap = new ArrayList<>();

        Realm realm = null;

        try {

            realm = Realm.getDefaultInstance();

            for (StoredParticipant participant : realm.where(StoredParticipant.class).findAll()) {
                Integer version = excluding.get(participant.getId());
                // new version
                if (version == null || participant.getVersion() > version) {
                    participantMap.add(participant);
                }
            }

        } finally {
            if (realm != null){
                realm.close();
            }
        }

        return participantMap;
    }

    public List<MapMessage> getAllParticipantsAsMapMessage(Map<String,Integer> excluding){
        List<MapMessage> participantMap = new ArrayList<>();

        Realm realm = null;

        try {

            realm = Realm.getDefaultInstance();

            for (StoredParticipant participant : realm.where(StoredParticipant.class).findAll()) {
                Integer version = excluding.get(participant.getId());
                // new version
                if (version == null || participant.getVersion() > version) {
                    participantMap.add(participant.toMessage());
                }
            }

        } finally {
            if (realm != null){
                realm.close();
            }
        }

        return participantMap;
    }

    public List<MapMessage> getHouseholdsForParticipantIds(List<String> ids){
        List<MapMessage> hhMaps = new ArrayList<>();

        Realm realm = null;

        try {

            realm = Realm.getDefaultInstance();

            for (Household hh : realm.where(Household.class)
                    .in("headOfHousehold", ids.toArray(new String[]{}))
                    .or()
                    .in("members.id", ids.toArray(new String[]{}))
                    .findAll()) {

                // new version
                hhMaps.add(hh.toMessage());
            }

        }  catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (realm != null){
                realm.close();
            }
        }

        return hhMaps;

    }

    public long getParticipantCount(){
        Realm realm = null;

        try {

            realm = Realm.getDefaultInstance();

           return realm.where(StoredParticipant.class).count();

        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }
    public long getHouseholdCount(){
        Realm realm = null;

        try {

            realm = Realm.getDefaultInstance();

            return realm.where(Household.class).count();

        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }
}
