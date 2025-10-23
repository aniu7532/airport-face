package com.arcsoft.arcfacedemo.ui.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.ErrorCodeUtil;
import com.arcsoft.arcfacedemo.util.FaceRectTransformer;
import com.arcsoft.arcfacedemo.util.face.FaceFeatureCallback;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.arcfacedemo.util.face.constants.LivenessType;
import com.arcsoft.arcfacedemo.util.face.constants.RecognizeColor;
import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.face.model.RecognizeConfiguration;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.arcsoft.arcfacedemo.widget.FaceRectView;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.ImageQualitySimilar;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.LivenessParam;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.ExtractType;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.arcsoft.imageutil.ArcSoftRotateDegree;
import com.blankj.utilcode.util.ObjectUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * modified by slm 2025-01-21 活体检测ViewModel
 */
public class LivenessDetectViewModel extends ViewModel {

    private static final String TAG = "LivenessDetectViewModel";

    private FaceEngine flEngine;
    private FaceEngine ftEngine;
    private FaceEngine mainFaceEngine;
    private static final int INIT_MASK = FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT
            | FaceEngine.ASF_GENDER | FaceEngine.ASF_AGE | FaceEngine.ASF_MASK_DETECT | FaceEngine.ASF_IMAGEQUALITY;
    private FaceFeatureCallback faceFeatureCallback;

    // 设置回调接口
    public void setFaceFeatureCallback(FaceFeatureCallback callback) {
        this.faceFeatureCallback = callback;
    }

    private FaceHelper faceHelper;
    private byte[] irNv21;
    private Camera.Size previewSize;

    private MutableLiveData<Integer> ftInitCode = new MutableLiveData<>();
    private MutableLiveData<Integer> flInitCode = new MutableLiveData<>();

    private int livenessMask;
    private ExecutorService livenessExecutor;

    private ConcurrentHashMap<Integer, Integer> rgbLivenessMap;
    private ConcurrentHashMap<Integer, Integer> irLivenessMap;
    private final ReentrantLock livenessDetectLock = new ReentrantLock();

    public void init(boolean canOpenDualCamera) {
        Context context = ArcFaceApplication.getApplication();
        String livenessTypeStr = ConfigUtil.getLivenessDetectType(ArcFaceApplication.getApplication());
        LivenessType livenessType;
        if (livenessTypeStr.equals(ArcFaceApplication.getApplication().getString(R.string.value_liveness_type_ir))) {
            livenessType = LivenessType.IR;
        } else {
            livenessType = LivenessType.RGB;
        }
        rgbLivenessMap = new ConcurrentHashMap<>();
        if (canOpenDualCamera && livenessType == LivenessType.IR) {
            irLivenessMap = new ConcurrentHashMap<>();
            livenessMask = FaceEngine.ASF_LIVENESS | FaceEngine.ASF_IR_LIVENESS | FaceEngine.ASF_FACE_DETECT;
        } else {
            livenessMask = FaceEngine.ASF_LIVENESS;
        }

        LivenessParam livenessParam = new LivenessParam(ConfigUtil.getRgbLivenessThreshold(context),
                ConfigUtil.getIrLivenessThreshold(context));

        ftEngine = new FaceEngine();
        ftInitCode.postValue(ftEngine.init(context, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(context),
                ConfigUtil.getRecognizeMaxDetectFaceNum(context), FaceEngine.ASF_FACE_DETECT));

        flEngine = new FaceEngine();
        flInitCode.postValue(flEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, ConfigUtil.getFtOrient(context),
                ConfigUtil.getRecognizeMaxDetectFaceNum(context), livenessMask));
        flEngine.setLivenessParam(livenessParam);

        mainFaceEngine = new FaceEngine();
        ftInitCode.postValue(mainFaceEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE,
                DetectFaceOrientPriority.ASF_OP_ALL_OUT, 6, INIT_MASK));

        livenessExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread t = new Thread(r);
            t.setName("flThread-" + t.getId());
            return t;
        });
    }

    /**
     * 销毁引擎，faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    private void unInit() {
        if (ftEngine != null) {
            synchronized (ftEngine) {
                int ftUnInitCode = ftEngine.unInit();
                Log.e(TAG, "ftUnInitCode: " + ftUnInitCode);
            }
        }
        if (flEngine != null) {
            synchronized (flEngine) {
                int frUnInitCode = flEngine.unInit();
                Log.e(TAG, "frUnInitCode: " + frUnInitCode);
            }
        }

        if (mainFaceEngine != null) {
            synchronized (mainFaceEngine) {
                int mainUnInitCode = mainFaceEngine.unInit();
                Log.e(TAG, "mainUnInitCode: " + mainUnInitCode);
            }
        }

    }

    public void destroy() {
        if (livenessExecutor != null) {
            livenessExecutor.shutdown();
            livenessExecutor = null;
        }
        unInit();
    }

    public void onRgbCameraOpened(Camera camera) {
        Camera.Size lastPreviewSize = previewSize;
        previewSize = camera.getParameters().getPreviewSize();
        // 切换相机的时候可能会导致预览尺寸发生变化
        initFaceHelper(lastPreviewSize);
    }

    public void setRgbFaceRectTransformer(FaceRectTransformer rgbFaceRectTransformer) {
        faceHelper.setRgbFaceRectTransformer(rgbFaceRectTransformer);
    }

    public void setIrFaceRectTransformer(FaceRectTransformer irFaceRectTransformer) {
        faceHelper.setIrFaceRectTransformer(irFaceRectTransformer);
    }

    public List<FacePreviewInfo> onPreviewFrameOnfaceFeature(byte[] nv21, FaceFeature faceFeature) {
        List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21, irNv21, false);
        clearLeftFace(facePreviewInfoList);
        return processLiveness(nv21, irNv21, facePreviewInfoList, faceFeature);
    }

    public int RecognizeOneOnOneFace(FaceFeature mainFeature, byte[] nv21, FacePreviewInfo facePreviewInfo,
            float faceScore) {
        if (mainFeature == null) {
            return -1;
        }
        Log.e(TAG, "faceScore: " + faceScore);
        FaceFeature faceFeature = new FaceFeature();
        FaceSimilar faceSimilar = new FaceSimilar();
        if (faceScore < ConfigUtil.getImageQualityNoMaskRegisterThreshold(ArcFaceApplication.getApplication()))
            return -1;
        int res = mainFaceEngine.extractFaceFeature(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21,
                facePreviewInfo.getFaceInfoRgb(), ExtractType.RECOGNIZE, facePreviewInfo.getMask(), faceFeature);
        if (res != ErrorInfo.MOK) {
            Log.d(TAG, "特征提取失败，错误码为:" + res);
            return res;
        }

        int compareResult = mainFaceEngine.compareFaceFeature(mainFeature, faceFeature, faceSimilar);
        ALog.i("faceSimilar.getScore(): " + faceSimilar.getScore());
        boolean pass = faceSimilar.getScore() > ConfigUtil.getRecognizeThreshold(ArcFaceApplication.getApplication());
        if (compareResult == ErrorInfo.MOK && pass) {
            Log.e(TAG, "人脸比对成功 ");
        } else {
            Log.d(TAG, "比对失败，错误码为:" + compareResult);
        }

        /*
         * 1.保存注册结果（注册图、特征数据） 2.为了美观，扩大rect截取注册图
         */
        Rect cropRect = getBestRect(previewSize.width, previewSize.height, facePreviewInfo.getFaceInfoRgb().getRect());
        if (cropRect == null) {
            Log.e(TAG, "registerNv21: cropRect is null!");
        }

        cropRect.left &= ~3;
        cropRect.top &= ~3;
        cropRect.right &= ~3;
        cropRect.bottom &= ~3;

        // 创建一个头像的Bitmap，存放旋转结果图
        if (faceScore > ConfigUtil.getImageQualityNoMaskRegisterThreshold(ArcFaceApplication.getApplication())) {
            Bitmap headBmp = getHeadImage(nv21, previewSize.width, previewSize.height,
                    facePreviewInfo.getFaceInfoRgb().getOrient(), cropRect, ArcSoftImageFormat.NV21);
            facePreviewInfo.getFaceInfoRgb().setFaceId(0);
            // 调用回调函数
            if (faceFeatureCallback != null) {
                faceFeatureCallback.onFaceFeatureAvailable(headBmp, faceSimilar.getScore(), faceScore, pass);
            }
        }
        return compareResult;
    }

    /**
     * 将图像中需要截取的Rect向外扩张一倍，若扩张一倍会溢出，则扩张到边界，若Rect已溢出，则收缩到边界
     *
     * @param width   图像宽度
     * @param height  图像高度
     * @param srcRect 原Rect
     * @return 调整后的Rect
     */
    private static Rect getBestRect(int width, int height, Rect srcRect) {
        if (srcRect == null) {
            return null;
        }
        Rect rect = new Rect(srcRect);

        // 原rect边界已溢出宽高的情况
        int maxOverFlow = Math.max(-rect.left, Math.max(-rect.top, Math.max(rect.right - width, rect.bottom - height)));
        if (maxOverFlow >= 0) {
            rect.inset(maxOverFlow, maxOverFlow);
            return rect;
        }

        // 原rect边界未溢出宽高的情况
        int padding = rect.height() / 2;

        // 若以此padding扩张rect会溢出，取最大padding为四个边距的最小值
        if (!(rect.left - padding > 0 && rect.right + padding < width && rect.top - padding > 0
                && rect.bottom + padding < height)) {
            padding = Math.min(Math.min(Math.min(rect.left, width - rect.right), height - rect.bottom), rect.top);
        }
        rect.inset(-padding, -padding);
        return rect;
    }

    public FaceFeature getFeature(Bitmap bitmap) {
        if (bitmap == null) {
            ALog.e("getFeature failed bitmap == null");
            return null;
        }
        if (flEngine == null) {
            ALog.e("getFeature failed flEngine == null");
            return null; // 添加默认返回值
        }
        // 接口需要的bgr24宽度必须为4的倍数
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        if (bitmap == null) {
            ALog.e("getFeature failed bitmap == null");
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // bitmap转bgr24
        byte[] bgr24 =
                ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            ALog.d("getFeature failed failed to transform bitmap to imageData, code is " + transformCode);
            return null;
        }
        List<FaceInfo> faceInfoList = new ArrayList<>();
        // 人脸检测
        int detectCode = mainFaceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
        if (detectCode != 0 || faceInfoList.isEmpty()) {
            ALog.d("getFeature failed face detection finished, code is " + detectCode + ", face num is "
                    + faceInfoList.size());
            return null;
        }
        // 人脸质量检测
        ImageQualitySimilar imageQualitySimilar = new ImageQualitySimilar();
        int qualityCode = mainFaceEngine.imageQualityDetect(bgr24, width, height, FaceEngine.CP_PAF_BGR24,
                faceInfoList.get(0), 0, imageQualitySimilar);
        if (qualityCode != ErrorInfo.MOK) {
            ALog.d("getFeature failed imageQualityDetect failed! code is " + qualityCode);
            return null;
        }
        FaceFeature faceFeature = new FaceFeature();
        int res = mainFaceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0),
                ExtractType.REGISTER, 0, faceFeature);
        if (res != ErrorInfo.MOK) {
            return null;
        }
        return faceFeature;
    }

    public float getFeatureValue(Bitmap bitmap) {
        if (bitmap == null) {
            ALog.e("getFeatureValue failed bitmap == null");
            return 0;
        }
        if (flEngine == null) {
            ALog.e("getFeatureValue failed flEngine == null");
            return 0; // 添加默认返回值
        }
        // 接口需要的bgr24宽度必须为4的倍数
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        if (bitmap == null) {
            ALog.e("getFeatureValue failed bitmap == null");
            return 0;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // bitmap转bgr24
        byte[] bgr24 =
                ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            ALog.d("getFeatureValue failed to transform bitmap to imageData, code is " + transformCode);
            return 0;
        }
        List<FaceInfo> faceInfoList = new ArrayList<>();
        // 人脸检测
        int detectCode = mainFaceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
        if (detectCode != 0 || faceInfoList.isEmpty()) {
            ALog.d("getFeatureValue failed face detection finished, code is " + detectCode + ", face num is "
                    + faceInfoList.size());
            return 0;
        }
        // 人脸质量检测
        ImageQualitySimilar imageQualitySimilar = new ImageQualitySimilar();
        int qualityCode = mainFaceEngine.imageQualityDetect(bgr24, width, height, FaceEngine.CP_PAF_BGR24,
                faceInfoList.get(0), 0, imageQualitySimilar);
        if (qualityCode != ErrorInfo.MOK) {
            ALog.d("getFeatureValue failed imageQualityDetect failed! code is " + qualityCode);
            return 0;
        }
        ALog.e("getFeatureValue quality:" + imageQualitySimilar.getScore());
        return imageQualitySimilar.getScore();
    }

    /**
     * 截取合适的头像并旋转，保存为注册头像
     *
     * @param originImageData 原始的BGR24数据
     * @param width           BGR24图像宽度
     * @param height          BGR24图像高度
     * @param orient          人脸角度
     * @param cropRect        裁剪的位置
     * @param imageFormat     图像格式
     * @return 头像的图像数据
     */
    private Bitmap getHeadImage(byte[] originImageData, int width, int height, int orient, Rect cropRect,
            ArcSoftImageFormat imageFormat) {
        byte[] headImageData = ArcSoftImageUtil.createImageData(cropRect.width(), cropRect.height(), imageFormat);
        int cropCode = ArcSoftImageUtil.cropImage(originImageData, headImageData, width, height, cropRect, imageFormat);
        if (cropCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            throw new RuntimeException("crop image failed, code is " + cropCode);
        }

        // 判断人脸旋转角度，若不为0度则旋转注册图
        byte[] rotateHeadImageData = null;
        int cropImageWidth;
        int cropImageHeight;
        // 90度或270度的情况，需要宽高互换
        if (orient == FaceEngine.ASF_OC_90 || orient == FaceEngine.ASF_OC_270) {
            cropImageWidth = cropRect.height();
            cropImageHeight = cropRect.width();
        } else {
            cropImageWidth = cropRect.width();
            cropImageHeight = cropRect.height();
        }
        ArcSoftRotateDegree rotateDegree = null;
        switch (orient) {
        case FaceEngine.ASF_OC_90:
            rotateDegree = ArcSoftRotateDegree.DEGREE_270;
            break;
        case FaceEngine.ASF_OC_180:
            rotateDegree = ArcSoftRotateDegree.DEGREE_180;
            break;
        case FaceEngine.ASF_OC_270:
            rotateDegree = ArcSoftRotateDegree.DEGREE_90;
            break;
        case FaceEngine.ASF_OC_0:
        default:
            rotateHeadImageData = headImageData;
            break;
        }
        // 非0度的情况，旋转图像
        if (rotateDegree != null) {
            rotateHeadImageData = new byte[headImageData.length];
            int rotateCode = ArcSoftImageUtil.rotateImage(headImageData, rotateHeadImageData, cropRect.width(),
                    cropRect.height(), rotateDegree, imageFormat);
            if (rotateCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                throw new RuntimeException("rotate image failed, code is : " + rotateCode + ", code description is : "
                        + ErrorCodeUtil.imageUtilErrorCodeToFieldName(rotateCode));
            }
        }
        // 将创建一个Bitmap，并将图像数据存放到Bitmap中
        Bitmap headBmp = Bitmap.createBitmap(cropImageWidth, cropImageHeight, Bitmap.Config.RGB_565);
        int imageDataToBitmapCode = ArcSoftImageUtil.imageDataToBitmap(rotateHeadImageData, headBmp, imageFormat);
        if (imageDataToBitmapCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            throw new RuntimeException("failed to transform image data to bitmap, code is : " + imageDataToBitmapCode
                    + ", code description is : " + ErrorCodeUtil.imageUtilErrorCodeToFieldName(imageDataToBitmapCode));
        }
        return headBmp;
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        Enumeration<Integer> keys = rgbLivenessMap.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == key) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                rgbLivenessMap.remove(key);
                if (irLivenessMap != null) {
                    irLivenessMap.remove(key);
                }
            }
        }
    }

    private List<FacePreviewInfo> processLiveness(byte[] nv21, byte[] irNv21, List<FacePreviewInfo> previewInfoList,
            FaceFeature faceFeature) {
        if (previewInfoList == null || previewInfoList.size() == 0) {
            return null;
        }
        if (!livenessDetectLock.isLocked() && livenessExecutor != null) {
            livenessExecutor.execute(() -> {
                List<FacePreviewInfo> facePreviewInfoList = new LinkedList<>(previewInfoList);
                livenessDetectLock.lock();
                try {
                    int processRgbLivenessCode;
                    if (facePreviewInfoList.isEmpty()) {
                        Log.e(TAG, "facePreviewInfoList isEmpty");
                    } else {
                        synchronized (flEngine) {
                            if (ObjectUtils.isEmpty(facePreviewInfoList)
                                    || ObjectUtils.isEmpty(facePreviewInfoList.get(0))
                                    || ObjectUtils.isEmpty(facePreviewInfoList.get(0).getFaceInfoRgb())) {
                                Log.e(TAG, "facePreviewInfoList isEmpty2");
                                return;
                            }
                            processRgbLivenessCode = flEngine.process(nv21, previewSize.width, previewSize.height,
                                    FaceEngine.CP_PAF_NV21,
                                    new ArrayList<>(
                                            Collections.singletonList(facePreviewInfoList.get(0).getFaceInfoRgb())),
                                    FaceEngine.ASF_LIVENESS);
                        }

                        // 判断no mask 质量
                        ImageQualitySimilar imageQualitySimilar = new ImageQualitySimilar();
                        int qualityCode = mainFaceEngine.imageQualityDetect(nv21, previewSize.width, previewSize.height,
                                FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(0).getFaceInfoRgb(), 0,
                                imageQualitySimilar);
                        float quality = imageQualitySimilar.getScore();
                        if (qualityCode != ErrorInfo.MOK) {
                            Log.e(TAG, "imageQualityDetect failed! code is " + qualityCode);
                        } else {
                            RecognizeOneOnOneFace(faceFeature, nv21, facePreviewInfoList.get(0), quality);
                        }

                        if (processRgbLivenessCode != ErrorInfo.MOK) {
                            Log.e(TAG, "process RGB Liveness error: " + processRgbLivenessCode);
                        } else {
                            List<LivenessInfo> rgbLivenessInfoList = new ArrayList<>();
                            int getRgbLivenessCode = flEngine.getLiveness(rgbLivenessInfoList);
                            if (getRgbLivenessCode != ErrorInfo.MOK) {
                                Log.e(TAG, "get RGB LivenessResult error: " + getRgbLivenessCode);
                            } else {
                                rgbLivenessMap.put(facePreviewInfoList.get(0).getTrackId(),
                                        rgbLivenessInfoList.get(0).getLiveness());
                            }
                        }
                        if ((livenessMask & FaceEngine.ASF_IR_LIVENESS) != 0) {
                            List<FaceInfo> rgbFaceInfoList = new ArrayList<>();
                            FaceInfo irFaceInfo = facePreviewInfoList.get(0).getFaceInfoIr();
                            int fdCode = flEngine.detectFaces(irNv21, previewSize.width, previewSize.height,
                                    FaceEngine.CP_PAF_NV21, rgbFaceInfoList);
                            if (rgbFaceInfoList != null && !rgbFaceInfoList.isEmpty() && facePreviewInfoList != null
                                    && !facePreviewInfoList.isEmpty()) {
                                if (fdCode == ErrorInfo.MOK
                                        && FaceHelper.isFaceSame(rgbFaceInfoList.get(0), irFaceInfo, 0.3)) {
                                    processIrLive(irFaceInfo, facePreviewInfoList.get(0).getTrackId());
                                } else {
                                    Log.e(TAG, "IR Liveness detectFaces error: " + fdCode);
                                }
                            }
                        }
                    }
                } finally {
                    livenessDetectLock.unlock();
                }
            });
        }
        for (FacePreviewInfo facePreviewInfo : previewInfoList) {
            Integer rgbLiveness = rgbLivenessMap.get(facePreviewInfo.getTrackId());
            if (rgbLiveness != null) {
                facePreviewInfo.setRgbLiveness(rgbLiveness);
            }
            if (irLivenessMap != null) {
                Integer irLiveness = irLivenessMap.get(facePreviewInfo.getTrackId());
                if (irLiveness != null) {
                    facePreviewInfo.setIrLiveness(irLiveness);
                }
            }
        }
        return previewInfoList;
    }

    private void processIrLive(FaceInfo irFaceInfo, int trackId) {
        int processIrLivenessCode = -1;
        synchronized (flEngine) {
            processIrLivenessCode = flEngine.processIr(irNv21, previewSize.width, previewSize.height,
                    FaceEngine.CP_PAF_NV21, Arrays.asList(irFaceInfo), FaceEngine.ASF_IR_LIVENESS);
        }
        if (processIrLivenessCode != ErrorInfo.MOK) {
            Log.e(TAG, "process IR Liveness error: " + processIrLivenessCode);
        } else {
            List<LivenessInfo> irLivenessInfoList = new ArrayList<>();
            int getIrLivenessCode = flEngine.getIrLiveness(irLivenessInfoList);
            if (getIrLivenessCode != ErrorInfo.MOK) {
                Log.e(TAG, "get IR LivenessResult error: " + getIrLivenessCode);
            } else {
                if (irLivenessInfoList != null && !irLivenessInfoList.isEmpty()) {
                    irLivenessMap.put(trackId, irLivenessInfoList.get(0).getLiveness());
                }
            }
        }
    }

    /**
     * 当相机打开时由activity调用，进行一些初始化操作
     *
     * @param camera 相机实例
     */
    public void onIrCameraOpened(Camera camera) {
        Camera.Size lastPreviewSize = previewSize;
        previewSize = camera.getParameters().getPreviewSize();
        // 切换相机的时候可能会导致预览尺寸发生变化
        initFaceHelper(lastPreviewSize);
        faceHelper.setDualCameraFaceInfoTransformer(faceInfo -> {
            return new FaceInfo(faceInfo);
        });
    }

    private void initFaceHelper(Camera.Size lastPreviewSize) {
        if (faceHelper == null || lastPreviewSize == null || lastPreviewSize.width != previewSize.width
                || lastPreviewSize.height != previewSize.height) {
            Integer trackedFaceCount = null;
            // 记录切换时的人脸序号
            if (faceHelper != null) {
                trackedFaceCount = faceHelper.getTrackedFaceCount();
                faceHelper.release();
            }
            Context context = ArcFaceApplication.getApplication().getApplicationContext();

            faceHelper = new FaceHelper.Builder().ftEngine(ftEngine).previewSize(previewSize).onlyDetectLiveness(true)
                    .recognizeConfiguration(new RecognizeConfiguration.Builder().keepMaxFace(true).build())
                    .trackedFaceCount(
                            trackedFaceCount == null ? ConfigUtil.getTrackedFaceCount(context) : trackedFaceCount)
                    .build();
        }
    }

    public void refreshIrPreviewData(byte[] nv21) {
        irNv21 = nv21;
    }

    /**
     * 根据预览信息生成绘制信息
     *
     * @param facePreviewInfoList 预览信息
     * @return 绘制信息
     */
    public List<FaceRectView.DrawInfo> getDrawInfo(List<FacePreviewInfo> facePreviewInfoList,
            LivenessType livenessType) {
        List<FaceRectView.DrawInfo> drawInfoList = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
            int liveness = livenessType == LivenessType.RGB ? facePreviewInfoList.get(i).getRgbLiveness()
                    : facePreviewInfoList.get(i).getIrLiveness();
            Rect rect = livenessType == LivenessType.RGB ? facePreviewInfoList.get(i).getRgbTransformedRect()
                    : facePreviewInfoList.get(i).getIrTransformedRect();
            // 根据识别结果和活体结果设置颜色
            int color;
            String name;
            switch (liveness) {
            case LivenessInfo.ALIVE:
                color = RecognizeColor.COLOR_SUCCESS;
                name = "ALIVE";
                break;
            case LivenessInfo.NOT_ALIVE:
                color = RecognizeColor.COLOR_FAILED;
                name = "NOT_ALIVE";
                break;
            default:
                color = RecognizeColor.COLOR_UNKNOWN;
                name = "UNKNOWN";
                break;
            }

            drawInfoList.add(
                    new FaceRectView.DrawInfo(rect, GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, liveness, color, name));
        }
        return drawInfoList;
    }

    public Point loadPreviewSize() {
        String[] size = ConfigUtil.getPreviewSize(ArcFaceApplication.getApplication()).split("x");
        return new Point(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
    }
}
