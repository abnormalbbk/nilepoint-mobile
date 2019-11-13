package com.nilepoint.monitorevaluatemobile.tracking;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.nilepoint.model.Distribution;
import com.nilepoint.model.Project;
import com.nilepoint.model.TrackedActivity;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.activity_tracking.ActivityTrackingActivity;
import com.nilepoint.monitorevaluatemobile.distributions.DistributionTrackingActivity;
import com.nilepoint.monitorevaluatemobile.viewpager.ViewPagerAdapter;

import java.util.HashMap;
import java.util.List;

public class SelectActivityActivity extends AppCompatActivity {

    private String projectId;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;
    private Project project;

    private List<TrackedActivity> trainings;
    private List<TrackedActivity> distributions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_activity);
        Intent intent = getIntent();
        projectId = intent.getStringExtra("id");
        project = TrackingUtils.getProjectById(projectId);

        if(project != null){
            trainings = TrackingUtils.getTrainingsForProject(project);
            distributions = TrackingUtils.getDistributionsForProject(project);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Select Activity");

        viewPager = (ViewPager) findViewById(R.id.activity_select_activity_viewPager);
        viewPagerAdapter = new ActivityViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.activity_select_activity_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Color.GRAY, Color.WHITE);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch(tab.getPosition()){
                    case 0:
                        getSupportActionBar().setTitle("Select Training");
                        break;
                    case 1:
                        getSupportActionBar().setTitle("Select Distribution");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("SelectActivityActivity: OnPause");

    }

    @Override
    protected void onResume() {
        super.onResume();

        viewPagerAdapter = new ActivityViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Color.GRAY, Color.WHITE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        System.out.println("SelectActivityActivity: onSaveInstanceSTate");

        outState.putString("project.id", projectId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        System.out.println("SelectActivityActivity: onRestoreInstanceState");

        projectId = savedInstanceState.getString("project.id");

        project = TrackingUtils.getProjectById(projectId);

        if(project != null){
            trainings = TrackingUtils.getTrainingsForProject(project);
            distributions = TrackingUtils.getDistributionsForProject(project);
        }
    }

    /**
     * method that launches add new activity activity
     */
    public void addNewActivity(){
        Intent intent = new Intent(SelectActivityActivity.this, ActivityTrackingActivity.class);

        intent.putExtra("project.id", projectId);

        startActivity(intent);

       // finish();
    }

    public void addNewDistribution(){
        Intent intent = new Intent(SelectActivityActivity.this, DistributionTrackingActivity.class);

        intent.putExtra("project.id", projectId);

        startActivity(intent);

        // finish();
    }

    public List<TrackedActivity> getTrainings() {
        return trainings;
    }

    public List<TrackedActivity> getDistributions() {
        return distributions;
    }

    /**
     * Handles tabs and fragments
     */
    public class ActivityViewPagerAdapter extends ViewPagerAdapter {

        private String[] tabTitles = {"Training", "Distribution"};
        private HashMap<Integer, String> fragmentTags;

        ActivityViewPagerAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
            setTabTitles(tabTitles);
            fragmentTags = new HashMap<Integer, String>();
        }

        public HashMap<Integer, String> getFragmentTags(){
            return fragmentTags;
        }


        @Override
        public Fragment getItem(int position){
            Fragment fragment = null;
            switch(position){
                case 0:
                    fragment = PlannedActivitiesFragment.newInstance(TrackingUtils.TYPE_TRAINING);
                    getSupportActionBar().setTitle("Select Training");
                    break;
                case 1:
                    fragment = PlannedActivitiesFragment.newInstance(TrackingUtils.TYPE_DIST);
                    break;
            }

            fragmentTags.put(position,fragment.getTag());

            return fragment;
        }

    } //eoc
}
