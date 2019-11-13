package com.nilepoint.monitorevaluatemobile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nilepoint.monitorevaluatemobile.R;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by claudiatrafton on 4/22/17.
 * Adapter for participant information on the review participant screen after all new participant details have been entered.
 */

public class ParticipantReviewInfoAdapter extends RecyclerView.Adapter<ParticipantReviewInfoAdapter.ViewHolder>{

    private static final String TAG = "ReviewInfoAdapter";

    private Context context;
    private ArrayList<Map.Entry<String, String>>  data;

    public ParticipantReviewInfoAdapter(ArrayList<Map.Entry<String, String>>  data, Context context){
        this.data = data;
        this.context = context;

    }

    /**
     * Create and initialize the view within the activity
     * @param parent the parent view
     * @param viewType
     * @return the viewholder containing the view data
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profile_review_info_card, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, parent.getContext());
        return viewHolder;
    }

    /**
     * Method that binds data from the model to the view. Checks if there is a phone number and shows icon if so
     * Also performs checks on the important fields, and will only show certain fields specified
     * Not sure if this is how this should be done, but it is scalable and will probably perform  alright
     * @param holder the view holder to bind to
     * @param position the position in the map of data passed from the acitivty
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(data.get(position).getKey().equals("Phone")){
            holder.phoneIcon.setVisibility(View.VISIBLE);
        }
        holder.infoTitle.setText(data.get(position).getKey());
        holder.infoData.setText(data.get(position).getValue());
        //Check the ID of the form, and decide whether or not to show it

        //Log.d(TAG, listData.get(position).getKey().getId());

        /* TODO update the information so that not every field is created and some are conglomerated*/
    }

    /**
     * Alerts the actvity to the number of items that were passed in to the adapter
     * @return the number of items passed in from the activity
     */
    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView infoTitle;
        private TextView infoData;
        private ImageView phoneIcon;
        private Context context;

        /**
         * Create the viewholder for the information review - populates the view with model data
         * @param itemLayoutView
         * @param context
         */
        public ViewHolder(View itemLayoutView, Context context){
            super(itemLayoutView);
            this.context = context;
            infoTitle = (TextView) itemLayoutView.findViewById(R.id.review_card_title);
            infoData = (TextView) itemLayoutView.findViewById(R.id.review_card_info_body);
            phoneIcon = (ImageView) itemLayoutView.findViewById(R.id.review_info_card_phone_icon);
            phoneIcon.setVisibility(View.GONE);
        }


    }
}
