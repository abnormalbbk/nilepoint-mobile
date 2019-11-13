package com.nilepoint.monitorevaluatemobile.selector;

import android.view.View;

/**
 * Created by ashaw on 9/14/17.
 */

public class Selection {

    String title;

    View.OnClickListener onClick;

    public Selection(String title, View.OnClickListener onClick) {
        this.title = title;
        this.onClick = onClick;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public View.OnClickListener getOnClick() {
        return onClick;
    }

    public void setOnClick(View.OnClickListener onClick) {
        this.onClick = onClick;
    }
}
