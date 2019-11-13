package com.nilepoint.monitorevaluatemobile.participant;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.dkharrat.nexusdialog.FormController;
import com.github.dkharrat.nexusdialog.FormElementController;
import com.github.dkharrat.nexusdialog.controllers.FormSectionController;
import com.github.dkharrat.nexusdialog.controllers.SelectionController;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.dtn.DTN;
import com.nilepoint.formbuilder.CustomFormActivity;
import com.nilepoint.formbuilder.RegExInputValidator;
import com.nilepoint.model.Area;
import com.nilepoint.model.ElementValidator;
import com.nilepoint.model.FormElement;
import com.nilepoint.model.FormElementType;
import com.nilepoint.model.StoredForm;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.WorkflowDialog;
import com.nilepoint.monitorevaluatemobile.camera.SelectPhotoActivity;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;
import com.nilepoint.monitorevaluatemobile.logging.RemoteLogger;
import com.nilepoint.monitorevaluatemobile.services.ParticipantService;
import com.nilepoint.utils.DateUtilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmQuery;


/**
 * Created by ashaw on 2/9/17.
 */

public class ParticipantHouseholdFormActivity extends CustomFormActivity {
    public static String TAG = "PFormActivity";
    private MapMessage msg = new MapMessage();

    static String toolbarTitle = "";
    private static final int IMAGE_HEIGHT = 200;

    private String headOfHouseholdId;
    private TextView titleText;
    private Button participantPhotoButton;
    private LinearLayout parentLayout;
    private Context context;
    private Drawable drawablePhoto;

    private String formName;
    private String participantId;
    private StoredParticipant participant;
    private StoredParticipant headOfHousehold;
    private RemoteLogger logger = new RemoteLogger();
    private ParticipantService participantService = WLTrackApp.participantService;

    private LinkedHashMap<String, String> formElements = new LinkedHashMap<>();
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        context = this;

        this.pagePerSection = true;

        formName = "Default";

        formController = new FormController(this);

        setContentView(R.layout.activity_form);


        //titleText = (TextView) findViewById(R.id.form_title_text);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setTitle("Custom Form Example");

        initForm(formController);

        WLTrackApp app = (WLTrackApp) getApplication();

        final Intent intent = getIntent();

        formName = intent.getStringExtra("formName");
        headOfHouseholdId = intent.getStringExtra("headOfHouseholdId");
        participantId = intent.getStringExtra("participant.id");

        if (headOfHouseholdId != null) {
            headOfHousehold = realm.where(StoredParticipant.class).equalTo("id", headOfHouseholdId).findFirst();
        }

        // copy location information of HoH
        if (headOfHousehold != null && participantId == null)
        {
            formElements.put(FormKeyIDs.CLUSTER_ID,headOfHousehold.getCluster());
            formElements.put(FormKeyIDs.VILLAGE_ID,headOfHousehold.getVillage());
            formElements.put(FormKeyIDs.COMMUNITY_ID,headOfHousehold.getCommunity());
            msg.put(FormKeyIDs.CLUSTER_ID,headOfHousehold.getCluster());
            msg.put(FormKeyIDs.VILLAGE_ID,headOfHousehold.getVillage());
            msg.put(FormKeyIDs.COMMUNITY_ID,headOfHousehold.getCommunity());
        }

        Log.i(TAG, String.format("Form: %s HoHId: %s Participant: %s" ,formName, headOfHouseholdId, participantId));

        msg.put("className", "Participant");

        //to be used inside of the inner class
        final String formNameFinal = formName;

        Toolbar titleBar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(titleBar);
        getSupportActionBar().setTitle(formNameFinal);

        //toolbar.setTitle(formName);

        app.getFormService().addListener(this);

        final RealmQuery<StoredForm> forms = realm.where(StoredForm.class).equalTo("name", formName);

        if (forms.count() > 0){
            this.form = forms.findFirst().toForm();

            onFormUpdate(form);
        } else {
            System.out.println("Can't find form " + formName);
        }

        Button btn = (Button)findViewById(R.id.form_next_button);

        /**
         * Load a participant if this is an edit form.
         */

        if (participantId != null){

            participantPhotoButton = new Button(this);
            parentLayout = (LinearLayout) findViewById(R.id.scroll_container);
            participantPhotoButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    ,IMAGE_HEIGHT));

            parentLayout.addView(participantPhotoButton, 0);
            participantPhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, SelectPhotoActivity.class);
                    intent.putExtra("participant.id", participant.getId());
                    startActivity(intent);
                }
            });

            participant = realm
                    .where(StoredParticipant.class)
                    .equalTo("id", participantId)
                    .findFirst();

            // form creation is in the main looper, so we have to load the participant after this.

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    initDynamicData();
                    if (participant == null) {
                        getSupportActionBar().setTitle("Add Household Members");
                    } else {
                        getSupportActionBar().setTitle("Participant Overview Edit");
                    }

                    loadParticipant(participant);


                }
            });

            if(participant.getPhoto() != null) {
                participantPhotoButton.setBackground(new BitmapDrawable(getResources(), participant.getPhoto().getBitmap()));
                participantPhotoButton.setText(R.string.change_photo);
            }
            else {
                participantPhotoButton.setText(R.string.add_photo);
            }

            this.setPagePerSection(false);



            btn.setText("Done");
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (final Map.Entry<FormElement, FormElementController> entryForm : formElementMap.entrySet()) {

                        System.out.println(entryForm.getKey().getId() + " hidden: " + entryForm.getKey().getHidden());

                        if (entryForm.getKey().getHidden() == true) {
                            entryForm.getValue().getView().setVisibility(View.GONE);
                        }

                        if (entryForm.getKey().getType().equals(FormElementType.CHECK)) {
                            try {
                                LinearLayout layout = (LinearLayout) entryForm.getValue().getView();
                                LinearLayout subLayout = (LinearLayout) layout.getChildAt(1);
                                FrameLayout frame = (FrameLayout) subLayout.getChildAt(0);
                                LinearLayout linearLayout = (LinearLayout) frame.getChildAt(0);
                                CheckBox checkBox = (CheckBox) linearLayout.getChildAt(0);

                                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                        entryForm.getValue().getModel().setValue(entryForm.getValue().getName(), Boolean.valueOf(isChecked));

                                        for (Map.Entry<String, FormElement> entry : form.getAllElements().entrySet()) {
                                            if (entryForm.getKey().getId().equals(entry.getValue().getLinkedField())) {
                                                FormElementController ec = formElementMap.get(entry.getValue());
                                                ec.getView().setVisibility(ec.getView().getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                                            }
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // build the map message as we go.

                //to be send to the review activity

                for (FormSectionController ctrl : formController.getSections()){
                    Log.d(TAG, "Form Section: " + ctrl.getName());
                    for (FormElementController elctrl : ctrl.getElements()){
                        Log.d(TAG, "Form Element: " + elctrl.getName());

                        FormElement element = form.getAllElements().get(elctrl.getName());

                        Object value = elctrl.getModel().getValue(elctrl.getName());

                        if (element != null && element.getValidators().size() > 0
                                && elctrl.getView().getVisibility() == View.VISIBLE){
                            if (value == null) {
                                WLTrackApp.customToast(ParticipantHouseholdFormActivity.this,
                                        element.getLabel() + " is a required field");
                                return;
                            }
                            for (ElementValidator validator : element.getValidators()) {
                                RegExInputValidator reVal = new RegExInputValidator(validator.getExpression(),
                                        true);
                                if (reVal.validate(value, element.getLabel(), element.getId()) != null) {
                                    WLTrackApp.customToast(ParticipantHouseholdFormActivity.this,
                                            element.getLabel() + " error: "
                                                    + validator.getErrorMessage());
                                }

                            }
                        } else if (element == null) {
                            // these are always required

                            if (value == null && !elctrl.getName().equals("village") && !elctrl.getName().equals("cluster")){
                                WLTrackApp.customToast(ParticipantHouseholdFormActivity.this,
                                        elctrl.getName()
                                                + " is a required field");
                                return;
                            }
                        }

                        if (element != null && element.getType() == FormElementType.CHECK){
                            if (value == null){
                                value = Boolean.valueOf(false);
                            } else {
                                value = Boolean.valueOf(true);
                            }
                        }

                        //Format date form entries correctly and convert to string
                        if(value !=null && value.getClass().equals(Date.class)){
                            DateFormat df = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
                            String strVal = df.format((Date)value);
                            msg.put(elctrl.getName(), strVal);
                            formElements.put(element.getId(), strVal);
                        } else if (value != null) {
                            msg.put(elctrl.getName(), value.toString());
                            if (element != null) {
                                formElements.put(element.getId(), value.toString());
                            }
                        }
                    }
                }

                System.out.println("Estimated m: " + msg.getMap().get("estimated_months"));

                if ((participantId == null || msg.getMap().get("birthday") == null) && msg.getMap().get("estimated_months") != null){

                    SimpleDateFormat fmt = new SimpleDateFormat(DateUtilities.BIRTHDAY_FORMAT, Locale.US);

                    msg.put(FormKeyIDs.BIRTHDAY_ID, fmt.format(calculateBirthday(msg.getMap().get("estimated_months"),
                            msg.getMap().get("estimated_years"))));

                    formElements.put(FormKeyIDs.BIRTHDAY_ID,msg.getMap().get("birthday"));
                }

                final DTN dtn = WLTrackApp.dtnService.getDTN();

                if (participantId != null){
                    msg.setId(participantId);

                    SimpleDateFormat fmt = new SimpleDateFormat(DateUtilities.DATE_AND_TIME_FORMAT);

                    msg.setVersion(participant.getVersion()+1);

                    msg.put("lastUpdated", fmt.format(new Date()));

                    System.out.println(String.format("Adding cluster / community : %s / %s ", intent.getStringExtra("cluster"), intent.getStringExtra("community")));

                    if (participant.getPhoto() != null) {
                        msg.put("photo", participant.getPhoto().toBase64());
                    }

                    WLTrackApp.participantService.updateParticipant(msg);

                    finish();

                    return;
                }

                if (currentSection != form.getSections().size()-1){
                    //setTitleText(currentSection + 1);

                    nextSection();


                    initDynamicData();
                    return;
                }

                //If the user is creating a participant, store the info this way. This might be a series of switch cases in the future.
                if (formNameFinal.equals("Participant")){
                    finishReview(FinishNewParticipantActivity.class);
                } else {
                    finishReview(ReviewNewParticipantActivity.class);
                }

            }
        });

    }

    public void loadParticipant(StoredParticipant participant){
        msg = participant.toMessage();

        Map<String,String> msg = participant.toMessage().getMap();

        SimpleDateFormat fmt = new SimpleDateFormat("MMMMMM dd, yyyy");

        for (FormSectionController ctrl : formController.getSections()) {
            Log.d(TAG,"loadParticipant(): Form Section: " + ctrl.getName());

            for (FormElementController elctrl : ctrl.getElements()) {
                String fieldLabel = elctrl.getName();
                if (msg.get(fieldLabel) != null){
                    String fieldValue = msg.get(fieldLabel);

                    System.out.println("Filling field " + fieldLabel + " to " + fieldValue);

                    if ("consent".equals(fieldLabel)){
                        if ("true".equals(fieldValue)){
                            // weird way that nexus dialog populates checkboxes...
                            elctrl.getModel().setValue(fieldLabel, Boolean.parseBoolean(fieldValue));
                        }
                    } else if ("isHeadOfHousehold".equals(fieldLabel)){
                        if ("true".equals(fieldValue)){
                            // weird way that nexus dialog populates checkboxes...
                            elctrl.getModel().setValue(fieldLabel, Boolean.parseBoolean(fieldValue));
                        }
                    } else if ("isDeceased".equals(fieldLabel)){
                        if ("true".equals(fieldValue)){
                            // weird way that nexus dialog populates checkboxes...
                            elctrl.getModel().setValue(fieldLabel, Boolean.parseBoolean(fieldValue));
                        }
                    } else if (FormKeyIDs.ESTIMATED_AGE.equals(fieldLabel)) {
                        if ("true".equals(fieldValue)){
                            elctrl.getModel().setValue(fieldLabel, Boolean.parseBoolean(fieldValue));
                           /* for (Map.Entry<String, FormElement> entry : form.getAllElements().entrySet()) {
                                if (FormKeyIDs.ESTIMATED_AGE.equals(entry.getValue().getLinkedField())) {
                                    FormElementController ec = formElementMap.get(entry.getValue());
                                    ec.getView().setVisibility(ec.getView().getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                                }
                            }*/
                        }
                    } else if (form.getAllElements().get(fieldLabel) != null && form.getAllElements().get(fieldLabel).getType() == FormElementType.DATE){
                        try {
                            elctrl.getModel().setValue(fieldLabel, fmt.parse(fieldValue));
                        } catch (Exception ex){
                            // couldn't parse date.
                        }
                    } else {
                        Area area = null;
                        switch(fieldLabel){
                            case "cluster":
                                area = realm.where(Area.class)
                                        .equalTo("name",fieldValue)
                                        .equalTo("type", "Cluster").findFirst();
                                elctrl.getModel().setValue(fieldLabel, area);
                                break;
                            case "community":
                                area = realm.where(Area.class)
                                        .equalTo("name",fieldValue)
                                        .equalTo("type", "Community").findFirst();
                                elctrl.getModel().setValue(fieldLabel, area);
                                break;
                            case "village":
                                area = realm.where(Area.class)
                                        .equalTo("name",fieldValue)
                                        .equalTo("type", "Village").findFirst();
                                elctrl.getModel().setValue(fieldLabel, area);
                                break;
                            default:
                                try {
                                    elctrl.getModel().setValue(fieldLabel, fieldValue);
                                } catch (Exception e){
                                    Log.d(TAG,"Can't sent field " + fieldLabel + " to " + fieldValue);
                                }
                        }

                    }
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (currentSection == 0) {
            new WorkflowDialog(this, this, false);
        }

        else {
            //setTitleText(currentSection - 1);
            previousSection();
        }
    }

    private void initDynamicData(){
        Realm realm = null;

        for (final Map.Entry<FormElement, FormElementController> entryForm : formElementMap.entrySet()) {

           // System.out.println(entryForm.getKey().getId() + " hidden: " + entryForm.getKey().getHidden());

            if (entryForm.getKey().getHidden() == true){
                entryForm.getValue().getView().setVisibility(View.GONE);
            }

            if (entryForm.getKey().getType().equals(FormElementType.CHECK)){
                try {
                    LinearLayout layout = (LinearLayout) entryForm.getValue().getView();
                    LinearLayout subLayout = (LinearLayout) layout.getChildAt(1);
                    FrameLayout frame = (FrameLayout) subLayout.getChildAt(0);
                    LinearLayout linearLayout = (LinearLayout) frame.getChildAt(0);
                    CheckBox checkBox = (CheckBox) linearLayout.getChildAt(0);

                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            entryForm.getValue().getModel().setValue(entryForm.getValue().getName(), Boolean.valueOf(isChecked));

                            /*for (Map.Entry<String, FormElement> entry : form.getAllElements().entrySet()) {
                                if (entryForm.getKey().getId().equals(entry.getValue().getLinkedField())) {
                                    FormElementController ec = formElementMap.get(entry.getValue());
                                    ec.getView().setVisibility(ec.getView().getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                                }
                            }*/
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        try {
            realm = Realm.getDefaultInstance();
            final ViewGroup containerView = (ViewGroup)findViewById(com.nilepoint.formbuilder.R.id.form_elements_container);

            for (final FormSectionController ctrl : formController.getSections()) {
                if (ctrl.getTitle().equals("Location")) {
                    if (participantId == null || participantId.equals(headOfHouseholdId)) {
                        List<Area> areas = realm.where(Area.class)
                                .equalTo("type", "Cluster")
                                .findAll().sort("name");
                        List<String> areaNames = new ArrayList<>();

                        for (Area area : areas) {
                            areaNames.add(area.getName());
                        }

                        SelectionController elctrl = new SelectionController(this, "cluster", "Cluster", null, "Select a Cluster", areaNames, areas);

                        ctrl.addElement(elctrl);

                        formController.recreateViews(containerView);

                        elctrl.getModel().addPropertyChangeListener(new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                                if ("cluster".equals(propertyChangeEvent.getPropertyName())) {
                                    if (propertyChangeEvent.getNewValue() == null) {
                                        return;
                                    }

                                    ctrl.removeElement("community");
                                    ctrl.removeElement("village");

                                    Area area = (Area) propertyChangeEvent.getNewValue();

                                    List<Area> areas = area.getChildren().sort("name");
                                    List<String> areaNames = new ArrayList<>();

                                    for (Area a : areas) {
                                        areaNames.add(a.getName());
                                    }

                                    SelectionController sec = new SelectionController(ParticipantHouseholdFormActivity.this,
                                            "community", "Community", null, "Select a Community", areaNames,
                                            areas);

                                    ctrl.addElement(sec);

                                    Realm realm = null;

                                    formController.recreateViews(containerView);
                                    try {
                                        realm = Realm.getDefaultInstance();

                                        if (participant != null) {
                                            Area fieldValue = realm.where(Area.class)
                                                    .equalTo("name", participant.toMessage().getMap().get("community"))
                                                    .equalTo("type", "Community").findFirst();

                                            ctrl.getModel().setValue("community", fieldValue);
                                        }
                                    } finally {
                                        if (realm != null) {
                                            realm.close();
                                        }
                                    }
                                    // this is for the review page, add it to the map

                                    formElements.put(FormKeyIDs.COMMUNITY_ID, "");
                                    formElements.put(FormKeyIDs.VILLAGE_ID, "");
                                    formElements.put(FormKeyIDs.CLUSTER_ID, area.getName());

                                    // add it to the map message
                                    msg.put(FormKeyIDs.COMMUNITY_ID, "");
                                    msg.put(FormKeyIDs.VILLAGE_ID, "");
                                    msg.put(FormKeyIDs.CLUSTER_ID, area.getName());
                                }

                                if ("community".equals(propertyChangeEvent.getPropertyName())) {

                                    if (propertyChangeEvent.getNewValue() == null) {
                                        return;
                                    }

                                    ctrl.removeElement("village");

                                    Area area = (Area) propertyChangeEvent.getNewValue();

                                    List<Area> areas = area.getChildren().sort("name");
                                    List<String> areaNames = new ArrayList<>();

                                    for (Area a : areas) {
                                        areaNames.add(a.getName());
                                    }

                                    if (areas.size() > 0) {
                                        SelectionController sec = new SelectionController(ParticipantHouseholdFormActivity.this,
                                                "village", "Gott (optional)", null, "Select a Gott", areaNames,
                                                areas);

                                        ctrl.addElement(sec);

                                        formController.recreateViews(containerView);

                                        Realm realm = null;

                                        try {
                                            realm = Realm.getDefaultInstance();

                                            if (participant != null) {
                                                Area fieldValue = realm.where(Area.class)
                                                        .equalTo("name", participant.toMessage().getMap().get("community"))
                                                        .equalTo("type", "Village").findFirst();

                                                ctrl.getModel().setValue("village", fieldValue);
                                            }
                                        } finally {
                                            if (realm != null) {
                                                realm.close();
                                            }
                                        }
                                    }

                                    formElements.put(FormKeyIDs.VILLAGE_ID, "");
                                    formElements.put(FormKeyIDs.COMMUNITY_ID, area.getName());

                                    msg.put(FormKeyIDs.COMMUNITY_ID, area.getName());
                                    msg.put(FormKeyIDs.VILLAGE_ID, "");
                                }

                                if ("village".equals(propertyChangeEvent.getPropertyName())) {
                                    if (propertyChangeEvent.getNewValue() == null) {
                                        return;
                                    }

                                    Area area = (Area) propertyChangeEvent.getNewValue();

                                    msg.put(FormKeyIDs.VILLAGE_ID, area.getName());
                                    formElements.put(FormKeyIDs.VILLAGE_ID, area.getName());
                                }
                            }

                        });

                        break;//incase we get a second Location section
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }

    //goes to review activity, creates seralizable map of input to display on next page


    @Override
    protected void onResume() {
        super.onResume();
        if(participant != null) {
            if(participant.getPhoto() != null)
                participantPhotoButton.setBackground(new BitmapDrawable(getResources(), participant.getPhoto().getBitmap()));
        }
    }

    /**
     * Packages up new participant data and sends it to the review activity for that particular form
     *
     */
    public void finishReview(Class nextActivity){
        Log.d(TAG, "finish: " + formElements);

        Intent intent = new Intent(this, nextActivity); //TODO go to review and store after review

        intent.putExtra("filled_form", formElements);

        if(formName.equals("Participant")){
            msg.put("className", "Participant");

            logger.info(TAG, "New participant " + msg.getId() +" created. HoH:  " + headOfHouseholdId);

            participantService.createParticipant(headOfHouseholdId, msg);
        }

        if (headOfHouseholdId != null && !"".equals(headOfHouseholdId)) {
            intent.putExtra("headOfHouseholdId", headOfHouseholdId);
        }

        if (participantId != null){
            //saveParticipantToHousehold();
            intent.putExtra("participant.id",participantId);
            intent.putExtra("headOfHouseholdId", headOfHouseholdId); //need to know the head to add the participant to the household

        }

        startActivity(intent);
    }

    private Date calculateBirthday(String months, String years){
        try {
            Calendar c = Calendar.getInstance();

            c.setTime(new Date());

            c.add(Calendar.MONTH, -Integer.valueOf(months));
            c.add(Calendar.YEAR, -Integer.valueOf(years));

            return c.getTime();

        } catch (Exception e){

        }

        return null;
    }

}