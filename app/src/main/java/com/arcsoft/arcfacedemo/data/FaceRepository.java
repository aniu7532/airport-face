package com.arcsoft.arcfacedemo.data;

import java.io.File;
import java.util.List;

import com.arcsoft.arcfacedemo.facedb.dao.FaceDao;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.faceserver.RegisterFailedException;

import android.content.Context;
import android.util.Log;

/**
 * 人脸库数据仓库，封装分页加载、注册、删除等人脸管理操作。
 */
public class FaceRepository {
    private FaceDao faceDao;
    private int currentIndex = 0;
    private int pageSize;
    private static final String TAG = "FaceRepository";
    private FaceServer faceServer;

    /** @param pageSize 分页大小 @param faceDao 人脸数据访问 @param faceServer 人脸引擎服务 */
    public FaceRepository(int pageSize, FaceDao faceDao, FaceServer faceServer) {
        this.pageSize = pageSize;
        this.faceDao = faceDao;
        this.faceServer = faceServer;
    }

    /** 分页加载下一批人脸记录 */
    public List<FaceEntity> loadMore() {
        List<FaceEntity> faceEntities = faceDao.getFaces(currentIndex, pageSize);
        currentIndex += faceEntities.size();
        return faceEntities;
    }

    /** 重置分页索引并重新加载第一页 */
    public List<FaceEntity> reload() {
        currentIndex = 0;
        return loadMore();
    }

    /** 清空全部已注册人脸（含本地图片文件） */
    public int clearAll() {
        // 由于涉及到文件删除操作，所以使用faceServer
        int faceCount = faceServer.clearAllFaces();
        currentIndex = 0;
        return faceCount;
    }

    /** 删除单条人脸记录及其本地图片文件 */
    public int delete(FaceEntity faceEntity) {
        int index = faceDao.deleteFace(faceEntity);
        boolean delete = new File(faceEntity.getImagePath()).delete();
        if (!delete) {
            Log.w(TAG, "deleteFace: failed to delete headImageFile '" + faceEntity.getImagePath() + "'");
        }
        return index;
    }

    /** 从 JPEG 字节数据注册人脸，默认识别模式关闭 */
    public FaceEntity registerJpeg(Context context, byte[] bytes, String name) throws RegisterFailedException {
        return faceServer.registerJpeg(context, bytes, name, false);
    }

    /** 从 JPEG 字节数据注册人脸，可指定是否立即进入识别模式 */
    public FaceEntity registerJpeg(Context context, byte[] bytes, String name, boolean recognize)
            throws RegisterFailedException {
        return faceServer.registerJpeg(context, bytes, name, recognize);
    }

    /** 从 BGR24 原始图像数据注册人脸，默认识别模式关闭 */
    public FaceEntity registerBgr24(Context context, byte[] bgr24Data, int width, int height, String name) {
        return faceServer.registerBgr24(context, bgr24Data, width, height, name, false);
    }

    /** 从 BGR24 原始图像数据注册人脸，可指定是否立即进入识别模式 */
    public FaceEntity registerBgr24(Context context, byte[] bgr24Data, int width, int height, String name,
            boolean recognize) {
        return faceServer.registerBgr24(context, bgr24Data, width, height, name, recognize);
    }

    /** 获取已注册人脸总数 */
    public int getTotalFaceCount() {
        return faceDao.getFaceCount();
    }
}
