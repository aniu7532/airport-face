package com.arcsoft.arcfacedemo.util;

import android.content.Context;

import com.arcsoft.arcfacedemo.entity.Base;
import com.arcsoft.arcfacedemo.entity.Version;
import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.arcsoft.arcfacedemo.widget.dialog.UpdatePopDialog;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.lxj.xpopup.XPopup;
import com.lzy.okgo.OkGo;
import com.arcsoft.arcfacedemo.data.http.JsonCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;

import java.io.File;

/**
 * 应用版本更新检查与弹窗调度，防止重复弹窗和并发下载。
 */
public final class AppUpdateHelper {

    public static final int MSG_CHECK_UPDATE = 7;

    private static final long CHECK_INTERVAL_MS = 60_000L;
    /** 下载并尝试安装后延长检查间隔，避免安装完成前重复弹窗 */
    private static final long CHECK_INTERVAL_AFTER_DOWNLOAD_MS = 600_000L;

    private AppUpdateHelper() {
    }

    public static void scheduleCheck(WeakHandler handler, long delayMs) {
        handler.removeMessages(MSG_CHECK_UPDATE);
        handler.sendEmptyMessageDelayed(MSG_CHECK_UPDATE, delayMs);
    }

    public static void scheduleCheck(WeakHandler handler) {
        scheduleCheck(handler, CHECK_INTERVAL_MS);
    }

    /**
     * 检查服务端是否有新版本；有更新且当前无弹窗/下载时弹出 {@link UpdatePopDialog}。
     */
    public static void checkForUpdate(Context context, WeakHandler handler) {
        if (UpdatePopDialog.isActive()) {
            ALog.d("更新弹窗或下载进行中，跳过版本检查");
            scheduleCheck(handler);
            return;
        }

        GetRequest<Base<Version>> getRequest =
                OkGo.<Base<Version>>get(UrlConstants.URL_GET_APP_LAST_VERSION).params("type", 3);
        getRequest.headers("tenant-id", UrlConstants.TENANT_ID);
        if (ApiUtils.accessToken != null) {
            getRequest.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }
        getRequest.execute(new JsonCallback<Base<Version>>() {
            @Override
            public void onError(Response<Base<Version>> response) {
                ALog.w("版本检查请求失败");
                scheduleCheck(handler);
            }

            @Override
            public void onSuccess(Response<Base<Version>> response) {
                if (ObjectUtils.isEmpty(response.body()) || ObjectUtils.isEmpty(response.body().getData())) {
                    ALog.d("无可用更新");
                    scheduleCheck(handler);
                    return;
                }
                Version version = response.body().getData();
                if (!hasNewVersion(version)) {
                    ALog.d("当前已是最新版本: local=" + AppUtils.getAppVersionName()
                            + ", remote=" + version.getVersion());
                    scheduleCheck(handler);
                    return;
                }
                if (UpdatePopDialog.isActive()) {
                    ALog.d("版本检查完成时已有更新流程进行中，跳过弹窗");
                    scheduleCheck(handler);
                    return;
                }
                ALog.d("发现新版本: " + version.getVersion()
                        + ", force=" + (version.getIsForceUpdate() == 1));
                boolean forceUpdate = version.getIsForceUpdate() == 1;
                new XPopup.Builder(context)
                        .dismissOnTouchOutside(false)
                        .dismissOnBackPressed(!forceUpdate)
                        .asCustom(new UpdatePopDialog(context, version, createDownloadCallback(handler),
                                () -> scheduleCheck(handler)))
                        .show();
            }
        });
    }

    static boolean hasNewVersion(Version remote) {
        if (remote == null || ObjectUtils.isEmpty(remote.getVersion())) {
            return false;
        }
        return remote.getVersion().compareTo(AppUtils.getAppVersionName()) > 0;
    }

    public static UpdatePopDialog.DownloadCallback createDownloadCallback(WeakHandler handler) {
        return new UpdatePopDialog.DownloadCallback() {
            @Override
            public void onStart() {
                ALog.d("update download onStart");
                handler.removeMessages(MSG_CHECK_UPDATE);
            }

            @Override
            public void onProgress(float progress, long total) {
                ALog.d("update download progress:" + progress + ", total:" + total);
            }

            @Override
            public void onSuccess(File file) {
                ALog.d("update download success:" + file.getAbsolutePath());
                scheduleCheck(handler, CHECK_INTERVAL_AFTER_DOWNLOAD_MS);
            }

            @Override
            public void onError(Throwable throwable) {
                ALog.e("update download error:" + (throwable != null ? throwable.getMessage() : "unknown"));
                // 弹窗仍在时由弹窗自行冷却重试，避免 60 秒轮询叠加下载
                if (!UpdatePopDialog.isActive()) {
                    scheduleCheck(handler);
                }
            }
        };
    }
}
