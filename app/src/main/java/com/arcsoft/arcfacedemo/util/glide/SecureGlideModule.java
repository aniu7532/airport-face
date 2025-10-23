package com.arcsoft.arcfacedemo.util.glide;

import java.io.File;

import javax.crypto.SecretKey;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import android.content.Context;
import android.graphics.Bitmap;

@GlideModule
public class SecureGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // 注册加密文件解码器
        SecretKey secretKey = AESUtils.generateKey();
        registry.append(File.class, Bitmap.class, new EncryptedFileDecoder(glide.getBitmapPool(), secretKey));

        // // 使用自定义的 OkHttpClient
        // OkHttpClient okHttpClient = NetworkConfig.createOkHttpClient();
        // registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(okHttpClient));

        // OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS) // 设置连接超时时间
        // .readTimeout(30, TimeUnit.SECONDS) // 设置读取超时时间
        // .build();
        //
        // registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }
}
