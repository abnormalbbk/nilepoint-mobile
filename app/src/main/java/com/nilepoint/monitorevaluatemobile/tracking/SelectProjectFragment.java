package com.nilepoint.monitorevaluatemobile.tracking;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nilepoint.model.Project;
import com.nilepoint.monitorevaluatemobile.R;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.nilepoint.monitorevaluatemobile.tracking.TrackingUtils.ACTIVITY_TRACKING_FLAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectProjectFragment extends Fragment {

    private RecyclerView projectListRecyclerView;
    private RecyclerView.Adapter adapter;
    private ArrayList<Project> projects;

    private Realm realm;

    public Realm getRealm() {
        return realm;
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    public SelectProjectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_select_project, container, false);

        projectListRecyclerView = (RecyclerView) rootView.findViewById(R.id.select_project_recycler_view);

        projects = new ArrayList<Project>();

        RealmResults<Project> projectsResults = realm.where(Project.class)
                .findAll();

        for(Project p: projectsResults){
            projects.add(p);
        }

        adapter = new ProjectListAdapter(projects, getActivity(), TrackingUtils.ACTIVITY_TRACKING_FLAG);

        projectListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        projectListRecyclerView.setAdapter(adapter);

        return rootView;
    }

}
