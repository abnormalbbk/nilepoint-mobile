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
import com.nilepoint.model.StoredLog;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.camera.SelectPhotoActivity;

import java.util.List;

/**
 * Created by claudiatrafton on 4/7/17.
 * Adapter class to display data for each log
 */

public class LogListAdapter extends RecyclerView.Adapter<LogListAdapter.ViewHolder> {

    protected static final String TAG = "LogListAdapater";

    //Beginning of adapter class code
    private List<StoredLog> mLogs;
    private Context mContext;
    private boolean isClickable;

    public LogListAdapter(List<StoredLog> logs, Context context, boolean isClickable){
        mLogs = logs;
        mContext = context;
        this.isClickable = isClickable;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.log_card_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, parent.getContext());

        return viewHolder;
    }

    //Bind the data to the view
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //TODO move this to the activity
        Log.d(TAG, "ViewHolder data bound!");

        StoredLog log = mLogs.get(position);

        holder.logNameView.setText(log.getSeverity() + " : " + log.getText());

        if (log.getDevice() != null) {
            holder.logDeviceView.setText(log.getDevice().getId());
        } else {
            holder.logDeviceView.setText("No Device");
        }

        holder.logDateView.setText(log.getDateCreated().toString()); //this can be updated when changes are pulled in from form updates

    }

    @Override
    public int getItemCount() {
        return mLogs.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView logNameView;
        private TextView logDateView;
        private TextView logDeviceView;
        private Context context;

        /*
         *
         */
        public ViewHolder(View listLayoutView, Context c) {
            super(listLayoutView);
            listLayoutView.setOnClickListener(this);
            context = c;
            logNameView = (TextView) listLayoutView.findViewById(R.id.list_log_text);
            logDateView = (TextView) listLayoutView.findViewById(R.id.list_log_date);
            logDeviceView = (TextView) listLayoutView.findViewById(R.id.list_log_device);
            Log.d(TAG, "ViewHolder created!");

        }

        /**
         * OnClick is overrided to open the log profile and passes the log ID to the activity
         * @param v view that is clicked
         */
        @Override
        public void onClick(View v){
            int pos = getLayoutPosition();

            if (pos >= mLogs.size()){
                Log.e(TAG, "Got a position that was > the data (pos: " + pos + ", data: " + mLogs.size() + ")");
                return;
            }

            if(isClickable){
               // openLogProfile(pos, context, mLogs.get(pos).getId());
               // Log.d("LogList", mLogs.get(pos).getId() + " Log Clicked!");
            }

        }

        /*
         *Opens an activity that displays the log selected
         * @param pos the position in the recyclerView
         * @param current application context
        */
        public void openLogProfile(int pos, Context context, String key){
          /*  final String extraId = "spId";
            Intent intent = new Intent(context, LogProfileActivity.class);
            intent.putExtra(extraId, key);
            context.startActivity(intent);*/
        }

    } //end of inner ViewHolder inner class

}

