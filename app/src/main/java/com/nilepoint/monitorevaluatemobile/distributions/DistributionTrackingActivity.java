package com.nilepoint.monitorevaluatemobile.distributions;

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
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.activity_tracking.ActivityTrackingFormFactory;
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

public class DistributionTrackingActivity extends CustomFormActivity {
    String formName;
    TextView titleText;

    private LinkedHashMap<String, String> valueMap = new LinkedHashMap<>();

    private MapMessage msg = new MapMessage();

    Project selectedProject;
    Area selectedCluster;
    PlannedActivity selectedActivity;

    private FormSectionController activitySection = new FormSectionController(this, "Activity Profile");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.pagePerSection = true;

        formName = "Add Distribution";

        formController = new FormController(this);

        setContentView(R.layout.activity_form);

       // titleText = (TextView) findViewById(R.id.form_title_text);

        setTitle(formName);

        initForm(formController);

        WLTrackApp app = (WLTrackApp) getApplication();

        final Intent intent = getIntent();

        this.setPagePerSection(true);

        onFormUpdate(ActivityTrackingFormFactory.getActivityTrackingForm());

        Toolbar titleBar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(titleBar);
        getSupportActionBar().setTitle(formName);

        formHandler.post(new Runnable() {
            @Override
            public void run() {
                initDynamicData();
            }
        });

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

                Realm realm = null;

                try {
                    realm = Realm.getDefaultInstance();

                    String projectId = getIntent().getStringExtra("project.id");

                    TrackedActivity existingDistribution = realm.where(TrackedActivity.class)
                            .equalTo("category.id", selectedActivity.getId())
                            .equalTo("cluster.id", selectedCluster.getId())
                            .findFirst();

                    if (existingDistribution != null) {
                        finish();

                        WLTrackApp.customToast("Could not create this distribution, a similar one exists.");

                        return;
                    }

                } finally {
                    if (realm != null){
                        realm.close();
                    }
                }

                Intent intent = new Intent(DistributionTrackingActivity.this, ActivityDetailsHost.class);

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

                List<PlannedActivity> distributions = selectedProject.getActivities().where()
                        .equalTo("type", PlannedActivity.DISTRIBUTION)
                        .isNotEmpty("plannedDistributions")
                        .findAll().sort("name");

                List<String> distributionNames = new ArrayList<>();

                for (PlannedActivity distribution : distributions) {
                    Log.i(TAG, "Distribution: " + distribution.getName()
                            + " category: " + distribution.getType()
                            + " projectId: " + distribution.getProjectId());
                    distributionNames.add(distribution.getName());
                }



                SelectionController elctrl2 = new SelectionController(this, "distribution.id", "Distribution",
                        null, "Select Distribution", distributionNames, distributions);

                ctrl.addElement(elctrl2);


                formController.recreateViews(containerView);


                ctrl.getModel().addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

                        if ("cluster".equals(propertyChangeEvent.getPropertyName())) {
                            if (propertyChangeEvent.getNewValue() == null) {
                                return;
                            }

                            Area area = (Area) propertyChangeEvent.getNewValue();

                            selectedCluster = area;

                            // this is for the review page, add it to the map

                            valueMap.put(FormKeyIDs.CLUSTER_ID, area.getName());

                            msg.put(FormKeyIDs.CLUSTER_ID, area.getName());
                        }

                        if ("distribution.id".equals(propertyChangeEvent.getPropertyName())) {
                            if (propertyChangeEvent.getNewValue() == null) {
                                return;
                            }

                            Realm frealm = null;

                            try {
                                frealm = Realm.getDefaultInstance();
                                PlannedActivity activity = (PlannedActivity) propertyChangeEvent.getNewValue();

                                selectedActivity = activity;

                                List<Area> areas = activity.getAreas().where().equalTo("type", "Cluster")
                                        .findAll().sort("name");


                                if (areas.isEmpty()){
                                    areas = frealm.where(Area.class).equalTo("type", "Cluster")
                                            .findAll().sort("name");
                                }

                                List<String> areaNames = new ArrayList<>();

                                List<String> projectNames = new ArrayList<>();

                                for (Area area : areas) {
                                    areaNames.add(area.getName());
                                }

                                activitySection.removeElement("cluster");

                                SelectionController elctrl = new SelectionController(DistributionTrackingActivity.this, "cluster", "Cluster",
                                        null, "Select a Cluster", areaNames, areas);


                                ctrl.addElement(elctrl);

                                formController.recreateViews(containerView);

                                msg.put("distribution.id", activity.getId());
                                valueMap.put("distribution.id", activity.getId());
                            } catch  (Exception e){
                                Crashlytics.logException(e);
                                e.printStackTrace();
                            } finally {
                                if (frealm != null){
                                    frealm.close();
                                }
                            }

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
