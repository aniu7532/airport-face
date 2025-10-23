package com.arcsoft.arcfacedemo.widget.dialog;

import java.io.File;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.entity.Version;
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
import androidx.core.content.FileProvider;

public class UpdatePopDialog extends CenterPopupView {
    NumberProgressBar npb_progress;
    Button btn_update;
    Button btn_cancle;
    TextView tv_update_info;
    Version version;
    DownloadCallback callback;
    int count = 10;
    WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                if (count == 0) {
                    btn_update.setText("立即更新(" + count + "秒)");
                    handler.removeCallbacksAndMessages(null);
                    download();
                    return true;
                }
                btn_update.setText("立即更新(" + count + "秒)");
                count--;
                handler.removeMessages(1);
                handler.sendEmptyMessageDelayed(1, 1000L);
            }
            return false;
        }
    });

    public UpdatePopDialog(@NonNull Context context) {
        super(context);
    }

    public UpdatePopDialog(@NonNull Context context, Version version, DownloadCallback callback) {
        super(context);
        this.version = version;
        this.callback = callback;

    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.xupdate_dialog_update_port2;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        npb_progress = findViewById(R.id.npb_progress);
        btn_update = findViewById(R.id.btn_update);
        btn_cancle = findViewById(R.id.btn_cancle);
        tv_update_info = findViewById(R.id.tv_update_info);
        if (ObjectUtils.isNotEmpty(version.getRemark())) {
            tv_update_info.setText(version.getRemark());
        }

        count = 10;
        handler.removeMessages(1);
        handler.sendEmptyMessageDelayed(1, 100L);
        btn_cancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        btn_update.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.removeCallbacksAndMessages(null);
                download();
            }
        });
    }

    @Override
    public void dismiss() {
        handler.removeCallbacksAndMessages(null);
        super.dismiss();

    }

    // 自适应， 最大高度为Window的0.85
    @Override
    protected int getMaxHeight() {
        return 0;
    }

    @Override
    protected int getMaxWidth() {
        return 0;
    }

    public void download() {
        try {
            String apkName = UpdateUtils.getApkNameByDownloadUrl(version.getUrl());
            File apkCacheDir = UpdateUtils.getDefaultDiskCacheDir();
            if (!FileUtils.isFileExists(apkCacheDir)) {
                apkCacheDir.mkdirs();
            }
            String target = apkCacheDir + File.separator + version.getVersion();
            download(version.getUrl(), target, apkName, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void download(@NonNull String url, @NonNull String path, @NonNull String fileName,
            final @NonNull DownloadCallback callback) {
        // ThreadUtils.executeByCached(new SmallTask() {
        // @Override
        // public String doInBackground() throws Throwable {
        // ALog.e("url:" + url);
        // ALog.e("path:" + path);
        // ALog.e("fileName:" + fileName);
        // // 创建 UnsafeOkHttpClient 实例
        // ImageDownloader.UnsafeOkHttpClient unsafeClient = ImageDownloader.unsafeOkHttpClient();
        //
        // OkHttpClient client = new OkHttpClient.Builder()
        // .sslSocketFactory(unsafeClient.getSocketFactory(), unsafeClient.getTrustManager()).build();
        //
        //
        // okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        //
        // try {
        // okhttp3.Response response = client.newCall(request).execute();
        // if (!response.isSuccessful()) {
        // throw new IOException("Failed to connect to the server for URL: " + path);
        // }
        //
        // InputStream inputStream = response.body().byteStream();
        // // File directory = new File(Constants.DEFAULT_REGISTER_FACES_DIR);
        // File directory = new File(path);// 应用的私有目录
        // if (!directory.exists()) {
        // directory.mkdirs();
        // }
        // File file = new File(directory, fileName);
        //
        // try {
        // FileOutputStream fileOutputStream = new FileOutputStream(file);
        // byte[] buffer = new byte[4096];
        // int bytesRead;
        // while ((bytesRead = inputStream.read(buffer)) != -1) {
        // fileOutputStream.write(buffer, 0, bytesRead);
        // }
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // ALog.e("Image downloaded successfully: " + file.getAbsolutePath());
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // return null;
        // }
        // });
        OkGo.<File> get(url).tag(url).execute(new FileCallback(path, fileName) {
            @Override
            public void onError(Response<File> response) {
                response.getException().printStackTrace();
                ALog.e(response.getException().getMessage());
                callback.onError(response.getException());
                ToastUtils.showLong("更新失敗");
                npb_progress.setVisibility(View.GONE);
                npb_progress.setProgress(0);
            }

            @Override
            public void onSuccess(Response<File> response) {
                ALog.e(response.code());
                if (response.code() == 200) {
                    callback.onSuccess(response.body());
                    // AppUtils.installApp(response.body());

                    int screenSize = ScreenUtils.getScreenWidth();
                    int typeDevice = screenSize > 800 ? 1 : 2;
                    ALog.d("获取屏幕尺寸宽度:" + screenSize);

                    if (typeDevice == 1) {
                        Uri uri = Uri.fromFile(response.body());
                        if (uri != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                String authority = getContext().getPackageName();
                                Uri contentUri = FileProvider.getUriForFile(getContext(), authority + ".fileprovider",
                                        response.body());
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                            } else {
                                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                            }
                            getContext().startActivity(intent);
                        }
                    } else {
                        // 安装 test.apk
                        MyManager manager = MyManager.getInstance(getContext());
                        boolean success = manager.silentInstallApk(response.body().getAbsolutePath(), true);
                        // 静默安装 YiShentTest.apk,安装成功后打开 apk
                    }

                } else {
                    ToastUtils.showLong("下载失败");
                    npb_progress.setVisibility(View.GONE);
                    npb_progress.setProgress(0);
                }
            }

            @Override
            public void downloadProgress(Progress progress) {
                callback.onProgress(progress.fraction, progress.totalSize);
                npb_progress.setProgress(Math.round(progress.fraction * 100));
                npb_progress.setMax(100);
            }

            @Override
            public void onStart(Request<File, ? extends Request> request) {
                callback.onStart();
                npb_progress.setVisibility(View.VISIBLE);
                npb_progress.setProgress(0);
            }
        });
    }

    /**
     * 下载回调
     */
    public interface DownloadCallback {
        /**
         * 下载之前
         */
        void onStart();

        /**
         * 更新进度
         *
         * @param progress 进度0.00 - 0.50  - 1.00
         * @param total    文件总大小 单位字节
         */
        void onProgress(float progress, long total);

        /**
         * 结果回调
         *
         * @param file 下载好的文件
         */
        void onSuccess(File file);

        /**
         * 错误回调
         *
         * @param throwable 错误提示
         */
        void onError(Throwable throwable);

    }
}
