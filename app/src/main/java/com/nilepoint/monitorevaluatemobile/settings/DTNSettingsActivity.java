package com.nilepoint.monitorevaluatemobile.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nilepoint.exchange.model.Participant;
import com.nilepoint.model.Area;
import com.nilepoint.model.Group;
import com.nilepoint.model.Household;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.Project;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.stats.StatisticsManager;

import io.paperdb.Book;
import io.paperdb.Paper;
import io.realm.Realm;

/**
 * Created by ashaw on 6/18/17.
 */

public class DTNSettingsActivity extends AppCompatActivity {

    private SettingsStorage storage = new SettingsStorage();

    Boolean bluetoothOn = false;
    Boolean tcpOn = false;
    Boolean amqpOn = false;

    ToggleButton amqpButton;
    ToggleButton bluetoothButton;
    ToggleButton tcpButton;

    Button amqpSendButton;

    Button btSendButton;

    TextView dbHashValue;

    TextView lastContactServer;
    TextView lastContactPeer;

    public final static String TCP_LAYER_ACTIVE_LABEL = "dtn.tcp.layer.active";
    public final static String AMQP_LAYER_ACTIVE_LABEL = "dtn.amqp.layer.active";
    public final static String BT_LAYER_ACTIVE_LABEL = "dtn.bt.layer.active";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Book book = Paper.book();

        setContentView(R.layout.activity_dtn_settings);

        amqpButton = (ToggleButton) findViewById(R.id.amqp_toggle_slider);
        bluetoothButton = (ToggleButton) findViewById(R.id.bt_toggle_slider);
        tcpButton = (ToggleButton) findViewById(R.id.tcp_toggle_slider);

        /*amqpSendButton = (Button) findViewById(R.id.amqp_send_all);

        btSendButton = (Button) findViewById(R.id.bt_send_all);

        dbHashValue = (TextView) findViewById(R.id.db_hash_value);*/
        lastContactServer = (TextView) findViewById(R.id.last_contact_amqp_value);
        lastContactPeer = (TextView) findViewById(R.id.last_contact_peer_value);


        Boolean tcpEnabled = book.read(TCP_LAYER_ACTIVE_LABEL);
        Boolean btEnabled = book.read(BT_LAYER_ACTIVE_LABEL);
        Boolean amqpEnabled = book.read(AMQP_LAYER_ACTIVE_LABEL);

        tcpButton.setChecked(tcpEnabled);
        bluetoothButton.setChecked(btEnabled);
        amqpButton.setChecked(amqpEnabled);

        tcpButton.setOnCheckedChangeListener(new ToggleSettingListener(TCP_LAYER_ACTIVE_LABEL));
        bluetoothButton.setOnCheckedChangeListener(new ToggleSettingListener(BT_LAYER_ACTIVE_LABEL));
        amqpButton.setOnCheckedChangeListener(new ToggleSettingListener(AMQP_LAYER_ACTIVE_LABEL));

        amqpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (com.nilepoint.dtn.Bundle b : WLTrackApp.dtnService.getDTN().getDatabase().getAllBundles()) {
                    WLTrackApp.dtnService.amqpConvergenceLayer.sendToAllNeighbors(b);
                }
            }
        });

        amqpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (com.nilepoint.dtn.Bundle b : WLTrackApp.dtnService.getDTN().getDatabase().getAllBundles()) {
                    WLTrackApp.dtnService.btlayer.sendToAllNeighbors(b);
                }
            }
        });

        StatisticsManager stats = StatisticsManager.getStatistics();

        if (stats != null){
//            dbHashValue.setText(stats.getParticipantDatabaseHash());
            if (stats.getLastContactWithMessageQueue() != null) {
                lastContactServer.setText(stats.getLastContactWithMessageQueue().toString());
            } else {
                lastContactServer.setText("Never");
            }
            if (stats.getLastContactWithPeer() != null) {
                lastContactPeer.setText(stats.getLastContactWithPeer().toString());
            } else {
                lastContactPeer.setText("Never");
            }

        }

        TextView pCount = (TextView) findViewById(R.id.participant_count_value);
        TextView hCount = (TextView) findViewById(R.id.household_count_value);
        TextView aCount = (TextView) findViewById(R.id.area_count_value);
        TextView prCount = (TextView) findViewById(R.id.project_count_value);
        TextView gCount = (TextView) findViewById(R.id.group_count_value);
        TextView actCount = (TextView) findViewById(R.id.activities_count_value);

        Realm realm = Realm.getDefaultInstance();
        try {
            pCount.setText(String.valueOf(realm.where(StoredParticipant.class).count()));
            hCount.setText(String.valueOf(realm.where(Household.class).count()));
            aCount.setText(String.valueOf(realm.where(Area.class).count()));
            prCount.setText(String.valueOf(realm.where(Project.class).count()));
            gCount.setText(String.valueOf(realm.where(Group.class).count()));
            actCount.setText(String.valueOf(realm.where(PlannedActivity.class).count()));
        } finally {
            realm.close();
        }
    }

    class ToggleSettingListener implements CompoundButton.OnCheckedChangeListener {
        String settingNamespace;

        public ToggleSettingListener(String settingNamespace) {
            this.settingNamespace = settingNamespace;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            Paper.book().write(settingNamespace, b);
        }
    }


}
