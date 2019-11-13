package com.nilepoint.monitorevaluatemobile.participant;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.tracking.SelectParticipantFromGroupFragment;

import java.util.ArrayList;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by ashaw on 9/17/17.
 */

public class SelectMultipleParticipantHostActivity extends AppCompatActivity {
    private static final String TAG = "SelectMultipleParticipant";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_multiple_participant_host);

        SelectMultipleParticipantsFragment fragment = new SelectMultipleParticipantsFragment();

        final ArrayList<String> memberIds = getIntent()
                .getStringArrayListExtra("participant.ids");

        fragment.setDataSource(new ParticipantDataSource() {
            @Override
            public List<StoredParticipant> getParticipants() {
                Realm realm = null;

                List<StoredParticipant> members = new ArrayList<>();
                try {
                    realm = Realm.getDefaultInstance();
                    if (memberIds != null && memberIds.size() > 0) {


                            members = new ArrayList(realm.where(StoredParticipant.class)
                                    .in("id", memberIds.toArray(new String[]{}))
                                    .findAll().sort("firstName", Sort.ASCENDING,
                                            "lastName", Sort.ASCENDING));

                    } else if (memberIds == null){
                        members = realm.where(StoredParticipant.class).findAll()
                                .sort("firstName", Sort.ASCENDING,
                                "lastName", Sort.ASCENDING);
                    }
                } finally {
                    if (realm != null) {
                        realm.close();
                    }
                }
                return members;
            }

            @Override
            public List<StoredParticipant> getParticipants(String search) {

                Realm realm = null;

                List<StoredParticipant> members = new ArrayList<>();

                try {
                    realm = Realm.getDefaultInstance();

                    if (memberIds != null && memberIds.size() > 0) {
                        members = new ArrayList(realm.where(StoredParticipant.class)
                                .in("id", memberIds.toArray(new String[]{}))
                                .beginsWith("cluster", search,Case.INSENSITIVE)
                                .or()
                                .contains("externalId", search,Case.INSENSITIVE)
                                .or()
                                .beginsWith("firstName", search,Case.INSENSITIVE)
                                .or()
                                .beginsWith("lastName", search, Case.INSENSITIVE)
                                .findAll());
                    } else if (memberIds == null){
                        members = realm.where(StoredParticipant.class)
                                .beginsWith("cluster", search,Case.INSENSITIVE)
                                .or()
                                .contains("externalId", search,Case.INSENSITIVE)
                                .or()
                                .beginsWith("firstName", search,Case.INSENSITIVE)
                                .or()
                                .beginsWith("lastName", search,Case.INSENSITIVE).findAll();
                    }
                } finally {
                    if (realm != null){
                        realm.close();
                    }
                }

                return members;
            }

            @Override
            public List<StoredParticipant> getParticipants(String search, String sort) {
                Realm realm = null;

                List<StoredParticipant> members = new ArrayList<>();

                try {
                    realm = Realm.getDefaultInstance();

                    if (memberIds != null && memberIds.size() > 0) {
                        members = new ArrayList(realm.where(StoredParticipant.class)
                                .in("id", memberIds.toArray(new String[]{}))
                                .beginsWith("firstName", search)
                                .or()
                                .beginsWith("lastName", search)
                                .findAllSorted(sort));
                    } else if (memberIds == null){
                        members = realm.where(StoredParticipant.class)
                                .beginsWith("firstName", search)
                                .or()
                                .beginsWith("lastName", search)
                                .findAllSorted(sort);
                    }
                } finally {
                    if (realm != null){
                        realm.close();
                    }
                }

                return members;
            }
        });

        startFragment(fragment);
    }

    public void startFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.select_multiple_participant_fragment_container, fragment, TAG)
                .commit();
    }
}
