package com.nilepoint.monitorevaluatemobile.participant;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nilepoint.model.Household;
import com.nilepoint.model.HouseholdRelationship;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.HhMembersProfileAdapter;

import java.util.Map;

import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 */
public class ParticipantProfileHouseholdFragment extends Fragment {
    private static String title;
    private static int page;
    private Household hh;
    private StoredParticipant participant;

    private TextView residenceTextView;
    private TextView membersTextView;

    private RecyclerView hhMembersRecycler;
    GridLayoutManager gridLayoutManager;
    private RecyclerView.Adapter membersAdapter;



    public ParticipantProfileHouseholdFragment() {

    }

    public static ParticipantProfileHouseholdFragment newInstance(){
        ParticipantProfileHouseholdFragment fragment = new ParticipantProfileHouseholdFragment();
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

        if (getHostActivity() != null){
           hh = getHostActivity().getHousehold();
           participant = getHostActivity().getParticipant();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View rootview =  inflater.inflate(R.layout.fragment_participant_profile_household, container, false);

        HhInfoParser infoParser = new HhInfoParser();

        if (getHostActivity().getHhHead() != null) {
            Map<String, String> hhMap = getHostActivity().getHhHead().toMessage().getMap();

            residenceTextView = (TextView) rootview.findViewById(R.id.profile_hh_residence);
            residenceTextView.setText(infoParser.parseResidence(hhMap));

            TextView hohTextView = (TextView) rootview.findViewById(R.id.profile_is_hoh);

            if (participant.getId().equals(hh.getHeadOfHousehold())){
                hohTextView.setText("Yes");
            } else {
                hohTextView.setText("No");
            }

            gridLayoutManager = new GridLayoutManager(getActivity(), 3); //show three across

            hhMembersRecycler = (RecyclerView) rootview.findViewById(R.id.hh_members_profile_recyclerView);
            hhMembersRecycler.setLayoutManager(gridLayoutManager);

            membersAdapter = new HhMembersProfileAdapter(getHostActivity().getHhMembers(),
                    getActivity(), getHostActivity().getHhHead().getId());

            hhMembersRecycler.setAdapter(membersAdapter); //todo write adapter
        }

        return rootview;
    }

    public int getMembersByGender(String gender){
        int count = 0;


        if (hh == null){
            return 0;
        }
        Realm realm = null;

        try {
            for(HouseholdRelationship hhRel: hh.getMembers()){

                realm = Realm.getDefaultInstance();

                StoredParticipant p = realm.where(StoredParticipant.class)
                            .equalTo("id", hhRel.getParticipantId()).findFirst();

                if(p.toMessage().getMap().get("gender").equals(gender)){
                    count++;
                }
            }
        } finally {
            if (realm != null){
                realm.close();
            }
        }
        return count;
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
