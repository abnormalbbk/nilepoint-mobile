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

import com.nilepoint.monitorevaluatemobile.R;
import com.nilepoint.monitorevaluatemobile.camera.ImageSelectedListener;
import com.nilepoint.monitorevaluatemobile.camera.PhotoProcessor;

import java.util.ArrayList;

/**
 * Created by claudiatrafton on 5/28/17.
 */

public class PhotoGalleryFileAdapter extends RecyclerView.Adapter<PhotoGalleryFileAdapter.ViewHolder> {

    private static final String TAG = "PhotoGalleryFileAdapter";
    private static final int PHOTO_TAKEN_REQUEST_CODE = 1888;
    private static final int PHOTO_SELECTED_REQUEST_CODE = 2888;
    private ArrayList<String> images;
    private Context context;
    private ImageSelectedListener imageSelectedListener;

    public PhotoGalleryFileAdapter(Context context, ArrayList<String> images){
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

    public static Bitmap decodeSampledBitmap(String filePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap scaled = BitmapFactory.decodeFile(filePath, options);
        return scaled;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);

        int position = holder.getAdapterPosition();

        if (position == 0){
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_photo_camera_black_48dp);
            holder.galleryItemView.setImageBitmap(bitmap);
        } else {
            String fname = images.get(position-1);

            Bitmap img = decodeSampledBitmap(fname, 128, 128);

            holder.galleryItemView.setImageBitmap(img);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == 0){
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_photo_camera_black_48dp);
            holder.galleryItemView.setImageBitmap(bitmap);
        } else {
            String fname = images.get(position-1);
            Bitmap img = decodeSampledBitmap(fname, 128, 128);
            holder.galleryItemView.setImageBitmap(img);
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
                PhotoProcessor pp = new PhotoProcessor();

                String selectedPhoto = images.get(pos-1);

                Bitmap img = BitmapFactory.decodeFile(selectedPhoto);

                Log.d(TAG, "Image at position " + (pos-1) + " selected");

                if (imageSelectedListener != null){
                    imageSelectedListener.onImageSelected(img);
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

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }
}
