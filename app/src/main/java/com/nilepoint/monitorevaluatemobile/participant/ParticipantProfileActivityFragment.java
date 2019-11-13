package com.nilepoint.monitorevaluatemobile.participant;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nilepoint.monitorevaluatemobile.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ParticipantProfileActivityFragment extends Fragment {
    private static String title;
    private static int page;

    private RecyclerView activitiesRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;


    public ParticipantProfileActivityFragment() {
        // Required empty public constructor
    }

    public static ParticipantProfileActivityFragment newInstance(){
        ParticipantProfileActivityFragment fragment = new ParticipantProfileActivityFragment();
        Bundle args = new Bundle();
        args.putInt("pageNum", page);
        args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_participant_profile_activity, container, false);
        activitiesRecyclerView = (RecyclerView) rootView.findViewById(R.id.profile_activity_recycler);
        adapter = new ParticipantProfileActivityAdapter(getHostActivity().getTrackedActivties());
        layoutManager = new LinearLayoutManager(getActivity());
        activitiesRecyclerView.setLayoutManager(layoutManager);
        activitiesRecyclerView.setAdapter(adapter);

        return rootView;
    }

    public ParticipantProfileActivity getHostActivity(){
        return (ParticipantProfileActivity) this.getActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

}
