package com.nilepoint.monitorevaluatemobile.integration;

import android.util.Log;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.fh.bridge.model.FHActivity;
import com.nilepoint.fh.bridge.model.FHActivityDetail;
import com.nilepoint.fh.bridge.model.FHAddress;
import com.nilepoint.fh.bridge.model.FHArea;
import com.nilepoint.fh.bridge.model.FHAreaType;
import com.nilepoint.fh.bridge.model.FHGroup;
import com.nilepoint.fh.bridge.model.FHGroupRole;
import com.nilepoint.fh.bridge.model.FHHousehold;
import com.nilepoint.fh.bridge.model.FHHouseholdRelationship;
import com.nilepoint.fh.bridge.model.FHParticipant;
import com.nilepoint.fh.bridge.model.FHPlannedActivity;
import com.nilepoint.fh.bridge.model.FHProject;
import com.nilepoint.model.Area;
import com.nilepoint.model.Group;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.PlannedDistribution;
import com.nilepoint.model.Project;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.realm.Realm;

/**
 * Created by ashaw on 11/21/17.
 */

public class SystemDataProcessor {
    Map<String, PlannedActivity> activityCache = new HashMap<String, PlannedActivity>();

    public Integer processGroups(Realm realm, final Collection<FHGroup> groups){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Group.class).findAll().deleteAllFromRealm();

                for (FHGroup group : groups){
                    Group g = new Group();

                    g.setBeginDate(group.getStartDate());
                    g.setEndDate(group.getEndDate());

                    if (group.getAreaId() != null) {
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

                    System.out.println("Persisting Group " + g.getId());

                    for (FHGroupRole gr : group.getMembers()) {

                        FHParticipant participant = gr.getParticipant();

                        try {
                            MapMessage message = new MapMessage();

                            message.setId(participant.getUuid());

                            StoredParticipant stored = realm.where(StoredParticipant.class)
                                    .equalTo("externalId", participant.getParticipantCode()).findFirst();

                            message.put(FormKeyIDs.GIVEN_NAME_ID, participant.getFirstName());
                            message.put(FormKeyIDs.FATHER_NAME_ID, participant.getLastName());
                            message.put(FormKeyIDs.PREFERRED_NAME_ID, participant.getNickname());
                            message.put(FormKeyIDs.PHONE_NUMBER_ID, participant.getTelephoneNumber());
                            message.put(FormKeyIDs.PREFERRED_NAME_ID, participant.getNickname());
                            message.put(FormKeyIDs.GENDER_ID, participant.getGender());
                            message.put(FormKeyIDs.PARTICIPANT_CODE, participant.getParticipantCode());


                            for (FHHouseholdRelationship householdRelationship : participant.getHouseholds()) {
                                FHHousehold household = householdRelationship.getHousehold();

                                for (FHAddress address : household.getAddresses()) {
                                    if (address.getCommunity() != null) {
                                        message.put(FormKeyIDs.CLUSTER_ID, address.getCommunity());
                                    }
                                    if (address.getCluster() != null) {
                                        message.put(FormKeyIDs.COMMUNITY_ID, address.getCluster());
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

                            //log.debug(TAG,"OAuth: Added participant to group (" + group.getName() + ") " + message.getMap());

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

        return groups.size();
    }

    public Long processBaseArea(Realm realm, final FHArea fharea) throws Exception {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // get rid of all of our areas

                try {
                    realm.where(Area.class).findAll().deleteAllFromRealm();
                    Area area = processArea(null, fharea);

                    realm.copyToRealm(area);
                } catch (Exception e) {
                    e.printStackTrace();
                    ;
                }
            }
        });

        return 0L;
    }

    private Area processArea(Area parent, FHArea area) {
        Area realmArea = new Area();

        realmArea.setId(area.getId().toString());
        realmArea.setType(area.getTypeName().name());
        realmArea.setName(area.getAreaName());

        if (parent != null){
            realmArea.setParent(parent);
        }


        if (area.getChildren() != null && area.getChildren().size() > 0) {
            for (FHArea subArea : area.getChildren()) {
                realmArea.getChildren().add(processArea(realmArea, subArea));
            }
        }

        return realmArea;
    }

    public Long processProjects(final Collection<FHProject> projects) throws Exception {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    try {
                        realm.where(Project.class).findAll().deleteAllFromRealm();
                        realm.where(PlannedActivity.class).findAll().deleteAllFromRealm();
                        realm.where(PlannedDistribution.class).findAll().deleteAllFromRealm();

                        for (FHProject project : projects) {
                            Project realmProject = processProject(realm, project);

                            realm.copyToRealm(realmProject);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        // log.error(TAG, "Exception thrown attempting to populate projects", e);
                    }
                }
            });

            return realm.where(PlannedActivity.class).count();

        } catch (Exception e) {
            throw e;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Convert to a realm area and persist.
     * //@param project
     *
     * @return
     */
    private Project processProject(Realm realm, FHProject project) {
        Project realmProject = new Project();

        realmProject.setId(project.getId());
        realmProject.setName(project.getName() == null ? "Project " + project.getId() : project.getName());

        if (project.getPlannedActivities().size() > 0) {
            for (FHPlannedActivity plannedActivity : project.getPlannedActivities()) {
                PlannedActivity pa = processPlannedActivity(realm, null, plannedActivity,0);

                realmProject.getActivities().add(pa);
            }

        }

        return realmProject;
    }

    private void processActivity (Realm realm, PlannedActivity realmActivity, FHActivity activity){
        if (activity.getActivityDetails() != null && !activity.getActivityDetails().isEmpty()) {
            for (FHActivityDetail detail : activity.getActivityDetails()) {

                if (activity.getPlannedActivity().getType() != null && activity.getPlannedActivity().getType().equals("AT-Distribution")){

                    if (detail.getBeneficiary() != null) {
                        PlannedDistribution dist = new PlannedDistribution();

                        dist.setType(detail.getType());
                        dist.setBeneficiaryUuid(detail.getBeneficiary().getUuid());
                        dist.setParent(activityCache.get(activity.getPlannedActivity().getId()));
                        dist.setQuantity(detail.getQuantity());
                        // do lookup instead of raw code
                        dist.setUnit(detail.getUnitCode());

                        realmActivity.getPlannedDistributions().add(dist);
                    }
                }
            }

        }


    }
    private PlannedActivity processPlannedActivity(Realm realm, FHPlannedActivity parent, FHPlannedActivity activity, int level) {
        PlannedActivity realmActivity = new PlannedActivity();

        realmActivity.setId(activity.getId());
        realmActivity.setName(activity.getName());
        realmActivity.setType(activity.getType());
        realmActivity.setProjectId(activity.getProjectId());
        realmActivity.setCategory(activity.getCategory());
        realmActivity.setLevelCode(activity.getLevelCode());

        if (activity.getActivities() != null && !activity.getActivities().isEmpty()) {
            for (FHActivity childActivity : activity.getActivities()) {
                //processPlannedActivity(realm, activity, pa ,level+1);

                processActivity(realm, realmActivity, childActivity);
                if (childActivity.getPlannedActivity().getPlannedDetails() != null
                        && realmActivity.getPlannedDistributions().isEmpty()) {
                    for (FHActivityDetail detail : childActivity.getPlannedActivity().getPlannedDetails()) {
                        System.out.println("Got detail " + detail + " for planned activity " + activity.getId());
                        PlannedDistribution dist = new PlannedDistribution();

                        dist.setType(detail.getType());
                        dist.setParent(realmActivity);
                        dist.setQuantity(detail.getQuantity());
                        // do lookup instead of raw code
                        dist.setUnit(detail.getUnitCode());

                        realmActivity.getPlannedDistributions().add(dist);
                    }
                }
                if (childActivity.getArea() != null) {
                    System.out.println("Adding area " + childActivity.getArea().getAreaName());

                    Area area = realm.where(Area.class).equalTo("id", childActivity.getAreaId()).findFirst();

                    if (area != null) {
                        realmActivity.getAreas().add(area);

                        if (FHAreaType.Community.equals(childActivity.getArea().getTypeName())) {
                            // add the cluster as well
                            Area cluster = realm.where(Area.class).equalTo("name", childActivity.getArea()
                                    .getCluster().getAreaName()).findFirst();

                            if (!realmActivity.getAreas().contains(cluster)) {
                                realmActivity.getAreas().add(cluster);
                            }
                        }
                    }


                }
            }
        }

        if (activity.getParentId() != null) {
            System.out.println("Activity parent: " + activity.getParentId());
            PlannedActivity p = activityCache.get(activity.getParentId());

            p.getChildren().add(realmActivity);
        }

        activityCache.put(activity.getId(), realmActivity);

        return realmActivity;
    }
}
