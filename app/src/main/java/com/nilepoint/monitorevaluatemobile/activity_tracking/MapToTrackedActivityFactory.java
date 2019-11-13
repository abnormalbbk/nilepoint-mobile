package com.nilepoint.monitorevaluatemobile.activity_tracking;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.model.Area;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.TrackedActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import io.realm.Realm;

/**
 * Created by claudiatrafton on 6/18/17.
 */

public class MapToTrackedActivityFactory {

    //TODO this code will need to get changed once we get the Areas / ActivityModules / Categories etc populated.
    public static TrackedActivity mapFieldsToTrackedActivity(LinkedHashMap<String, String> map) {
        TrackedActivity trackedActivity = new TrackedActivity();
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            for (Map.Entry<String, String> entry : map.entrySet()) {
                switch (entry.getKey()) {
                    case "module.id":
                        trackedActivity.setModule(realm.where(PlannedActivity.class).equalTo("id", entry.getValue()).findFirst());
                        break;
                    case "cluster.id":
                        trackedActivity.setCluster(realm.where(Area.class).equalTo("id", entry.getValue()).findFirst());
                        break;
                    case "community.id":
                        trackedActivity.setCommunity(realm.where(Area.class).equalTo("id", entry.getValue()).findFirst());
                        break;
                    case "category.id":
                        break;
                    case "lesson.id":
                        trackedActivity.setLesson(realm.where(PlannedActivity.class).equalTo("id", entry.getValue()).findFirst());
                        break;
                    case "training.id":
                        trackedActivity.setTraining(realm.where(PlannedActivity.class).equalTo("id", entry.getValue()).findFirst());
                        break;
                    case "activityDate":
                        trackedActivity.setActivityDate(parseDate(entry.getValue()));
                }

            }
        } finally {
            if (realm != null){
                realm.close();
            }
        }

        return trackedActivity;
    }

    public static Date parseDate(String dateString){
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        Date date = null;
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        return date;
    }
}
