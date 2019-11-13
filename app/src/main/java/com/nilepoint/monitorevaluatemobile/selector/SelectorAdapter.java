package com.nilepoint.monitorevaluatemobile.selector;

/**
 * Created by ashaw on 9/14/17.
 */

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
import com.nilepoint.monitorevaluatemobile.tracking.SelectActivityActivity;
import com.nilepoint.monitorevaluatemobile.tracking.TrackingUtils;

import java.util.ArrayList;

/**
 * Generic adapter for creating lists of selections.
 *
 */
public class SelectorAdapter extends RecyclerView.Adapter<SelectorAdapter.ViewHolder> {

    protected static final String TAG = "SelectorAdapter";

    //Beginning of adapter class code
    private ArrayList<Selection> selections;
    private Activity activity;

    public SelectorAdapter(ArrayList<Selection> selections, Activity activity){
        this.selections = selections;
        this.activity = activity;
    }

    @Override
    public SelectorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.setting_title_card, parent, false);

        SelectorAdapter.ViewHolder viewHolder
                = new SelectorAdapter.ViewHolder(itemLayoutView, parent.getContext());

        return viewHolder;
    }

    //Bind the data to the view
    @Override
    public void onBindViewHolder(final SelectorAdapter.ViewHolder holder, int position) {
        Selection str = selections.get(position);

        holder.title.setText(str.getTitle());
    }

    @Override
    public int getItemCount() {
        return selections.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView title;
        private Context context;

        /*
         *
         */
        public ViewHolder(View listLayoutView, Context c) {
            super(listLayoutView);

            listLayoutView.setOnClickListener(this);

            context = c;

            title = (TextView) listLayoutView.findViewById(R.id.setting_card_text);

            Log.d(TAG, "ViewHolder created!");

        }

        /**
         * OnClick is overrided to open the log profile and passes the log ID to the activity
         * @param v view that is clicked
         */
        @Override
        public void onClick(View v){
            int pos = getLayoutPosition();

            Selection selection = selections.get(pos);

            selection.getOnClick().onClick(v);
        }

    }

}

