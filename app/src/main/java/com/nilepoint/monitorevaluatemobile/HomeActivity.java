package com.nilepoint.monitorevaluatemobile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kishan.askpermission.PermissionCallback;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.adapter.HomeButtonsAdapter;
import com.nilepoint.monitorevaluatemobile.fragment.HomeFragment;
import com.nilepoint.monitorevaluatemobile.group.GroupListActivity;
import com.nilepoint.monitorevaluatemobile.init.DataSyncActivity;
import com.nilepoint.monitorevaluatemobile.participant.FindPartcipantHostActivity;
import com.nilepoint.monitorevaluatemobile.settings.DTNSettingsActivity;
import com.nilepoint.monitorevaluatemobile.settings.SettingsActivity;
import com.nilepoint.monitorevaluatemobile.tracking.SelectProjectsHostActivity;
import com.nilepoint.monitorevaluatemobile.user.UserSession;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class HomeActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener, PermissionCallback {

    public static final String TAG = HomeActivity.class.getName();

    //drawer
    private NavigationView navView;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private View drawerHeader;
    private ImageView profilePic;
    private TextView profileName;

    //main page content
    private RecyclerView homeRecycler;
    private RecyclerView.LayoutManager layoutManager;
    private List<HomeOption> homeOptionsList = new ArrayList<>(); //initialize empty list
    private RecyclerView.Adapter adapter;

    //Array of option titles
    private String[] navBarOptions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Set up toolbar, replaces old ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        getSupportActionBar().setLogo(R.drawable.toolbar_home_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);


        //Set up drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(drawerToggle);
        //Set up navigation view
        navView = (NavigationView) findViewById(R.id.nav_view);

        //set up header and its content
        drawerHeader = navView.getHeaderView(0);
        profilePic = (ImageView) drawerHeader.findViewById(R.id.drawer_profilePic);
        profileName = (TextView) drawerHeader.findViewById(R.id.drawer_profileName);

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            User user = realm.where(User.class).equalTo("id", UserSession.userId).findFirst();
            if (user != null) {
                if (user.getPhoto() != null) {
                    profilePic.setImageBitmap(user.getPhoto().getBitmap());
                }
                profileName.setText(user.getFirstName() + " " + user.getLastName());
            }
        } finally {
            if (realm != null){
                realm.close();
            }
        }
        //Draw the drawer
        setupDrawerContent(navView);

        //List of homescreen options
        populateOptionsList();

        //recyclerview containing navigation options
        homeRecycler = (RecyclerView) findViewById(R.id.home_options_recyclerView);
        layoutManager = new LinearLayoutManager(this);
        adapter = new HomeButtonsAdapter(homeOptionsList, this);
        homeRecycler.setAdapter(adapter);
        homeRecycler.setLayoutManager(layoutManager);

    }

    /***
     * method that adds new options to the list places to navigate from the home activity
     */
    public void populateOptionsList(){
        //homeOptionsList.add(new HomeOption("Find a participant",R.drawable.ic_search_white_24dp,R.color.colorHomeCard1, new Intent(getBaseContext(), ParticipantListActivity.class)));
        homeOptionsList.add(new HomeOption("Find a Participant",R.drawable.ic_search_white_24dp,R.color.colorHomeCard1, new Intent(getBaseContext(), FindPartcipantHostActivity.class)));
        homeOptionsList.add(new HomeOption("Add New Household",R.drawable.add_white, R.color.colorHomeCard2, new Intent(getBaseContext(), ConsentInfoActivity.class)));
        //homeOptionsList.add(new HomeOption("Find a group", R.drawable.find_a_group, R.color.colorHomeCard3, new Intent(getBaseContext(), HomeActivity.class)));
        //homeOptionsList.add(new HomeOption("Add a new group", R.drawable.create_a_group, R.color.colorHomeCard4, new Intent(getBaseContext(), HomeActivity.class)));
        homeOptionsList.add(new HomeOption("Manage Groups", R.drawable.create_a_group, R.color.colorHomeCard3, new Intent(getBaseContext(), GroupListActivity.class)));
       // homeOptionsList.add(new HomeOption("Implement Activity", R.drawable.arrow_back, R.color.bugsee_screenshot_label_blue_color, new Intent(getBaseContext(), ActivityTrackingActivity.class)));
        homeOptionsList.add(new HomeOption("Track Activity", R.drawable.ic_content_paste_white_24dp, R.color.lightTeal, new Intent(getBaseContext(), SelectProjectsHostActivity.class)));
        //TODO add the other four options
    }



    /*Helper Methods to aid in the selection of drawer items*/

    /**
     * Creates a listener for handling selection of items in the drawer
     * @param nv navigation view for the drawer
     */
    private void setupDrawerContent(NavigationView nv){
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                selectItem(item);
                return true;
            }
        });

    }

    /**
     * Handles the selection of items and which fragment to load depending on which was clicked.
     * To extend this with new items, add a case for a new Option Item and link it to your new fragment
     * @param item
     */
    public void selectItem(MenuItem item){
        //Create an instance of a fragment to be filled with the one clicked
        //TODO change each of these options to open a new activity, each of which will have several fragments for each page of the flow
        switch(item.getItemId()){
            case R.id.nav_home:
                launchMenuItem(HomeActivity.class);
                break;
            case R.id.nav_dtn:
                launchMenuItem(DataSyncActivity.class);
                break;
            /*case R.id.nav_log:
                launchMenuItem(LogListActivity.class);
                break;
            case R.id.nav_queue:
                launchMenuItem(QueueActivity.class);
                break;*/
            case R.id.nav_clientList:
                launchMenuItem(FindPartcipantHostActivity.class);
                break;
            case R.id.nav_settings:
                launchMenuItem(SettingsActivity.class);
                break;
            /*case R.id.nav_peer_init:
                launchMenuItem(DataSyncActivity.class);
                break;*/
            case R.id.nav_signOut:
                logout();
            default:
                //selectedFragmentClass = HomeFragment.class;
        }
        drawer.closeDrawers();
    }


    //Close the drawer with the soft key
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //TODO come back to this..
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)){
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Launches new activity from the drawer
     */
    public void launchMenuItem(Class menuItem){
        Intent intent = new Intent(getBaseContext(), menuItem);
        startActivity(intent);
    }

    /*Signs the user out on confirmation and then redirects them back to the login page*/
    public void logout(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Sign out");
        alertDialogBuilder.setMessage(R.string.logout_dialog);
                alertDialogBuilder.setPositiveButton("yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                //TODO signout the current user
                                launchMenuItem(LoginActivity.class);
                            }
                        });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    //TODO not sure if this will be used in case we decide items should be activities as opposed to fragments
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onPermissionsGranted(int requestCode) {

    }

    @Override
    public void onPermissionsDenied(int requestCode) {

    }
}
