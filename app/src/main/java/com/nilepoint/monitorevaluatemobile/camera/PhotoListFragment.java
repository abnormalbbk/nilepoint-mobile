package com.nilepoint.monitorevaluatemobile.camera;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.kishan.askpermission.AskPermission;
import com.kishan.askpermission.ErrorCallback;
import com.kishan.askpermission.PermissionCallback;
import com.kishan.askpermission.PermissionInterface;
import com.nilepoint.model.Photo;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.adapter.PhotoGalleryFileAdapter;
import com.nilepoint.monitorevaluatemobile.adapter.PhotoGalleryPhotoAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

/**
 * Created by ashaw on 10/3/17.
 */

public class PhotoListFragment extends Fragment implements PermissionCallback, ErrorCallback  {

    public static String TAG = "PhotoListFragment";


    public static final int PHOTO_TAKEN_REQUEST_CODE = 1888;
    public static final int PHOTO_SELECTED_REQUEST_CODE = 2888;
    final String CAMERA_PERMISSION = Manifest.permission.CAMERA;

    final String READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    final String WRITE_READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE; //implicitly allows read

    List<Photo> photos = new ArrayList<>(); //Images loaded from the phone

    PhotoListener listener;

    Bitmap cameraIconBmp;

    RecyclerView galleryRecyclerView;
    GridLayoutManager galleryGridManager;

    private PhotoGalleryPhotoAdapter adapter;

    Button openCameraButton;

    ImageView viewImage;

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_select_photo, container, false);

        viewImage = (ImageView) rootView.findViewById(R.id.view_photo);

        Bundle bundle=getArguments();

        getPermissions();

        return rootView;
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
       // getBitmapsFromFiles(this.getActivity());
        adapter = getAdapter();

        galleryRecyclerView.swapAdapter(adapter, true);

    }

    @Override
    public void onPermissionsGranted(int requestCode) {

        Log.d(TAG, "permission granted!");

        //images.addAll(getBitmapsFromFiles(this));

       // getBitmapsFromFiles(this.getActivity());

        /* Set up photo gallery and button */

        galleryRecyclerView = (RecyclerView) rootView.findViewById(R.id.select_photo_recycler);

        galleryGridManager = new GridLayoutManager(this.getActivity(), 3); //TODO get this based on screen size

        adapter = getAdapter();

        galleryRecyclerView.setAdapter(adapter);

        galleryRecyclerView.setLayoutManager(galleryGridManager);


    }

    PhotoGalleryPhotoAdapter getAdapter(){
        PhotoGalleryPhotoAdapter adapter = new PhotoGalleryPhotoAdapter(this.getActivity(),photos);

        adapter.setImageSelectedListener(new ImageSelectedListener() {
            @Override
            public void onImageSelected(final Bitmap bitmap) {
            // show image full size.
            }
        });

        return adapter;
    }

    @Override
    public void onPermissionsDenied(int requestCode) {
        Toast.makeText(this.getActivity(), "Permissions Denied.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onShowRationalDialog(final PermissionInterface permissionInterface, int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
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

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    static public PhotoListFragment newInstance(List<Photo> photos){
        PhotoListFragment fragment = new PhotoListFragment();

        fragment.photos = photos;

        Bundle args = new Bundle();

        //args.putSerializable("photos", photos);

        fragment.setArguments(args);

        return fragment;
    }
}
