package com.nilepoint.monitorevaluatemobile.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.activity_tracking.ActivityTrackingHostActivity;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;
import com.nilepoint.monitorevaluatemobile.group.GroupUtils;
import com.nilepoint.monitorevaluatemobile.group.ManageGroupMembersActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ashaw on 9/17/17.
 */

public class SelectMultipleParticipantAdapter extends RecyclerView.Adapter<SelectMultipleParticipantAdapter.ViewHolder>{
    private List<StoredParticipant> participants = new ArrayList<>();
    private List<StoredParticipant> selectedParticipants = new ArrayList<>();


    public SelectMultipleParticipantAdapter(List<StoredParticipant> participants) {
        this.participants = participants;
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    @Override
    public SelectMultipleParticipantAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_participant_card, parent, false);

        SelectMultipleParticipantAdapter.ViewHolder viewHolder = new SelectMultipleParticipantAdapter
                .ViewHolder(itemLayoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SelectMultipleParticipantAdapter.ViewHolder holder, final int position) {
        //TODO move this to the activity
        final StoredParticipant participant = participants.get(position);

        MapMessage mm = participant.toMessage();

        String fullName = participant.getFirstName() + " " + participant.getLastName();
        String gott = mm.getMap().get(FormKeyIDs.VILLAGE_ID) != null ? mm.getMap().get(FormKeyIDs.VILLAGE_ID) : "";
        String residence = mm.getMap().get(FormKeyIDs.CLUSTER_ID) + " / " + mm.getMap().get(FormKeyIDs.COMMUNITY_ID );

        if (gott != null) {
            residence = residence + " / " + gott;
        }

        String Id = participant.getExternalId();

        holder.participantNameView.setText(fullName);

        holder.participantNeighborhoodView.setText(residence); //this can be updated when changes are pulled in from form updates

        holder.participantIdView.setText(Id);

        holder.participantSelectedView.setChecked(selectedParticipants.contains(participant));

        if (participant.getPhoto() != null) {
            holder.participantPhotoView.setImageBitmap(participant.getPhoto().getBitmap());
        }
        else {
            holder.participantPhotoView.setImageResource(R.drawable.add_photo);
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView participantNameView;
        private TextView participantNeighborhoodView;
        private TextView participantIdView;
        private ImageView participantPhotoView;
        private CheckBox participantSelectedView;


        /*
         *
         */
        public ViewHolder(View listLayoutView) {
            super(listLayoutView);

            //this.setIsRecyclable(false);
            listLayoutView.setOnClickListener(this);

            participantNameView = (TextView) listLayoutView.findViewById(R.id.select_participant_name);
            participantNeighborhoodView = (TextView) listLayoutView.findViewById(R.id.select_participant_neighborhood);
            participantIdView = (TextView) listLayoutView.findViewById(R.id.select_participant_id);
            participantPhotoView = (ImageView) listLayoutView.findViewById(R.id.select_participant_photo);

            participantSelectedView = (CheckBox) listLayoutView.findViewById(R.id.selected_radio_button);
            participantSelectedView.setVisibility(View.VISIBLE);

            participantSelectedView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final int pos = getLayoutPosition();

                    final StoredParticipant participant = participants.get(pos);

                    if (isChecked){
                        if (!selectedParticipants.contains(participant)) {
                            selectedParticipants.add(participant);
                        }
                    } else {
                        selectedParticipants.remove(participant);
                    }
                }

            });

        }



        /**
         * OnClick is overrided to open the participant profile and passes the participant ID to the activity
         * @param v view that is clicked
         */
        @Override
        public void onClick(View v){
            int pos = getLayoutPosition();

            // do nothing
        }

    }

    public List<StoredParticipant> getSelectedParticipants() {
        return selectedParticipants;
    }

    public void setSelectedParticipants(List<StoredParticipant> selectedParticipants) {
        this.selectedParticipants = selectedParticipants;
    }
}
