package com.nilepoint.monitorevaluatemobile.logging;

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

import com.nilepoint.model.StoredLog;
import com.nilepoint.monitorevaluatemobile.NameComparator;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.LogListAdapter;
import com.nilepoint.persistence.Datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Sort;

public class LogListActivity extends AppCompatActivity {

    protected static final String TAG = "LogListActivity";
    // keys in the Storelog is firstname / lastname

    private RecyclerView logListRecycler;
    protected RecyclerView.LayoutManager layoutManager;
    protected RecyclerView.Adapter adapter;
    protected SearchView searchView;
    protected List<StoredLog> mlogs = new ArrayList<>();
    private Context c = this;

    //Key which determines whether to sort by first or last name

    /*
 *Grab data from Realm
 */
    protected Datastore data = Datastore.init(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Search logs");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //get all of the logs from storage
        //mlogs = getAlllogs(mlogs);

        Log.d(TAG, String.valueOf(mlogs.size()));

        //search("t");
        //Log.d(TAG, String.valueOf(mlogs.size()));

        //Set up views
        logListRecycler = (RecyclerView) findViewById(R.id.log_list_recyclerView);
        searchView = (SearchView) findViewById(R.id.client_list_searchview);
        layoutManager = new LinearLayoutManager(this);

        // initialize with all logs.

        mlogs = data.getDefaultInstance()
                .where(StoredLog.class).findAll()
                .sort("dateCreated", Sort.DESCENDING);

        adapter = new LogListAdapter(mlogs, this, true);

        logListRecycler.setAdapter(adapter);

        logListRecycler.setLayoutManager(layoutManager);

        Log.d(TAG, "item count: " + adapter.getItemCount());

        /*
         * Switch controls whether the logs are loaded based on alphabetical order based on first or last name
         * First name will be the default.
         */

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
        * Finds the log by specified criteria
     */
    public void search(String str){
        Switch sortlogBy = (Switch) findViewById(R.id.sort_by_switch);

        mlogs = data.getDefaultInstance().where(StoredLog.class).contains("text", str).findAll();

        adapter = new LogListAdapter(mlogs, this, true);

        logListRecycler.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume(){
        super.onResume();
        logListRecycler.swapAdapter(adapter, false);
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

