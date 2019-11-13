package com.nilepoint.monitorevaluatemobile.tracking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nilepoint.model.Distribution;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by claudiatrafton on 9/10/17.
 */

public class TrackedActivitiesAdapter extends RecyclerView.Adapter<TrackedActivitiesAdapter.ViewHolder>{

    protected static final String TAG = "TrackedActAdapter";

    //Beginning of adapter class code
    private List<TrackedActivity> trackedActivities;
    private List<Distribution> distributions;
    private Activity activity;

    public TrackedActivitiesAdapter(List<TrackedActivity> trackedActivities, Activity activity){
        this.trackedActivities = trackedActivities;
        this.activity = activity;

    }

    public TrackedActivitiesAdapter(List<Distribution> distributions){
        this.distributions = distributions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.setting_title_card, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, parent.getContext());

        return viewHolder;
    }

    //Bind the data to the view
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //TODO move this to the activity
        Log.d(TAG, "ViewHolder data bound!");


        if (trackedActivities != null) {
            TrackedActivity ta = trackedActivities.get(position);

            StringBuilder sb = new StringBuilder();

            if (ta.getTraining() != null) {
                sb.append(ta.getTraining().getName());
            }
            if (ta.getCategory() != null) {
                sb.append(ta.getCategory().getName());
            }

            if (ta.getModule() != null) {
                sb.append(" / ").append(ta.getModule().getName());
            }

            if (ta.getLesson() != null) {
                sb.append(" / ").append(ta.getLesson().getName());
            }

            sb.append("\n ");

            if (ta.getCluster() != null)
                sb.append(ta.getCluster().getName());

            if (ta.getCommunity() != null) {
                sb.append(", ")
                        .append(ta.getCommunity().getName());
            }

            holder.text.setText(sb.toString());
        } else {
            Distribution ta = distributions.get(position);

            StringBuilder sb = new StringBuilder();

            if (ta.getCluster() != null)
                sb.append(ta.getCluster().getName());

            if (ta.getDistribution().getName() != null) {
                sb.append(", ")
                        .append(ta.getDistribution().getName());
            }

            sb.append("\n");

            if (ta.getParticipant() != null) {
                sb
                        .append(ta.getParticipant().getFirstName())
                        .append(", ")
                        .append(ta.getParticipant().getLastName())
                .append("\n");
            }

            if (ta.getPlannedDistribution() != null){
                sb
                        .append(ta.getPlannedDistribution().getQuantity())
                        .append(" ")
                        .append(ta.getPlannedDistribution().getType());
            }

            holder.text.setText(sb.toString());
        }

    }

    @Override
    public int getItemCount() {
        if (trackedActivities != null) {
            return trackedActivities.size();
        } else {
            return distributions.size();
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView text;
        private Context context;

        /*
         *
         */
        public ViewHolder(View listLayoutView, Context c) {
            super(listLayoutView);
            listLayoutView.setOnClickListener(this);
            context = c;
            text = (TextView) listLayoutView.findViewById(R.id.setting_card_text);
            Log.d(TAG, "ViewHolder created!");

        }

        /**
         * OnClick is overrided to open the log profile and passes the log ID to the activity
         * @param v view that is clicked
         */
        @Override
        public void onClick(View v){
            int pos = getLayoutPosition();

            if (trackedActivities != null) {
                TrackedActivity pa = trackedActivities.get(pos);

                Intent intent = new Intent(activity, ActivityDetailsHost.class);

                intent.putExtra("activity.id", pa.getId());

                activity.startActivity(intent);
            }
        }


    } //end of inner ViewHolder inner class

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
