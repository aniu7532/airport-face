package com.arcsoft.arcfacedemo.util.glide;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class NetworkConfig {
    public static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS) // 连接超时
                .readTimeout(20, TimeUnit.SECONDS) // 读取超时
                .writeTimeout(20, TimeUnit.SECONDS) // 写入超时
                .build();
    }
}
