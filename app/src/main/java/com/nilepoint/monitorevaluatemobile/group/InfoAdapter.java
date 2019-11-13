package com.nilepoint.monitorevaluatemobile.group;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nilepoint.monitorevaluatemobile.R;

import java.util.ArrayList;

/**
 * Created by claudiatrafton on 8/20/17.
 */

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> {

    private ArrayList<InfoPair> info;
    private Context context;

    public InfoAdapter(Context context, ArrayList<InfoPair> info) {
        this.context = context;
        this.info = info;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_info_pair_card, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, parent.getContext());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(InfoAdapter.ViewHolder holder, int pos) {
        holder.title.setText(info.get(pos).getTitle());
        holder.content.setText(info.get(pos).getValue());

    }

    @Override
    public int getItemCount() {
        return info.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView content;

        public ViewHolder(View itemView, Context c){
            super(itemView);
            context = c;
            title = (TextView) itemView.findViewById(R.id.info_header);
            content = (TextView) itemView.findViewById(R.id.info_content);


        }
    }
}
