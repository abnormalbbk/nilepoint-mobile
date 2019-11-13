package com.nilepoint.monitorevaluatemobile.group;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nilepoint.monitorevaluatemobile.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupAboutFragment extends Fragment {

    private RecyclerView infoRecyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;


    public GroupAboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


         View rootView = inflater.inflate(R.layout.fragment_group_about, container, false);
        infoRecyclerView = (RecyclerView) rootView.findViewById(R.id.group_info_list);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new InfoAdapter(getActivity(), getHostActivity().getGroupInfo());
        infoRecyclerView.setAdapter(adapter);
        infoRecyclerView.setLayoutManager(layoutManager);
        return rootView;
    }

    public static GroupAboutFragment newInstance(){
        GroupAboutFragment fragment = new GroupAboutFragment();
        Bundle args = new Bundle();
        //args.putInt("pageNum", page);
        //args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }

    public GroupInfoHostActivity getHostActivity(){
        return (GroupInfoHostActivity) this.getActivity();
    }

}
