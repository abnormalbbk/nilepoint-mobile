package com.nilepoint.monitorevaluatemobile.services;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.dtn.Bundle;
import com.nilepoint.model.Area;
import com.nilepoint.model.Group;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.Project;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.utils.DateUtilities;

import java.text.SimpleDateFormat;
import java.util.Map;

import io.realm.Realm;

/**
 * Created by ashaw on 11/30/17.
 */

public class ActivityService {
    public void updateOrAddActivity(final Bundle bundle, final MapMessage msg){
        Realm realm = Realm.getDefaultInstance();

        System.out.println("Got update/add activity");

        try {

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    TrackedActivity activity = realm.where(TrackedActivity.class).equalTo("id", msg.getId()).findFirst();
                    if (activity == null){
                        System.out.println("Got new activity");
                        activity = new TrackedActivity();
                        activity.setId(msg.getId());
                        activity = realm.copyToRealm(activity);
                    }

                    Map<String,String> map = msg.getMap();
                    System.out.println("Got map " + map);
                    SimpleDateFormat sdf = new SimpleDateFormat(DateUtilities.DATE_AND_TIME_FORMAT);

                    Area cluster = realm.where(Area.class).equalTo("id", map.get("clusterId")).findFirst();
                    Area community = realm.where(Area.class).equalTo("id", map.get("communityId")).findFirst();
                    Project project = realm.where(Project.class).equalTo("id", map.get("projectId")).findFirst();

                    PlannedActivity category = realm.where(PlannedActivity.class).equalTo("id", map.get("categoryId")).findFirst();
                    PlannedActivity lesson = realm.where(PlannedActivity.class).equalTo("id", map.get("lessonId")).findFirst();
                    PlannedActivity module = realm.where(PlannedActivity.class).equalTo("id", map.get("moduleId")).findFirst();
                    PlannedActivity training = realm.where(PlannedActivity.class).equalTo("id", map.get("trainingId")).findFirst();
                    System.out.println(String.format("category: %s lesson: %s, module: %s, training: %s", category, lesson, module, training));

                    activity.setCluster(cluster);
                    activity.setCommunity(community);
                    activity.setProject(project);
                    activity.setLesson(lesson);
                    activity.setModule(module);
                    activity.setTraining(training);
                    activity.setCategory(category);
                    activity.setStatus(map.get("status"));

                    try {
                        activity.setActivityDate(sdf.parse(map.get("activityDate")));
                    } catch (Exception e){
                        Crashlytics.logException(e);
                        e.printStackTrace();
                    }

                    activity.getParticipantList().clear();

                    for(Map.Entry<String,String> entry : map.entrySet()){

                        if (entry.getKey().startsWith("participants")){
                            StoredParticipant sp = realm.where(StoredParticipant.class).equalTo("id", entry.getValue()).findFirst();
                            if (sp != null && activity.getParticipantList().where().equalTo("id", sp.getId()).findFirst() == null){
                                activity.getParticipantList().add(sp);
                            }
                        }
                    }

                    System.out.println("Created " + activity);

                    WLTrackApp.dtnService.sendToAll(bundle);
                }
            });
        } finally {
            realm.close();
        }
    }
}
