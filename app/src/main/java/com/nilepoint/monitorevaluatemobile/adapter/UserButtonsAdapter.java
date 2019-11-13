package com.nilepoint.monitorevaluatemobile.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.nilepoint.api.MobileDevice;
import com.nilepoint.model.Photo;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.UserOption;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;

import java.util.List;

import io.paperdb.Paper;
import io.realm.Realm;

/**
 * Created by claudiatrafton on 5/8/17.
 */

public class UserButtonsAdapter extends RecyclerView.Adapter<UserButtonsAdapter.ViewHolder> {

    public static final String TAG = "UserButtonsAdapter";

    private List<UserOption> userOptions;
    private Context context;
    private Boolean checkboxVisible = false;

    public UserButtonsAdapter(List<UserOption> userOptions, Context mContext) {
        this.userOptions = userOptions;
        this.context = mContext;
    }
    public UserButtonsAdapter(List<UserOption> userOptions, Context mContext, Boolean checkboxVisible) {
        this.userOptions = userOptions;
        this.context = mContext;
        this.checkboxVisible = checkboxVisible;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_activity_card, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, context);
        return viewHolder;
    }

    private int getColor(Context context, int id){
        return ContextCompat.getColor(context, id);
    }

    @Override
    @TargetApi(23)
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context ctx = holder.cardView.getContext();
        UserOption hopt = userOptions.get(position);

        int color = ContextCompat.getColor(ctx,hopt.getColorId());
        Drawable fillerPic;
        Bitmap fillerPicBmp;

        if (color == getColor(ctx, R.color.colorWhite)) { //if the background is white, color the text black
            holder.cardText.setTextColor(holder.cardView.getContext().getColor(R.color.colorBlack));
            fillerPic = ResourcesCompat.getDrawable(ctx.getResources(),R.drawable.add_new_photo_white_transparent,null);
            fillerPicBmp = BitmapFactory.decodeResource(ctx.getResources(),R.drawable.add_new_photo_white_transparent);
            fillerPicBmp.setHasAlpha(true);
        }
        else {
            holder.cardText.setTextColor(getColor(ctx,R.color.colorWhite));
            fillerPic = ResourcesCompat.getDrawable(ctx.getResources(),R.drawable.add_new_photo_white_transparent,null);
            fillerPicBmp = BitmapFactory.decodeResource(ctx.getResources(),R.drawable.add_new_photo_white_transparent);
            fillerPicBmp.setHasAlpha(true);
        }

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            User user = realm.where(User.class).equalTo("id", hopt.getUserId()).findFirst();

            if (user != null){
                // protect against closed realm
                Photo photo = user.getPhoto();


                if (photo != null) {
                    holder.cardImage.setImageBitmap(photo.getBitmap());
                }
                else {
                    holder.cardImage.setImageDrawable(fillerPic);
                    holder.cardImage.setImageBitmap(fillerPicBmp);
                }
                holder.cardText.setText(user.getFirstName() + " " + user.getLastName());
            }

            else {
                //holder.cardImage.setImageResource(hopt.getIconId());
                holder.cardText.setText(hopt.getText());
                //holder.cardImage.setImageDrawable(fillerPic);
                holder.cardImage.setImageBitmap(fillerPicBmp);
            }


            holder.cardView.setCardBackgroundColor(color);

            if (checkboxVisible == true){
                holder.checkbox.setVisibility(View.VISIBLE);
                holder.checkbox.setChecked(user.getLoginEnabled());
            }

        } finally {
            if (realm != null){
                realm.close();
            }
        }

    }

    @Override
    public int getItemCount() {
        return userOptions != null ? userOptions.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private CardView cardView;
        private TextView cardText;
        private ImageView cardImage;
        private CheckBox checkbox;
        private Context context;

        public ViewHolder(View homeButtonsListLayout, Context c){
            super(homeButtonsListLayout);
            homeButtonsListLayout.setOnClickListener(this);
            cardView = (CardView) homeButtonsListLayout.findViewById(R.id.card_view_home);
            cardView.getLayoutParams().height = getCardHeight(homeButtonsListLayout);
            cardText = (TextView) homeButtonsListLayout.findViewById(R.id.home_activity_card_text);
            cardImage = (ImageView) homeButtonsListLayout.findViewById(R.id.home_activity_card_image);
            checkbox = (CheckBox) homeButtonsListLayout.findViewById(R.id.home_card_checkbox);
            context = c;
            Log.d(TAG, "ViewHolder created!");
        }

        /***
         * Gets the size of the window and then uses it to dynamically create card height.
         * @param v view context
         * @return card height depending on amount of items
         */
        public int getCardHeight(View v){
           /* DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int cardHeight = dm.heightPixels/6; */
            return 200;
        }

        @Override
        public void onClick(View v) {
            int pos = getLayoutPosition();

            UserOption cur = userOptions.get(pos);

            try {
                WLTrackApp.afterLogin(this.context, cur.getUserId());
            } catch (Exception ex){
                Log.e(TAG, "Could not get mobile device to update user.", ex);
                Crashlytics.logException(ex);
            }

            if(cur.getIntent() != null){
                context.startActivity(cur.getIntent());
            }

        }
    }

}
