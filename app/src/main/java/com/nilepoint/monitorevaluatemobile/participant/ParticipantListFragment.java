package com.nilepoint.monitorevaluatemobile.participant;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.NameComparator;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.ParticipantListAdapater;
import com.nilepoint.persistence.Datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ParticipantListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ParticipantListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ParticipantListFragment extends Fragment {

    public static final String TAG = "findPartFragment";

    private OnFragmentInteractionListener mListener;

    protected Datastore data = Datastore.init(getHostActivity());
    private RecyclerView participantListRecycler;
    protected RecyclerView.LayoutManager layoutManager;
    protected RecyclerView.Adapter adapter;
    protected FloatingActionButton addFab;
    protected boolean isAddButtonPresent = false;
    protected View.OnClickListener onClickListener; //for the add button
    protected List<StoredParticipant> mParticipants = new ArrayList<>();
    // default DS just does all participants
    protected ParticipantDataSource participantDataSource = new ParticipantDataSource() {
        @Override
        public List<StoredParticipant> getParticipants() {
            return data.findParticipants("", FindPartcipantHostActivity.getFirstNameKey(), 100);
        }

        @Override
        public List<StoredParticipant> getParticipants(String search) {
            return data.findParticipants(search, FindPartcipantHostActivity.getFirstNameKey(), 100);
        }
        @Override
        public List<StoredParticipant> getParticipants(String search, String sort) {
            return data.findParticipants(search, sort, 100);
        }
    };


    public ParticipantListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment ParticipantListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ParticipantListFragment newInstance(ParticipantDataSource pds) {
        ParticipantListFragment fragment = new ParticipantListFragment();

        fragment.participantDataSource = pds;

        return fragment;
    }

    /**
     * use this factory method to instantiate this fragment with a button for adding to the list
     * @param pds
     * @param isAddButtonPresent
     * @return a new instance fragment of the ParticipantListFragment with an add button in the bottom right
     */
    public static ParticipantListFragment newInstance(ParticipantDataSource pds, boolean isAddButtonPresent, View.OnClickListener onClickListener){
        ParticipantListFragment fragment = new ParticipantListFragment();

        fragment.participantDataSource = pds;
        fragment.isAddButtonPresent = isAddButtonPresent;
        fragment.onClickListener = onClickListener;

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View rootView = inflater.inflate(R.layout.fragment_participant_list, container, false);

        if (!mParticipants.isEmpty()){
            Log.d(TAG, mParticipants.get(0).toString());
            sortByName(FindPartcipantHostActivity.getFirstNameKey());
        }

        //Set up views
        participantListRecycler = (RecyclerView) rootView.findViewById(R.id.participant_list_recyclerView);

        setUpAddButton(isAddButtonPresent, rootView, onClickListener);

        layoutManager = new LinearLayoutManager(getHostActivity());

        //get participant data if none was supplied
        mParticipants = participantDataSource.getParticipants();

        adapter = new ParticipantListAdapater(mParticipants, getHostActivity(), true);

        participantListRecycler.setAdapter(adapter);

        participantListRecycler.setLayoutManager(layoutManager);

        Log.d(TAG, "item count: " + adapter.getItemCount());


        return rootView;
    }

    /*
        * Finds the participant by specified criteria
     */
    public void search(String str){

        //getHostActivity().getSwitchChecked() ? getHostActivity().getLastNameKey() :
        mParticipants = participantDataSource.getParticipants(str);

        adapter = new ParticipantListAdapater(mParticipants, getHostActivity(), true);

        if (participantListRecycler != null){
            participantListRecycler.setAdapter(adapter);
        }

        adapter.notifyDataSetChanged();

        this.onResume();
    }

    public void sortParticipantsByName(String key){
        ParticipantListAdapater updateAdapter;
        mParticipants = participantDataSource.getParticipants("",key);
        updateAdapter = new ParticipantListAdapater(mParticipants, getHostActivity(), true);
        participantListRecycler.swapAdapter(updateAdapter, false);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void setUpAddButton(boolean isPresent, View v, View.OnClickListener onClickListener){
        addFab = (FloatingActionButton) v.findViewById(R.id.participant_list_add_fab);
        if (!isPresent) {
            addFab.setVisibility(View.GONE);
        } else {
            addFab.setVisibility(View.VISIBLE);
        }
        addFab.setOnClickListener(onClickListener);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public Activity getHostActivity(){
        return getActivity();
    }

    public void sortByName(String key){
        Collections.sort(mParticipants, new NameComparator(key));
    }


    public RecyclerView getParticipantRecycler(){
        return participantListRecycler;
    }

    @Override
    public void onResume() {
        super.onResume();
        participantListRecycler.swapAdapter(adapter,false);
    }

    /**
     *
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
