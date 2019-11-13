package com.nilepoint.monitorevaluatemobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.forms.FormIntent;
import com.nilepoint.monitorevaluatemobile.participant.ParticipantProfileActivity;

import java.util.ArrayList;

/**
 * Created by claudiatrafton on 7/2/17.
 */

public class HhMembersProfileAdapter extends RecyclerView.Adapter<HhMembersProfileAdapter.ViewHolder> {

    private static final String TAG = "HhMembersProfileAdapter";
    private static final int ITEM_TYPE = 1;
    private static final int ADD_BUTTON_TYPE = 2;



    private ArrayList<StoredParticipant> members;
    private Context activity;
    private String hhId;

    public HhMembersProfileAdapter(ArrayList<StoredParticipant> members, Context activity, String hhId){
        this.activity = activity;
        this.members = members;
        this.hhId = hhId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hh_member_icon_profile, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, activity);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(position == members.size()){
            holder.profilePic.setImageResource(R.drawable.add_new_small);
            holder.name.setText(R.string.add);
        }
        else {
            if (members.get(position).getPhoto() != null) {
                holder.profilePic.setImageBitmap(members.get(position).getPhoto().getBitmap());
            }
            holder.name.setText(members.get(position).getFirstName());
            holder.relationship.setText(getRelationshipToHead(members.get(position))); //TODO get relationship to displayed profile
        }
    }

    @Override
    public int getItemCount() {
        if(members == null){
            return 1;
        }
        else {
            return members.size() + 1; //extra item accounts for add button
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == members.size()) ? ADD_BUTTON_TYPE : ITEM_TYPE;
    }

    /**
     * displays the relationship to the head of the household
     * @param sp the participant
     * @return String representation of the relationship
     */
    public String getRelationshipToHead(StoredParticipant sp) {
        if (sp.getId().equals(hhId)) {
            return activity.getString(R.string.head_of_household);
        }

        else {
            String relToHh = sp.toMessage().getMap().get("houseHeadRealation");

            if (relToHh != null) {
                return relToHh;
            }

            else {
                return "";
            }

        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private CircularImageView profilePic;
        private TextView name;
        private TextView relationship;
        private Context activity;

        public ViewHolder(View itemView, Context activity) {
            super(itemView);
            itemView.setOnClickListener(this);
            profilePic = (CircularImageView) itemView.findViewById(R.id.profile_hh_member_picture);
            name = (TextView) itemView.findViewById(R.id.profile_hh_member_name);
            relationship = (TextView) itemView.findViewById(R.id.profile_hh_member_relation);
            this.activity = activity;

        }

        @Override
        public void onClick(View view) {
            if(getLayoutPosition() == members.size()) {
                Intent formIntent = new FormIntent(activity, "Participant");
                formIntent.putExtra("headOfHouseholdId", hhId);
                activity.startActivity(formIntent);
            }
            else {
                Intent intent = new Intent(activity, ParticipantProfileActivity.class);
                intent.putExtra("spId", members.get(getLayoutPosition()).getId());
                Log.d(TAG, members.get(getLayoutPosition()).getId());
                activity.startActivity(intent);
                //activity.finish();
            }

        }
    }
}
