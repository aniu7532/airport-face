package com.arcsoft.arcfacedemo.util.face;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.ui.model.CompareResult;
import com.arcsoft.arcfacedemo.util.FaceRectTransformer;
import com.arcsoft.arcfacedemo.util.face.constants.LivenessType;
import com.arcsoft.arcfacedemo.util.face.constants.RequestFeatureStatus;
import com.arcsoft.arcfacedemo.util.face.constants.RequestLivenessStatus;
import com.arcsoft.arcfacedemo.util.face.facefilter.FaceMoveFilter;
import com.arcsoft.arcfacedemo.util.face.facefilter.FaceRecognizeAreaFilter;
import com.arcsoft.arcfacedemo.util.face.facefilter.FaceRecognizeFilter;
import com.arcsoft.arcfacedemo.util.face.facefilter.FaceSizeFilter;
import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.face.model.RecognizeConfiguration;
import com.arcsoft.arcfacedemo.util.face.model.RecognizeInfo;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.ImageQualitySimilar;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.MaskInfo;
import com.arcsoft.face.enums.ExtractType;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.arcsoft.imageutil.ArcSoftRotateDegree;
import com.google.gson.Gson;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 人脸操作辅助类
 */
public class FaceHelper implements FaceListener {

    private static final String TAG = "FaceHelper";

    /**
     * 识别结果的回调
     */
    private RecognizeCallback recognizeCallback;

    /**
     * 用于记录人脸识别过程信息
     */
    private ConcurrentHashMap<Integer, RecognizeInfo> recognizeInfoMap = new ConcurrentHashMap<>();

    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();
    /**
     * 转换方式，用于IR活体检测
     */
    private IDualCameraFaceInfoTransformer dualCameraFaceInfoTransformer;

    /**
     * 线程池正在处理任务
     */
    private static final int ERROR_BUSY = -1;
    /**
     * 特征提取引擎为空
     */
    private static final int ERROR_FR_ENGINE_IS_NULL = -2;
    /**
     * 活体检测引擎为空
     */
    private static final int ERROR_FL_ENGINE_IS_NULL = -3;
    /**
     * 人脸追踪引擎
     */
    private FaceEngine ftEngine;
    /**
     * 口罩检测引擎
     */
    private FaceEngine maskEngine;
    /**
     * 特征提取引擎
     */
    private FaceEngine frEngine;
    /**
     * 活体检测引擎
     */
    private FaceEngine flEngine;

    private Camera.Size previewSize;

    private List<FaceInfo> faceInfoList = new CopyOnWriteArrayList<>();
    private List<MaskInfo> maskInfoList = new CopyOnWriteArrayList<>();
    /**
     * 特征提取线程池
     */
    private ExecutorService frExecutor;
    /**
     * 活体检测线程池
     */
    private ExecutorService flExecutor;
    /**
     * 特征提取线程队列
     */
    private LinkedBlockingQueue<Runnable> frThreadQueue;
    /**
     * 活体检测线程队列
     */
    private LinkedBlockingQueue<Runnable> flThreadQueue;

    private FaceRectTransformer rgbFaceRectTransformer;
    private FaceRectTransformer irFaceRectTransformer;
    /**
     * 控制可识别区域（相对于View），若未设置，则是全部区域
     */
    private Rect recognizeArea = new Rect(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private List<FaceRecognizeFilter> faceRecognizeFilterList = new ArrayList<>();
    /**
     * 上次应用退出时，记录的该App检测过的人脸数了
     */
    private int trackedFaceCount = 0;
    /**
     * 本次打开引擎后的最大faceId
     */
    private int currentMaxFaceId = 0;

    /**
     * 是否只检测活体
     */
    private boolean onlyDetectLiveness;

    /**
     * 识别的配置项
     */
    private RecognizeConfiguration recognizeConfiguration;
    private List<Integer> currentTrackIdList = new ArrayList<>();
    private List<FacePreviewInfo> facePreviewInfoList = new ArrayList<>();
    private Disposable timerDisposable;

    private FaceHelper(Builder builder) {
        onlyDetectLiveness = builder.onlyDetectLiveness;
        ftEngine = builder.ftEngine;
        maskEngine = builder.maskEngine;
        trackedFaceCount = builder.trackedFaceCount;
        previewSize = builder.previewSize;
        frEngine = builder.frEngine;
        flEngine = builder.flEngine;
        recognizeCallback = builder.recognizeCallback;
        recognizeConfiguration = builder.recognizeConfiguration;
        dualCameraFaceInfoTransformer = builder.dualCameraFaceInfoTransformer;
        /*
         * fr 线程队列大小
         */
        int frQueueSize = recognizeConfiguration.getMaxDetectFaces();
        if (builder.frQueueSize > 0) {
            frQueueSize = builder.frQueueSize;
        } else {
            ALog.e("frThread num must > 0, now using default value:" + frQueueSize);
        }
        frThreadQueue = new LinkedBlockingQueue<>(frQueueSize);
        frExecutor = new ThreadPoolExecutor(1, frQueueSize, 0, TimeUnit.MILLISECONDS, frThreadQueue, r -> {
            Thread t = new Thread(r);
            t.setName("frThread-" + t.getId());
            return t;
        });

        /*
         * fl 线程队列大小
         */
        int flQueueSize = recognizeConfiguration.getMaxDetectFaces();
        if (builder.flQueueSize > 0) {
            flQueueSize = builder.flQueueSize;
        } else {
            ALog.e("flThread num must > 0, now using default value:" + flQueueSize);
        }
        flThreadQueue = new LinkedBlockingQueue<Runnable>(flQueueSize);
        flExecutor = new ThreadPoolExecutor(1, flQueueSize, 0, TimeUnit.MILLISECONDS, flThreadQueue, r -> {
            Thread t = new Thread(r);
            t.setName("flThread-" + t.getId());
            return t;
        });
        if (previewSize == null) {
            throw new RuntimeException("previewSize must be specified!");
        }
        if (recognizeConfiguration.isEnableFaceSizeLimit()) {
            // 由于目前人脸框的宽高接近一致，所以在使用时horizontalSize和verticalSize的值设置成一样
            faceRecognizeFilterList.add(new FaceSizeFilter(recognizeConfiguration.getFaceSizeLimit(),
                    recognizeConfiguration.getFaceSizeLimit()));
        }
        if (recognizeConfiguration.isEnableFaceMoveLimit()) {
            faceRecognizeFilterList.add(new FaceMoveFilter(recognizeConfiguration.getFaceMoveLimit()));
        }
        if (recognizeConfiguration.isEnableFaceAreaLimit()) {
            faceRecognizeFilterList.add(new FaceRecognizeAreaFilter(recognizeArea));
        }

    }

    /**
     * 请求获取人脸特征数据
     *
     * @param nv21            图像数据
     * @param facePreviewInfo 人脸信息
     * @param width           图像宽度
     * @param height          图像高度
     * @param format          图像格式
     */
    public void requestFaceFeature(byte[] nv21, FacePreviewInfo facePreviewInfo, int width, int height, int format) {
        if (frEngine != null && frThreadQueue.remainingCapacity() > 0) {
            frExecutor.execute(new FaceRecognizeRunnable(nv21, facePreviewInfo, width, height, format));
        } else {
            onFaceFeatureInfoGet(null, facePreviewInfo.getTrackId(), ERROR_BUSY, 0);
        }
    }

    /**
     * 请求获取活体检测结果，需要传入活体的参数，以下参数同
     *
     * @param nv21         NV21格式的图像数据
     * @param faceInfo     人脸信息
     * @param width        图像宽度
     * @param height       图像高度
     * @param format       图像格式
     * @param livenessType 活体检测类型
     * @param waitLock
     */
    public void requestFaceLiveness(byte[] nv21, FacePreviewInfo faceInfo, int width, int height, int format,
            LivenessType livenessType, Object waitLock) {
        if (flEngine != null && flThreadQueue.remainingCapacity() > 0) {
            flExecutor.execute(
                    new FaceLivenessDetectRunnable(nv21, faceInfo, width, height, format, livenessType, waitLock));
        } else {
            onFaceLivenessInfoGet(null, faceInfo.getTrackId(), ERROR_BUSY);
        }

    }

    /**
     * 释放对象
     */
    public void release() {
        if (getFeatureDelayedDisposables != null) {
            getFeatureDelayedDisposables.clear();
        }
        if (!frExecutor.isShutdown()) {
            frExecutor.shutdownNow();
            frThreadQueue.clear();
        }
        if (!flExecutor.isShutdown()) {
            flExecutor.shutdownNow();
            flThreadQueue.clear();
        }
        if (faceInfoList != null) {
            faceInfoList.clear();
        }
        if (frThreadQueue != null) {
            frThreadQueue.clear();
            frThreadQueue = null;
        }
        if (flThreadQueue != null) {
            flThreadQueue.clear();
            flThreadQueue = null;
        }
        faceInfoList = null;
    }

    /**
     * 处理帧数据
     *
     * @param rgbNv21     可见光相机预览回传的NV21数据
     * @param irNv21      红外相机预览回传的NV21数据
     * @param doRecognize 是否进行识别
     * @return 实时人脸处理结果，封装添加了一个trackId，trackId的获取依赖于faceId，用于记录人脸序号并保存
     */
    public List<FacePreviewInfo> onPreviewFrame(@NonNull byte[] rgbNv21, @Nullable byte[] irNv21, boolean doRecognize) {
        if (ftEngine != null) {
            faceInfoList.clear();
            maskInfoList.clear();
            facePreviewInfoList.clear();
            int code = ftEngine.detectFaces(rgbNv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21,
                    faceInfoList);
            if (code != ErrorInfo.MOK) {
                onFail(new Exception("detectFaces failed,code is " + code));
                return facePreviewInfoList;
            }
            if (recognizeConfiguration.isKeepMaxFace()) {
                keepMaxFace(faceInfoList);
            }
            refreshTrackId(faceInfoList);
            if (faceInfoList.isEmpty()) {
                return facePreviewInfoList;
            }
            if (!onlyDetectLiveness) {
                code = maskEngine.process(rgbNv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21,
                        faceInfoList, FaceEngine.ASF_MASK_DETECT);
                if (code == ErrorInfo.MOK) {
                    code = maskEngine.getMask(maskInfoList);
                    if (code != ErrorInfo.MOK) {
                        onFail(new Exception("process getMask failed,code is " + code));
                        return facePreviewInfoList;
                    }
                } else {
                    onFail(new Exception("process mask failed,code is " + code));
                    return facePreviewInfoList;
                }
            }

            for (int i = 0; i < faceInfoList.size(); i++) {
                FacePreviewInfo facePreviewInfo = new FacePreviewInfo(faceInfoList.get(i), currentTrackIdList.get(i));
                if (!maskInfoList.isEmpty()) {
                    MaskInfo maskInfo = maskInfoList.get(i);
                    facePreviewInfo.setMask(maskInfo.getMask());
                }
                if (rgbFaceRectTransformer != null && recognizeArea != null) {
                    Rect rect = rgbFaceRectTransformer.adjustRect(faceInfoList.get(i).getRect());
                    Rect foreRect = rgbFaceRectTransformer.adjustRect(faceInfoList.get(i).getForeheadRect());
                    facePreviewInfo.setRgbTransformedRect(rect);
                    facePreviewInfo.setForeRect(foreRect);
                }
                if (irFaceRectTransformer != null) {
                    FaceInfo faceInfo = faceInfoList.get(i);
                    if (dualCameraFaceInfoTransformer != null) {
                        faceInfo = dualCameraFaceInfoTransformer.transformFaceInfo(faceInfo);
                    }
                    facePreviewInfo.setFaceInfoIr(faceInfo);
                    facePreviewInfo.setIrTransformedRect(irFaceRectTransformer.adjustRect(faceInfo.getRect()));
                }
                facePreviewInfoList.add(facePreviewInfo);
            }
            clearLeftFace(facePreviewInfoList);
            if (doRecognize) {
                doRecognize(rgbNv21, irNv21, facePreviewInfoList);
            }
        } else {
            facePreviewInfoList.clear();
        }
        return facePreviewInfoList;
    }

    public void setRgbFaceRectTransformer(FaceRectTransformer rgbFaceRectTransformer) {
        this.rgbFaceRectTransformer = rgbFaceRectTransformer;
    }

    public void setIrFaceRectTransformer(FaceRectTransformer irFaceRectTransformer) {
        this.irFaceRectTransformer = irFaceRectTransformer;
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            if (getFeatureDelayedDisposables != null) {
                getFeatureDelayedDisposables.clear();
            }
        }
        Enumeration<Integer> keys = recognizeInfoMap.keys();
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
                RecognizeInfo recognizeInfo = recognizeInfoMap.remove(key);
                if (recognizeInfo != null) {
                    recognizeCallback.onNoticeChanged("");
                    // 人脸离开时，通知特征提取线程，避免一直等待活体结果
                    synchronized (recognizeInfo.getWaitLock()) {
                        recognizeInfo.getWaitLock().notifyAll();
                    }
                }
            }
        }
    }

    private Bitmap getHeadImage(byte[] originImageData, int width, int height, int orient, Rect cropRect,
            ArcSoftImageFormat imageFormat) {
        byte[] headImageData = null;
        try {
            headImageData = ArcSoftImageUtil.createImageData(cropRect.width(), cropRect.height(), imageFormat);
            int cropCode =
                    ArcSoftImageUtil.cropImage(originImageData, headImageData, width, height, cropRect, imageFormat);
            if (cropCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                // throw new RuntimeException("crop image failed, code is " + cropCode);
                return null;
            }
        } catch (Exception e) {
            ALog.i("byte[]转Bitmap图像转换出错: " + e);
            return null;
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
                // throw new RuntimeException("rotate image failed, code is : " + rotateCode + ", code description is :
                // " + ErrorCodeUtil.imageUtilErrorCodeToFieldName(rotateCode));
                ALog.i("getHeadImage: 错误");
                return null;
            }
        }
        // 将创建一个Bitmap，并将图像数据存放到Bitmap中
        Bitmap headBmp = Bitmap.createBitmap(cropImageWidth, cropImageHeight, Bitmap.Config.RGB_565);
        int imageDataToBitmapCode = ArcSoftImageUtil.imageDataToBitmap(rotateHeadImageData, headBmp, imageFormat);
        if (imageDataToBitmapCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            // throw new RuntimeException("failed to transform image data to bitmap, code is : " + imageDataToBitmapCode
            // + ", code description is : " + ErrorCodeUtil.imageUtilErrorCodeToFieldName(imageDataToBitmapCode));
            ALog.i("getHeadImage: 错误");
            return null;
        }
        return headBmp;
    }

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

    private void doRecognize(byte[] rgbNv21, byte[] irNv21, List<FacePreviewInfo> facePreviewInfoList) {
        if (facePreviewInfoList != null && !facePreviewInfoList.isEmpty() && previewSize != null) {
            for (FaceRecognizeFilter faceRecognizeFilter : faceRecognizeFilterList) {
                faceRecognizeFilter.filter(facePreviewInfoList);
            }
            for (int i = 0; i < facePreviewInfoList.size(); i++) {
                FacePreviewInfo facePreviewInfo = facePreviewInfoList.get(i);
                if (!facePreviewInfo.isQualityPass()) {
                    continue;
                }
                // 跳过mask值为MaskInfo.UNKNOWN的人脸
                if (!onlyDetectLiveness && facePreviewInfo.getMask() == MaskInfo.UNKNOWN) {
                    continue;
                }
                /*
                 * 1.保存注册结果（注册图、特征数据） 2.为了美观，扩大rect截取注册图
                 */
                Rect cropRect =
                        getBestRect(previewSize.width, previewSize.height, facePreviewInfo.getFaceInfoRgb().getRect());
                if (cropRect == null) {
                    ALog.e("registerNv21: cropRect is null!");
                    continue;
                }

                cropRect.left &= ~3;
                cropRect.top &= ~3;
                cropRect.right &= ~3;
                cropRect.bottom &= ~3;
                RecognizeInfo recognizeInfo = getRecognizeInfo(recognizeInfoMap, facePreviewInfo.getTrackId());
                Bitmap headBmp = getHeadImage(rgbNv21, previewSize.width, previewSize.height,
                        facePreviewInfo.getFaceInfoRgb().getOrient(), cropRect, ArcSoftImageFormat.NV21);
                if (headBmp == null) {
                    Log.e(TAG, "registerNv21: headBmp is null!");
                    continue;
                }
                recognizeInfo.setImageData(headBmp); // 存储当前的视频流图像数据

                int status = recognizeInfo.getRecognizeStatus();
                /*
                 * 在活体检测开启，在人脸识别状态不为成功或人脸活体状态不为处理中（ANALYZING）且不为处理完成（ALIVE、NOT_ALIVE）时重新进行活体检测
                 */
                if (recognizeConfiguration.isEnableLiveness() && status != RequestFeatureStatus.SUCCEED) {
                    int liveness = recognizeInfo.getLiveness();
                    if (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE
                            && liveness != RequestLivenessStatus.ANALYZING || status == RequestFeatureStatus.FAILED) {
                        changeLiveness(facePreviewInfo.getTrackId(), RequestLivenessStatus.ANALYZING);
                        requestFaceLiveness(irNv21 == null ? rgbNv21 : irNv21, facePreviewInfo, previewSize.width,
                                previewSize.height, FaceEngine.CP_PAF_NV21,
                                irNv21 == null ? LivenessType.RGB : LivenessType.IR, recognizeInfo.getWaitLock());
                    }
                }
                /*
                 * 对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数）， 特征提取回传的人脸特征结果在{@link
                 * FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}中回传
                 */
                if (status == RequestFeatureStatus.TO_RETRY) {
                    changeRecognizeStatus(facePreviewInfo.getTrackId(), RequestFeatureStatus.SEARCHING);
                    requestFaceFeature(rgbNv21, facePreviewInfo, previewSize.width, previewSize.height,
                            FaceEngine.CP_PAF_NV21);
                }
            }
        }
    }

    @Override
    public void onFail(Exception e) {
        Log.e(TAG, "onFail:" + e.getMessage());
    }

    /**
     * 获取识别信息，识别信息为空则创建一个新的
     *
     * @param recognizeInfoMap 存放识别信息的map
     * @param trackId          人脸唯一标识
     * @return 识别信息
     */
    public RecognizeInfo getRecognizeInfo(Map<Integer, RecognizeInfo> recognizeInfoMap, int trackId) {
        RecognizeInfo recognizeInfo = recognizeInfoMap.get(trackId);
        if (recognizeInfo == null) {
            recognizeInfo = new RecognizeInfo();
            recognizeInfoMap.put(trackId, recognizeInfo);
        }
        return recognizeInfo;
    }

    @Override
    public void onFaceFeatureInfoGet(@Nullable FaceFeature faceFeature, Integer trackId, Integer errorCode,
            float quality) {
        // FR成功
        RecognizeInfo recognizeInfo = getRecognizeInfo(recognizeInfoMap, trackId);

        if (faceFeature != null) {
            // 人脸已离开，不用处理
            if (recognizeInfo == null) {
                return;
            }
            // 不做活体检测的情况，直接搜索
            if (!recognizeConfiguration.isEnableLiveness()) {
                // searchFace(faceFeature, trackId);
                searchFace(faceFeature, trackId, recognizeInfo.getImageData(), quality); // 传入图像数据
            }
            // 活体检测通过，搜索特征
            else if (recognizeInfo.getLiveness() == LivenessInfo.ALIVE) {
                // searchFace(faceFeature, trackId);
                searchFace(faceFeature, trackId, recognizeInfo.getImageData(), quality); // 传入图像数据
            }
            // 活体检测未出结果，或者非活体，等待
            else {
                synchronized (recognizeInfo.getWaitLock()) {
                    try {
                        recognizeInfo.getWaitLock().wait();
                        if (recognizeInfoMap.containsKey(trackId)) {
                            onFaceFeatureInfoGet(faceFeature, trackId, errorCode, quality);
                        }
                    } catch (InterruptedException e) {
                        ALog.e("onFaceFeatureInfoGet: 等待活体结果时退出界面会执行，正常现象，可注释异常代码块");
                        e.printStackTrace();
                    }
                }
            }

        }
        // 特征提取失败时，为了及时提示做个UI反馈，将name修改为"ExtractCode:${errorCode}"，再重置状态
        else {
            if (recognizeInfo.increaseAndGetExtractErrorRetryCount() > recognizeConfiguration.getExtractRetryCount()) {
                // 在尝试最大次数后，特征提取仍然失败，则认为识别未通过
                recognizeInfo.setExtractErrorRetryCount(0);
                retryRecognizeDelayed(trackId);
            } else {
                changeRecognizeStatus(trackId, RequestFeatureStatus.TO_RETRY);
            }
        }
    }

    /**
     * 延迟 {@link RecognizeConfiguration#getLivenessFailedRetryInterval()}后，重新进行活体检测
     *
     * @param trackId 人脸ID
     */
    private void retryLivenessDetectDelayed(final Integer trackId) {
        Observable.timer(recognizeConfiguration.getLivenessFailedRetryInterval(), TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        changeLiveness(trackId, LivenessInfo.UNKNOWN);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    /**
     * 延迟 {@link RecognizeConfiguration#getRecognizeFailedRetryInterval()}后，重新进行人脸识别
     *
     * @param trackId 人脸ID
     */
    private void retryRecognizeDelayed(final Integer trackId) {
        changeRecognizeStatus(trackId, RequestFeatureStatus.FAILED);
        Observable.timer(recognizeConfiguration.getRecognizeFailedRetryInterval(), TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
                        changeRecognizeStatus(trackId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    @Override
    public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo, Integer trackId, Integer errorCode) {
        if (livenessInfo != null) {
            int liveness = livenessInfo.getLiveness();
            Log.i(TAG, "onFaceLivenessInfoGet liveness:" + liveness);
            changeLiveness(trackId, liveness);
            // 非活体，重试
            if (liveness != LivenessInfo.ALIVE) {
                noticeCurrentStatus("活体检测未通过");
                // 延迟 FAIL_RETRY_INTERVAL 后，将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                retryLivenessDetectDelayed(trackId);
            }
        } else {
            RecognizeInfo recognizeInfo = getRecognizeInfo(recognizeInfoMap, trackId);
            // 连续多次活体检测失败（接口调用回传值非0），将活体检测值重置为未知，会在帧回调中重新进行活体检测
            if (recognizeInfo.increaseAndGetLivenessErrorRetryCount() > recognizeConfiguration
                    .getLivenessRetryCount()) {
                recognizeInfo.setLivenessErrorRetryCount(0);
                retryLivenessDetectDelayed(trackId);
            } else {
                changeLiveness(trackId, LivenessInfo.UNKNOWN);
            }
        }
    }

    private void noticeCurrentStatus(String notice) {
        if (recognizeCallback != null) {
            recognizeCallback.onNoticeChanged(notice);
        }
        if (timerDisposable != null && !timerDisposable.isDisposed()) {
            timerDisposable.dispose();
        }
        timerDisposable = Observable.timer(1500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (recognizeCallback != null) {
                        recognizeCallback.onNoticeChanged("");
                    }
                });
    }

    // private GoOffWork goOffWork = new GoOffWork();

    private void searchFace(final FaceFeature faceFeature, final Integer trackId, final Bitmap imageData,
            float quality) {
        CompareResult compareResult = FaceServer.getInstance().searchFaceFeature(faceFeature, frEngine);
        if (compareResult == null || compareResult.getFaceEntity() == null) {
            retryRecognizeDelayed(trackId);
            return;
        }
        compareResult.setTrackId(trackId);
        ALog.i("compareResult.getSimilar():" + compareResult.getSimilar());
        boolean pass = compareResult.getSimilar() > recognizeConfiguration.getSimilarThreshold();
        recognizeCallback.onRecognized(compareResult, getRecognizeInfo(recognizeInfoMap, trackId).getLiveness(), pass);

        if (pass) {
            FaceEntity faceEntity = compareResult.getFaceEntity();
            if (faceEntity == null) {
                ALog.i("searchFace: faceEntity is null");
                return;
            }
            String userName = faceEntity.getUserName();
            if (userName.equals("")) {
                ALog.i("searchFace: userName is null");
                return;
            }
            setName(trackId, "识别通过");
            noticeCurrentStatus("识别通过");
            changeRecognizeStatus(trackId, RequestFeatureStatus.SUCCEED);
            ALog.i("通过的bitmap: " + imageData);
            recognizeCallback.onRecognized(imageData, compareResult.getSimilar(), quality, userName, pass);
            // goOffWork.start(imageData,userName);
        } else {
            noticeCurrentStatus("未通过：未注册");
            retryRecognizeDelayed(trackId);
            recognizeCallback.onRecognized(imageData, compareResult.getSimilar(), quality, null, pass);
            // goOffWork.start(imageData);
        }
    }

    /**
     * 人脸特征提取线程
     */
    public class FaceRecognizeRunnable implements Runnable {
        private FaceInfo faceInfo;
        private int width;
        private int height;
        private int format;
        private Integer trackId;
        private byte[] nv21Data;
        private int isMask;

        /**
         * 异步特征提取任务的构造函数
         *
         * @param nv21Data        可见光图像数据
         * @param facePreviewInfo 人脸信息
         * @param width           图像宽度
         * @param height          图像高度
         * @param format          图像格式
         */
        private FaceRecognizeRunnable(byte[] nv21Data, FacePreviewInfo facePreviewInfo, int width, int height,
                int format) {
            if (nv21Data == null) {
                return;
            }
            this.nv21Data = nv21Data;
            this.faceInfo = new FaceInfo(facePreviewInfo.getFaceInfoRgb());
            this.width = width;
            this.height = height;
            this.format = format;
            this.trackId = facePreviewInfo.getTrackId();
            this.isMask = facePreviewInfo.getMask();
        }

        @Override
        public void run() {
            if (nv21Data != null) {
                if (frEngine != null) {
                    if (recognizeConfiguration.isEnableImageQuality()) {
                        /*
                         * 开启人脸质量检测
                         */
                        ImageQualitySimilar qualitySimilar = new ImageQualitySimilar();
                        int iqCode;
                        long iqStartTime = System.currentTimeMillis();
                        synchronized (frEngine) {
                            iqCode = frEngine.imageQualityDetect(nv21Data, width, height, format, faceInfo, isMask,
                                    qualitySimilar);
                        }
                        Log.i(TAG,
                                "fr iqTime:" + (System.currentTimeMillis() - iqStartTime) + "ms" + ", mask=" + isMask);
                        if (iqCode == ErrorInfo.MOK) {
                            float quality = qualitySimilar.getScore();
                            float destQuality = isMask == MaskInfo.WORN
                                    ? recognizeConfiguration.getImageQualityMaskRecognizeThreshold()
                                    : recognizeConfiguration.getImageQualityNoMaskRecognizeThreshold();
                            if (quality >= destQuality) {
                                extractFace(quality);
                            } else {
                                onFaceFail(iqCode, "fr imageQualityDetect score invalid", quality);
                            }
                        } else {
                            onFaceFail(iqCode, "fr imageQuality failed errorCode is " + iqCode, 0);
                        }
                    } else {
                        extractFace(0);
                    }
                } else {
                    onFaceFail(ERROR_FR_ENGINE_IS_NULL, "fr failed ,frEngine is null", 0);
                }
            }
            nv21Data = null;
        }

        /**
         * 对人脸图像进行特征提取
         */
        private void extractFace(float quality) {
            long irStartTime = System.currentTimeMillis();
            FaceFeature faceFeature = new FaceFeature();
            int frCode;
            synchronized (frEngine) {
                /*
                 * 该场景为识别场景，所以参数“ExtractType”值为ExtractType.RECOGNIZE，且参数“mask”值为实际检测到的值，即isMask
                 */
                frCode = frEngine.extractFaceFeature(nv21Data, width, height, format, faceInfo, ExtractType.RECOGNIZE,
                        isMask, faceFeature);
            }
            ALog.i("frTime:" + (System.currentTimeMillis() - irStartTime) + "ms" + ",frCode:" + frCode);
            if (frCode == ErrorInfo.MOK) {
                onFaceFeatureInfoGet(faceFeature, trackId, frCode, quality);
            } else {
                onFaceFail(frCode, "fr failed errorCode is " + frCode, quality);
            }
        }

        private void onFaceFail(int code, String errorMsg, float quality) {
            onFaceFeatureInfoGet(null, trackId, code, quality);
            onFail(new Exception(errorMsg));
        }
    }

    /**
     * 活体检测的线程
     */
    public class FaceLivenessDetectRunnable implements Runnable {
        private FaceInfo faceInfo;
        private int width;
        private int height;
        private int format;
        private Integer trackId;
        private byte[] nv21Data;
        private LivenessType livenessType;
        private Object waitLock;

        /**
         * 异步活体任务的构造函数
         *
         * @param nv21Data     可见光或红外图像数据
         * @param faceInfo     可见光人脸检测得到的人脸信息
         * @param width        图像宽度
         * @param height       图像高度
         * @param format       图像格式
         * @param livenessType 活体检测类型，可以是可见光活体检测{@link LivenessType#RGB}或红外活体检测{@link LivenessType#IR}
         * @param waitLock     活体检测通过后，调用该对象的notifyAll函数，通知识别线程活体已通过
         */
        private FaceLivenessDetectRunnable(byte[] nv21Data, FacePreviewInfo faceInfo, int width, int height, int format,
                LivenessType livenessType, Object waitLock) {
            if (nv21Data == null) {
                return;
            }
            this.nv21Data = nv21Data;
            this.faceInfo = new FaceInfo(faceInfo.getFaceInfoRgb());
            this.width = width;
            this.height = height;
            this.format = format;
            this.trackId = faceInfo.getTrackId();
            this.livenessType = livenessType;
            this.waitLock = waitLock;
        }

        @Override
        public void run() {
            if (nv21Data != null) {
                if (flEngine != null) {
                    processLiveness();
                } else {
                    onProcessFail(ERROR_FL_ENGINE_IS_NULL, "fl failed ,frEngine is null");
                }
            }
            nv21Data = null;
        }

        /**
         * 执行活体检测
         */
        private void processLiveness() {
            List<LivenessInfo> livenessInfoList = new ArrayList<>();
            int flCode = -1;
            synchronized (flEngine) {
                long flStartTime = System.currentTimeMillis();
                if (livenessType == LivenessType.RGB) {
                    // RGB活体检测
                    flCode = flEngine.process(nv21Data, width, height, format, Arrays.asList(faceInfo),
                            FaceEngine.ASF_LIVENESS);
                } else {
                    // IR活体检测
                    FaceInfo rgbFaceInfo = faceInfo;
                    List<FaceInfo> irFaceInfoList = new ArrayList<>();
                    int fdCode = flEngine.detectFaces(nv21Data, width, height, format, irFaceInfoList);
                    boolean isFaceOverlap = false;
                    if (fdCode == ErrorInfo.MOK && irFaceInfoList.size() > 0) {
                        keepMaxFace(irFaceInfoList);
                        isFaceOverlap = isFaceSame(rgbFaceInfo, irFaceInfoList.get(0), 0.3);
                        ALog.i("isFaceOverlap:" + isFaceOverlap);
                    }
                    if (fdCode == ErrorInfo.MOK && isFaceOverlap) {
                        flCode = flEngine.processIr(nv21Data, width, height, format, irFaceInfoList,
                                FaceEngine.ASF_IR_LIVENESS);
                    } else {
                        onFail(new Exception("ir detectFaces failed fdCode:" + fdCode + ", flCode:" + flCode
                                + ",isFaceOverlap:" + isFaceOverlap));
                    }
                }
                Log.i(TAG, "flTime:" + (System.currentTimeMillis() - flStartTime) + "ms");
            }
            if (flCode == ErrorInfo.MOK) {
                if (livenessType == LivenessType.RGB) {
                    flCode = flEngine.getLiveness(livenessInfoList);
                } else {
                    flCode = flEngine.getIrLiveness(livenessInfoList);
                }
            }

            if (flCode == ErrorInfo.MOK && !livenessInfoList.isEmpty()) {
                onFaceLivenessInfoGet(livenessInfoList.get(0), trackId, flCode);
                if (livenessInfoList.get(0).getLiveness() == LivenessInfo.ALIVE) {
                    synchronized (waitLock) {
                        waitLock.notifyAll();
                    }
                }
            } else {
                onProcessFail(flCode, "fl failed errorCode is " + flCode);
            }
        }

        private void onProcessFail(int code, String msg) {
            onFaceLivenessInfoGet(null, trackId, code);
            onFail(new Exception(msg));
        }
    }

    /**
     * 判断IR图片中的人脸FaceInfo和RGB图片中的人脸FaceInfo是否是同一个人脸
     * 此方案原理：判断两个FaceInfo是否有相交区域，如果有相交区域，则说明是同一个人脸
     *
     * @param faceInfoList 人脸信息列表
     * @param faceInfo     人脸信息
     * @return 人脸信息列表中是否有人脸和传入的人脸信息相交
     */
    public static boolean isSameFace(List<FaceInfo> faceInfoList, FaceInfo faceInfo) {
        if (faceInfoList == null || faceInfoList.isEmpty() || faceInfo == null) {
            return false;
        }
        for (FaceInfo info : faceInfoList) {
            if (Rect.intersects(faceInfo.getRect(), info.getRect())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 计算两个人脸框的重叠面积，以判断它们的重叠程度
     *
     * @param rect1 人脸框
     * @param rect2 人脸框
     * @return
     */

    public static Rect getOverlapArea(Rect rect1, Rect rect2) {
        // 计算两个矩形的重叠区域
        int left = Math.max(rect1.left, rect2.left);
        int right = Math.min(rect1.right, rect2.right);
        int top = Math.max(rect1.top, rect2.top);
        int bottom = Math.min(rect1.bottom, rect2.bottom);

        // 如果没有重叠区域，则返回空的矩形
        if (left < right && top < bottom) {
            return new Rect(left, top, right, bottom);
        }
        return null; // 无重叠
    }

    /**
     * 设置重叠阈值，当两个矩形的重叠面积超过这个阈值时，认为是同一张脸
     *
     * @param rgbFaceInfo rgb人脸信息
     * @param irFaceInfo  ir人脸信息
     * @param threshold   重叠阈值
     * @return Boolean
     */

    public static boolean isFaceSame(FaceInfo rgbFaceInfo, FaceInfo irFaceInfo, double threshold) {
        Rect rect1 = rgbFaceInfo.getRect();
        Rect rect2 = irFaceInfo.getRect();
        Rect overlapRect = getOverlapArea(rect1, rect2);
        if (overlapRect != null) {
            int overlapArea = overlapRect.width() * overlapRect.height();
            int face1Area = rect1.width() * rect1.height();
            int face2Area = rect2.width() * rect2.height();
            // 计算重叠面积占两个矩形面积的比例
            double overlapRatio = (double) overlapArea / Math.min(face1Area, face2Area);
            ALog.d("overlapRatio:" + overlapRatio + "," + threshold);

            return overlapRatio > threshold;
        }
        return false;
    }

    /**
     * 刷新trackId
     *
     * @param ftFaceList 传入的人脸列表
     */
    private void refreshTrackId(List<FaceInfo> ftFaceList) {
        currentTrackIdList.clear();
        for (FaceInfo faceInfo : ftFaceList) {
            currentTrackIdList.add(faceInfo.getFaceId() + trackedFaceCount);
        }
        if (!ftFaceList.isEmpty()) {
            currentMaxFaceId = ftFaceList.get(ftFaceList.size() - 1).getFaceId();
        }
    }

    /**
     * 获取当前的最大trackID,可用于退出时保存
     *
     * @return 当前trackId
     */
    public int getTrackedFaceCount() {
        // 引擎的人脸下标从0开始，因此需要+1
        return trackedFaceCount + currentMaxFaceId + 1;
    }

    /**
     * 新增搜索成功的人脸
     *
     * @param trackId 指定的trackId
     * @param name    trackId对应的人脸
     */
    public void setName(int trackId, String name) {
        RecognizeInfo recognizeInfo = recognizeInfoMap.get(trackId);
        if (recognizeInfo != null) {
            Gson gson = new Gson(); // 创建 Gson 实例
            recognizeInfo.setName(name);
        }
    }

    /**
     * 设置转换方式，用于IR活体检测
     *
     * @param transformer 转换方式
     */
    public void setDualCameraFaceInfoTransformer(IDualCameraFaceInfoTransformer transformer) {
        this.dualCameraFaceInfoTransformer = transformer;
    }

    public String getName(int trackId) {
        RecognizeInfo recognizeInfo = recognizeInfoMap.get(trackId);
        return recognizeInfo == null ? null : recognizeInfo.getName();
    }

    /**
     * 设置可识别区域（相对于View）
     *
     * @param recognizeArea 可识别区域
     */
    public void setRecognizeArea(Rect recognizeArea) {
        if (recognizeArea != null) {
            this.recognizeArea.set(recognizeArea);
        }
    }

    @IntDef(value = { RequestFeatureStatus.FAILED, RequestFeatureStatus.SEARCHING, RequestFeatureStatus.SUCCEED,
            RequestFeatureStatus.TO_RETRY })
    @Retention(RetentionPolicy.SOURCE)
    @interface RequestFaceFeatureStatus {
    }

    @IntDef(value = { LivenessInfo.ALIVE, LivenessInfo.NOT_ALIVE, LivenessInfo.UNKNOWN,
            LivenessInfo.FACE_NUM_MORE_THAN_ONE, LivenessInfo.FACE_TOO_SMALL, LivenessInfo.FACE_ANGLE_TOO_LARGE,
            LivenessInfo.FACE_BEYOND_BOUNDARY, RequestLivenessStatus.ANALYZING })
    @Retention(RetentionPolicy.SOURCE)
    @interface RequestFaceLivenessStatus {
    }

    /**
     * 修改人脸识别的状态
     *
     * @param trackId   根据VIDEO模式人脸检测获取的人脸的唯一标识
     * @param newStatus 新的识别状态，详见{@link RequestFeatureStatus}中的定义
     */
    public void changeRecognizeStatus(int trackId, @RequestFaceFeatureStatus int newStatus) {
        getRecognizeInfo(recognizeInfoMap, trackId).setRecognizeStatus(newStatus);
    }

    /**
     * 修改活体活体值或活体检测状态
     *
     * @param trackId     根据VIDEO模式人脸检测获取的人脸的唯一标识
     * @param newLiveness 新的活体值或活体检测状态
     */
    public void changeLiveness(int trackId, @RequestFaceLivenessStatus int newLiveness) {
        getRecognizeInfo(recognizeInfoMap, trackId).setLiveness(newLiveness);
    }

    /**
     * 获取活体值或活体检测状态
     *
     * @param trackId 根据VIDEO模式人脸检测获取的人脸的唯一标识
     * @return 活体值或活体检测状态
     */
    public Integer getLiveness(int trackId) {
        return getRecognizeInfo(recognizeInfoMap, trackId).getLiveness();
    }

    /**
     * 获取人脸识别状态
     *
     * @param trackId 根据VIDEO模式人脸检测获取的人脸的唯一标识
     * @return 人脸识别状态
     */
    public Integer getRecognizeStatus(int trackId) {
        return getRecognizeInfo(recognizeInfoMap, trackId).getRecognizeStatus();
    }

    /**
     * 保留ftFaceList中最大的人脸
     *
     * @param ftFaceList 人脸追踪时，一帧数据的人脸信息
     */
    private static void keepMaxFace(List<FaceInfo> ftFaceList) {
        if (ftFaceList == null || ftFaceList.size() <= 1) {
            return;
        }
        FaceInfo maxFaceInfo = ftFaceList.get(0);
        for (FaceInfo faceInfo : ftFaceList) {
            if (faceInfo.getRect().width() > maxFaceInfo.getRect().width()) {
                maxFaceInfo = faceInfo;
            }
        }
        ftFaceList.clear();
        ftFaceList.add(maxFaceInfo);
    }

    public static final class Builder {
        private FaceEngine ftEngine;
        private FaceEngine maskEngine;
        private FaceEngine frEngine;
        private FaceEngine flEngine;
        private Camera.Size previewSize;
        private boolean onlyDetectLiveness;
        private RecognizeConfiguration recognizeConfiguration;
        private RecognizeCallback recognizeCallback;
        private IDualCameraFaceInfoTransformer dualCameraFaceInfoTransformer;
        private int frQueueSize;
        private int flQueueSize;
        private int trackedFaceCount;

        public Builder() {
        }

        public Builder recognizeConfiguration(RecognizeConfiguration val) {
            recognizeConfiguration = val;
            return this;
        }

        public Builder dualCameraFaceInfoTransformer(IDualCameraFaceInfoTransformer val) {
            dualCameraFaceInfoTransformer = val;
            return this;
        }

        public Builder recognizeCallback(RecognizeCallback val) {
            recognizeCallback = val;
            return this;
        }

        public Builder ftEngine(FaceEngine val) {
            ftEngine = val;
            return this;
        }

        public Builder maskEngine(FaceEngine val) {
            maskEngine = val;
            return this;
        }

        public Builder frEngine(FaceEngine val) {
            frEngine = val;
            return this;
        }

        public Builder flEngine(FaceEngine val) {
            flEngine = val;
            return this;
        }

        public Builder previewSize(Camera.Size val) {
            previewSize = val;
            return this;
        }

        public Builder frQueueSize(int val) {
            frQueueSize = val;
            return this;
        }

        public Builder flQueueSize(int val) {
            flQueueSize = val;
            return this;
        }

        public Builder trackedFaceCount(int val) {
            trackedFaceCount = val;
            return this;
        }

        public Builder onlyDetectLiveness(boolean val) {
            onlyDetectLiveness = val;
            return this;
        }

        public FaceHelper build() {
            return new FaceHelper(this);
        }
    }
}
