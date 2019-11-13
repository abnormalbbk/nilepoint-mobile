package com.nilepoint.monitorevaluatemobile.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.SearchView;

import com.nilepoint.model.Group;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.GroupListAdapter;
import com.nilepoint.persistence.Datastore;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class GroupListActivity extends AppCompatActivity {

    private RecyclerView groupsRecyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    protected SearchView searchView;

    private ArrayList<Group> groups = new ArrayList<>();

    Realm realm = Realm.getDefaultInstance();

    private static final String ACTIVITY_TAG = "GroupListActivity";

    private GroupListAdapter.OnGroupSelectedListener groupClickListener = new GroupListAdapter.OnGroupSelectedListener () {
        @Override
        public void onGroupSelected(Group group) {
            //ManageGroupMembersActivity
            Intent intent = new Intent(GroupListActivity.this, GroupInfoHostActivity.class);

            intent.putExtra("group.id",group.getId());

            intent.putExtra("listAction", "ADD");

            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.group);

        groupsRecyclerView = (RecyclerView) findViewById(R.id.group_list_recyclerView);
        layoutManager = new LinearLayoutManager(this);
        groups.addAll(getGroups(""));

        adapter = new GroupListAdapter(this, groups, groupClickListener);

        groupsRecyclerView.setLayoutManager(layoutManager);
        groupsRecyclerView.setAdapter(adapter);

        searchView = (SearchView) findViewById(R.id.client_list_searchview);
        searchView.setQueryHint("Search by Name, Area or Type");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String str) {

                search(str);

                return false;
            }
        });
    }

    public void getGroups(){
        Realm realm = null;
        RealmResults<Group> groupRealmResults;

        groupRealmResults = realm.where(Group.class)
                .findAll();

        for(Group group: groupRealmResults){
            groups.add(group);
        }

    }

    public ArrayList<Group> getGroups(String str){

        RealmResults<Group> groupRealmResults;
        ArrayList<Group> groups = new ArrayList<>();

        groupRealmResults = realm.where(Group.class)
                .lessThanOrEqualTo("endDate", new Date())
                .beginsWith("id", str,Case.INSENSITIVE)
                .or()
                .contains("name", str, Case.INSENSITIVE)
                .or()
                .contains("type", str, Case.INSENSITIVE)
                .or()
                .contains("community",str,Case.INSENSITIVE)
                .or()
                .contains("cluster",str,Case.INSENSITIVE)
                .findAllSorted("name", Sort.ASCENDING);

        for (Group group : groupRealmResults) {
            groups.add(group);
        }

        return groups;
    }

    public void search(String str){

        adapter = new GroupListAdapter(this, getGroups(str), groupClickListener);

        groupsRecyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();

        realm.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        groupsRecyclerView.swapAdapter(adapter,false);

        realm = Realm.getDefaultInstance();

        search("");

    }
}
