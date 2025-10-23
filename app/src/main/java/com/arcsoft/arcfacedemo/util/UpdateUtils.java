package com.arcsoft.arcfacedemo.util;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.entity.Base;
import com.arcsoft.arcfacedemo.entity.Version;
import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;
import com.lzy.okgo.request.base.Request;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateEntity;
import com.xuexiang.xupdate.listener.IUpdateParseCallback;
import com.xuexiang.xupdate.listener.OnInstallListener;
import com.xuexiang.xupdate.listener.OnUpdateFailureListener;
import com.xuexiang.xupdate.listener.impl.DefaultInstallListener;
import com.xuexiang.xupdate.proxy.IUpdateHttpService;
import com.xuexiang.xupdate.proxy.IUpdateParser;
import com.xuexiang.xupdate.utils.ColorUtils;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class UpdateUtils {
    public static final String mJsonAddr = UrlConstants.URL_GET_APP_LAST_VERSION;

    public static void update(Context context, OnUpdateFailureListener listener) {
        ALog.d("update");
        update(context, listener, new DefaultInstallListener());
    }

    public static void update(Context context, OnUpdateFailureListener listener, OnInstallListener onInstallListener) {
        ALog.d("update");
        XUpdate.get().debug(true).isWifiOnly(false).isGet(true).setOnUpdateFailureListener(listener)
                .supportSilentInstall(false).setIUpdateHttpService(new OKHttpUpdateHttpService())
                .setOnInstallListener(onInstallListener).init(ArcFaceApplication.getApplication());

        XUpdate.newBuild(context).updateUrl(mJsonAddr).param("type", 3)
//                .param("versionCode", AppUtils.getAppVersionCode())
                .promptLayout(R.layout.xupdate_dialog_update_port)
                .promptThemeColor(ContextCompat.getColor(context, R.color.light_red))
                .promptFocusColor(ColorUtils.colorDeep(ContextCompat.getColor(context, R.color.light_red)))
                .promptButtonTextColor(Color.WHITE).promptTopResId(R.drawable.update_top_bg).promptWidthRatio(0.8F)
                .updateParser(new CustomUpdateParser()).update();
    }

    public static class CustomUpdateParser implements IUpdateParser {
        @Override
        public UpdateEntity parseJson(String json) throws Exception {
            return getParseResult(json);
        }

        private UpdateEntity getParseResult(String json) {
            Base<Version> base = GsonUtils.fromJson(json, new TypeToken<Base<Version>>() {
            }.getType());
            if (base.getCode() != 200) {
                ALog.e("检查更新失败");
                return new UpdateEntity().setHasUpdate(false);
            }

            if (ObjectUtils.isEmpty(base.getData())) {
                ALog.e("无可用更新");
                return new UpdateEntity().setHasUpdate(false);
            }
            final Version result = base.getData();
            if (result != null && result.getVersion().compareTo(AppUtils.getAppVersionName()) > 0) {
                UpdateEntity entity = new UpdateEntity().setHasUpdate(true)
                        .setIsIgnorable(false).setForce(result.getIsForceUpdate() == 1)
                        .setIsSilent(false).setIsAutoInstall(false)
                        .setShowNotification(false)
//                        .setVersionCode(result.getVersion())
                        .setVersionName(result.getVersion())
                        .setUpdateContent(result.getRemark()).setDownloadUrl(result.getUrl());
//                        .setMd5(result.getMd5())
//                        .setSize(result.getSize() / 1024);
                ALog.e(entity.toString());
                return entity;
            }
            return null;
        }

        @Override
        public void parseJson(String json, @NonNull IUpdateParseCallback callback) throws Exception {
            // 当isAsyncParser为 true时调用该方法, 所以当isAsyncParser为false可以不实现
            callback.onParseResult(getParseResult(json));
        }

        @Override
        public boolean isAsyncParser() {
            return false;
        }
    }

    /**
     * 使用okhttp
     *
     * @author xuexiang
     * @since 2018/7/10 下午4:04
     */
    public static class OKHttpUpdateHttpService implements IUpdateHttpService {

        private boolean mIsPostJson;

        public OKHttpUpdateHttpService() {
            this(false);
        }

        public OKHttpUpdateHttpService(boolean isPostJson) {
            mIsPostJson = isPostJson;
        }

        @Override
        public void asyncGet(@NonNull String url, @NonNull Map<String, Object> params,
                             final @NonNull Callback callBack) {
            GetRequest<String> getRequest = OkGo.<String>get(url).params(transform(params));
            getRequest.params("timestamp", String.valueOf(System.currentTimeMillis()));
            getRequest.headers("tenant-id", "1");
            // 检查是否有 accessToken，如果有则添加 Authorization 头
            if (ApiUtils.accessToken != null) {
                getRequest.headers("Authorization", "Bearer " + ApiUtils.accessToken);
            }
            getRequest.execute(new StringCallback() {
                @Override
                public void onError(Response<String> response) {
                    callBack.onError(response.getException());
                }

                @Override
                public void onSuccess(Response<String> response) {
                    callBack.onSuccess(response.body());
                }
            });
        }

        @Override
        public void asyncPost(@NonNull String url, @NonNull Map<String, Object> params,
                              final @NonNull Callback callBack) {
            // 这里默认post的是Form格式，使用json格式的请修改 post -> postString
            PostRequest<String> postRequest = OkGo.<String>post(url);
            postRequest.params("timestamp", String.valueOf(System.currentTimeMillis()));
            postRequest.headers("tenant-id", "1");
            // 检查是否有 accessToken，如果有则添加 Authorization 头
            if (ApiUtils.accessToken != null) {
                postRequest.headers("Authorization", "Bearer " + ApiUtils.accessToken);
            }
            if (mIsPostJson) {
                postRequest.upJson(GsonUtils.toJson(params));
            } else {
                postRequest.params(transform(params));
            }

            postRequest.execute(new StringCallback() {

                @Override
                public void onError(Response<String> response) {
                    callBack.onError(response.getException());
                }

                @Override
                public void onSuccess(Response<String> response) {
                    callBack.onSuccess(response.body());
                }
            });
        }

        @Override
        public void download(@NonNull String url, @NonNull String path, @NonNull String fileName,
                             final @NonNull DownloadCallback callback) {
            GetRequest<File> getRequest = OkGo.<File>get(url).tag(url);

            getRequest.headers("tenant-id", "1");
            // 检查是否有 accessToken，如果有则添加 Authorization 头
            if (ApiUtils.accessToken != null) {
                getRequest.headers("Authorization", "Bearer " + ApiUtils.accessToken);
            }
            getRequest.execute(new FileCallback(path, fileName) {
                @Override
                public void onError(Response<File> response) {
                    callback.onError(response.getException());
                }

                @Override
                public void onSuccess(Response<File> response) {
                    callback.onSuccess(response.body());
                }

                @Override
                public void downloadProgress(Progress progress) {
                    callback.onProgress(progress.fraction, progress.totalSize);
                }

                @Override
                public void onStart(Request<File, ? extends Request> request) {
                    callback.onStart();
                }
            });
        }

        @Override
        public void cancelDownload(@NonNull String url) {
            OkGo.getInstance().cancelTag(url);
        }

        private Map<String, String> transform(Map<String, Object> params) {
            Map<String, String> map = new TreeMap<>();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                map.put(entry.getKey(), entry.getValue().toString());
            }
            return map;
        }

    }
}
