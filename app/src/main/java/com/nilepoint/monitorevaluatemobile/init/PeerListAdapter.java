package com.nilepoint.monitorevaluatemobile.init;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nilepoint.api.MobileDevice;
import com.nilepoint.dtn.discovery.NeighborRegistration;
import com.nilepoint.dtn.discovery.Node;
import com.nilepoint.model.Photo;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.dtn.MobileDeviceRegistry;
import com.nilepoint.monitorevaluatemobile.dtn.commands.ResponseMessageCommand;

import java.util.List;

import io.paperdb.Paper;

/**
 * Created by ashaw on 11/21/17.
 */

public class PeerListAdapter extends RecyclerView.Adapter<PeerListAdapter.ViewHolder> {
    protected static final String TAG = "LogListAdapater";

    //Beginning of adapter class code
    private List<Node> nodes;
    private Context context;

    private Activity activity;

    public PeerListAdapter(Activity activity, List<Node> nodes, Context context){
        this.activity = activity;
        this.nodes = nodes;
        this.context = context;

        for (Node node : nodes){

        }
    }

    @Override
    public PeerListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.peer_card_layout, parent, false);

        PeerListAdapter.ViewHolder viewHolder = new PeerListAdapter.ViewHolder(itemLayoutView, parent.getContext());

        return viewHolder;
    }

    //Bind the data to the view
    @Override
    public void onBindViewHolder(final PeerListAdapter.ViewHolder holder, int position) {
        //TODO move this to the activity
        Log.d(TAG, "ViewHolder data bound!");

        final Node node = nodes.get(position);

        if (node == null){
            return;
        }

        holder.peerNameView.setText(node.getName() + " / " + node.getLocalAddress());

        NeighborRegistration registration = node.getLayer().getNeighborRegistry()
                .findRegistration(node);


        MobileDeviceRegistry mdRegistry = MobileDeviceRegistry.getInstance();

        final MobileDevice device = mdRegistry.findByNode(node);

        System.out.println(mdRegistry.nodeDeviceMap.entrySet());

        if (device != null){
           Photo photo = mdRegistry.getPhoto(device);
           if (photo != null){
               holder.deviceImageView.setImageBitmap(photo.getBitmap());
           }
        }

        holder.peerLastContact.setText(registration.getLastContact().toString()); //this can be updated when changes are pulled in from form updates

        holder.peerSyncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ConnectedPeerActivity.class);

                intent.putExtra("device.id", device.getId());
                intent.putExtra("isInit", (Boolean) Paper.book().read("data.init"));

                activity.startActivity(intent);
            }
        });


        holder.peerSendHelloBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WLTrackApp.dtnService.btlayer.sendRaw(
                        new ResponseMessageCommand("Hello from " + WLTrackApp.device.getName()),
                        node);
            }
        });

    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView peerNameView;
        private TextView peerLastContact;
        private ImageView deviceImageView;
        private Button peerSyncBtn;
        private Button peerPSyncBtn;
        private Button peerSendHelloBtn;
        private Context context;

        /*
         *
         */
        public ViewHolder(View listLayoutView, Context c) {
            super(listLayoutView);
            listLayoutView.setOnClickListener(this);
            context = c;

            peerNameView = (TextView) listLayoutView.findViewById(R.id.peer_name_view);
            peerLastContact = (TextView) listLayoutView.findViewById(R.id.peer_last_contact);
            peerSyncBtn = (Button) listLayoutView.findViewById(R.id.peer_sync_btn);
            //peerPSyncBtn = (Button) listLayoutView.findViewById(R.id.peer_participant_btn);
            peerSendHelloBtn = (Button) listLayoutView.findViewById(R.id.peer_hello_btn);
            deviceImageView = (ImageView) listLayoutView.findViewById(R.id.peer_photo);

        }

        /**
         * OnClick is overrided to open the log profile and passes the log ID to the activity
         * @param v view that is clicked
         */
        @Override
        public void onClick(View v){
            int pos = getLayoutPosition();

            if (pos >= nodes.size()){
                Log.e(TAG, "Got a position that was > the data (pos: " + pos + ", data: " + nodes.size() + ")");
                return;
            }

            // open node info

        }

    } //end of inner ViewHolder inner class

}
