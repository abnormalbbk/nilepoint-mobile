package com.nilepoint.monitorevaluatemobile.tracking;

import android.util.Log;

import com.nilepoint.model.Area;
import com.nilepoint.model.Distribution;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.Project;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.group.InfoPair;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by claudiatrafton on 9/4/17.
 */

public class TrackingUtils {
    public static final String ACTIVITY_ID = "activityId";
    public static final String MODULE = "AT-Module";
    public static final String TYPE_TRAINING = "AT-Training";
    public static final String LESSON = "AT-Lesson";
    public static final String TYPE_DIST = "AT-Distribution";

    public static final String ACTIVITY_TRACKING_FLAG = "ActivityTracking";

    /**
     * get all of the projects from the Realm and add it to the list
     * @param projects
     */
    public static void getAllProjects(final ArrayList<Project> projects){
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction(){
                @Override
                public void execute(Realm realm) {
                    RealmResults<Project> projectsResults = realm.where(Project.class)
                            .findAll();

                    for(Project p: projectsResults){
                        projects.add(p);
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
     * get the project based on its ID from reall
     * @param id
     * @return the project matched with the ID
     */
    public static Project getProjectById(final String id){
        Realm realm = null;
        Project project;
        try {
            realm = Realm.getDefaultInstance();
            project = realm.where(Project.class)
                    .beginsWith("id", id)
                    .findFirst();
        }
        finally {
            if (realm != null) {
                realm.close();
            }

        }
       return project;
    }

    public static PlannedActivity getPlannedActivityById(final String id){
        Realm realm = null;
        PlannedActivity plannedActivity;
        try {
            realm = Realm.getDefaultInstance();
            plannedActivity = realm.where(PlannedActivity.class)
                    .equalTo("id", id)
                    .findFirst();
        }
        finally {
            if (realm != null) {
                realm.close();
            }

        }
        return plannedActivity;
    }

    public static TrackedActivity getTrackedActivityById(final String id){
        Realm realm = null;
        TrackedActivity trackedActivity;
        try {
            realm = Realm.getDefaultInstance();
            trackedActivity = realm.where(TrackedActivity.class)
                    .equalTo("id", id)
                    .findFirst();
        }
        finally {
            if (realm != null) {
                realm.close();
            }

        }
        return trackedActivity;
    }

    public static Area getAreaByName(final String name){
        Realm realm = null;
        Area area;
        try {
            realm = Realm.getDefaultInstance();
            area = realm.where(Area.class)
                    .equalTo("name", name)
                    .findFirst();
        }
        finally {
            if (realm != null) {
                realm.close();
            }

        }
        return area;
    }

    /**
     * adds trainings to a separate list
     * @param project
     */
    public static List<TrackedActivity> getTrainingsForProject(Project project){
        Realm realm = null;

        List<TrackedActivity> activities;

        try {
            realm = Realm.getDefaultInstance();
            activities = realm.where(TrackedActivity.class)
                    .equalTo("project.id", project.getId())
                    .isNull("category.category")
                    .findAll();
        }
        finally {
            if (realm != null) {
                realm.close();
            }

        }
        return activities;
    }

    /**
     * adds distributions to a separate list
     * @param project
     */
    public static List<TrackedActivity> getDistributionsForProject(Project project){
        Realm realm = null;

        List<TrackedActivity> activities;

        try {
            realm = Realm.getDefaultInstance();
            activities = realm.where(TrackedActivity.class)
                    .equalTo("project.id", project.getId())
                    .isNotNull("category")
                    .findAll();
        }
        finally {
            if (realm != null) {
                realm.close();
            }

        }
        return activities;
    }



}
