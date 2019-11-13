package com.nilepoint.monitorevaluatemobile.group;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.Group;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by claudiatrafton on 8/27/17.
 * Adapter used for group selection and removal
 */

public class GroupMemberAddRemoveAdapter extends RecyclerView.Adapter<GroupMemberAddRemoveAdapter.ViewHolder>  {

    private static final boolean SELECT_FLAG = true;
    private static final boolean DELETE_FLAG  = false;

    private Group group;
    private List<StoredParticipant> participants;
    private List<StoredParticipant> selectedList = new ArrayList<>(); //list of the checked participants

    boolean flag;
    private Activity context;

    public GroupMemberAddRemoveAdapter(List<StoredParticipant> participants, Activity context) {
        //this.selectedParticipants = selectedParticipants;
        //this.flag = flag;
        this.participants = participants;
        this.context = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_participant_card, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final StoredParticipant participant = participants.get(position);

        MapMessage mm = participant.toMessage();
        String residence = mm.getMap().get(FormKeyIDs.CLUSTER_ID) + " , " + mm.getMap().get(FormKeyIDs.COMMUNITY_ID )+ " , " + mm.getMap().get(FormKeyIDs.VILLAGE_ID);
        String Id = participant.getExternalId();

        //Set cardview info
        holder.nameView.setText(participant.getFirstName() + " " + participant.getLastName());

        holder.neighborhoodView.setText(residence); //this can be updated when changes are pulled in from form updates

        holder.idView.setText(Id);

        if (participant.getPhoto() != null) {
            holder.photoView.setImageBitmap(participant.getPhoto().getBitmap());
        }
        else {
            holder.photoView.setImageResource(R.drawable.add_photo);
        }

        if(holder.selectedView != null){
            holder.selectedView.setChecked(selectedList.contains(participant));
        }

    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public List<StoredParticipant> getSelectedMembers(){
        return selectedList;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView nameView;
        private TextView neighborhoodView;
        private TextView idView;
        private ImageView photoView;
        private CheckBox selectedView;

        public ViewHolder(View listLayoutView, final Context ctx){
            super(listLayoutView);

            nameView = (TextView) listLayoutView.findViewById(R.id.select_participant_name);
            neighborhoodView = (TextView) listLayoutView.findViewById(R.id.select_participant_neighborhood);
            idView = (TextView) listLayoutView.findViewById(R.id.select_participant_id);
            photoView = (ImageView) listLayoutView.findViewById(R.id.select_participant_photo);
            selectedView = (CheckBox) listLayoutView.findViewById(R.id.selected_radio_button);
            selectedView.setVisibility(View.VISIBLE);

            selectedView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    selectedList.add(participants.get(getLayoutPosition()));
                }
            });


        }
    }
}
