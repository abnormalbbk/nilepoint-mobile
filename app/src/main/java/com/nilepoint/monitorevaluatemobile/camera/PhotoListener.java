package com.nilepoint.monitorevaluatemobile.camera;

import com.nilepoint.model.Photo;

import java.io.Serializable;

/**
 * Created by ashaw on 10/3/17.
 */

public interface PhotoListener extends Serializable {
    void onNewPhoto(Photo photo);
    void onPhotoSelected(Photo photo);
}
