package com.arcsoft.arcfacedemo.util;

import android.app.Activity;
import android.view.View;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.db.dao.LongTermPassDao;
import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.facedb.FaceDatabase;
import com.arcsoft.arcfacedemo.facedb.dao.FaceDao;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.ui.activity.BaseActivity;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.arcsoft.face.FaceEngine;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人脸清理：侧边栏仅清除同 user 重复人脸；注册时按 userId 清理旧证人脸。
 */
public class DuplicateFaceCleanupUtils {

    private static final String TAG = "DuplicateFaceCleanupUtils";

    private static volatile DuplicateFaceCleanupUtils instance;

    public boolean doing = false;

    private DuplicateFaceCleanupUtils() {
    }

    public static DuplicateFaceCleanupUtils getInstance() {
        if (instance == null) {
            synchronized (FaceServer.class) {
                if (instance == null) {
                    instance = new DuplicateFaceCleanupUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 侧边栏入口：扫描并清除同 user 重复人脸
     */
    public void start() {
        if (doing) {
            ToastUtils.showShort("正在处理中...");
            return;
        }
        if (LongPassCardsRemedialMeasuresUtils.getInstance().doing) {
            ToastUtils.showShort("数据完整性检查进行中，请稍后再试");
            return;
        }
        if (LongPassCardsReInitUtils.getInstance().isDoing()) {
            ToastUtils.showShort("在线数据完整性检查进行中，请稍后再试");
            return;
        }

        Activity activity = ActivityUtils.getTopActivity();
        if (activity == null || !(activity instanceof BaseActivity)) {
            ToastUtils.showLong("无法获取Activity，清除重复人脸功能启动失败");
            return;
        }

        View rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) {
            ToastUtils.showLong("无法获取Activity，清除重复人脸功能启动失败");
            return;
        }

        final Snackbar[] snackbarRef = new Snackbar[1];
        snackbarRef[0] = Snackbar.make(rootView, "开始清除重复人脸...", Snackbar.LENGTH_INDEFINITE);
        snackbarRef[0].setAction("关闭", v -> {
            if (snackbarRef[0] != null) {
                snackbarRef[0].dismiss();
            }
        });
        snackbarRef[0].show();

        final Activity finalActivity = activity;
        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {
                if (doing) {
                    return null;
                }
                doing = true;
                try {
                    FaceDao faceDao = FaceDatabase.getInstance(ArcFaceApplication.getApplication()).faceDao();
                    int beforeCount = faceDao.getFaceCount();
                    CleanupProgressCallback progressCallback = (current, total, removed) -> {
                        if (finalActivity == null || finalActivity.isFinishing() || snackbarRef[0] == null) {
                            return;
                        }
                        finalActivity.runOnUiThread(() -> {
                            if (snackbarRef[0] != null && snackbarRef[0].isShown()) {
                                snackbarRef[0].setText(String.format(
                                        "共 %d 条，检查第 %d 个，已移除 %d 个", total, current, removed));
                            }
                        });
                    };
                    int removed = clearDuplicateFacesSync(progressCallback);
                    int afterCount = faceDao.getFaceCount();
                    ALog.i(TAG, String.format("清除完成: 清理前 %d 条, 移除 %d 个 pass, 剩余 %d 条",
                            beforeCount, removed, afterCount));

                    if (finalActivity.isFinishing()) {
                        return null;
                    }
                    finalActivity.runOnUiThread(() -> {
                        if (snackbarRef[0] != null && snackbarRef[0].isShown()) {
                            snackbarRef[0].dismiss();
                        }
                        if (removed > 0) {
                            ToastUtils.showLong(String.format("已清除 %d 条重复人脸，当前剩余 %d 条",
                                    removed, afterCount));
                        } else {
                            ToastUtils.showLong("未发现需要清除的重复人脸");
                        }
                    });
                } finally {
                    doing = false;
                }
                return null;
            }
        });
    }

    /**
     * 注册人脸前：移除同 user 其它 pass id 的人脸，并清除当前 pass id 旧特征
     */
    public void prepareRegisterFace(String passId) {
        removeOtherFacesForPassId(passId);
        removeFaceByPassId(passId);
    }

    /**
     * 从人脸引擎和本地库中移除指定通行证 id 对应的人脸
     */
    public void removeFaceByPassId(String passId) {
        if (passId == null || passId.isEmpty()) {
            return;
        }
        if (FaceServer.getInstance().getFaceEngine() == null
                && FaceServer.getInstance().getFrEngine() == null) {
            ALog.e(TAG + " removeFaceByPassId: FaceEngine 未初始化, passId=" + passId);
            return;
        }
        ArcFaceApplication app = ArcFaceApplication.getApplication();
        List<FaceEntity> faceEntityList = FaceDatabase.getInstance(app).faceDao().getAllFaces();
        if (faceEntityList == null || faceEntityList.isEmpty()) {
            return;
        }
        List<FaceEntity> toRemove = new ArrayList<>();
        for (FaceEntity faceEntity : faceEntityList) {
            if (passId.equals(faceEntity.getUserName())) {
                toRemove.add(faceEntity);
            }
        }
        for (FaceEntity faceEntity : toRemove) {
            ALog.i(TAG + " removeFaceByPassId: " + passId + ", " + faceEntity.toString2());
            removeFaceFeatureFromEngines(faceEntity.getFaceId());
            FaceServer.getInstance().removeOneFace(faceEntity);
            deleteFaceImageFile(faceEntity);
            FaceDatabase.getInstance(app).faceDao().deleteFace(faceEntity);
        }
    }

    /**
     * 注册新证前，移除同一用户其它 pass id 的人脸（含已注销旧证）
     */
    public void removeOtherFacesForPassId(String keepPassId) {
        if (keepPassId == null || keepPassId.isEmpty()) {
            return;
        }
        LongTermPassDao dao = ArcFaceApplication.getApplication().getDb().longTermPassDao();
        LongTermPass keepPass = dao.getById(keepPassId);
        if (keepPass == null || keepPass.userId == null || keepPass.userId.isEmpty()) {
            return;
        }
        List<LongTermPass> userPasses = dao.getByUserId(keepPass.userId);
        if (userPasses == null) {
            return;
        }
        for (LongTermPass pass : userPasses) {
            if (!keepPassId.equals(pass.id)) {
                ALog.i(TAG + " removeOtherFacesForPassId: 用户 " + keepPass.userId
                        + " 移除旧证人脸 " + pass.id + "，保留 " + keepPassId);
                removeFaceByPassId(pass.id);
            }
        }
    }

    private interface CleanupProgressCallback {
        void onProgress(int current, int total, int removed);
    }

    /**
     * 全盘扫描：仅清除同 user 重复人脸（仅侧边栏入口调用）。
     * 同一 user 只有一条人脸时，不论通行证 status 是否为 1，均保留。
     */
    private int clearDuplicateFacesSync(CleanupProgressCallback callback) {
        ArcFaceApplication app = ArcFaceApplication.getApplication();
        LongTermPassDao dao = app.getDb().longTermPassDao();
        FaceDao faceDao = FaceDatabase.getInstance(app).faceDao();
        List<FaceEntity> allFaces = faceDao.getAllFaces();
        if (allFaces == null || allFaces.isEmpty()) {
            return 0;
        }

        Map<String, List<String>> userPassIds = groupFacePassIdsByUserId(allFaces, dao);
        List<String> passIdsToRemove = collectDuplicatePassIdsToRemove(userPassIds, dao);

        List<FaceEntity> snapshot = new ArrayList<>(allFaces);
        int total = snapshot.size();
        int removed = 0;
        for (int i = 0; i < snapshot.size(); i++) {
            if (callback != null) {
                callback.onProgress(i + 1, total, removed);
            }
            String passId = snapshot.get(i).getUserName();
            if (passId == null || passId.isEmpty() || !passIdsToRemove.contains(passId)) {
                continue;
            }
            ALog.i(TAG + " clearDuplicateFaces: 移除重复证人脸 " + passId);
            removeFaceByPassId(passId);
            removed++;
        }
        return removed;
    }

    private Map<String, List<String>> groupFacePassIdsByUserId(List<FaceEntity> allFaces, LongTermPassDao dao) {
        Map<String, List<String>> userPassIds = new HashMap<>();
        for (FaceEntity face : allFaces) {
            String passId = face.getUserName();
            if (passId == null || passId.isEmpty()) {
                continue;
            }
            LongTermPass pass = dao.getById(passId);
            if (pass == null || pass.userId == null || pass.userId.isEmpty()) {
                continue;
            }
            List<String> passIds = userPassIds.get(pass.userId);
            if (passIds == null) {
                passIds = new ArrayList<>();
                userPassIds.put(pass.userId, passIds);
            }
            if (!passIds.contains(passId)) {
                passIds.add(passId);
            }
        }
        return userPassIds;
    }

    private List<String> collectDuplicatePassIdsToRemove(Map<String, List<String>> userPassIds, LongTermPassDao dao) {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : userPassIds.entrySet()) {
            List<String> passIds = entry.getValue();
            if (passIds.size() <= 1) {
                continue;
            }
            String keepPassId = resolveKeepPassId(entry.getKey(), passIds, dao);
            ALog.i(TAG + " clearDuplicateFaces: userId=" + entry.getKey() + " 存在 " + passIds.size()
                    + " 条人脸, 保留 " + keepPassId);
            for (String passId : passIds) {
                if (!keepPassId.equals(passId)) {
                    toRemove.add(passId);
                }
            }
        }
        return toRemove;
    }

    /**
     * 优先保留 status=1 的有效证；若无则在人脸关联的证里选最优一条。
     */
    private String resolveKeepPassId(String userId, List<String> facePassIds, LongTermPassDao dao) {
        List<LongTermPass> activePasses = dao.getActiveByUserId(userId);
        if (activePasses != null) {
            for (LongTermPass activePass : activePasses) {
                if (facePassIds.contains(activePass.id)) {
                    return activePass.id;
                }
            }
        }
        LongTermPass best = null;
        for (String passId : facePassIds) {
            LongTermPass pass = dao.getById(passId);
            if (pass == null) {
                continue;
            }
            if (best == null || isPreferredPass(pass, best)) {
                best = pass;
            }
        }
        if (best != null) {
            return best.id;
        }
        return facePassIds.get(0);
    }

    private boolean isPreferredPass(LongTermPass candidate, LongTermPass current) {
        if (candidate.status == 1 && current.status != 1) {
            return true;
        }
        if (candidate.status != 1 && current.status == 1) {
            return false;
        }
        if (candidate.status == 2 && current.status != 2) {
            return false;
        }
        if (candidate.status != 2 && current.status == 2) {
            return true;
        }
        if (candidate.type != current.type) {
            return candidate.type > current.type;
        }
        return compareUpdateTime(candidate.updateTime, current.updateTime) > 0;
    }

    private int compareUpdateTime(String left, String right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    private void removeFaceFeatureFromEngines(long faceId) {
        FaceEngine frEngine = FaceServer.getInstance().getFrEngine();
        if (frEngine != null) {
            frEngine.removeFaceFeature((int) faceId);
        }
        FaceEngine faceEngine = FaceServer.getInstance().getFaceEngine();
        if (faceEngine != null) {
            faceEngine.removeFaceFeature((int) faceId);
        }
    }

    private void deleteFaceImageFile(FaceEntity faceEntity) {
        if (faceEntity == null || faceEntity.getImagePath() == null) {
            return;
        }
        File imageFile = new File(faceEntity.getImagePath());
        if (imageFile.exists() && !imageFile.delete()) {
            ALog.w(TAG + " deleteFaceImageFile failed: " + faceEntity.getImagePath());
        }
    }
}
