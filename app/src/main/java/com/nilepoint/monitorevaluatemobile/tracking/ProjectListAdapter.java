package com.nilepoint.monitorevaluatemobile.tracking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nilepoint.model.Project;
import com.nilepoint.monitorevaluatemobile.R;

import java.util.ArrayList;

/**
 * Created by claudiatrafton on 9/10/17.
 */

public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListAdapter.ViewHolder> {

    protected static final String TAG = "LogListAdapater";

    //Beginning of adapter class code
    private ArrayList<Project> projects;
    private Activity activity;
    private String flag;

    public ProjectListAdapter(ArrayList<Project> projects, Activity activity, String flag){
        this.projects = projects;
        this.activity = activity;
        this.flag = flag;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.setting_title_card, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, parent.getContext());

        return viewHolder;
    }

    //Bind the data to the view
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //TODO move this to the activity
        Log.d(TAG, "ViewHolder data bound!");

        Project proj = projects.get(position);

        holder.projectTitle.setText(proj.getName());



    }

    @Override
    public int getItemCount() {
        return projects.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView projectTitle;
        private Context context;

        /*
         *
         */
        public ViewHolder(View listLayoutView, Context c) {
            super(listLayoutView);
            listLayoutView.setOnClickListener(this);
            context = c;
            projectTitle = (TextView) listLayoutView.findViewById(R.id.setting_card_text);
        }

        /**
         * OnClick is overrided to open the log profile and passes the log ID to the activity
         * @param v view that is clicked
         */
        @Override
        public void onClick(View v){
            int pos = getLayoutPosition();
            Project project = projects.get(pos);
            if(flag.equals(TrackingUtils.ACTIVITY_TRACKING_FLAG)){
                Intent intent = new Intent(activity, SelectActivityActivity.class);
                intent.putExtra("id", project.getId());
                activity.startActivity(intent);
            }
            else {
                Log.d("ProjectListAdapter", "No action for this flag!");
            }



        }

        public void openProjectDetails(int pos){
            //TODO fill this in

        }

    } //end of inner ViewHolder inner class

}
