package com.nilepoint.monitorevaluatemobile.services;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.dtn.Bundle;
import com.nilepoint.model.Group;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;

import java.util.Map;

import io.realm.Realm;

/**
 * Created by ashaw on 11/30/17.
 */

public class GroupService {
    public void updateOrAddGroup(final Bundle bundle, final MapMessage msg){
        Realm realm = Realm.getDefaultInstance();

        try {
            Group group = realm.where(Group.class).equalTo("id", msg.getId()).findFirst();
            if (group == null){
                group = new Group();
                realm.copyToRealm(group);
            }

            final Group fGroup = group;

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Map<String,String> map = msg.getMap();

                    fGroup.setCluster(map.get("cluster"));
                    fGroup.setCommunity(map.get("cluster"));
                    fGroup.setName(map.get("name"));
                    fGroup.setDescription(map.get("description"));
                    fGroup.setType(map.get("type"));

                    fGroup.getLeaders().clear();
                    fGroup.getMembers().clear();

                    for(Map.Entry<String,String> entry : map.entrySet()){
                        if (entry.getKey().startsWith("leader")){
                            StoredParticipant sp = realm.where(StoredParticipant.class).equalTo("id", entry.getValue()).findFirst();
                            if (sp != null && fGroup.getLeaders().where().equalTo("id", sp.getId()).findFirst() == null){
                                fGroup.getLeaders().add(sp);
                            }
                        }

                        if (entry.getKey().startsWith("member")){
                            StoredParticipant sp = realm.where(StoredParticipant.class).equalTo("id", entry.getValue()).findFirst();
                            if (sp != null && fGroup.getMembers().where().equalTo("id", sp.getId()).findFirst() == null){
                                fGroup.getMembers().add(sp);
                            }
                        }
                    }

                    WLTrackApp.dtnService.sendToAll(bundle);


                }
            });
        } finally {
            realm.close();
        }
    }
}
