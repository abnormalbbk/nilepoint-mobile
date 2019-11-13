package com.nilepoint.monitorevaluatemobile.participant;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.activity_tracking.ActivityTrackingHostActivity;
import com.nilepoint.monitorevaluatemobile.group.GroupUtils;
import com.nilepoint.monitorevaluatemobile.group.ManageGroupMembersActivity;
import com.nilepoint.persistence.Datastore;

import java.util.ArrayList;
import java.util.Collections;

import io.realm.Realm;

public class BarcodeScannerHostActivity extends AppCompatActivity {


    public static String TAG = "BarcodeScannerHost";

    private static final String ACTIVITY_TRACKING = "activityTracking";
    private static final String VIEW_PROFILE = "viewProfile";
    private static final String GROUP_MEMBERS = "groupMembers"; //tentative names
    private static final String ACTIVITY_MEMBERS = "activityMembers";

    protected Datastore ds = Datastore.init(this);
    private Intent intent;
    private String activityStartFlag;
    private ArrayList<String> ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner_host);
        scanCustomScanner();
        intent = getIntent();
        activityStartFlag = intent.getStringExtra("activityStartFlag");
        if(activityStartFlag.equals(ACTIVITY_MEMBERS))
            ids = new ArrayList<>();
    }

    public void scanCustomScanner() {
        new IntentIntegrator(this)
                .setOrientationLocked(false)
                .setCaptureActivity(CustomScannerActivity.class)

                .setDesiredBarcodeFormats(Collections.singletonList("PDF_417"))
                .initiateScan();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            String resultStr = result.getContents();
            System.out.println("Barcode result: " + resultStr);
            Realm realm = null;
            try {
                realm = Realm.getDefaultInstance();

                StoredParticipant participant = realm.where(StoredParticipant.class)
                        .equalTo("id", resultStr)
                        .or()
                        .equalTo("externalId",resultStr)
                        .findFirst();

                if ((resultStr == null) || participant == null) {
                    Toast.makeText(this, R.string.cant_find_participant_message, Toast.LENGTH_LONG).show();
                    this.finish();

                } else {
                    Log.d("SCANNER", resultStr);
                    Intent intent;
                    switch(activityStartFlag) {
                        case (VIEW_PROFILE):
                            intent = new Intent(this, ParticipantProfileActivity.class);
                            intent.putExtra("spId", participant.getId());
                            this.startActivity(intent);
                            break;

                        case (ACTIVITY_TRACKING):
                            intent = new Intent(this, ActivityTrackingHostActivity.class);
                            intent.putExtra("spIdScan", participant.getId());
                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivityIfNeeded(intent, 0);
                            break;

                        case (GROUP_MEMBERS):
                            String groupId = this.intent.getStringExtra("groupId");
                            GroupUtils.addParticipantToGroup(groupId, participant);
                            intent = new Intent(this, ManageGroupMembersActivity.class);
                            intent.putExtra("listAction", "ADD");
                            Log.d("SCANNER", "Successfully added " + participant.getFirstName());
                            Toast.makeText(getApplicationContext(), participant.getLastName() + ", " + participant.getFirstName() +
                                    " Successfully added to the group!", Toast.LENGTH_LONG).show();
                            this.startActivity(intent);
                            break;

                        case (ACTIVITY_MEMBERS): //need to figure out multiple scans
                            StoredParticipant participant1 = realm.where(StoredParticipant.class)
                                    .equalTo("externalId", resultStr).findFirst();
                            if (participant1 != null) {
                                ids.add(participant.getId());

                                Toast.makeText(this, participant.getFirstName() + " added!", Toast.LENGTH_SHORT).show();

                                intent = new Intent();

                                intent.putStringArrayListExtra("participant.ids", ids);

                                Log.d(this.getClass().getName(), "You've added: " + ids.size());

                                setResult(RESULT_OK, intent);
                            }
                            break;
                        } //end of switch block
                    this.finish();
                    } //end of else

            } finally {
                if (realm != null){
                    realm.close();
                }
            }

        }
        else {
            // This is important, otherwise the result will not be passed to the fragment

            System.out.println ("No barcode result");

            super.onActivityResult(requestCode, resultCode, data);

            if(activityStartFlag != ACTIVITY_MEMBERS) //allow for repetition in this case
                this.finish();
        }
    }

}
