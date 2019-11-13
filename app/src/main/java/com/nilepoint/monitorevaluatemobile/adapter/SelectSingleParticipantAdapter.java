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
 * Created by claudiatrafton on 6/8/17.
 */

public class SelectSingleParticipantAdapter extends RecyclerView.Adapter<SelectSingleParticipantAdapter.ViewHolder> {

    private static final String ACTIVITY_TRACKING = ActivityTrackingHostActivity.class.getClass().getName();
    private static final String GROUP_MEMBERS_ADD = ActivityTrackingHostActivity.class.getClass().getName();




    private List<StoredParticipant> participants = new ArrayList<>();
    private Activity activity;
    private boolean selectable, deletable;
    private String activityId;
    private ParticipantSelectedListener listener;

    public SelectSingleParticipantAdapter(List<StoredParticipant> participants, ParticipantSelectedListener listener){
        this.participants = participants;
        this.listener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_participant_card, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, activity, activityId);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //TODO move this to the activity
        final StoredParticipant participant = participants.get(position);

        MapMessage mm = participant.toMessage();

        String fullName = participant.getFirstName() + " " + participant.getLastName();

        String gott = mm.getMap().get(FormKeyIDs.VILLAGE_ID) != null ? mm.getMap().get(FormKeyIDs.VILLAGE_ID) : "";

        String residence = mm.getMap().get(FormKeyIDs.CLUSTER_ID) + " , " + mm.getMap().get(FormKeyIDs.COMMUNITY_ID );

        if (mm.getMap().get(FormKeyIDs.VILLAGE_ID) != null){
            residence = residence + " , " + mm.getMap().get(FormKeyIDs.VILLAGE_ID);
        }

        String Id = participant.getExternalId();

        holder.participantNameView.setText(fullName);

        holder.participantNeighborhoodView.setText(residence);

        holder.participantIdView.setText(Id);

        if (participant.getPhoto() != null) {
            holder.participantPhotoView.setImageBitmap(participant.getPhoto().getBitmap());
        }
        else {
            holder.participantPhotoView.setImageResource(R.drawable.add_photo);
        }


    }

    @Override
    public int getItemCount() {
        if (participants != null){
            return participants.size();
        }

        return 0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView participantNameView;
        private TextView participantNeighborhoodView;
        private TextView participantIdView;
        private ImageView participantPhotoView;

        /*
         *
         */
        public ViewHolder(View listLayoutView, final Activity activity, final String activityId) {
            super(listLayoutView);
            final int pos = getLayoutPosition();
            //this.setIsRecyclable(false);
            listLayoutView.setOnClickListener(this);

            participantNameView = (TextView) listLayoutView.findViewById(R.id.select_participant_name);
            participantNeighborhoodView = (TextView) listLayoutView.findViewById(R.id.select_participant_neighborhood);
            participantIdView = (TextView) listLayoutView.findViewById(R.id.select_participant_id);
            participantPhotoView = (ImageView) listLayoutView.findViewById(R.id.select_participant_photo);

        }

        /**
         * OnClick is overrided to open the participant profile and passes the participant ID to the activity
         * @param v view that is clicked
         */
        @Override
        public void onClick(View v){
            int pos = getLayoutPosition();

            listener.onParticipantSelected(participants.get(pos));

            System.out.println("Fired listener " + pos);
        }


    } //end of inner ViewHolder inner class


    public static interface ParticipantSelectedListener {
        void onParticipantSelected(StoredParticipant participant);
    }

}