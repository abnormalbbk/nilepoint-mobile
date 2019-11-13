package com.nilepoint.monitorevaluatemobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.nilepoint.model.Photo;
import com.nilepoint.model.User;

import io.realm.Realm;

/***
 *
 * Created by claudiatrafton on 5/8/17.
 * Class that stores information about an option that can be on the Homescreen. It was created this way to make it easy to add and remove options
 */

public class UserOption {

    private String userId;
    private String text;
    private int iconId; //get icon from resource file
    private int colorId; //get color from resource file
    private Intent intent; //an intent for the activity that should be opened on click

    public UserOption(User user, int colorId, Intent intent){
        this.userId = user.getId();
        this.colorId = colorId;
        this.intent = intent;
    }


    //for the "add user" button appended to the bottom of the recyclerViews
    public UserOption(Context context, int colorId){
        this.userId = null;
        this.text = "Add a user";
        this.colorId = colorId;
        this.intent = null;

        //this.intent = new Intent() //TODO go to the edit screen
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public Intent getIntent() {
        if (intent != null) {
            this.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getUser() {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            User user = realm.where(User.class).equalTo("id", userId).findFirst();

            user.getPhoto();

            return user;
        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }
    public Bitmap getPhoto() {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            User user = realm.where(User.class).equalTo("id", userId).findFirst();

            if (user.getPhoto() == null){
                return null;
            }

            return user.getPhoto().getBitmap();
        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }

    public void setUser(User user) {
        this.userId = user.getId();
    }
}
