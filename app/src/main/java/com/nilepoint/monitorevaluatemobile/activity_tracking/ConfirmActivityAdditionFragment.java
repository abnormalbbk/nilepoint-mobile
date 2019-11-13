package com.nilepoint.monitorevaluatemobile.activity_tracking;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.SelectParticipantAdapter;

/**
 * Created by claudiatrafton on 6/9/17.
 */

public class ConfirmActivityAdditionFragment extends Fragment {

    public static final String TITLE = "Confirm Activity Addition";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private TextView headerLabel;
    private Activity activity;


    public ConfirmActivityAdditionFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getActivity().findViewById(R.id.activity_tracking_next_button);
        activity = getActivity();
        if (view instanceof Button) {
            ((Button) view).setText("Done");
        }

        /*if(isHostActivityInstance()){
            attendees = ((ActivityTrackingHostActivity)context).getAttendees();
        } */

        getActivity().setTitle(TITLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_confirm_activity_addition, container, false);

        headerLabel = (TextView) rootView.findViewById(R.id.confirm_activity_heading_text);
        String headerMessage = "Training attendance saved for ";
        /*if(getHostActivity().getFormElements().get("lesson.id") == null){
            headerMessage = headerMessage + " food safety";
        }
        else {
            headerMessage = headerMessage + getHostActivity().getFormElements().get("lesson.id");
        } */

        headerLabel.setText(headerMessage);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.select_participant_or_group_recyclerView);
        if(getHostActivity().getAttendees() == null){
                //TODO add a none selected message
        }
        else {

            layoutManager = new LinearLayoutManager(activity);

            adapter = new SelectParticipantAdapter(getHostActivity().getAttendees(),getActivity(),getHostActivity().getClass().getName(), false, true);

            recyclerView.setAdapter(adapter);

            recyclerView.setLayoutManager(layoutManager);
        }

        return rootView;
    }

    public ActivityTrackingHostActivity getHostActivity(){
        return (ActivityTrackingHostActivity) this.getActivity();
    }



}