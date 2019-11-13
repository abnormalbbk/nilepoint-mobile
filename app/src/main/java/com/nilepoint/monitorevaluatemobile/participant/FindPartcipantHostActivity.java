package com.nilepoint.monitorevaluatemobile.participant;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.SearchView;

import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.persistence.Datastore;

/**
 * Activity that holds all searching capabilities, from the list as well as the barcode
 */
public class FindPartcipantHostActivity extends AppCompatActivity implements
        BarcodeScannerButtonFragment.OnFragmentInteractionListener,
        ParticipantListFragment.OnFragmentInteractionListener{

    protected static final String TAG = "FindHostActivity";

    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";

    private static final String SPECIFY = "specify_activity";
    protected Datastore data = Datastore.init(this);
    private ParticipantListFragment listFragment = new ParticipantListFragment();

    //private  Switch sortParticipantBy;


    //Views for host activity
    protected SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_partcipant_host);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.find_a_participant_heading);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Fragment frg = new BarcodeScannerButtonFragment();
        Bundle args = new Bundle();
        args.putString("activityStartFlag", "viewProfile");
        frg.setArguments(args);
        startFragment(frg);


        searchView = (SearchView) findViewById(R.id.client_list_searchview);
        searchView.clearFocus();
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                startFragment(listFragment);
            }
        });

        searchView.setQueryHint("Name, FH Code or Area");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            public boolean onQueryTextChange(String s) {

                listFragment.search(s);

                return false;
            }
        });

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
        }
    }

    public static String getFirstNameKey() {
        return FIRST_NAME_KEY;
    }

    public static String getLastNameKey() {
        return LAST_NAME_KEY;
    }

    public void startFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.find_participant_fragment_container, fragment, SPECIFY)
                .commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    @Override
    public void onBackPressed(){
        super.onBackPressed();
        this.finish();
    }

}
