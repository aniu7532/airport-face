package com.arcsoft.arcfacedemo.util.face;

import android.graphics.Bitmap;

/**
 * 人脸特征提取结果回调接口。
 */
public interface FaceFeatureCallback {
    /**
     * 人脸特征可用时回调。
     *
     * @param bitmap        人脸截图
     * @param faceSimilar   人脸相似度
     * @param quality       图像质量分
     * @param result        比对是否通过
     */
    void onFaceFeatureAvailable(Bitmap bitmap, float faceSimilar, float quality, boolean result);
}
