package com.nilepoint.monitorevaluatemobile.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.kishan.askpermission.AskPermission;
import com.kishan.askpermission.ErrorCallback;
import com.kishan.askpermission.PermissionCallback;
import com.kishan.askpermission.PermissionInterface;
import com.nilepoint.model.Photo;
import com.nilepoint.model.StoredParticipant;
import com.nilepoint.model.User;
import com.nilepoint.monitorevaluatemobile.WLTrackApp;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.PhotoGalleryFileAdapter;
import com.nilepoint.monitorevaluatemobile.services.UpdateEvent;
import com.nilepoint.monitorevaluatemobile.services.UpdateEventType;
import com.nilepoint.persistence.Datastore;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;

public class SelectPhotoActivity extends AppCompatActivity implements PermissionCallback, ErrorCallback{

    private static final String TAG = "SelectPhotoActivity";

    private static final int PHOTO_TAKEN_REQUEST_CODE = 1888;
    private static final int PHOTO_SELECTED_REQUEST_CODE = 2888;
    final String CAMERA_PERMISSION = Manifest.permission.CAMERA;

    final String READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    final String WRITE_READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE; //implicitly allows read

    ArrayList<Bitmap> images = new ArrayList<>(); //Images loaded from the phone
    ArrayList<String> imageFileNames = new ArrayList<>();
    Bitmap cameraIconBmp;

    RecyclerView galleryRecyclerView;
    GridLayoutManager galleryGridManager;
    private PhotoGalleryFileAdapter adapter;
    Button openCameraButton;
    ImageView viewImage;
    boolean saveOnFinish = false;

    User user;
    String userId;
    StoredParticipant storedParticipant;

    @Override
    @TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_photo);
        getPermissions();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Select a Photo");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);


        viewImage = (ImageView) findViewById(R.id.view_photo);


        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            if (getIntent().hasExtra("user.id")) {
                userId = getIntent().getStringExtra("user.id");
                user = realm.where(User.class).equalTo("id", userId).findFirst();
            } else if (getIntent().hasExtra("participant.id")) {
                saveOnFinish = true;

                storedParticipant = realm.where(StoredParticipant.class).equalTo("id", getIntent().getStringExtra("participant.id"))
                .findFirst();

                Log.d(TAG, "will close on finish, and save for " + storedParticipant.getFirstName());

            }
        } finally {
            if (realm != null){
                realm.close();
            }
        }
    }

    /**
     * uses Ask-Permission Library from Kishan
     */
    private void getPermissions() {
        new AskPermission.Builder(this).setPermissions(
                WRITE_READ_EXTERNAL_STORAGE_PERMISSION,
                CAMERA_PERMISSION)
                .setCallback(this)
                .setErrorCallback(this)
                .request(PHOTO_TAKEN_REQUEST_CODE);

        Log.d(TAG, "getPermission run!");
    }

    public void resetAdapter(){
        getBitmapsFromFiles(this);

    }

    @Override
    public void onPermissionsGranted(int requestCode) {

        Log.d(TAG, "permission granted!");

        //images.addAll(getBitmapsFromFiles(this));

        getBitmapsFromFiles(this);

        /* Set up photo gallery and button */

        galleryRecyclerView = (RecyclerView) findViewById(R.id.select_photo_recycler);

        galleryGridManager = new GridLayoutManager(this, 3); //TODO get this based on screen size

        adapter = getAdapter();

        galleryRecyclerView.setAdapter(adapter);

        galleryRecyclerView.setLayoutManager(galleryGridManager);


    }

    PhotoGalleryFileAdapter getAdapter(){
        PhotoGalleryFileAdapter adapter = new PhotoGalleryFileAdapter(this,imageFileNames);

        adapter.setImageSelectedListener(new ImageSelectedListener() {
            @Override
            public void onImageSelected(final Bitmap bitmap) {


                if (user != null) {

                    Realm realm = null;
                    try {
                        realm = Realm.getDefaultInstance();

                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                Photo p = new Photo(bitmap);

                                user = realm.where(User.class).equalTo("id", userId).findFirst();

                                user.setPhoto(realm.copyToRealm(p));

                                Log.d(TAG, "Photo stored to user");
                            }
                        });
                    } finally {
                        if (realm != null){
                            realm.close();
                        }
                    }
                }
                /*This is a little repetitive, I think I'll take a second look at to clean it up at a later point (not best practice but still)*/
                else if (user == null && saveOnFinish){

                    Realm realm = null;
                    try {
                        realm = Realm.getDefaultInstance();

                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                Photo p = new Photo(bitmap);

                                storedParticipant.setPhoto(realm.copyToRealm(p));
                                storedParticipant.setVersion(storedParticipant.getVersion()+1);

                                Log.d(TAG, "Photo stored to participant, bytes: " + bitmap.getByteCount());
                                WLTrackApp.participantService.addParticipantEvent(new UpdateEvent(UpdateEventType.UPDATED,
                                        WLTrackApp.dtnService.createBundle(storedParticipant.toMessage())));
                            }
                        });
                    } finally {
                        if (realm != null){
                            realm.close();
                        }
                    }

                    SelectPhotoActivity.this.finish();
                }


                Intent intent = new Intent();

                PhotoProcessor pp = new PhotoProcessor();

                Log.d(TAG, "Took photo, bytes: " + bitmap.getByteCount());

                intent.putExtra("photo", pp.getPhotoByteArray(bitmap));

                setResult(RESULT_OK, intent);

                SelectPhotoActivity.this.finish();
            }
        });

        return adapter;
    }

    @Override
    public void onPermissionsDenied(int requestCode) {
        Toast.makeText(this, "Permissions Denied.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onShowRationalDialog(final PermissionInterface permissionInterface, int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We need your permissions to take a picture. Proceed?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionInterface.onDialogShown();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();

    }

    @Override
    public void onShowSettings(final PermissionInterface permissionInterface, int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We need your permissions to open your settings. Proceed?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionInterface.onDialogShown();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();

    }

    /**
     * Override of OnActivityResult -
     * gets the photo from the camera and compresses it to a Byte Array and sends it back to the calling activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("SelectPhoto", "onActivityForResult...");
        if (requestCode == PHOTO_TAKEN_REQUEST_CODE && resultCode == Activity.RESULT_OK) { //TODO add or item clicked
            Log.d("SelectPhoto","Photo selected successfully!");
            final Bitmap photo = (Bitmap) data.getExtras().get("data");

            String filename = saveBitmapToDevice(photo);

            if (filename != null) {
                imageFileNames.add(0,filename);

                Log.d(TAG,"There are now " + imageFileNames.size() + " images");

                galleryRecyclerView.swapAdapter(getAdapter(), true);

                galleryRecyclerView.invalidate();
            }
        }

        else {
            Toast.makeText(this, "Sorry, that photo could not be saved.", Toast.LENGTH_SHORT);
        }
    }

    public ArrayList<Bitmap> getBitmapsFromFiles(Context context){

        ArrayList<Bitmap> galleryImages = new ArrayList<>();

        Uri imageUri =  android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Bitmap bitmap = null;

        String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";

        String[] projection = { MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        Cursor cursor = context.getContentResolver().query(imageUri, projection, null, null, orderBy);

        int columnIndexData = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        //int indexFolder = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        //go through the gallery and get the image paths and convert to Bitmaps
        int i = 0;

        while(cursor.moveToNext()){
            imageFileNames.add(cursor.getString(columnIndexData)); // load the image file names
        }

        return galleryImages;
    }


    /***
     * Save the newly taken photo to the phone's picture storage directory as a jpg. File name is in the following format:
     * img-yyyy-MM-dd_HH:mm:ss.jpg
     * Thanks go to Yar at StackOverflow for help: https://stackoverflow.com/questions/7887078/android-saving-file-to-external-storage
     * @param bitmap
     * @return the filename of the bitmap
     */
    public String saveBitmapToDevice(Bitmap bitmap){

        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File dir = new File(root + "/WorldLink Trac");
        dir.mkdirs();

        //generate the name for the picture based on when it was taken
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
        String fileName = "img-" + currentDateTime + ".jpg";

        File file = new File(dir, fileName);
        if(file.exists())
            file.delete();

        try{
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

        }
        catch (Exception e){
            Toast.makeText(this, "Photo could not be saved.", Toast.LENGTH_SHORT);
            Log.d(TAG, e.getStackTrace().toString());
            Crashlytics.logException(e);
            return null;
        }

        //add to the media scanner
        MediaScannerConnection.scanFile(this, new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                    });


        return file.getAbsolutePath();
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
