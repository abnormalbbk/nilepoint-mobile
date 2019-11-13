package com.nilepoint.monitorevaluatemobile.group;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.ParticipantListAdapater;
import com.nilepoint.monitorevaluatemobile.participant.ParticipantDataSource;
import com.nilepoint.monitorevaluatemobile.participant.SelectMultipleParticipantHostActivity;
import com.nilepoint.monitorevaluatemobile.participant.SelectMultipleParticipantsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupMembersFragment extends Fragment implements Toolbar.OnMenuItemClickListener {

    private RecyclerView.Adapter adapter;
    private RecyclerView groupMembersRecycler;
    private RecyclerView.LayoutManager layoutManager;
    private FloatingActionButton addMember;
    private Toolbar membersToolbar;
    private SearchView searchBar;

    private ParticipantDataSource dataSource;
    private int actionType;

    public GroupMembersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //super.onCreateOptionsMenu(R.menu.group_members,inflater);
        // Inflate the layout for this fragment
//        if(getHostActivity().getSupportActionBar().isShowing())
//            getHostActivity().getSupportActionBar().hide();

        View rootView =  inflater.inflate(R.layout.fragment_group_members, container, false);
        membersToolbar = (Toolbar) rootView.findViewById(R.id.members_options);
        membersToolbar.inflateMenu(R.menu.group_members);

        Menu options = membersToolbar.getMenu();

        membersToolbar.setOnMenuItemClickListener(this);
        setHasOptionsMenu(true);

        groupMembersRecycler = (RecyclerView) rootView.findViewById(R.id.members_list_recycler);
        layoutManager = new LinearLayoutManager(getActivity());

        List<StoredParticipant> groupMembers = dataSource.getParticipants();

        adapter = new ParticipantListAdapater(groupMembers,getActivity(),true);

        groupMembersRecycler.setLayoutManager(layoutManager);
        groupMembersRecycler.setAdapter(adapter);

        searchBar = (SearchView) rootView.findViewById((R.id.group_list_searchview));
        searchBar.setQueryHint("Search by Name, Code or Area");

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String str) {

                adapter = new ParticipantListAdapater(dataSource.getParticipants(str),getActivity(), true);

                groupMembersRecycler.setAdapter(adapter);

                adapter.notifyDataSetChanged();

                return false;
            }
        });


        /*addMember = (FloatingActionButton) rootView.findViewById(R.id.add_group_member_button);
        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getHostActivity(), ManageGroupMembersActivity.class);
                intent.putExtra("listAction","add");
                intent.putExtra("group.id", getHostActivity().getGroup().getId());
                startActivity(intent);
            }
        });*/



        return rootView;
    }

    public static GroupMembersFragment newInstance(ParticipantDataSource dataSource, int actionType){
        GroupMembersFragment fragment = new GroupMembersFragment();

        Bundle args = new Bundle();

        fragment.setArguments(args);

        fragment.dataSource = dataSource;
        fragment.actionType = actionType;

        return fragment;
    }

    public GroupInfoHostActivity getHostActivity(){
        return (GroupInfoHostActivity) this.getActivity();
    }


    @Override
    public void onResume() {
        super.onResume();
        adapter = new ParticipantListAdapater(dataSource.getParticipants(),getActivity(),true);
        groupMembersRecycler.swapAdapter(adapter, false);
        getHostActivity().getSupportActionBar().hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        getHostActivity().getSupportActionBar().show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Intent intent = null;

        switch (item.getItemId()){
            /*case R.id.open_search_bar:
                searchBar.setVisibility(View.VISIBLE);
                membersToolbar.setVisibility(View.GONE);
                return true;*/

            case R.id.remove_members:
                intent = new Intent(getHostActivity(), ManageGroupMembersActivity.class);
                intent.putExtra("listAction","DELETE");
                intent.putExtra("memberType", actionType == GroupInfoHostActivity.ADD_MEMBERS_TO_GROUP ? "members" : "leaders");
                intent.putExtra("group.id", getHostActivity().getGroup().getId());
                startActivity(intent);
                return true;

            case R.id.add_members:
                intent = new Intent(getHostActivity(), SelectMultipleParticipantHostActivity.class);
                getHostActivity().startActivityForResult(intent, actionType);
                return true;
        }
        return true;
    }
}
