package com.nilepoint.monitorevaluatemobile.tracking;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.participant.SelectParticipantFragment;
import com.nilepoint.monitorevaluatemobile.participant.ParticipantDataSource;

import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by ashaw on 9/14/17.
 */

public class SelectParticipantHostActivity extends AppCompatActivity {

    private static final String TAG = "specify_activity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_partcipant_host);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setTitle(R.string.find_a_participant_heading);

        SelectParticipantFragment fragment = new SelectParticipantFragment();

        fragment.setDataSource(new ParticipantDataSource() {
            @Override
            public List<StoredParticipant> getParticipants() {
                Realm realm = null;
                try{
                    realm = Realm.getDefaultInstance();
                    return realm.where(StoredParticipant.class)
                            .findAllSorted("firstName", Sort.ASCENDING, "lastName", Sort.ASCENDING);
                } finally {
                    if (realm != null){
                        realm.close();
                    }
                }
            }

            @Override
            public List<StoredParticipant> getParticipants(String search) {
                Realm realm = null;
                try{
                    realm = Realm.getDefaultInstance();
                    return realm.where(StoredParticipant.class)
                            .beginsWith("id",search,Case.INSENSITIVE)
                            .or()
                            .beginsWith("firstName", search, Case.INSENSITIVE)
                            .or()
                            .beginsWith("lastName", search,Case.INSENSITIVE)
                            .or()
                            .contains("externalId", search,Case.INSENSITIVE)
                            .or()
                            .beginsWith("cluster", search,Case.INSENSITIVE)
                            .findAllSorted("firstName", Sort.ASCENDING, "lastName", Sort.ASCENDING);
                } finally {
                    if (realm != null){
                        realm.close();
                    }
                }
            }

            @Override
            public List<StoredParticipant> getParticipants(String search, String sort) {
                Realm realm = null;
                try{
                    realm = Realm.getDefaultInstance();
                    return realm.where(StoredParticipant.class)
                            .beginsWith("firstName", search)
                            .or()
                            .beginsWith("lastName", search)
                            .findAllSorted(sort);
                } finally {
                    if (realm != null){
                        realm.close();
                    }
                }
            }
        });
        startFragment(fragment);
    }
    public void startFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.find_participant_fragment_container, fragment, TAG)
                .commit();
    }

}
