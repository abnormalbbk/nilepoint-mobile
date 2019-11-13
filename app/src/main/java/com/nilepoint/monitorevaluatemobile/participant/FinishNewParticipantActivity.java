package com.nilepoint.monitorevaluatemobile.participant;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.FormElement;
import com.nilepoint.model.FormElementType;
import com.nilepoint.model.Household;
import com.nilepoint.model.HouseholdRelationship;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.forms.FormDataConverter;
import com.nilepoint.monitorevaluatemobile.HomeActivity;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.ParticipantListAdapater;
import com.nilepoint.monitorevaluatemobile.forms.FormIntent;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;
import com.nilepoint.persistence.Datastore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;

public class FinishNewParticipantActivity extends AppCompatActivity {

    public static final String TAG = "ReviewNewParticipant";

    //indices for certain contents of the user info
    public static final int infoStart = 3;
    public static final FormElement FIRST_NAME_KEY = new FormElement(FormElementType.TEXT, "firstName", "");
    public static final FormElement LAST_NAME_KEY = new FormElement(FormElementType.TEXT, "lastName", "");

    private MapMessage msg;
    private Datastore ds = Datastore.init(getBaseContext());

    //Using a recyclerview here since it will update with n amout of new household members
    private RecyclerView newParticipantRecycler;
    protected RecyclerView.LayoutManager layoutManager;
    protected RecyclerView.Adapter adapter;

    private Button done;
    private LinearLayout addHouseholdMember;

    private List<StoredParticipant> householdMembers;
    private String headOfHouseholdId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_new_participant);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Complete");

        //get key val pairs from ParticipantHouseholdFormActivity
        final Intent intent = getIntent();

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();


            Map<String, String> infoMap = (Map<String, String>) intent.getSerializableExtra("filled_form");

            headOfHouseholdId = intent.getStringExtra("headOfHouseholdId");

            Household hh = ds.findHouseholdForHeadOfHousehold(headOfHouseholdId);

            FormDataConverter converter = new FormDataConverter();

            msg = converter.toMapMessage(infoMap);

            newParticipantRecycler = (RecyclerView) findViewById(R.id.review_participant_list_recycler);

            layoutManager = new LinearLayoutManager(this);

            householdMembers = new ArrayList<>();

            householdMembers.add(getParticipantById(hh.getHeadOfHousehold()));

            for (HouseholdRelationship r : hh.getMembers()) {
                StoredParticipant p = realm.where(StoredParticipant.class)
                        .equalTo("id",r.getParticipantId()).findFirst();

                if (r.getParticipant() != null) {
                    householdMembers.add(p);
                }
            }

            adapter = new ParticipantListAdapater(householdMembers, this, false);
            newParticipantRecycler.setAdapter(adapter);
            newParticipantRecycler.setLayoutManager(layoutManager);

            //add new person
            addHouseholdMember = (LinearLayout) findViewById(R.id.add_member_ll);
            addHouseholdMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FormIntent intent = new FormIntent(getBaseContext(), "Participant");

                    intent.putExtra("headOfHouseholdId", headOfHouseholdId);

                    intent.putExtra("cluster", msg.getMap().get(FormKeyIDs.CLUSTER_ID));
                    intent.putExtra("community", msg.getMap().get(FormKeyIDs.COMMUNITY_ID));

                    startActivity(intent);
                }
            });

            //Go home when finished
            done = (Button) findViewById(R.id.done_button_review_participant);
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goHome();
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

    }

    private StoredParticipant getParticipantById(String id){
        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();
            return realm.where(StoredParticipant.class).equalTo("id", id).findFirst();
        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }

    // when we return from a photo we need to refresh
    @Override
    protected void onResume() {
        super.onResume();

        Realm realm = null;

        StoredParticipant headOfHousehold = getParticipantById(headOfHouseholdId);

        try {
            realm = Realm.getDefaultInstance();

            realm.setAutoRefresh(true);

            Household hh =  realm.where(Household.class)
                    .equalTo("headOfHousehold", headOfHouseholdId).findFirst();

            householdMembers = new ArrayList<>();

            householdMembers.add(headOfHousehold);

            for (HouseholdRelationship r : hh.getMembers()) {
                StoredParticipant p = realm.where(StoredParticipant.class)
                        .equalTo("id", r.getParticipantId()).findFirst();

                if (r.getParticipant() != null) {
                    householdMembers.add(p);
                }
            }

            adapter = new ParticipantListAdapater(householdMembers, this, false);

            newParticipantRecycler.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }

    public void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);

        startActivity(intent);
    }

}


