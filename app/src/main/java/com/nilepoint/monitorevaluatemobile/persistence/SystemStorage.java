package com.nilepoint.monitorevaluatemobile.persistence;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.fh.bridge.model.FHArea;
import com.nilepoint.fh.bridge.model.FHProject;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.User;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import io.realm.Realm;

/**
 * Created by ashaw on 11/21/17.
 *
 * Helper class for system objects that are stored (they may be in realm or Paper).
 *
 */

public class SystemStorage {
    public List<MapMessage> getUsersAsMapMessages(){
        List<MapMessage> messages = new ArrayList<MapMessage>();

        Realm realm = null;

        try {

            realm = Realm.getDefaultInstance();

            for (User user : realm.where(User.class).findAll()) {
                messages.add(user.toMessage());
            }

        } finally {
            if (realm != null){
                realm.close();
            }
        }

        return messages;
    }

    public List<FHProject> getProjects(){
        return Paper.book().read("projects.fh");
    }

    public FHArea getBaseArea(){
       return (FHArea) Paper.book().read("area.fh");
    }
}
