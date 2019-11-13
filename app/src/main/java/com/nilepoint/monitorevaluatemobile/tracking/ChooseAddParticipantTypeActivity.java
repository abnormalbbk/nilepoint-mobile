package com.nilepoint.monitorevaluatemobile.tracking;

import android.content.Intent;
import android.view.View;

import com.nilepoint.monitorevaluatemobile.participant.BarcodeScannerHostActivity;
import com.nilepoint.monitorevaluatemobile.selector.GenericSelectorActivity;
import com.nilepoint.monitorevaluatemobile.selector.Selection;

import java.util.ArrayList;

/**
 * Created by ashaw on 9/14/17.
 */

public class ChooseAddParticipantTypeActivity extends GenericSelectorActivity {

    public final int PARTICIPANT_SINGLE_SELECT = 1;
    public final int PARTICIPANT_MULTI_SELECT = 2;

    @Override
    protected ArrayList<Selection> getSelections() {
        ArrayList<Selection> selections = new ArrayList<>();

        /**
         * Go to the group selection
         */
        selections.add(new Selection("Group", new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ChooseAddParticipantTypeActivity.this,
                        SelectGroupHostActivity.class), PARTICIPANT_MULTI_SELECT);
            }
        }));

        /**
         * Go to the individual participant search.
         *
         */
        selections.add(new Selection("Individual Search", new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ChooseAddParticipantTypeActivity.this,
                        SelectParticipantHostActivity.class), PARTICIPANT_SINGLE_SELECT);
            }
        }));

        /**
         * Go to the barcode scanner
         */

        selections.add(new Selection("Barcode Scan", new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseAddParticipantTypeActivity.this,
                BarcodeScannerHostActivity.class);
                intent.putExtra("activityStartFlag", "activityMembers");
                startActivityForResult(intent, PARTICIPANT_SINGLE_SELECT);
            }
        }));

        return selections;
    }

    @Override
    /**
     * On activity result, pass through the result to the calling activity.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // pass through the result

        setResult(1, data);

        finish();
    }
}
