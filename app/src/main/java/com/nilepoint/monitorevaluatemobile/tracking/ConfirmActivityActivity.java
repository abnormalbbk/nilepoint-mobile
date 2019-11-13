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

import com.nilepoint.model.Distribution;
import com.nilepoint.model.PlannedDistribution;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.ParticipantListAdapater;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

/**
 * Created by ashaw on 10/9/17.
 */

public class ConfirmActivityActivity extends AppCompatActivity {
    List<StoredParticipant> participants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_confirm_activity);

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            TrackedActivity activity = realm.where(TrackedActivity.class)
                    .equalTo("id", getIntent().getStringExtra("activity.id"))
                    .findFirst();


            participants = activity.getParticipantList();

            TextView projectName = (TextView) findViewById(R.id.project_name);
            TextView moduleName = (TextView) findViewById(R.id.module_name);
            TextView lessonName = (TextView) findViewById(R.id.lesson_name);
            TextView trainingName = (TextView) findViewById(R.id.training_name);
            TextView communityName = (TextView) findViewById(R.id.value_community);
            TextView clusterName = (TextView) findViewById(R.id.value_cluster);

            if (activity.getProject() != null) {
                projectName.setText(activity.getProject().getName());
            }
            if (activity.getModule() != null){
                moduleName.setText(activity.getModule().getName());
            }

            if (activity.getLesson() != null){
                lessonName.setText(activity.getLesson().getName());
            }

            if (activity.getTraining() != null){
                trainingName.setText(activity.getTraining().getName());
            }

            if (activity.getCluster() != null){
                clusterName.setText(activity.getCluster().getName());
            }

            if (activity.getCommunity() != null){
                communityName.setText(activity.getCommunity().getName());
            }


            Button receiveButton = (Button) findViewById(R.id.confirm_button);

            receiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // set status to completed or something



                    finish();
                }
            });

            RecyclerView distributionListRecyclerView = (RecyclerView) findViewById(R.id.participant_list_recyclerView);
            distributionListRecyclerView.setAdapter(new ParticipantListAdapater(participants, this, false));
            distributionListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }
}
