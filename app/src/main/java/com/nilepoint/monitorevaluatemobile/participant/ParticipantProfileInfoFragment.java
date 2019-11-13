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
import com.nilepoint.monitorevaluatemobile.adapter.ParticipantReviewInfoAdapter;


public class ParticipantProfileInfoFragment extends Fragment {
    private static String title;
    private static int page;
    private RecyclerView infoRecycler;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;


    public ParticipantProfileInfoFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ParticipantProfileInfoFragment newInstance() {
        ParticipantProfileInfoFragment fragment = new ParticipantProfileInfoFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_participant_profile_info, container, false);
        infoRecycler = (RecyclerView) rootView.findViewById(R.id.participant_profile_info_recycle);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new ParticipantReviewInfoAdapter(getHostActivity().getParticipantInfo(), getActivity());
        infoRecycler.setLayoutManager(layoutManager);
        infoRecycler.setAdapter(adapter);
        infoRecycler.setNestedScrollingEnabled(false);
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
