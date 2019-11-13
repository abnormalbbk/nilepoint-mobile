package com.nilepoint.monitorevaluatemobile.activity_tracking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.github.dkharrat.nexusdialog.FormController;
import com.github.dkharrat.nexusdialog.FormElementController;
import com.github.dkharrat.nexusdialog.controllers.FormSectionController;
import com.github.dkharrat.nexusdialog.controllers.SelectionController;
import com.nilepoint.amqp.messages.MapMessage;
import com.nilepoint.formbuilder.CustomFormActivity;
import com.nilepoint.model.Area;
import com.nilepoint.model.FormElement;
import com.nilepoint.model.PlannedActivity;
import com.nilepoint.model.Project;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.forms.FormKeyIDs;
import com.nilepoint.monitorevaluatemobile.tracking.ActivityDetailsHost;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.realm.Realm;

/**
 * Created by ashaw on 6/11/17.
 */

public class ActivityTrackingActivity extends CustomFormActivity {
    String formName;
    TextView titleText;

    private LinkedHashMap<String, String> valueMap = new LinkedHashMap<>();

    private MapMessage msg = new MapMessage();

    Project selectedProject;
    PlannedActivity training;

    private FormSectionController activitySection = new FormSectionController(this, "Activity Profile");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.pagePerSection = true;

        formName = "Add Activity";

        formController = new FormController(this);

        setContentView(R.layout.activity_form);


        //titleText = (TextView) findViewById(R.id.form_title_text);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setTitle(formName);

        initForm(formController);

        WLTrackApp app = (WLTrackApp) getApplication();

        final Intent intent = getIntent();

        this.setPagePerSection(true);

        onFormUpdate(ActivityTrackingFormFactory.getActivityTrackingForm());

        formHandler.post(new Runnable() {
            @Override
            public void run() {
                initDynamicData();
            }
        });

        Toolbar titleBar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(titleBar);
        getSupportActionBar().setTitle(form.getName());

        Button btn = (Button) findViewById(R.id.form_next_button);

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            String projectId = getIntent().getStringExtra("project.id");

            selectedProject = realm.where(Project.class).equalTo("id", projectId).findFirst();

            valueMap.put("project.id", projectId);
        } finally {
            if (realm != null){
                realm.close();
            }
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // build the map message as we go.
                for (FormSectionController ctrl : formController.getSections()) {
                    Log.d(TAG,"Form Section: " + ctrl.getName());
                    for (FormElementController elctrl : ctrl.getElements()) {
                        Log.d(TAG,"Form Element: " + elctrl.getView().getClass().getSimpleName());

                        FormElement element = form.getAllElements().get(elctrl.getName());

                        Object value = elctrl.getModel().getValue(elctrl.getName());

                        if (value == null){
                            WLTrackApp.customToast(elctrl.getName() + " is required");
                            return;
                        }

                        //Format date form entries correctly and convert to string
                        if (value != null && value.getClass().equals(Date.class)) {
                            DateFormat df = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
                            String strVal = df.format((Date) value);
                            msg.put(elctrl.getName(), strVal);
                            valueMap.put(element.getId(), strVal);
                        } else if (value != null) {
                            msg.put(elctrl.getName(), value.toString());
                            if (element != null) {
                                valueMap.put(element.getId(), value.toString());
                            }
                        }
                    }

                }


                Log.d("ActivityTracking", "Done, now going to the host...");

                Intent intent = new Intent(ActivityTrackingActivity.this, ActivityDetailsHost.class);

                for (Map.Entry<String,String> entry : valueMap.entrySet()){
                    intent.putExtra(entry.getKey(), entry.getValue());
                }

                startActivity(intent);

                finish();

            }
        });
    }

    public void setTitleText(int sectionIndex) {
        titleText.setText(form.getSections().get(sectionIndex).getLabel());
    }

    /**
     * This method adds the dynamic location selectors to the form.
     */
    private void initDynamicData() {
        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            final ViewGroup containerView = (ViewGroup) findViewById(com.nilepoint.formbuilder.R.id.form_elements_container);

            for (final FormSectionController ctrl : formController.getSections()) {

                List<String> projectNames = new ArrayList<>();


                List<PlannedActivity> trainings = selectedProject.getActivities().where()
                        .equalTo("type", "AT-Training")
                        .findAll().sort("name");

                List<String> trainingNames = new ArrayList<>();

                for (PlannedActivity training : trainings) {
                    Log.i(TAG, "Training: " + training.getName()
                            + " category: " + training.getType()
                            + " projectId: " + training.getProjectId());
                    trainingNames.add(training.getName());
                }




                SelectionController elctrl2 = new SelectionController(this, "training.id", "Training",
                        null, "Select Training", trainingNames, trainings);

                ctrl.addElement(elctrl2);

                formController.recreateViews(containerView);

                final Realm frealm = realm;

                ctrl.getModel().addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

                        if ("cluster".equals(propertyChangeEvent.getPropertyName())) {
                            if (propertyChangeEvent.getNewValue() == null) {
                                return;
                            }

                            ctrl.removeElement("community");

                            Area area = (Area) propertyChangeEvent.getNewValue();

                            List<Area> areas = training.getAreas().where()
                                    .equalTo("type", "Community")
                                    .findAllSorted("name");

                            if (areas.isEmpty()) {
                                areas =   area.getChildren().sort("name");
                            }

                            final List<String> areaNames = new ArrayList<>();

                            for (Area a : areas) {
                                areaNames.add(a.getName());
                            }

                            SelectionController sec = new SelectionController(ActivityTrackingActivity.this,
                                    "community", "Community", null, "Select a Community", areaNames,
                                    areas);

                            if (PlannedActivity.COMMUNITY.equals(training.getLevelCode())){
                                ctrl.addElement(sec);
                                formController.recreateViews(containerView);
                                // this is for the review page, add it to the map
                            }


                            valueMap.put(FormKeyIDs.COMMUNITY_ID, "");
                            valueMap.put(FormKeyIDs.VILLAGE_ID, "");
                            valueMap.put(FormKeyIDs.CLUSTER_ID, area.getName());

                            // add it to the map message
                            msg.put(FormKeyIDs.COMMUNITY_ID, "");
                            msg.put(FormKeyIDs.VILLAGE_ID, "");
                            msg.put(FormKeyIDs.CLUSTER_ID, area.getName());
                        }

                        if ("community".equals(propertyChangeEvent.getPropertyName())) {

                            if (propertyChangeEvent.getNewValue() == null) {
                                return;
                            }

                            Area area = (Area) propertyChangeEvent.getNewValue();


                            valueMap.put(FormKeyIDs.COMMUNITY_ID, area.getName());

                            msg.put(FormKeyIDs.COMMUNITY_ID, area.getName());
                        }

                        if ("training.id".equals(propertyChangeEvent.getPropertyName())) {
                            if (propertyChangeEvent.getNewValue() == null) {
                                return;
                            }

                            ctrl.removeElement("module.id");
                            ctrl.removeElement("lesson.id");
                            ctrl.removeElement("cluster");
                            ctrl.removeElement("village");

                            PlannedActivity activity = (PlannedActivity) propertyChangeEvent.getNewValue();

                            training = activity;

                            List<Area> areas = null;

                            if (training.getAreas().isEmpty()){
                                areas = frealm.where(Area.class)
                                        .equalTo("type", "Cluster")
                                        .findAll().sort("name");
                            } else {
                                areas = training.getAreas().where()
                                        .equalTo("type", "Cluster")
                                        .findAllSorted("name");
                            }

                            final List<String> areaNames = new ArrayList<>();

                            for (Area a : areas) {
                                areaNames.add(a.getName());
                            }

                            SelectionController clusterSelector = new SelectionController(ActivityTrackingActivity.this, "cluster", "Cluster",
                                    null, "Select a Cluster", areaNames, areas);

                            List<PlannedActivity> modules = activity.getChildren().sort("name");

                            if (modules.isEmpty()){
                                return;
                            }

                            List<String> moduleNames = new ArrayList<>();

                            for (PlannedActivity module : modules) {
                                moduleNames.add(module.getName());
                            }

                            SelectionController elctrl = new SelectionController(ActivityTrackingActivity.this,
                                    "module.id", "Module", null, "Select Module",
                                    moduleNames, modules);

                            ctrl.addElement(elctrl, 1);


                            if (PlannedActivity.CLUSTER.equals(activity.getLevelCode())
                                    || PlannedActivity.COMMUNITY.equals(activity.getLevelCode())){
                                ctrl.addElement(clusterSelector);
                            }

                            formController.recreateViews(containerView);

                            msg.put("training.id", activity.getId());
                            valueMap.put("training.id", activity.getId());
                        }

                        if ("module.id".equals(propertyChangeEvent.getPropertyName())) {
                            if (propertyChangeEvent.getNewValue() == null) {
                                return;
                            }

                            ctrl.removeElement("lesson.id");

                            PlannedActivity activity = (PlannedActivity) propertyChangeEvent.getNewValue();

                            List<PlannedActivity> lessons = activity.getChildren().sort("name");

                            if (lessons.isEmpty()){
                                return;
                            }

                            List<String> trainingNames = new ArrayList<>();

                            for (PlannedActivity training : lessons) {
                                trainingNames.add(training.getName());
                            }

                            SelectionController elctrl = new SelectionController(ActivityTrackingActivity.this,
                                    "lesson.id", "Lesson", null, "Select Lesson",
                                    trainingNames, lessons);

                            ctrl.addElement(elctrl,2);

                            msg.put("module.id", activity.getId());
                            valueMap.put("module.id", activity.getId());

                            formController.recreateViews(containerView);
                        }
                        if ("lesson.id".equals(propertyChangeEvent.getPropertyName())) {
                            if (propertyChangeEvent.getNewValue() == null) {
                                return;
                            }

                            PlannedActivity activity = (PlannedActivity) propertyChangeEvent.getNewValue();

                            msg.put("lesson.id", activity.getId());
                            valueMap.put("lesson.id", activity.getId());
                        }
                    }

                });

                break;
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }


}
