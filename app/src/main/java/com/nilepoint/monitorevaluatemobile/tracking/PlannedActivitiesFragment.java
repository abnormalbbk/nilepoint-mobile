package com.nilepoint.monitorevaluatemobile.tracking;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nilepoint.model.Distribution;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlannedActivitiesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlannedActivitiesFragment extends Fragment {

    private static final String TAG = PlannedActivitiesFragment.class.getName().toString();

    private RecyclerView plannedActivityRecycler;
    private TrackedActivitiesAdapter adapter;
    private String type;
    private FloatingActionButton addFab;
    private List<TrackedActivity> trackedActivities;
    private List<TrackedActivity> distributions;

    public PlannedActivitiesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        type = getArguments().getString("type");
        View rootView =  inflater.inflate(R.layout.fragment_planned_activities, container, false);
        plannedActivityRecycler = (RecyclerView) rootView.findViewById(R.id.planned_activity_recycler);
        addFab = (FloatingActionButton) rootView.findViewById(R.id.planned_activities_add_fab);

        if(type.equals(TrackingUtils.TYPE_TRAINING)) {
            trackedActivities = getHostActivity().getTrainings();
            addFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Attempting to add new training...");
                    getHostActivity().addNewActivity();
                }
            });

            adapter = new TrackedActivitiesAdapter(trackedActivities, getActivity());
            plannedActivityRecycler.setAdapter(adapter);
        }

        else if(type.equals(TrackingUtils.TYPE_DIST)) {

            distributions = getHostActivity().getDistributions();
            addFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Attempting to add new distribution...");
                    getHostActivity().addNewDistribution();
                }
            });
            adapter = new TrackedActivitiesAdapter(distributions, getActivity());
            adapter.setActivity(this.getHostActivity());
            plannedActivityRecycler.setAdapter(adapter);
        }
        else
            trackedActivities = getHostActivity().getTrainings();


        plannedActivityRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));


        return rootView;
    }

    public SelectActivityActivity getHostActivity(){
        return (SelectActivityActivity) getActivity();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment PlannedActivitiesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlannedActivitiesFragment newInstance(String type) {
        PlannedActivitiesFragment fragment = new PlannedActivitiesFragment();
        Bundle args = new Bundle();

        args.putString("type", type);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


}
