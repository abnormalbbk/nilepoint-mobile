package com.nilepoint.monitorevaluatemobile.activity_tracking;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.SelectParticipantAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectParticipantsFromGroupFragment extends Fragment {

    public final static String TAG = "SelectFromGroup";

    TextView groupNameTitle;
    RecyclerView groupMemberRecyclerview;
    RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private Context context = getActivity();


    String title;
    int groupPosition;

    public SelectParticipantsFromGroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_participants_from_group, container, false);
        groupNameTitle = (TextView) rootView.findViewById(R.id.select_from_group_group_name);
        groupMemberRecyclerview = (RecyclerView) rootView.findViewById(R.id.select_participant_from_group_recyclerView);

        title = getArguments().getString("groupName");
        groupNameTitle.setText(title);

        groupPosition = getArguments().getInt("listPosition");

        Log.d(TAG, title + " " + groupPosition);

        layoutManager = new LinearLayoutManager(context);

        adapter = new SelectParticipantAdapter(getHostActivity().getGroupMemberList(groupPosition), getActivity(),getHostActivity().getClass().getName(), true, false);

        groupMemberRecyclerview.setAdapter(adapter);

        groupMemberRecyclerview.setLayoutManager(layoutManager);

        return rootView;
    }

    public ActivityTrackingHostActivity getHostActivity(){
        return (ActivityTrackingHostActivity) this.getActivity();
    }

}
