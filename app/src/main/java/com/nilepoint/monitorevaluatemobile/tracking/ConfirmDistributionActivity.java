package com.nilepoint.monitorevaluatemobile.tracking;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.Distribution;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.PlannedDistribution;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

/**
 * Created by ashaw on 10/9/17.
 */

public class ConfirmDistributionActivity extends AppCompatActivity {


    RecyclerView distributionListRecyclerView;

    StoredParticipant participant;
    List<PlannedDistribution> distributions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_confirm_distribution);

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            ArrayList<String> distributionIds = getIntent().getStringArrayListExtra("distribution.ids");

            System.out.println("Distribution Ids" + distributionIds);

            final List<Distribution> dists = new ArrayList<Distribution>();

            for (String id : distributionIds){
                Distribution dist = realm.where(Distribution.class)
                        .equalTo("id", id)
                        .findFirst();

                if (dist != null){
                    System.out.println("Found distribution " + id);
                    dists.add(dist);
                } else {
                    System.out.println("Did not find distribution " + id);
                }
            }


            System.out.println("Got distributions: " + dists);

            for (final Distribution dist : dists){
                String participantId = dist.getParticipant().getId();

                if (participant== null){
                    participant = dist.getParticipant();
                }

                final PlannedDistribution distribution = realm.where(PlannedDistribution.class)
                        .equalTo("beneficiaryUuid", participantId)
                        .findFirst();

                if (distribution != null){
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            dist.setPlannedDistribution(distribution);
                        }
                    });

                    distributions.add(distribution);
                } else {

                    System.out.println("Planned activity: " + dist.getDistribution().getId());
                    System.out.println("Planned activity: " + dist.getDistribution().getPlannedDistributions());
                    System.out.println("Planned Details: " + realm.where(PlannedDistribution.class)
                            .isNull("beneficiaryUuid")
                            .equalTo("parent.id", dist.getDistribution().getId()).findAll());

                    final List<PlannedDistribution> pDists = realm.where(PlannedDistribution.class)
                            .isNull("beneficiaryUuid")
                            .equalTo("parent.id", dist.getDistribution().getId())
                            .findAll();

                    distributions.addAll(pDists);

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            if (!pDists.isEmpty()) {
                                dist.setPlannedDistribution(pDists.get(0));
                            }
                        }
                    });

                    /*WLTrackApp.customToast(this, "Could not find distribution information for "
                            + dist.getParticipant().getFirstName()
                            + " " + dist.getParticipant().getLastName() );*/
                }
            }

            TextView participantName = (TextView) findViewById(R.id.participant_profile_name);
            LinearLayout participantProfilePhotoContainer = (LinearLayout) findViewById(R.id.participant_profile_header);
            ImageView participantProfilePhoto = (ImageView) findViewById(R.id.participant_profile_picture);

            Button receiveButton = (Button) findViewById(R.id.receive_button);

            receiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // set status to completed or something

                    finish();
                }
            });

            MapMessage pMap = participant.toMessage();

            String name = pMap.get(FormKeyIDs.GIVEN_NAME_ID,"") + " " + pMap.get(FormKeyIDs.FATHER_NAME_ID,"");

            participantName.setText(name);

            TextView participantIdView = (TextView) findViewById(R.id.participant_profile_ID);

            participantIdView.setText(pMap.get(FormKeyIDs.PARTICIPANT_CODE,"Participant Code Pending"));

            distributionListRecyclerView = (RecyclerView) findViewById(R.id.distribution_list_recyclerView);
            distributionListRecyclerView.setAdapter(new DistributionAdapter(this, distributions));
            distributionListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        } finally {
            if (realm != null){
                realm.close();
            }
        }

    }
}
