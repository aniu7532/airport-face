package com.arcsoft.arcfacedemo.widget.dialog;

import java.io.File;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.entity.Version;
import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.util.WeakHandler;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lxj.xpopup.core.CenterPopupView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.xuexiang.xupdate.utils.FileUtils;
import com.xuexiang.xupdate.utils.UpdateUtils;
import com.xuexiang.xupdate.widget.NumberProgressBar;
import com.ys.rkapi.MyManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

/**
 * 应用版本更新弹窗，展示更新说明、倒计时自动下载，并支持进度展示与安装。
 */
public class UpdatePopDialog extends CenterPopupView {

    private static final long DOWNLOAD_RETRY_COOLDOWN_MS = 30_000L;
    private static final int MSG_COUNTDOWN = 1;
    private static final int MSG_RETRY_COUNTDOWN = 2;

    private static volatile boolean sDialogShowing;
    private static volatile boolean sDownloading;

    NumberProgressBar npb_progress;
    Button btn_update;
    Button btn_cancle;
    TextView tv_update_info;
    Version version;
    DownloadCallback callback;
    @Nullable
    Runnable onCancelListener;
    @Nullable
    private String currentDownloadTag;
    private boolean allowDismiss;
    private int retrySeconds = 30;
    int count = 10;
    WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == MSG_COUNTDOWN) {
                if (count == 0) {
                    btn_update.setText("立即更新(" + count + "秒)");
                    handler.removeCallbacksAndMessages(null);
                    startDownload();
                    return true;
                }
                btn_update.setText("立即更新(" + count + "秒)");
                count--;
                handler.removeMessages(MSG_COUNTDOWN);
                handler.sendEmptyMessageDelayed(MSG_COUNTDOWN, 1000L);
            } else if (message.what == MSG_RETRY_COUNTDOWN) {
                if (retrySeconds <= 0) {
                    btn_update.setEnabled(true);
                    btn_update.setText("立即更新");
                    if (isForceUpdate()) {
                        startDownload();
                    }
                    return true;
                }
                btn_update.setText("重试下载(" + retrySeconds + "秒)");
                retrySeconds--;
                handler.removeMessages(MSG_RETRY_COUNTDOWN);
                handler.sendEmptyMessageDelayed(MSG_RETRY_COUNTDOWN, 1000L);
            }
            return false;
        }
    });

    public static boolean isActive() {
        return sDialogShowing || sDownloading;
    }

    public UpdatePopDialog(@NonNull Context context) {
        super(context);
    }

    public UpdatePopDialog(@NonNull Context context, Version version, DownloadCallback callback) {
        this(context, version, callback, null);
    }

    public UpdatePopDialog(@NonNull Context context, Version version, DownloadCallback callback,
            @Nullable Runnable onCancelListener) {
        super(context);
        this.version = version;
        this.callback = callback;
        this.onCancelListener = onCancelListener;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.xupdate_dialog_update_port2;
    }

    /** 初始化更新说明、倒计时与下载按钮。 */
    @Override
    protected void onCreate() {
        super.onCreate();
        sDialogShowing = true;
        npb_progress = findViewById(R.id.npb_progress);
        btn_update = findViewById(R.id.btn_update);
        btn_cancle = findViewById(R.id.btn_cancle);
        tv_update_info = findViewById(R.id.tv_update_info);
        if (ObjectUtils.isNotEmpty(version.getRemark())) {
            tv_update_info.setText(version.getRemark());
        }

        if (isForceUpdate()) {
            btn_cancle.setVisibility(View.GONE);
        }

        count = 10;
        handler.removeMessages(MSG_COUNTDOWN);
        handler.sendEmptyMessageDelayed(MSG_COUNTDOWN, 100L);
        btn_cancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onCancelListener != null) {
                    onCancelListener.run();
                }
                allowDismiss = true;
                dismiss();
            }
        });
        btn_update.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!btn_update.isEnabled()) {
                    return;
                }
                handler.removeCallbacksAndMessages(null);
                startDownload();
            }
        });
    }

    @Override
    public void dismiss() {
        if (isForceUpdate() && !allowDismiss) {
            return;
        }
        handler.removeCallbacksAndMessages(null);
        if (currentDownloadTag != null) {
            OkGo.getInstance().cancelTag(currentDownloadTag);
            currentDownloadTag = null;
        }
        super.dismiss();
    }

    @Override
    protected void onDismiss() {
        sDialogShowing = false;
        sDownloading = false;
        super.onDismiss();
    }

    @Override
    protected int getMaxHeight() {
        return 0;
    }

    @Override
    protected int getMaxWidth() {
        return 0;
    }

    private boolean isForceUpdate() {
        return version != null && version.getIsForceUpdate() == 1;
    }

    /** 根据版本信息开始下载 APK 到本地缓存目录。 */
    public void download() {
        startDownload();
    }

    private void startDownload() {
        if (sDownloading) {
            ALog.w("已有下载任务进行中，跳过重复下载");
            return;
        }
        if (handler.hasMessages(MSG_RETRY_COUNTDOWN)) {
            ALog.w("下载失败冷却中，跳过重复下载");
            return;
        }
        try {
            String apkName = UpdateUtils.getApkNameByDownloadUrl(version.getUrl());
            File apkCacheDir = UpdateUtils.getDefaultDiskCacheDir();
            if (!FileUtils.isFileExists(apkCacheDir)) {
                apkCacheDir.mkdirs();
            }
            String target = apkCacheDir + File.separator + version.getVersion();
            File cachedApk = new File(target, apkName);
            if (cachedApk.exists() && cachedApk.length() > 1024) {
                ALog.d("APK 已缓存，跳过下载直接安装: " + cachedApk.getAbsolutePath());
                onDownloadCompleted(cachedApk);
                return;
            }
            download(version.getUrl(), target, apkName, callback);
        } catch (Exception e) {
            ALog.e("启动下载失败: " + e.getMessage());
            sDownloading = false;
            e.printStackTrace();
            onDownloadFailed(e);
        }
    }

    public void download(@NonNull String url, @NonNull String path, @NonNull String fileName,
            final @NonNull DownloadCallback callback) {
        final String downloadUrl = resolveDownloadUrl(url);
        final String downloadTag = downloadUrl + "_" + System.currentTimeMillis();
        if (currentDownloadTag != null) {
            OkGo.getInstance().cancelTag(currentDownloadTag);
        }
        currentDownloadTag = downloadTag;
        sDownloading = true;
        ALog.d("APK 下载地址: " + downloadUrl);
        OkGo.<File>get(downloadUrl).tag(downloadTag)
                .headers("tenant-id", UrlConstants.TENANT_ID)
                .headers("Authorization", ApiUtils.accessToken != null
                        ? "Bearer " + ApiUtils.accessToken : "")
                .execute(new FileCallback(path, fileName) {
            @Override
            public void onError(Response<File> response) {
                if (!downloadTag.equals(currentDownloadTag)) {
                    ALog.d("忽略已取消下载任务的 onError 回调");
                    return;
                }
                sDownloading = false;
                Throwable error = response != null ? response.getException() : null;
                if (error != null) {
                    error.printStackTrace();
                    ALog.e("下载失败: " + error.getMessage());
                }
                callback.onError(error);
                onDownloadFailed(error);
            }

            @Override
            public void onSuccess(Response<File> response) {
                if (!downloadTag.equals(currentDownloadTag)) {
                    ALog.d("忽略已取消下载任务的 onSuccess 回调");
                    return;
                }
                if (response == null || response.body() == null || response.code() != 200) {
                    sDownloading = false;
                    IllegalStateException error = new IllegalStateException("下载响应异常");
                    callback.onError(error);
                    onDownloadFailed(error);
                    return;
                }
                onDownloadCompleted(response.body());
            }

            @Override
            public void downloadProgress(Progress progress) {
                if (!downloadTag.equals(currentDownloadTag)) {
                    return;
                }
                callback.onProgress(progress.fraction, progress.totalSize);
                npb_progress.setProgress(Math.round(progress.fraction * 100));
                npb_progress.setMax(100);
            }

            @Override
            public void onStart(Request<File, ? extends Request> request) {
                if (!downloadTag.equals(currentDownloadTag)) {
                    return;
                }
                callback.onStart();
                npb_progress.setVisibility(View.VISIBLE);
                npb_progress.setProgress(0);
            }
        });
    }

    /**
     * 与 {@link com.arcsoft.arcfacedemo.util.ImageDownloader} 一致，通过业务 API 文件流代理下载，
     * 避免直连 OBS 域名在零信任隧道未发布该域名时 DNS 解析失败。
     */
    private static String resolveDownloadUrl(String originalUrl) {
        if (originalUrl != null
                && (originalUrl.startsWith("http://") || originalUrl.startsWith("https://"))) {
            return UrlConstants.fileStreamUrl(originalUrl);
        }
        return originalUrl;
    }

    private void onDownloadFailed(@Nullable Throwable error) {
        ToastUtils.showLong("更新失敗");
        npb_progress.setVisibility(View.GONE);
        npb_progress.setProgress(0);
        scheduleRetryCooldown();
    }

    private void scheduleRetryCooldown() {
        retrySeconds = (int) (DOWNLOAD_RETRY_COOLDOWN_MS / 1000L);
        btn_update.setEnabled(false);
        handler.removeMessages(MSG_RETRY_COUNTDOWN);
        handler.sendEmptyMessageDelayed(MSG_RETRY_COUNTDOWN, 1000L);
    }

    private void onDownloadCompleted(File apkFile) {
        handler.removeCallbacksAndMessages(null);
        callback.onSuccess(apkFile);
        installApk(apkFile);
        sDownloading = false;
        allowDismiss = true;
        dismiss();
    }

    private void installApk(File apkFile) {
        int screenSize = ScreenUtils.getScreenWidth();
        boolean useIntentInstall = screenSize > 800;
        ALog.d("获取屏幕尺寸宽度:" + screenSize + ", 安装方式:" + (useIntentInstall ? "Intent" : "静默"));

        if (useIntentInstall) {
            installByIntent(apkFile);
            return;
        }

        MyManager manager = MyManager.getInstance(getContext());
        boolean success = manager.silentInstallApk(apkFile.getAbsolutePath(), true);
        ALog.d("silentInstallApk result:" + success + ", path:" + apkFile.getAbsolutePath());
        if (!success) {
            ALog.w("静默安装失败，降级为 Intent 安装");
            installByIntent(apkFile);
        }
    }

    private void installByIntent(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = getContext().getPackageName();
            Uri contentUri = FileProvider.getUriForFile(getContext(), authority + ".fileprovider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        getContext().startActivity(intent);
        ALog.d("已拉起系统安装界面: " + apkFile.getAbsolutePath());
    }

    /**
     * 下载回调
     */
    public interface DownloadCallback {
        void onStart();

        void onProgress(float progress, long total);

        void onSuccess(File file);

        void onError(Throwable throwable);
    }
}
