package com.nilepoint.monitorevaluatemobile.participant;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Switch;

import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.SelectMultipleParticipantAdapter;
import com.nilepoint.monitorevaluatemobile.adapter.SelectSingleParticipantAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectMultipleParticipantsFragment extends Fragment {

    private static final String TAG = "SelectMultipleParticipantsFragment";
    private static final String TITLE = "Select Participants";

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SelectMultipleParticipantAdapter adapter;

    private Button doneButton;

    protected SearchView searchView;

    private ParticipantDataSource dataSource;

    private Context context = getActivity();

    private static final String ACTIVITY_TAG = "SelectParticipantFragment";

    public SelectMultipleParticipantsFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(TITLE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_select_partcipants, container, false);

        searchView = (SearchView) rootView.findViewById(R.id.select_participant_searchview);

        searchView.setQueryHint("Search by Name, FH Code or Community");

        recyclerView = (RecyclerView) rootView.findViewById(R.id.select_participant_or_group_recyclerView);

        doneButton = (Button) rootView.findViewById(R.id.done_select_participants_button);

        searchView = (SearchView) rootView.findViewById(R.id.select_participant_searchview);
        searchView.setQueryHint("Search by name, cluster, or community.");

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        adapter = new SelectMultipleParticipantAdapter(new ArrayList(dataSource.getParticipants()));

        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                SelectMultipleParticipantAdapter newAdapter = new SelectMultipleParticipantAdapter(dataSource.getParticipants(s));

                newAdapter.setSelectedParticipants(adapter.getSelectedParticipants());

                recyclerView.setAdapter(newAdapter);

                adapter.notifyDataSetChanged();

                return false;
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                ArrayList<String> participantIds = new ArrayList<>();

                for (StoredParticipant participant : adapter.getSelectedParticipants()){
                    participantIds.add(participant.getId());
                }

                intent.putStringArrayListExtra("participant.ids", participantIds);

                System.out.println("Returning " + participantIds);

                getActivity().setResult(1, intent);
                getActivity().finish();
            }
        });


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
       // adapter = new SelectParticipantAdapter(getHostActivity().getParticipants(), getActivity(), true, false);
    }

    public Activity getHostActivity(){
        return  this.getActivity();
    }

    public ParticipantDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(ParticipantDataSource dataSource) {
        this.dataSource = dataSource;
    }
}