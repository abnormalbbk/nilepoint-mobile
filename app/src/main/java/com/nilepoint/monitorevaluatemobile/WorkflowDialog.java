package com.nilepoint.monitorevaluatemobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;


/**
 * Created by claudiatrafton on 5/23/17.
 */

public class WorkflowDialog {

    private Context context;
    private String title;
    private String message;


    /**
     * simple dialog for finish on positive, cancel on negative for general forms
     *@param goHome if true, the workflow should finish to home, otherwise, just finish()
     */
    public WorkflowDialog(Context context, final Activity currentActivity, final boolean goHome){
        this.context = context;
        final Context finalContext = context;
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(getDefaultTitle());
        alertDialogBuilder.setMessage(getDefaultMessage());
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (goHome){
                    Intent intent = new Intent(currentActivity, HomeActivity.class);
                    currentActivity.startActivity(intent);
                    currentActivity.finish();
                }
                else {
                    currentActivity.finish();
                }

            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDefaultTitle() {
        return "Are you sure you want to go back?";
    }

    public String getDefaultMessage() {
        return "All data entered will not be saved.";

    }


}
