package com.arcsoft.arcfacedemo.util.face;

import android.graphics.Bitmap;

public interface FaceFeatureCallback {
    void onFaceFeatureAvailable(Bitmap bitmap, float faceSimilar, float quality, boolean result);
}
