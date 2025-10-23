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

public class ApiUtils {
    private static final OkHttpClient client = getUnsafeOkHttpClient();
    // 存储 accessToken 的静态变量
    public static String accessToken;
    public static String refreshToken;
    public static String userId;

    // 设置 accessToken 的方法
    public static void setAccessToken(String token) {
        accessToken = token;
    }

    // 设置 refreshToken 的方法
    public static void setRefreshToken(String token) {
        refreshToken = token;
    }

    // 获取 refreshToken 的方法
    public static String getRefreshToken() {
        return refreshToken;
    }

    // 获取 accessToken 的方法
    public static String getAccessToken() {
        return accessToken;
    }

    // 定义一个接口，用于回调 API 调用的结果
    public interface ApiCallback {
        void onSuccess(String response);

        void onFailure(Throwable e);
    }

    // 封装一个 GET 请求的方法
    // public static void get(String url, Map<String, String> params, ApiCallback callback) {
    // Request.Builder requestBuilder = new Request.Builder()
    // .url(url)
    // .addHeader("tenant-id", "1");
    // // 检查是否有 accessToken，如果有则添加 Authorization 头
    // if (accessToken!= null) {
    // requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
    // }
    // Request request = requestBuilder.build();
    //
    // client.newCall(request).enqueue(new Callback() {
    // @Override
    // public void onFailure(Call call, IOException e) {
    // callback.onFailure(e);
    // }
    //
    // @Override
    // public void onResponse(Call call, Response response) throws IOException {
    // if (response.isSuccessful()) {
    // callback.onSuccess(response.body().string());
    // } else {
    // callback.onFailure(new IOException("Unexpected code " + response));
    // }
    // }
    // });
    // }
    // 封装一个 GET 请求的方法
    public static void get(String url, Map<String, String> params, ApiCallback callback) {
        // HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        // if (params != null) {
        // for (Map.Entry<String, String> entry : params.entrySet()) {
        // urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        // }
        // }
        // Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build()).addHeader("tenant-id", "1");
        // // 检查是否有 accessToken，如果有则添加 Authorization 头
        // if (accessToken != null) {
        // requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        // }
        // Request request = requestBuilder.build();
        //
        // client.newCall(request).enqueue(new Callback() {
        // @Override
        // public void onFailure(Call call, IOException e) {
        // callback.onFailure(e);
        // }
        //
        // @Override
        // public void onResponse(Call call, Response response) throws IOException {
        // if (response.isSuccessful()) {
        // callback.onSuccess(response.body().string());
        // } else {
        // callback.onFailure(new IOException("Unexpected code " + response));
        // }
        // }
        // });

        GetRequest<String> request = OkGo.<String> get(url).tag(url);
        if (params != null) {
            // 更新或添加 timestamp 参数
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            for (Map.Entry<String, String> entry : params.entrySet()) {
                request.params(entry.getKey(), entry.getValue());
            }
        }
        request.headers("tenant-id", "1");
        // 检查是否有 accessToken，如果有则添加 Authorization 头
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

    // 封装一个 GET 请求的方法
    public static void getPassCard(String url, Map<String, String> params, ApiCallback callback) {
        // HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        // if (params != null) {
        // // 更新或添加 timestamp 参数
        // params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        // for (Map.Entry<String, String> entry : params.entrySet()) {
        // urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        // }
        // }
        // Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build()).addHeader("tenant-id", "1");
        // // 检查是否有 accessToken，如果有则添加 Authorization 头
        // if (accessToken != null) {
        // requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        // }
        // Request request = requestBuilder.build();
        //
        // client.newCall(request).enqueue(new Callback() {
        // @Override
        // public void onFailure(Call call, IOException e) {
        // callback.onFailure(e);
        // }
        //
        // @Override
        // public void onResponse(Call call, Response response) throws IOException {
        // if (response.isSuccessful()) {
        // callback.onSuccess(response.body().string());
        // } else {
        // callback.onFailure(new IOException("Unexpected code " + response));
        // }
        // }
        // });

        GetRequest<String> request = OkGo.<String> get(url).tag(url);
        if (params != null) {
            // 更新或添加 timestamp 参数
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            for (Map.Entry<String, String> entry : params.entrySet()) {
                request.params(entry.getKey(), entry.getValue());
            }
        }
        request.headers("tenant-id", "1");
        // 检查是否有 accessToken，如果有则添加 Authorization 头
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

    // 封装一个 POST 请求的方法
    public static void post(String url, String json, ApiCallback callback) {
        // Request.Builder requestBuilder = new Request.Builder().url(url).addHeader("tenant-id", "1");
        // // 检查是否有 accessToken，如果有则添加 Authorization 头
        // if (accessToken != null) {
        // requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        // }
        // Request request = requestBuilder
        // .post(RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"))).build();
        //
        // client.newCall(request).enqueue(new Callback() {
        // @Override
        // public void onFailure(Call call, IOException e) {
        // callback.onFailure(e);
        // }
        //
        // @Override
        // public void onResponse(Call call, Response response) throws IOException {
        // if (response.isSuccessful()) {
        // callback.onSuccess(response.body().string());
        // } else {
        // callback.onFailure(new IOException("Unexpected code " + response));
        // }
        // }
        // });

        PostRequest<String> request = OkGo.<String> post(url).tag(url);
        request.headers("tenant-id", "1");
        // 检查是否有 accessToken，如果有则添加 Authorization 头
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

    // 上传图片
    public static void upload(String url, String imgStr, ApiCallback callback) {
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
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

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
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
