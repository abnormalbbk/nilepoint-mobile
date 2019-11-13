package com.nilepoint.monitorevaluatemobile.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.nilepoint.monitorevaluatemobile.HomeOption;
import com.nilepoint.monitorevaluatemobile.R;

import java.util.List;

/**
 * Created by claudiatrafton on 5/8/17.
 */

public class HomeButtonsAdapter extends RecyclerView.Adapter<HomeButtonsAdapter.ViewHolder> {

    public static final String TAG = "HomeButtonsAdapter";

    private List<HomeOption> homeOptions;
    private Context context;

    public HomeButtonsAdapter(List<HomeOption> homeOptions, Context mContext) {
        this.homeOptions = homeOptions;
        this.context = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_activity_card, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, parent.getContext());
        return viewHolder;
    }

    @Override
    @TargetApi(23)
    public void onBindViewHolder(ViewHolder holder, int position) {
        HomeOption hopt = homeOptions.get(position);
        int color = ContextCompat.getColor(holder.cardView.getContext(),hopt.getColorId());
        holder.cardImage.setImageResource(hopt.getIconId());
        holder.cardText.setText(hopt.getText());
        holder.cardText.setTextColor(ContextCompat.getColor(holder.cardView.getContext(),R.color.colorWhite));
        holder.cardView.setCardBackgroundColor(color);

    }

    @Override
    public int getItemCount() {
        return homeOptions != null ? homeOptions.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private CardView cardView;
        private TextView cardText;
        private ImageView cardImage;
        private Context context;

        public ViewHolder(View homeButtonsListLayout, Context c){
            super(homeButtonsListLayout);
            homeButtonsListLayout.setOnClickListener(this);
            cardView = (CardView) homeButtonsListLayout.findViewById(R.id.card_view_home);
            cardView.getLayoutParams().height = getCardHeight(homeButtonsListLayout);
            cardText = (TextView) homeButtonsListLayout.findViewById(R.id.home_activity_card_text);
            cardImage = (ImageView) homeButtonsListLayout.findViewById(R.id.home_activity_card_image);
            context = c;
            Log.d(TAG, "ViewHolder created!");
        }

        /***
         * Gets the size of the window and then uses it to dynamically create card height.
         * @param v view context
         * @return card height depending on amount of items
         */
        public int getCardHeight(View v){
            WindowManager wm = (WindowManager) v.getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return (size.y/(homeOptions.size() + 1)); //additional padding to account for all excess space from match parent
        }

        //TODO fill this is to open the appropriate page
        @Override
        public void onClick(View v) {
            int pos = getLayoutPosition();
            HomeOption cur = homeOptions.get(pos);
            context.startActivity(cur.getIntent());

        }
    }

}
