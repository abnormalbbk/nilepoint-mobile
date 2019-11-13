package com.nilepoint.monitorevaluatemobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.nilepoint.model.FormElement;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by claudiatrafton on 4/18/17.
 * TODO complete with Map information sent from previous activity
 */

public class ReviewParticipantAdapter extends RecyclerView.Adapter<ReviewParticipantAdapter.ViewHolder> {

    //List of items passed to the
    private ArrayList<Map<FormElement, String>> list;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(View v, Context c){
            super(v);

        }

        //Bind the data to the view

    }
}
