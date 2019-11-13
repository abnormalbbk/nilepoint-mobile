package com.nilepoint.monitorevaluatemobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;
import com.nilepoint.monitorevaluatemobile.participant.ParticipantProfileActivity;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.camera.SelectPhotoActivity;

import java.util.List;
import java.util.Map;

/**
 * Created by claudiatrafton on 4/7/17.
 * Adapter class to display data for each participant
 */

public class ParticipantListAdapater extends RecyclerView.Adapter<ParticipantListAdapater.ViewHolder> {

    protected static final String TAG = "ParticipantListAdapter";

    //Beginning of adapter class code
    private List<StoredParticipant> mParticipants;
    private Context mContext;
    private boolean isClickable;

    public ParticipantListAdapater(List<StoredParticipant> participants, Context context, boolean isClickable){
        mParticipants = participants;
        mContext = context;
        this.isClickable = isClickable;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.participant_card_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, parent.getContext());

        return viewHolder;
    }

    //Bind the data to the view
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //TODO move this to the activity

        if (position >= mParticipants.size()){
            return;
        }

        final StoredParticipant participant = mParticipants.get(position);
        MapMessage sp =  participant.toMessage();
        Map<String,String> map = sp.getMap();
        final int positionFinal = position;

        String fullName = sp.get(FormKeyIDs.GIVEN_NAME_ID, "") + " " + sp.get(FormKeyIDs.FATHER_NAME_ID, "") + getGenderSuffix(sp);
        String gott = sp.get(FormKeyIDs.VILLAGE_ID, (String) null);
        String residence = sp.getMap().get(FormKeyIDs.CLUSTER_ID) + sp.getMap().get(FormKeyIDs.COMMUNITY_ID )+ "," + sp.getMap().get(FormKeyIDs.VILLAGE_ID);
        String Id = participant.getExternalId();

        holder.participantNameView.setText(fullName);

        holder.participantNeighborhoodView.setText(map.get(FormKeyIDs.CLUSTER_ID) + " / " + map.get(FormKeyIDs.COMMUNITY_ID)); //this can be updated when changes are pulled in from form updates


        if (gott != null){
            holder.participantNeighborhoodView.append(" / " + sp.get(FormKeyIDs.VILLAGE_ID, ""));
        }

        holder.participantIdView.setText(Id);

        if (participant.getPhoto() != null) {
            holder.participantPhotoView.setImageBitmap(participant.getPhoto().getBitmap());
        } else {
            holder.participantPhotoView.setImageResource(R.drawable.add_photo);
        }

        holder.participantPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(participant.getPhoto() == null) {
                    Intent intent = new Intent(mContext, SelectPhotoActivity.class);

                    intent.putExtra("participant.id", participant.getId());

                    mContext.startActivity(intent);
                }
                else {
                    Log.d(TAG, "There is already a photo - opening profile!");
                    holder.openParticipantProfile(positionFinal ,mContext, participant.getId());
                }
            }
        });


    }

    /**
     * Gets the gender suffix to append to the name. TODO when gender is validated, can be changed to ternery operator
     * @param participant
     * @return String that represents the gender of the participant
     */
    public String getGenderSuffix(MapMessage participant){
        if(participant.getMap().get(FormKeyIDs.GENDER_ID) == null){
            return " (?)";
        }
        else if(participant.getMap().get(FormKeyIDs.GENDER_ID).equals("Female")){
            return " (F)";
        }
        else if(participant.getMap().get(FormKeyIDs.GENDER_ID).equals("Male")){
            return " (M)";
        }
        return "(?)";

    }



    /* *
      * Sorts the data according to the specification of the user by the slider
      * sortByFirst name is true, sort by first name. otherwise, sort by last
     */

    public String getParticipant(int pos, String field){
        String id = mParticipants.get(pos).toMessage().getMap().get(field);

        return id;
    }

    @Override
    public int getItemCount() {
        return mParticipants.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView participantNameView;
        private TextView participantNeighborhoodView;
        private TextView participantIdView;
        private ImageView participantPhotoView;
        private Context context;

        /*
         *
         */
        public ViewHolder(View listLayoutView, Context c) {
            super(listLayoutView);
            listLayoutView.setOnClickListener(this);
            context = c;
            participantNameView = (TextView) listLayoutView.findViewById(R.id.list_participant_name);
            participantNeighborhoodView = (TextView) listLayoutView.findViewById(R.id.list_participant_neighborhood);
            participantIdView = (TextView) listLayoutView.findViewById(R.id.list_participant_id);
            participantPhotoView = (ImageView) listLayoutView.findViewById(R.id.list_participant_photo);
            participantPhotoView.setClickable(false);

        }

        /**
         * OnClick is overrided to open the participant profile and passes the participant ID to the activity
         * @param v view that is clicked
         */
        @Override
        public void onClick(View v){
            int pos = getLayoutPosition();

            if (pos >= mParticipants.size()){
                Log.e(TAG, "Got a position that was > the data (pos: " + pos + ", data: " + mParticipants.size() + ")");
                return;
            }
            if(isClickable){
                openParticipantProfile(pos, context, mParticipants.get(pos).getId());
                Log.d("ParticipantList", mParticipants.get(pos).getId() + " Participant Clicked!");


            }

        }

        /*
         *Opens an activity that displays the participant selected
         * @param pos the position in the recyclerView
         * @param current application context
        */
        public void openParticipantProfile(int pos, Context context, String key){
            //TODO open individual profile for participant, will open host activity with fragment for details
            final String extraId = "spId";
            Intent intent = new Intent(context, ParticipantProfileActivity.class);
            intent.putExtra(extraId, key);
            context.startActivity(intent);
        }

    } //end of inner ViewHolder inner class

}
