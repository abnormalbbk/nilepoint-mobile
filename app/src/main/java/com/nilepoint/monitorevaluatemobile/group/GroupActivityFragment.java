package com.nilepoint.monitorevaluatemobile.group;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.activity_tracking.ActivityTrackingActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupActivityFragment extends Fragment {

    private FloatingActionButton addActivityButton;

    public GroupActivityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getHostActivity().getSupportActionBar().show();
        View rootview = inflater.inflate(R.layout.fragment_group_activity, container, false);

        addActivityButton = (FloatingActionButton) rootview.findViewById(R.id.group_activity_add);
        addActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ActivityTrackingActivity.class);
                startActivity(intent);
            }
        });

        return rootview;
    }

    public static GroupActivityFragment newInstance(){
        GroupActivityFragment fragment = new GroupActivityFragment();
        Bundle args = new Bundle();
        //args.putInt("pageNum", page);
        //args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }

    public GroupInfoHostActivity getHostActivity(){
        return (GroupInfoHostActivity) this.getActivity();
    }

    @Override
    public void onStart(){
        super.onStart();
        getHostActivity().getSupportActionBar().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        getHostActivity().getSupportActionBar().show();
    }
}
