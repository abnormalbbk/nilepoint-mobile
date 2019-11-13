package com.nilepoint.monitorevaluatemobile.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nilepoint.model.Group;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.activity_tracking.ActivityTrackingHostActivity;
import com.nilepoint.monitorevaluatemobile.activity_tracking.SelectParticipantsFromGroupFragment;
import com.nilepoint.monitorevaluatemobile.group.GroupInfoHostActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by claudiatrafton on 6/27/17.
 */

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    private List<Group> groups = new ArrayList<>();
    private OnGroupSelectedListener listener;
    private Activity activity;

    public GroupListAdapter(Activity activity, List<Group> groups, OnGroupSelectedListener listener){
        this.groups = groups;
        this.listener = listener;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_layout_card, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, activity);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.groupNameView.setText(group.getName() + " (" + group.getType().replaceAll("GT-","") +")");

        StringBuilder areaString = new StringBuilder();

        if (group.getCluster() != null){
            areaString.append(group.getCluster());
        }
        if (group.getCommunity() != null){
            areaString.append(" / ").append(group.getCommunity());
        }

        holder.groupCommunityView.setText(areaString.toString()); //TODO fix this, add the community here
        //holder.groupImage.setImageBitmap(); TODO set with associated image??

        holder.groupIcon.setText("GN"); //set the text to the
        GradientDrawable circleBackground = (GradientDrawable) activity.getResources().getDrawable(R.drawable.circular_background, null);

        circleBackground.setColor(Color.BLUE);

        holder.groupIcon.setBackground(circleBackground);

    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView groupImage;
        private TextView groupIcon;
        private TextView groupNameView;
        private TextView groupCommunityView;
        private Activity activity;

        public ViewHolder(View itemView, Activity activity) {
            super(itemView);
            itemView.setOnClickListener(this);
            groupIcon = (TextView) itemView.findViewById(R.id.group_icon_view); //This is currently just a placeholder
            groupNameView = (TextView) itemView.findViewById(R.id.list_group_name);
            groupCommunityView = (TextView) itemView.findViewById(R.id.list_group_community);



            this.activity = activity;

        }

        @Override
        public void onClick(View v) {
            int pos = getLayoutPosition(); //which group is selected

            listener.onGroupSelected(groups.get(pos));
        }
    }

    public static interface OnGroupSelectedListener {
        void onGroupSelected(Group group);
    }
}