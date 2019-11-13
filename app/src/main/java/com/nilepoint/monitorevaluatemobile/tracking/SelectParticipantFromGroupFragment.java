package com.nilepoint.monitorevaluatemobile.tracking;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nilepoint.model.Group;
import com.nilepoint.model.Project;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.GroupListAdapter;
import com.nilepoint.monitorevaluatemobile.participant.ParticipantDataSource;
import com.nilepoint.monitorevaluatemobile.participant.SelectMultipleParticipantsFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by ashaw on 9/17/17.
 */

public class SelectParticipantFromGroupFragment extends Fragment {
    RecyclerView groupRecyclerView;

    public SelectParticipantFromGroupFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_group_list, container, false);

        groupRecyclerView = (RecyclerView) rootView.findViewById(R.id.group_list_recyclerView);
        SearchView searchView = (SearchView) rootView.findViewById(R.id.client_list_searchview);

        searchView.setQueryHint("Search by Name, Area or Type");

        Realm realm = null;

        try {

            realm = Realm.getDefaultInstance();

            List<Group> groupList = realm.where(Group.class).findAll();

            groupRecyclerView.setAdapter(new GroupListAdapter(this.getActivity(), groupList,
                    new GroupListAdapter.OnGroupSelectedListener() {
                @Override
                public void onGroupSelected(final Group group) {
                    SelectGroupHostActivity parent = (SelectGroupHostActivity) getActivity();

                    SelectMultipleParticipantsFragment fragment = new SelectMultipleParticipantsFragment();

                    fragment.setDataSource(new ParticipantDataSource() {
                        @Override
                        public List<StoredParticipant> getParticipants() {
                            List<StoredParticipant> returnList = new ArrayList<>();

                            List<StoredParticipant> members =  group.getMembers().where()
                                    .findAll();
                            List<StoredParticipant> leaders =  group.getLeaders().where()
                                    .findAll();

                            returnList.addAll(members);
                            returnList.addAll(leaders);

                            Collections.sort(returnList,new FirstAndLastNameComparitor());

                            return returnList;
                        }

                        @Override
                        public List<StoredParticipant> getParticipants(String search) {
                            List<StoredParticipant> returnList = new ArrayList<>();

                            List<StoredParticipant> members =  group.getMembers().where()
                                    .beginsWith("firstName", search,Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("lastName",search, Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("cluster", search, Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("externalId", search, Case.INSENSITIVE)
                                    .findAllSorted("firstName", Sort.ASCENDING, "lastName",Sort.ASCENDING);

                            List<StoredParticipant> leaders =  group.getLeaders().where()
                                    .beginsWith("firstName", search,Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("lastName",search, Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("cluster", search, Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("externalId", search, Case.INSENSITIVE)
                                    .findAllSorted("firstName", Sort.ASCENDING, "lastName",Sort.ASCENDING);

                            returnList.addAll(members);
                            returnList.addAll(leaders);

                            Collections.sort(returnList,new FirstAndLastNameComparitor());
                            
                            return returnList;
                        }

                        @Override
                        public List<StoredParticipant> getParticipants(String search, String sort) {
                            return group.getMembers().where()
                                    .beginsWith("firstName", search,Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("lastName",search,Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("cluster", search, Case.INSENSITIVE)
                                    .or()
                                    .beginsWith("externalId", search, Case.INSENSITIVE)
                                    .findAllSorted(sort);
                        }
                    });

                    parent.startFragment(fragment);
                }
            }));

            groupRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (realm != null){
                realm.close();
            }
        }

        return rootView;
    }

    class FirstAndLastNameComparitor implements Comparator<StoredParticipant> {
        @Override
        public int compare(StoredParticipant p1, StoredParticipant p2){
            if (p1.getFirstName() == null){
                return -1;
            }

            if (p2.getFirstName() == null){
                return 1;
            }

            Integer cmp = p1.getFirstName().compareTo(p2.getFirstName());

            if (cmp != 0){
                return cmp;
            }

            if (p1.getLastName() != null && p2.getLastName() != null){
                return p1.getLastName().compareTo(p2.getLastName());
            }

            return cmp;
        }

    }

}
