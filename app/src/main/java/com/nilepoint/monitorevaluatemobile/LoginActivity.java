package com.nilepoint.monitorevaluatemobile;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.kishan.askpermission.AskPermission;
import com.kishan.askpermission.PermissionCallback;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.adapter.UserButtonsAdapter;
import com.nilepoint.monitorevaluatemobile.admin.FirstSetupWizardHostActivity;
import com.nilepoint.monitorevaluatemobile.location.GeoTagService;
import com.nilepoint.monitorevaluatemobile.new_user.OAuthActivity;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Book;
import io.paperdb.Paper;
import io.realm.Realm;


public class LoginActivity extends AppCompatActivity implements
        PermissionCallback {

    public static final String TAG = LoginActivity.class.getName();

    private String mUsername;
    private TextView mUserNameTextView;
    private AppCompatButton mSignInButton;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<UserOption> userOptionList = new ArrayList<>(); //initialize empty list
    private RecyclerView.Adapter adapter;

    private boolean isFirstUse = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO Realm storage of if first user or not
        setContentView(R.layout.activity_login);

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            for (User user : realm.where(User.class).equalTo("loginEnabled", true).findAll()) {
                Intent intent = new Intent(getBaseContext(), HomeActivity.class);

                intent.putExtra("user.id", user.getId());

                userOptionList.add(new UserOption(user, R.color.colorHomeCard1,
                        intent));
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        recyclerView = (RecyclerView) findViewById(R.id.user_options_recyclerView);

        layoutManager = new LinearLayoutManager(this);

        adapter = new UserButtonsAdapter(userOptionList, this.getBaseContext());

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(layoutManager);

        new AskPermission.Builder(this).setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .setCallback(this)
                .request(1);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Realm realm = null;
        Book settings = Paper.book();
        Boolean hasData = settings.read("data.init");

        if (hasData == null || hasData == false) {
            Intent intent = new Intent(getBaseContext(), FirstSetupWizardHostActivity.class);

            startActivity(intent);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Authenticates user
     * @return if the user was correctly authenticated
     */
    protected boolean authenticate(){
        //TODO Authenticate user...may need two methods for local/internet authentication?
        return true;
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        Intent i= new Intent(this, GeoTagService.class);

        this.startService(i);
    }

    @Override
    public void onPermissionsDenied(int requestCode) {

    }
}
