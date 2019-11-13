package com.nilepoint.monitorevaluatemobile.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.nilepoint.amqp.AMQPConfiguration;
import com.nilepoint.amqp.AMQPConnection;
import com.nilepoint.amqp.MessageListener;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.dtn.Bundle;
import com.nilepoint.dtn.EndpointId;
import com.nilepoint.fh.bridge.model.FHGroup;
import com.nilepoint.formbuilder.FormUpdateListener;
import com.nilepoint.model.Form;
import com.nilepoint.model.Group;
import com.nilepoint.model.Household;
import com.nilepoint.model.HouseholdRelationship;
import com.nilepoint.model.Photo;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.Project;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.settings.DTNSettingsActivity;
import com.nilepoint.monitorevaluatemobile.settings.SettingsMixin;
import com.nilepoint.monitorevaluatemobile.stats.StatisticsManager;
import com.nilepoint.persistence.Datastore;
import com.nilepoint.serialization.ProtostuffManager;

import org.bouncycastle.util.Store;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.paperdb.Paper;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Class that waits on the MQ for new participants.
 */

public class UpdateRoutingService implements MessageListener<MapMessage> {

    static final String TAG = "ParticipantUpdateSvc";
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
    private AMQPConnection connection;
    private ProtostuffManager<Form> psManager;
    private List<FormUpdateListener> listeners = new ArrayList();
    private Context context;
    private Realm realm;
    private Datastore data;
    private ParticipantService participantService = WLTrackApp.participantService;
    private HouseholdService householdService = WLTrackApp.householdService;


    public UpdateRoutingService(Context context, AMQPConfiguration<MapMessage> configuration) {
        this.context = context;

        data = Datastore.init(context);

        realm = Realm.getDefaultInstance();

        String deviceId = Paper.book().read("device.id");

        configuration.topic("device." + deviceId)
                .ack(false);

        this.connection = configuration.connection();

        this.connection.setConsumer(true);

        this.connection.listener(this);

        this.queueConnect();
    }

    private StoredParticipant getParticipantById(String id){
        realm = Realm.getDefaultInstance();
        return realm.where(StoredParticipant.class).equalTo("id", id).findFirst();
    }

    @Override
    public void messageReceived(final MapMessage mapMessage) {
        //Log.d(TAG,"Got update from particpant / bundle update queue: " + mapMessage);
        System.out.println("Got update from particpant / bundle update queue: " + mapMessage.getMap());

        Bundle bundle = new Bundle(new EndpointId("dtn://MQ"),
                new EndpointId("dtn://NOT-MQ"),
                mapMessage);

        StatisticsManager
                .getStatistics()
                .setLastContactWithMessageQueue(new Date()).store();

        Boolean amqpActive = Paper.book().read(DTNSettingsActivity.AMQP_LAYER_ACTIVE_LABEL);

        final StoredParticipant participant = getParticipantById(mapMessage.get("uuid"));

        if (participant == null ){
            Log.i(TAG, "No participant with id="+mapMessage.getId() + " nothing to update");
        }else {
            Log.i(TAG, "Updating Participant: " + participant);
            data.updateParticipant(mapMessage);
        }

        if (amqpActive) {

            if ("Participant".equals(mapMessage.getMap().get("className"))) {
                participantService.addParticipantEvent(new UpdateEvent(UpdateEventType.UPDATE, bundle));
            }

            if ("Household".equals(mapMessage.getMap().get("className"))) {
                Log.d(TAG,"Updating household from AMQP");

                householdService.addHouseholdEvent(new UpdateEvent(UpdateEventType.UPDATE, bundle));
            }

            if ("Command".equals(mapMessage.getMap().get("className"))){
                if (mapMessage.getMap().get("command") != null
                        && mapMessage.getMap().get("command").equals("KILLSWITCH")){
                    Realm realm = null;
                    try {
                        realm = Realm.getDefaultInstance();

                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.where(Group.class).findAll().deleteAllFromRealm();
                                realm.where(Project.class).findAll().deleteAllFromRealm();
                                realm.where(PlannedActivity.class).findAll().deleteAllFromRealm();
                                realm.where(StoredParticipant.class).findAll().deleteAllFromRealm();
                                realm.where(HouseholdRelationship.class).findAll().deleteAllFromRealm();
                                realm.where(Household.class).findAll().deleteAllFromRealm();
                                realm.where(Photo.class).findAll().deleteAllFromRealm();
                            }
                        });
                    } finally {
                        if (realm != null){
                            realm.close();
                        }
                    }
                }
            }
        }

    }

    public void queueConnect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isNetworkAvailable()){
                    Paper.book().write("dtn.connectionStatus", "No Network Available");
                    try {
                        Thread.sleep(5000);
                    } catch (Exception ex){

                    }

                    queueConnect();
                    return;
                }
                try {
                    connection.connect();
                } catch (Throwable e){
                    try {
                        Thread.sleep(5000);
                    } catch (Exception ex){

                    }
                    queueConnect();
                }
                Paper.book().write("dtn.connectionStatus",connection.debugConnectionStatus());
            }

        }).start();
    }

    private boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager)this.context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
