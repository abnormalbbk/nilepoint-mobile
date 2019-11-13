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
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.Switch;

import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.GroupListAdapter;
import com.nilepoint.monitorevaluatemobile.adapter.SelectParticipantAdapter;
import com.nilepoint.monitorevaluatemobile.adapter.SelectSingleParticipantAdapter;
import com.nilepoint.monitorevaluatemobile.participant.BarcodeScannerHostActivity;
import com.nilepoint.monitorevaluatemobile.participant.ParticipantDataSource;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectParticipantFragment extends Fragment {

    private static final String TAG = "SelectParticipantFragment";
    private static final String TITLE = "Find a Participant";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    protected SearchView searchView;
    private Switch toggleListSwitch;
    private Button scanButton;

    private ParticipantDataSource dataSource;

    private Context context = getActivity();

    private static final String ACTIVITY_TAG = "SelectParticipantFragment";



    public SelectParticipantFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(TITLE);

    }

    SelectSingleParticipantAdapter.ParticipantSelectedListener selectListener =
            new SelectSingleParticipantAdapter.ParticipantSelectedListener() {
        @Override
        public void onParticipantSelected(StoredParticipant participant) {
            System.out.println("Finished!");

            Intent intent = new Intent();

            intent.putExtra("participant.id", participant.getId());

            getActivity().setResult(1, intent);
            getActivity().finish();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_select_partcipant, container, false);

        searchView = (SearchView) rootView.findViewById(R.id.select_participant_searchview);
        searchView.setQueryHint("Search by Name, Area or Code");

        recyclerView = (RecyclerView) rootView.findViewById(R.id.select_participant_or_group_recyclerView);

        layoutManager = new LinearLayoutManager(context);

        adapter = new SelectSingleParticipantAdapter(dataSource.getParticipants(),
                selectListener);

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(layoutManager);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter = new SelectSingleParticipantAdapter(dataSource.getParticipants(s),
                        selectListener);

                recyclerView.setAdapter(adapter);

                adapter.notifyDataSetChanged();

                return false;
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