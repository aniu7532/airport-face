package com.arcsoft.arcfacedemo.network;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;

import okhttp3.OkHttpClient;

/**
 * 网络 API 请求工具类。
 * <p>
 * 职责：全局 Token 静态存储、统一请求头（{@code tenant-id}、{@code Authorization}）、
 * 基于 OkGo 的 GET/POST 异步封装。项目内仍有大量请求直接使用 OkGo，未全部经本类。
 * </p>
 */
public class ApiUtils {
    /** 信任所有证书的 OkHttpClient，当前未被 public 方法使用（遗留自旧版 OkHttp 实现） */
    private static final OkHttpClient client = getUnsafeOkHttpClient();

    /** 访问令牌，登录成功后由 {@link com.arcsoft.arcfacedemo.ui.activity.LoginActivity} 写入 */
    public static String accessToken;
    /** 刷新令牌，供 {@link com.arcsoft.arcfacedemo.service.TokenRefreshJobService} 刷新 */
    public static String refreshToken;
    /** 当前登录用户 ID，写入通行记录时作为 checkUserId，无 getter/setter */
    public static String userId;

    /** 设置访问令牌 */
    public static void setAccessToken(String token) {
        accessToken = token;
    }

    /** 设置刷新令牌 */
    public static void setRefreshToken(String token) {
        refreshToken = token;
    }

    /** 获取刷新令牌 */
    public static String getRefreshToken() {
        return refreshToken;
    }

    /** 获取访问令牌 */
    public static String getAccessToken() {
        return accessToken;
    }

    /**
     * API 异步请求结果回调。
     * <p>成功时返回原始 JSON 字符串，由调用方自行 Gson 解析。</p>
     */
    public interface ApiCallback {
        /** HTTP 2xx 且 body 非空 */
        void onSuccess(String response);

        /** 网络异常或 HTTP 非成功状态 */
        void onFailure(Throwable e);
    }

    /**
     * 发起 GET 请求。
     * <p>自动附加请求头 {@code tenant-id}、{@code Authorization: Bearer}（有 token 时）；
     * {@code params} 非空时会注入 {@code timestamp} 毫秒时间戳。</p>
     *
     * @param url      完整 URL
     * @param params   Query 参数，可为 null
     * @param callback 异步回调，在 OkGo 线程执行
     */
    public static void get(String url, Map<String, String> params, ApiCallback callback) {
        GetRequest<String> request = OkGo.<String> get(url).tag(url);
        if (params != null) {
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            for (Map.Entry<String, String> entry : params.entrySet()) {
                request.params(entry.getKey(), entry.getValue());
            }
        }
        request.headers("tenant-id", UrlConstants.TENANT_ID);
        if (accessToken != null) {
            request.headers("Authorization", "Bearer " + accessToken);
        }
        request.execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure(new IOException("Unexpected code " + response));
                }
            }

            @Override
            public void onError(Response<String> response) {
                callback.onFailure(response.getException());
            }
        });
    }

    /**
     * 发起通行证分页 GET 请求。
     * <p>与 {@link #get} 行为一致，专用于 {@link UrlConstants#URL_GetLongPass} 等分页拉证场景。</p>
     */
    public static void getPassCard(String url, Map<String, String> params, ApiCallback callback) {
        GetRequest<String> request = OkGo.<String> get(url).tag(url);
        if (params != null) {
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            for (Map.Entry<String, String> entry : params.entrySet()) {
                request.params(entry.getKey(), entry.getValue());
            }
        }
        request.headers("tenant-id", UrlConstants.TENANT_ID);
        if (accessToken != null) {
            request.headers("Authorization", "Bearer " + accessToken);
        }
        request.execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure(new IOException("Unexpected code " + response));
                }
            }

            @Override
            public void onError(Response<String> response) {
                callback.onFailure(response.getException());
            }

        });

    }

    /**
     * 发起 JSON 格式 POST 请求。
     *
     * @param url      完整 URL
     * @param json     请求体 JSON 字符串
     * @param callback 异步回调
     */
    public static void post(String url, String json, ApiCallback callback) {
        PostRequest<String> request = OkGo.<String> post(url).tag(url);
        request.headers("tenant-id", UrlConstants.TENANT_ID);
        if (accessToken != null) {
            request.headers("Authorization", "Bearer " + accessToken);
        }
        request.upJson(json).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure(new IOException("Unexpected code " + response));
                }
            }

            @Override
            public void onError(Response<String> response) {
                callback.onFailure(response.getException());
            }
        });

    }

    /**
     * 上传图片（未实现，空方法占位）。
     * <p>实际图片上传走 {@link com.arcsoft.arcfacedemo.util.ImageUploader}。</p>
     */
    public static void upload(String url, String imgStr, ApiCallback callback) {
    }

    /**
     * 创建信任所有 SSL 证书的 OkHttpClient。
     * <p>生产环境 HTTPS 信任策略以 {@link com.arcsoft.arcfacedemo.data.http.HttpInitUtils} 为准。</p>
     */
    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[] {};
                }
            } };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
