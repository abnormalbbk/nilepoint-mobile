package com.nilepoint.monitorevaluatemobile.tracking;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nilepoint.model.Distribution;
import com.nilepoint.model.Group;
import com.nilepoint.model.PlannedDistribution;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.GroupListAdapter;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by ashaw on 10/9/17.
 */

public class DistributionAdapter extends RecyclerView.Adapter<DistributionAdapter.ViewHolder>  {
    private List<PlannedDistribution> distributions = new ArrayList<>();
    private Activity activity;

    public DistributionAdapter(Activity activity, List<PlannedDistribution> distributions){
        this.distributions = distributions;
        this.activity = activity;
    }

    @Override
    public DistributionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.distribution_activity_card, parent, false);

        DistributionAdapter.ViewHolder viewHolder = new DistributionAdapter.ViewHolder(itemLayoutView, activity);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DistributionAdapter.ViewHolder holder, int position) {
        PlannedDistribution distribution = distributions.get(position);

        System.out.println("Bound view holder for distribution" + distribution);

        if (distribution != null) {
            Realm realm = Realm.getDefaultInstance();
            try {
                StoredParticipant participant = realm.where(StoredParticipant.class)
                        .equalTo("id", distribution.getBeneficiaryUuid())
                        .findFirst();
                if (participant != null) {
                    holder.nameView.setText(participant.getFirstName() + " " + participant.getLastName());
                }
            } finally {
                realm.close();
            }

            holder.quantityView.setText(distribution.getQuantity());
            holder.unitView.setText(distribution.getUnit());
            holder.typeView.setText(distribution.getType());
        }

    }

    @Override
    public int getItemCount() {
        return distributions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView nameView;
        private TextView typeView;
        private TextView unitView;
        private TextView quantityView;
        private Activity activity;

        public ViewHolder(View itemView, Activity activity) {
            super(itemView);
            itemView.setOnClickListener(this);

            nameView = (TextView) itemView.findViewById(R.id.value_name);
            typeView = (TextView) itemView.findViewById(R.id.value_type); //This is currently just a placeholder
            unitView = (TextView) itemView.findViewById(R.id.value_unit);
            quantityView = (TextView) itemView.findViewById(R.id.value_quantity);

            this.activity = activity;

        }

        @Override
        public void onClick(View v) {
            // nothing to do
        }
    }

}
