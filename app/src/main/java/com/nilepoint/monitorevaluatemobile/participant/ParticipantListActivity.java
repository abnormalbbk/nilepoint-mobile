package com.nilepoint.monitorevaluatemobile.participant;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.Switch;

import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.NameComparator;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.ParticipantListAdapater;
import com.nilepoint.persistence.Datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;

public class ParticipantListActivity extends AppCompatActivity {

    protected static final String TAG = "ParticipantListActivity";
    // keys in the StoreParticipant is firstname / lastname

    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";

    private RecyclerView participantListRecycler;
    protected RecyclerView.LayoutManager layoutManager;
    protected RecyclerView.Adapter adapter;
    protected SearchView searchView;
    protected List<StoredParticipant> mParticipants = new ArrayList<>();
    private Context c = this;
    private Realm realm;

    //Key which determines whether to sort by first or last name

    /*
 *Grab data from Realm
 */
    protected Datastore data = Datastore.init(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.find_a_participant_heading);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Log.d(TAG, String.valueOf(mParticipants.size()));

        //Set up views
        participantListRecycler = (RecyclerView) findViewById(R.id.participant_list_recyclerView);
        searchView = (SearchView) findViewById(R.id.client_list_searchview);
        layoutManager = new LinearLayoutManager(this);

        // initialize with all participants.

        mParticipants = data.findParticipants("", FIRST_NAME_KEY, 100);

        adapter = new ParticipantListAdapater(mParticipants, this, true);

        participantListRecycler.setAdapter(adapter);

        participantListRecycler.setLayoutManager(layoutManager);

        Log.d(TAG, "item count: " + adapter.getItemCount());

        /*
         * Switch controls whether the participants are loaded based on alphabetical order based on first or last name
         * First name will be the default.
         */
        Switch sortParticipantBy = (Switch) findViewById(R.id.sort_by_switch);

        sortParticipantBy.setChecked(true);

        sortParticipantBy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ParticipantListAdapater updateAdapter;
                if (isChecked){
                    mParticipants = data.findParticipants("",FIRST_NAME_KEY,100);
                     updateAdapter = new ParticipantListAdapater(mParticipants, c, true);
                    participantListRecycler.swapAdapter(updateAdapter, false);
                }
                else {
                    mParticipants = data.findParticipants("",FIRST_NAME_KEY,100);
                    updateAdapter = new ParticipantListAdapater(mParticipants, c, true);
                    participantListRecycler.swapAdapter(adapter, false);

                }

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                search(s);

                return false;
            }
        });


        // Get the intent, verify the action and get the query for the search
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
        }
    }

    /*
        *Get all of the participants from store and display them all initially before sorting takes place
     */
    /* public List getAllParticipants(List l){
        Realm realm = getDefaultInstance();

        RealmResults results = realm.where(StoredParticipant.class).findAll();
        Iterator iterator = results.iterator();

        while(iterator.hasNext()) {
            MapMessage result = (MapMessage)iterator.next();
            Log.d(TAG,result.getId());
            l.add(result);
        }
        return l;

    } */

    /*
        * either firstName or last name passed in
     */
    public void sortByName(String key){
        Collections.sort(mParticipants, new NameComparator(key));
    }

    /*
        * Finds the participant by specified criteria
     */
    public void search(String str){
        Switch sortParticipantBy = (Switch) findViewById(R.id.sort_by_switch);

        List<StoredParticipant> results = realm.where(StoredParticipant.class)
                .beginsWith("cluster", str, Case.INSENSITIVE)
                .or()
                .beginsWith("externalId", str,Case.INSENSITIVE)
                .or()
                .beginsWith("firstName", str,Case.INSENSITIVE)
                .or()
                .beginsWith("lastName", str, Case.INSENSITIVE)
                .findAll();

        mParticipants = results.subList(0, Math.min(results.size(), 100));

        adapter = new ParticipantListAdapater(mParticipants, this, true);

        participantListRecycler.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume(){
        super.onResume();
        participantListRecycler.swapAdapter(adapter, false);
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (realm != null){
            realm.close();
            realm = null;
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (realm != null){
            realm.close();
            realm = null;
        }
    }

    //Navigate home from up arrow in the action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
