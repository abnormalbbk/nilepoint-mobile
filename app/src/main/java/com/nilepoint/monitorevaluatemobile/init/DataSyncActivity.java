package com.nilepoint.monitorevaluatemobile.init;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nilepoint.amqp.AMQPConnection;
import com.nilepoint.api.MobileDevice;
import com.nilepoint.dtn.discovery.NeighborRegistration;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.model.Area;
import com.nilepoint.model.Group;
import com.nilepoint.model.Household;
import com.nilepoint.model.Photo;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.Project;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.MobileDeviceRegistry;
import com.nilepoint.monitorevaluatemobile.services.UpdateRoutingService;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import io.realm.Realm;

public class DataSyncActivity extends AppCompatActivity {

    boolean isInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_peer_init);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        RecyclerView peerList = (RecyclerView) findViewById(R.id.peer_list);

        if (WLTrackApp.dtnService.btlayer != null) {

            List<Node> nodes = new ArrayList<>();

            MobileDeviceRegistry mdRegistry = MobileDeviceRegistry.getInstance();

            // add nodes that are currently registered

            for (Node node : mdRegistry.nodeDeviceMap.keySet()) {
                NeighborRegistration registration = node.getLayer().getNeighborRegistry()
                        .findRegistration(node);

                if (registration != null) {
                    nodes.add(node);
                } else {
                    mdRegistry.untrack(node);
                }
            }

            PeerListAdapter adapter = new PeerListAdapter(this, nodes, this);


            peerList.setLayoutManager(new LinearLayoutManager(this));

            peerList.setAdapter(adapter);
        } else {
            finish();
            WLTrackApp.customToast(this, "Error: Bluetooth not available, please enable to sync.");
        }

        // counts

        final TextView pCount = (TextView) findViewById(R.id.participant_count_value);
        final TextView hCount = (TextView) findViewById(R.id.household_count_value);
        final TextView aCount = (TextView) findViewById(R.id.area_count_value);
        final TextView prCount = (TextView) findViewById(R.id.project_count_value);
        final TextView gCount = (TextView) findViewById(R.id.group_count_value);
        final TextView actCount = (TextView) findViewById(R.id.activities_count_value);
        final TextView actPartCount = (TextView) findViewById(R.id.p_activities_count_value);
        final TextView participantPhotoCount = (TextView) findViewById(R.id.participant_photo_count_value);
        final TextView activityPhotoCount = (TextView) findViewById(R.id.activity_photo_count_value);
        final TextView dtnConnectionStatus = (TextView) findViewById(R.id.dtn_status);
        if (Paper.book().contains("dtn.connectionStatus")) {
            dtnConnectionStatus.setText(Paper.book().read("dtn.connectionStatus", "DTN_Status_Unknown"));
        }


        Button clearButton = (Button) findViewById(R.id.clear_data_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Realm realm = Realm.getDefaultInstance();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(Household.class).findAll().deleteAllFromRealm();
                        realm.where(StoredParticipant.class).findAll().deleteAllFromRealm();
                        realm.where(PlannedActivity.class).findAll().deleteAllFromRealm();
                        realm.where(TrackedActivity.class).findAll().deleteAllFromRealm();
                        realm.where(Project.class).findAll().deleteAllFromRealm();
                        realm.where(User.class).findAll().deleteAllFromRealm();
                        realm.where(Area.class).findAll().deleteAllFromRealm();
                    }
                });

                pCount.setText("0");
                hCount.setText("0");
                aCount.setText("0");
                prCount.setText("0");
                gCount.setText("0");
                actCount.setText("0");
                actPartCount.setText("0");
                participantPhotoCount.setText("0");
                activityPhotoCount.setText("0");
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        final TextView pCount = (TextView) findViewById(R.id.participant_count_value);
        final TextView hCount = (TextView) findViewById(R.id.household_count_value);
        final TextView aCount = (TextView) findViewById(R.id.area_count_value);
        final TextView prCount = (TextView) findViewById(R.id.project_count_value);
        final TextView gCount = (TextView) findViewById(R.id.group_count_value);
        final TextView actCount = (TextView) findViewById(R.id.activities_count_value);
        final TextView actPartCount = (TextView) findViewById(R.id.p_activities_count_value);
        final TextView participantPhotoCount = (TextView) findViewById(R.id.participant_photo_count_value);
        final TextView activityPhotoCount = (TextView) findViewById(R.id.activity_photo_count_value);
        final TextView dtnConnectionStatus = (TextView) findViewById(R.id.dtn_status);

        Realm realm = Realm.getDefaultInstance();

        try {
            pCount.setText(String.valueOf(realm.where(StoredParticipant.class).count()));
            hCount.setText(String.valueOf(realm.where(Household.class).count()));
            aCount.setText(String.valueOf(realm.where(Area.class).count()));
            prCount.setText(String.valueOf(realm.where(Project.class).count()));
            gCount.setText(String.valueOf(realm.where(Group.class).count()));
            actCount.setText(String.valueOf(realm.where(PlannedActivity.class).count()));
            actPartCount.setText(String.valueOf(realm.where(TrackedActivity.class).count()));
            participantPhotoCount.setText(String.valueOf(realm.where(Photo.class).count()));
            if (Paper.book().contains("dtn.connectionStatus")) {
                dtnConnectionStatus.setText(Paper.book().read("dtn.connectionStatus", "DTN_Status_Unknown"));
            }

            int photoCount = 0;

            for (TrackedActivity ta : realm.where(TrackedActivity.class).isNotEmpty("photos").findAll()){
                photoCount += ta.getPhotos().size();
            }

            activityPhotoCount.setText(String.valueOf(photoCount));
        } finally {
            realm.close();
        }

    }
}
