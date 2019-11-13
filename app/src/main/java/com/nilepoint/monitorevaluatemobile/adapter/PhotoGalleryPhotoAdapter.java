package com.nilepoint.monitorevaluatemobile.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nilepoint.model.Photo;
import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.camera.ImageSelectedListener;
import com.nilepoint.monitorevaluatemobile.camera.PhotoProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by claudiatrafton on 5/28/17.
 */

public class PhotoGalleryPhotoAdapter extends RecyclerView.Adapter<PhotoGalleryPhotoAdapter.ViewHolder> {

    private static final String TAG = "PhotoGalleryFileAdapter";
    private static final int PHOTO_TAKEN_REQUEST_CODE = 1888;
    private static final int PHOTO_SELECTED_REQUEST_CODE = 2888;
    private List<Photo> images;
    private Context context;
    private ImageSelectedListener imageSelectedListener;

    public PhotoGalleryPhotoAdapter(Context context, List<Photo> images){
        this.context = context;
        this.images = images;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_gallery_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, context);

        return viewHolder;
    }


    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);

        int position = holder.getAdapterPosition();

        if (position == 0){
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_photo_camera_black_48dp);
            holder.galleryItemView.setImageBitmap(bitmap);
        } else {
            Photo photo = images.get(position-1);

            holder.galleryItemView.setImageBitmap(photo.getBitmap());
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == 0){
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_photo_camera_black_48dp);
            holder.galleryItemView.setImageBitmap(bitmap);
        } else {
            Photo photo = images.get(position-1);
            holder.galleryItemView.setImageBitmap(photo.getBitmap());
        }
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() + 1 : 1;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView galleryItemView;
        private Context context;
        private Activity activity;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            itemView.setOnClickListener(this);
            galleryItemView = (ImageView) itemView.findViewById(R.id.gallery_image_item);
            this.context = context;
            activity = ((Activity) this.context);

        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if(pos == 0){
                //open camera
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                activity.startActivityForResult(cameraIntent, PHOTO_TAKEN_REQUEST_CODE);
            }
            else {
                Photo selectedPhoto = images.get(pos-1);

                if (imageSelectedListener != null){
                    imageSelectedListener.onImageSelected(selectedPhoto.getBitmap());
                }

            }
        }
    }

    public ImageSelectedListener getImageSelectedListener() {
        return imageSelectedListener;
    }

    public void setImageSelectedListener(ImageSelectedListener imageSelectedListener) {
        this.imageSelectedListener = imageSelectedListener;
    }

    public List<Photo> getImages() {
        return images;
    }

    public void setImages(List<Photo> images) {
        this.images = images;
    }
}
