package com.nilepoint.monitorevaluatemobile.camera;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by claudiatrafton on 5/29/17.
 */

public class PhotoProcessor {

    public PhotoProcessor(){

    }

    /**
     * convert photo to a byte array
     * @param image a bitmap photo, either taken or selected
     */
    public byte[] getPhotoByteArray(Bitmap image){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        return stream.toByteArray();
    }
}
