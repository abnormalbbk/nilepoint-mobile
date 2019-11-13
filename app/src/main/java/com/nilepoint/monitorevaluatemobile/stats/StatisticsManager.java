package com.nilepoint.monitorevaluatemobile.stats;

import android.util.Log;

import com.google.common.hash.Hashing;
import com.nilepoint.api.MobileDevice;
import com.nilepoint.concurrency.Task;
import com.nilepoint.model.Area;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.logging.RemoteLogger;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;
import io.realm.Realm;

/**
 * Created by ashaw on 8/24/17.
 *
 * Class that will serve as a facade to get all of the database statistics. Uses Paper to
 * store these.
 *
 */

public class StatisticsManager implements Serializable {

    public static String TAG = "StatisticsManager";

    private Date dateCreated = new Date();

    private Date lastContactWithMessageQueue;
    private Date lastContactWithPeer;

    private Long numberOfParticipants;
    private String participantDatabaseHash;

    private transient RemoteLogger logger = new RemoteLogger();

    private transient static Task collectStatisticsTask;

    public static void startTask(){
        collectStatisticsTask = new CollectStatisticsTask().scheduleEvery(120, TimeUnit.SECONDS);
    }

    public void store(){
        Paper.book().write("stats.db", this);
    }

    public static StatisticsManager getStatistics(){
        if (Paper.book().contains("stats.db")) {
            StatisticsManager manager = Paper.book().read("stats.db");

            manager.logger = new RemoteLogger();

            return manager;

        } else {
            return null;
        }
    }

    static class CollectStatisticsTask extends Task {

        private RemoteLogger logger = new RemoteLogger();

        @Override
        public void run() {
            StatisticsManager stats = StatisticsManager.getStatistics() != null
                    ? StatisticsManager.getStatistics()
                    : new StatisticsManager();

            Realm realm = null;
            try {
                realm = Realm.getDefaultInstance();

                stats.participantDatabaseHash = makeParticipantHash(realm);
                stats.numberOfParticipants = realm.where(StoredParticipant.class).count();

                MobileDevice device = Paper.book().read("device");

                Log.i(TAG, "Statistics collecting for " + device);

                device.setDatabaseHash(stats.participantDatabaseHash);
                device.setNumberOfParticipants(stats.numberOfParticipants);

                device.register();

                stats.store();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (realm != null){
                    realm.close();
                }
            }
        }

        private String makeParticipantHash(Realm realm){
            List<StoredParticipant> participantList = realm.where(StoredParticipant.class).findAllSorted("id");
            List<Area> areas = realm.where(Area.class).findAllSorted("id");

            StringBuilder allIdString = new StringBuilder();

            for ( StoredParticipant participant : participantList ){
                allIdString.append(participant.getId()).append(participant.getVersion());
            }

            for ( Area area : areas ){
                allIdString.append(area.getId());
            }

            String hash = Hashing.sha1()
                    .hashString(allIdString, Charset.defaultCharset())
                    .toString();

            Log.d(TAG, "Got Participant Hash: " + hash);

            return hash;
        }
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public StatisticsManager setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    public Date getLastContactWithMessageQueue() {
        return lastContactWithMessageQueue;
    }

    public StatisticsManager setLastContactWithMessageQueue(Date lastContactWithMessageQueue) {
        this.lastContactWithMessageQueue = lastContactWithMessageQueue;
        return this;
    }

    public String getParticipantDatabaseHash() {
        return participantDatabaseHash;
    }

    public StatisticsManager setParticipantDatabaseHash(String participantDatabaseHash) {
        this.participantDatabaseHash = participantDatabaseHash;
        return this;
    }

    public Date getLastContactWithPeer() {
        return lastContactWithPeer;
    }

    public StatisticsManager setLastContactWithPeer(Date lastContactWithPeer) {
        this.lastContactWithPeer = lastContactWithPeer;
        return this;
    }

    public static Task getCollectStatisticsTask() {
        return collectStatisticsTask;
    }

    public static void setCollectStatisticsTask(Task collectStatisticsTask) {
        StatisticsManager.collectStatisticsTask = collectStatisticsTask;
    }

    public Long getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public StatisticsManager setNumberOfParticipants(Long numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;

        return this;
    }
}
