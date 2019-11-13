package com.nilepoint.monitorevaluatemobile.participant;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.model.Photo;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.adapter.ParticipantReviewInfoAdapter;
import com.nilepoint.monitorevaluatemobile.camera.SelectPhotoActivity;
import com.nilepoint.monitorevaluatemobile.forms.FormDataConverter;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;
import com.nilepoint.monitorevaluatemobile.logging.RemoteLogger;
import com.nilepoint.monitorevaluatemobile.services.ParticipantService;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/***
 * Review the participant data and add a photo to their information
 */
public class ReviewNewParticipantActivity extends AppCompatActivity {

    private Context context;
    private ImageButton photoButton;
    private ImageView profilePhoto;
    private TextView nameView;
    private TextView IdView;
    private TextView addPhotoText;
    private Button doneButton;
    private LinearLayout headerLayout;
    private NestedScrollView scrollView;
    private ArrayList<Map.Entry<String,String>> displayData;
    private RecyclerView.LayoutManager layoutManager;
    private ParticipantReviewInfoAdapter adapter;
    private RecyclerView infoRecycler;
    private Bitmap photoBitmap;
    private boolean photoTaken;

    private RemoteLogger logger = new RemoteLogger();

    private ParticipantService participantService = WLTrackApp.participantService;

    private final String HEAD_OF_HOUSEHOLD_ID = "headOfHouseholdId";

    private MapMessage msg;

    public final String TAG = "ReviewActivity";

    private String headOfHouseholdId;

    public final static int REQUEST_PHOTO = 2888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_new_participant);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_transparent);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Review Profile Information");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);

        context = this;
        //get Information from ParticipantHouseholdFormActivity, and parse to MapMessage
        final Intent intent = getIntent();

        if (intent.hasExtra(HEAD_OF_HOUSEHOLD_ID)){
            headOfHouseholdId = intent.getStringExtra(HEAD_OF_HOUSEHOLD_ID);
        }

        final HashMap<String,String> infoMap = (HashMap<String, String>)intent.getSerializableExtra("filled_form");

        for(Map.Entry<String, String> entry: infoMap.entrySet()){
            Log.d(TAG, "hh info: " + entry.getKey() + "," + entry.getValue());
        }

        FormDataConverter converter = new FormDataConverter();

        msg = converter.toMapMessage(infoMap);

        nameView = (TextView) findViewById(R.id.review_profile_name);
        nameView.setText(msg.get(FormKeyIDs.GIVEN_NAME_ID, "") + " " + msg.get(FormKeyIDs.FATHER_NAME_ID,""));
        IdView = (TextView) findViewById(R.id.review_profile_id);
        addPhotoText = (TextView) findViewById(R.id.review_profile_add_photo_text);
        //IdView.setText(infoMap.get()); TODO what piece of information is this? Is it the UUID or the FH number or PSNP?
        headerLayout = (LinearLayout) findViewById(R.id.profile_review_header);

        displayData = parseData(infoMap);
        infoRecycler = (RecyclerView) findViewById(R.id.review_profile_info_recycler);
        infoRecycler.setFocusable(false);
        layoutManager = new LinearLayoutManager(this);

        adapter = new ParticipantReviewInfoAdapter(displayData, this);
        infoRecycler.setAdapter(adapter);
        infoRecycler.setLayoutManager(layoutManager);
        infoRecycler.setNestedScrollingEnabled(false);

        //Photo and profile header
        profilePhoto = (ImageView) findViewById(R.id.profile_photo_display);
        profilePhoto.setVisibility(View.GONE);

        /* Button listeners */
        photoButton = (ImageButton) findViewById(R.id.review_profile_add_photo_button);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoActivityIntent = new Intent(context, SelectPhotoActivity.class);
                startActivityForResult(photoActivityIntent, REQUEST_PHOTO);
            }
        });

        doneButton = (Button) findViewById(R.id.review_profile_done);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Package up intent with the original map sent in, to have consistency in the finishActivity

                if (headOfHouseholdId == null) {

                    if (photoBitmap != null) {
                        msg.put("photo", new Photo(photoBitmap).toBase64());
                    }

                    final StoredParticipant headHousehold = participantService.createParticipant(null, msg);

                    headOfHouseholdId = headHousehold.getId();

                    logger.info(TAG, "New head of household created id: " + headOfHouseholdId);

                } else {
                    participantService.createParticipant(headOfHouseholdId, msg);

                }

                Intent sendIntent = new Intent(context, FinishNewParticipantActivity.class);

                sendIntent.putExtra("filled_form",intent.getSerializableExtra("filled_form"));
                sendIntent.putExtra(HEAD_OF_HOUSEHOLD_ID, headOfHouseholdId);

                startActivity(sendIntent);
            }
        });


    }

    /**
     * When data is received from the select photo activity, place the photo in the ImageView and save it to the participant
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_PHOTO && resultCode == RESULT_OK){
            photoButton.setVisibility(View.GONE);
            addPhotoText.setVisibility(View.GONE);

            if (data != null) {

                byte[] photoByteArray = data.getByteArrayExtra("photo");

                Log.d(TAG, "Got photo from activity.");

                photoBitmap = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.length);

                profilePhoto.setImageBitmap(BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.length));

                profilePhoto.setVisibility(View.VISIBLE);
                //profilePhoto.setMinimumHeight(headerLayout.getHeight());
                profilePhoto.setMinimumWidth(headerLayout.getWidth());

            } else {
                Log.e(TAG, "Error: could not save photo, data is null");
            }

        }
    }

    /***
     * Parses the data from the map and creates additional specific strings and parses data for design spec
     * @param map the data map from the form activity
     * @return data a Hashmap with the data formatted to design specs, including birthday and residence
     */
    public ArrayList<Map.Entry<String,String>> parseData(Map<String, String> map){
        ArrayList<Map.Entry<String,String>> data = new ArrayList<>();

        String gender = map.get(FormKeyIDs.GENDER_ID);

        String phoneNum = map.get(FormKeyIDs.PHONE_NUMBER_ID);

        String birthday = map.get(FormKeyIDs.BIRTHDAY_ID);

        String residence = map.get(FormKeyIDs.CLUSTER_ID) + "\n" +
                           map.get(FormKeyIDs.COMMUNITY_ID);

        if (map.get(FormKeyIDs.VILLAGE_ID) != null) {
            residence = residence + "\n" +
                    map.get(FormKeyIDs.VILLAGE_ID);
        }



        //create the string - this is maybe not the best way to do this, TODO revisit
        String household = "";
        for(int i = 0; i < FormKeyIDs.HOUSEHOLD_MEMBER_KEYS.length; i++){
            if(map.containsKey(FormKeyIDs.HOUSEHOLD_MEMBER_KEYS[i])) {
                household = household + map.get(FormKeyIDs.HOUSEHOLD_MEMBER_KEYS[i]) + " " + FormKeyIDs.HOUSEHOLD_MEMBER_LABELS[i]  + "\n";
            }
        }

        //Add all of the new data to the list of entries to be formatted in adapter
        data.add(new AbstractMap.SimpleEntry<>("Phone", phoneNum));

        data.add(new AbstractMap.SimpleEntry<>("Birthday",birthday));

        data.add(new AbstractMap.SimpleEntry<>("Sex", gender));

        data.add(new AbstractMap.SimpleEntry<>("Residence", residence));

        data.add(new AbstractMap.SimpleEntry<>("Household", household));

        return data;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //new WorkflowDialog(this, this, true);
    }

    //Navigate home from up arrow in the action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
