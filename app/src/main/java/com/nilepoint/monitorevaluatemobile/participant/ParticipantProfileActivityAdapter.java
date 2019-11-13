package com.nilepoint.monitorevaluatemobile.participant;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by claudiatrafton on 6/17/17.
 */

public class ParticipantProfileActivityAdapter extends RecyclerView.Adapter<ParticipantProfileActivityAdapter.ViewHolder> {

    private final static String TAG = "ActivityAdapter";
    private List<TrackedActivity> activities;

    public ParticipantProfileActivityAdapter(List<TrackedActivity> activities) {
        this.activities = activities;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.participant_profile_activity_card, parent, false);

        ParticipantProfileActivityAdapter.ViewHolder viewHolder = new ParticipantProfileActivityAdapter.ViewHolder(itemLayoutView);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TrackedActivity ta = activities.get(position);

        if (ta.getActivityDate() != null) {
            holder.activityTime.setText(formatDate(ta.getActivityDate()));
        }

        holder.activityDesc.setText((ta.getLesson() != null ? ta.getLesson().getName() : "")
                + " " + (ta.getTraining() != null ? ta.getTraining().getName() : "")); //just a placeholder

        holder.activityUpdater.setText("Last updated by: " + (ta.getLastUpdatedBy() != null
                ? ta.getLastUpdatedBy().getFullName()
                : "Unknown"));

    }

    @Override
    public int getItemCount() {
        if(activities != null) {
            Log.d(TAG, "Size is : " + activities.size());
            return activities.size();
        }
        else
            return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView activityTime;
        private TextView activityDesc;
        private TextView activityUpdater;

        public ViewHolder(View itemView) {
            super(itemView);
            activityTime = (TextView) itemView.findViewById(R.id.profile_activity_card_time);
            activityDesc = (TextView) itemView.findViewById(R.id.profile_activity_card_description);
            activityUpdater = (TextView) itemView.findViewById(R.id.profile_activity_card_updater);
        }
    }

    public String formatDate(Date date){
        String pattern = "EEEE MMMMMM dd HH:mm a";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
}
