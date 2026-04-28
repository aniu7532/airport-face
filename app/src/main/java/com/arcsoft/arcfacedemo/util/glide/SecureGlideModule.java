package com.arcsoft.arcfacedemo.util.glide;

import java.io.InputStream;

import javax.crypto.SecretKey;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.arcsoft.arcfacedemo.network.OkHttpUtils;

import android.content.Context;
import okhttp3.OkHttpClient;

@GlideModule
public class SecureGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // 使用信任所有证书的 OkHttpClient，解决 HTTPS 自签名/私有证书导致的 SSLHandshakeException（OkHttpClient 实现了 Call.Factory，可传入 Factory）
        OkHttpClient unsafeClient = OkHttpUtils.getUnsafeOkHttpClient();
        ModelLoaderFactory<GlideUrl, InputStream> urlLoaderFactory = new OkHttpUrlLoader.Factory(unsafeClient);
        registry.replace(GlideUrl.class, InputStream.class, urlLoaderFactory);
        // 注册加密文件 ModelLoader，强制 File 走解密链路
        SecretKey secretKey = AESUtils.generateKey();
        registry.append(EncryptedGlideFile.class, InputStream.class, new EncryptedFileModelLoader.Factory(secretKey));
    }
}
