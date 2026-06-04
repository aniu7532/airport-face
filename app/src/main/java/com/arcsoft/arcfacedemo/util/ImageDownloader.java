package com.arcsoft.arcfacedemo.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.util.glide.AESUtils;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.FutureTarget;

import android.graphics.Bitmap;

public class ImageDownloader {

    private static final String TAG = "ImageDownloader";
    private static final String ALGORITHM = "AES";
    public static final String KEY = "1Hbfh667adfDEJ78"; // 16字节密钥

    public static boolean downloadImage(File directory, String imageUrl, String imageName, String nickname, boolean zip) {

        File file = new File(directory, imageName + ".jpg");
        ALog.i("Image imageName: " + imageName);

        // 拼接基础下载地址
        String baseUrl = UrlConstants.fileStreamUrl(imageUrl);

        // 构造带有 Authorization 头的 GlideUrl
        LazyHeaders.Builder headersBuilder = new LazyHeaders.Builder();
        // 携带 accessToken（如果存在）
        if (ApiUtils.getAccessToken() != null) {
            headersBuilder.addHeader("Authorization", "Bearer " + ApiUtils.getAccessToken());
        }
        GlideUrl glideUrl = new GlideUrl(baseUrl, headersBuilder.build());

        FutureTarget<Bitmap> futureTarget = Glide.with(Utils.getApp()).asBitmap().load(glideUrl)
                .encodeFormat(Bitmap.CompressFormat.JPEG).diskCacheStrategy(DiskCacheStrategy.NONE).submit();
        try {
            Bitmap bitmap = futureTarget.get();
            if (bitmap == null) {
                ALog.e("下载图片失败，bitmap 为 null, url=" + baseUrl);
                return false;
            }
            if (bitmap.isRecycled()) {
                ALog.e("下载图片失败，bitmap 已被回收, url=" + baseUrl);
                return false;
            }
            // 如果启用压缩
            if (zip) {
                bitmap = compressBitmap(bitmap);
                if (bitmap == null || bitmap.isRecycled()) {
                    ALog.e("图片压缩后为 null 或已被回收，终止加密保存");
                    return false;
                }
            }
            SecretKey secretKey = AESUtils.generateKey();
            AESUtils.encryptBitmapToFile(bitmap, file, secretKey);
            ALog.i("Image downloaded successfully: " + nickname + "," + file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            ALog.e("加密失败", e);
        }
        return false;
    }

    /**
     * 压缩位图的多维度方法
     */
    private static Bitmap compressBitmap(Bitmap originalBitmap) {
        try {
            if (originalBitmap == null) {
                return null;
            }
            if (originalBitmap.isRecycled()) {
                ALog.e("compressBitmap: 传入的 bitmap 已被回收，跳过压缩");
                return null;
            }
            // 1. 尺寸缩放压缩（按比例缩小）
            int originalWidth = originalBitmap.getWidth();
            int originalHeight = originalBitmap.getHeight();

            // 设置目标最大尺寸（例如：1024px）
            int maxSize = 400;
            float scale = 1.0f;

            if (originalWidth > maxSize || originalHeight > maxSize) {
                if (originalWidth > originalHeight) {
                    scale = (float) maxSize / originalWidth;
                } else {
                    scale = (float) maxSize / originalHeight;
                }
            }

            int newWidth = (int) (originalWidth * scale);
            int newHeight = (int) (originalHeight * scale);

            if (scale < 1.0f) {
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
                // 不在这里回收 originalBitmap，交由调用方统一管理，避免误用已回收 bitmap
                originalBitmap = scaledBitmap;
                ALog.i("图片尺寸从 " + originalWidth + "x" + originalHeight + " 压缩到 " + newWidth + "x" + newHeight);
            }

            return originalBitmap;

        } catch (Exception e) {
            ALog.e("图片压缩失败", e);
            return originalBitmap;
        }
    }
    public static void encrypt(InputStream inputStream, OutputStream outputStream, String key) throws Exception {
        Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] buffer = new byte[4096];
        int bytesRead;
        try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static UnsafeOkHttpClient unsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[] {};
                }
            } };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            return new UnsafeOkHttpClient(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class UnsafeOkHttpClient {
        final javax.net.ssl.SSLSocketFactory sslSocketFactory;
        final javax.net.ssl.X509TrustManager trustManager;

        UnsafeOkHttpClient(javax.net.ssl.SSLSocketFactory sslSocketFactory,
                javax.net.ssl.X509TrustManager trustManager) {
            this.sslSocketFactory = sslSocketFactory;
            this.trustManager = trustManager;
        }

        public javax.net.ssl.SSLSocketFactory getSocketFactory() {
            return sslSocketFactory;
        }

        public javax.net.ssl.X509TrustManager getTrustManager() {
            return trustManager;
        }
    }
}
