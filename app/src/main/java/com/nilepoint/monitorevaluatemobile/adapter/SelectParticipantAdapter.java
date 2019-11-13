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
import com.nilepoint.monitorevaluatemobile.group.ManageGroupMembersActivity;
import com.nilepoint.monitorevaluatemobile.group.GroupUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by claudiatrafton on 6/8/17.
 */

public class SelectParticipantAdapter extends RecyclerView.Adapter<SelectParticipantAdapter.ViewHolder> {

    private static final String ACTIVITY_TRACKING = ActivityTrackingHostActivity.class.getClass().getName();
    private static final String GROUP_MEMBERS_ADD = ActivityTrackingHostActivity.class.getClass().getName();




    private List<StoredParticipant> participants = new ArrayList<>();
    private Activity activity;
    private boolean selectable, deletable;
    private String activityId;

    public SelectParticipantAdapter(List<StoredParticipant> participants,
                                    Activity activity, String activityId,
                                    boolean selectable, boolean deletable){
        this.participants = participants;
        this.activity = activity;
        this.selectable = selectable;
        this.deletable = deletable;
        this.activityId = activityId;
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
        String residence = mm.getMap().get(FormKeyIDs.CLUSTER_ID) + " , " + mm.getMap().get(FormKeyIDs.COMMUNITY_ID )+ " , " + mm.getMap().get(FormKeyIDs.VILLAGE_ID);
        String Id = participant.getId();//.substring(0,8);

        holder.participantNameView.setText(fullName);

        holder.participantNeighborhoodView.setText(residence); //this can be updated when changes are pulled in from form updates

        holder.participantIdView.setText(Id);

        if (holder.participantSelectedView != null) {
            if(activityId.equals(ACTIVITY_TRACKING))
                holder.participantSelectedView.setChecked(getHostActivity(activity).getAttendees().contains(participant));
            //else
                //holder.participantSelectedView.setChecked(true); //TODO update with members that arent already in the group
            else if(activityId.equals(GROUP_MEMBERS_ADD))
                holder.participantSelectedView.setChecked(getAddMemberToGroupActivity(activity).getGroupMembers().contains(participant));
        }

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
            Log.d("ADAPTERSELECT","Attendees: " + participants.size());
            return participants.size();
        }
        else {
            Log.d("ADAPTERSELECT","No attendees :(");
            return 0;
        }

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView participantNameView;
        private TextView participantNeighborhoodView;
        private TextView participantIdView;
        private ImageView participantPhotoView;
        private CheckBox participantSelectedView;
        private ImageButton participantDeleteView;

        /*
         *
         */
        public ViewHolder(View listLayoutView, final Activity activity, final String activityId) {
            super(listLayoutView);
            final int pos = getLayoutPosition();
            //this.setIsRecyclable(false);
            listLayoutView.setOnClickListener(this);
            Log.d("SelectAdapter", "ViewHolder created!");
            participantNameView = (TextView) listLayoutView.findViewById(R.id.select_participant_name);
            participantNeighborhoodView = (TextView) listLayoutView.findViewById(R.id.select_participant_neighborhood);
            participantIdView = (TextView) listLayoutView.findViewById(R.id.select_participant_id);
            participantPhotoView = (ImageView) listLayoutView.findViewById(R.id.select_participant_photo);

            if(selectable){
                participantSelectedView = (CheckBox) listLayoutView.findViewById(R.id.selected_radio_button);
                participantSelectedView.setVisibility(View.VISIBLE);
                participantSelectedView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) {
                            if(activityId.equals(ACTIVITY_TRACKING))
                            getHostActivity(activity).addAttendee(participants.get(pos));

                            else
                                onSelectAddToGroups(getLayoutPosition(),activity);

                            participantSelectedView.setChecked(true);
                        } else{
                                if(activityId.equals(GROUP_MEMBERS_ADD))
                                    getHostActivity(activity).removeAttendee(participants.get(pos));
                                else
                                    onDeselectGroup(getLayoutPosition(), activity);

                            participantSelectedView.setChecked(false);
                        }
                        }

                });

            }

            if(deletable){
                participantDeleteView = (ImageButton) listLayoutView.findViewById(R.id.deselect_participant_from_activity_button);
                participantDeleteView.setVisibility(View.VISIBLE);
                participantDeleteView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v.equals(participantDeleteView)){
                            Log.d("HOST", "layout position is: " + getLayoutPosition());
                            getHostActivity(activity).removeAttendee(participants.get(getAdapterPosition()));
                            notifyItemRemoved(getLayoutPosition());
                            notifyItemRangeChanged(getLayoutPosition(), getHostActivity(activity).getAttendees().size());
                        }

                    }
                });

            }

        }


        /**
         * defines behavior for checked items when in a group context
         * @param pos
         */
        public void onSelectAddToGroups(int pos, Activity activity){
            ManageGroupMembersActivity act = (ManageGroupMembersActivity) activity;
            GroupUtils.addParticipantToGroup(act.getGroup().getId(), participants.get(pos));

        }

        public void onDeselectGroup(int pos, Activity activity){
            ManageGroupMembersActivity act = (ManageGroupMembersActivity) activity;
            GroupUtils.removeParticipantFromGroup(act.getGroup().getId(), participants.get(pos));

        }


        public ActivityTrackingHostActivity getHostActivity(Activity activity){
            return (ActivityTrackingHostActivity) activity;
        }

        public ManageGroupMembersActivity getAddMemberToGroupActivity(Activity activity){
            return (ManageGroupMembersActivity) activity;
        }

        /**
         * OnClick is overrided to open the participant profile and passes the participant ID to the activity
         * @param v view that is clicked
         */
        @Override
        public void onClick(View v){
            int pos = getLayoutPosition();


        }


    } //end of inner ViewHolder inner class

    public ActivityTrackingHostActivity getHostActivity(Activity activity) {
        return (ActivityTrackingHostActivity) activity;
    }

    public ManageGroupMembersActivity getAddMemberToGroupActivity(Activity activity){
        return (ManageGroupMembersActivity) activity;
    }


}