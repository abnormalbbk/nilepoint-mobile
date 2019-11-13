package com.nilepoint.monitorevaluatemobile.group;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SelectableParticipantListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SelectableParticipantListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectableParticipantListFragment extends Fragment {

    private static final String DELETE = "delete";
    private static final String ADD = "add";

    private RecyclerView selectableListRecycler;
    private SearchView searchView;
    private LinearLayoutManager layoutManager;
    private GroupMemberAddRemoveAdapter adapter;
    private TextView infoText;

    private OnFragmentInteractionListener mListener;

    private String memberType;

    public SelectableParticipantListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectableParticipantListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectableParticipantListFragment newInstance(String param1, String param2) {
        SelectableParticipantListFragment fragment = new SelectableParticipantListFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args); */
        return fragment;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        memberType = args.getString("memberType");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootview = inflater.inflate(R.layout.fragment_selectable_participant_list, container, false);

        //searchView = (SearchView) rootview.findViewById(R.id.group_add_searchview);
        selectableListRecycler = (RecyclerView) rootview.findViewById(R.id.participant_list_recyclerView);
        selectableListRecycler.setVisibility(View.GONE);

        infoText = (TextView) rootview.findViewById(R.id.selectable_participant_list_text);


        infoText.setVisibility(View.GONE);
        selectableListRecycler.setVisibility(View.VISIBLE);

        List<StoredParticipant> listToDisplay = new ArrayList<>();
        if ("members".equals(memberType)) {
            listToDisplay = getHostActivity().getGroup().getMembers();
        } else if ("leaders".equals(memberType)){
            listToDisplay = getHostActivity().getGroup().getLeaders();
        } else {
            ArrayList<StoredParticipant> sp = new ArrayList<>();

            sp.addAll(getHostActivity().getGroup().getMembers());
            sp.addAll(getHostActivity().getGroup().getLeaders());

            listToDisplay = sp;
        }

        adapter = new GroupMemberAddRemoveAdapter(listToDisplay,
                getActivity());

        layoutManager = new LinearLayoutManager(getHostActivity());
        selectableListRecycler.setLayoutManager(layoutManager);
        selectableListRecycler.setAdapter(adapter);

        return rootview;
    }

    /**
     * Searches through the list and updates accordingly
     * @param str
     * @param action
     */
    public void search(String str, String action){
        List<StoredParticipant> listToDisplay;

        if(action.equals(ADD))
            listToDisplay = GroupUtils.searchParticipantsByName
                    (GroupUtils.getParticipantsNotInCurrentGroup(getHostActivity().getGroup().getId()), str);
        else if(action.equals(DELETE))
            if ("members".equals(memberType)) {
                listToDisplay = getHostActivity().getGroup().getMembers();
            } else {
                listToDisplay = getHostActivity().getGroup().getLeaders();
            }
        else
            listToDisplay = getHostActivity().getGroupMembers();


        adapter = new GroupMemberAddRemoveAdapter(listToDisplay,
                getActivity());

        //selectableListRecycler.setAdapter(adapter);
        selectableListRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        this.onResume();

    }

    /**
     *
     * @return list of selected partcipants in the adapter, for use in the parent activity
     */
    public List<StoredParticipant> getSelectedList(){
        return adapter.getSelectedMembers();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
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

    public ManageGroupMembersActivity getHostActivity(){
        return (ManageGroupMembersActivity) this.getActivity();
    }


}
