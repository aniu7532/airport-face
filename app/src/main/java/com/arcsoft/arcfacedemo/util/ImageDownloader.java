package com.arcsoft.arcfacedemo.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.arcsoft.arcfacedemo.entity.LongPassCard;
import com.arcsoft.arcfacedemo.util.glide.AESUtils;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;

import android.content.Context;
import android.graphics.Bitmap;

import okhttp3.OkHttpClient;

public class ImageDownloader {

    private static final String TAG = "ImageDownloader";
    private static final String ALGORITHM = "AES";
    public static final String KEY = "1Hbfh667adfDEJ78"; // 16字节密钥

    public static boolean downloadImage(File directory, String imageUrl, String imageName, String nickname, boolean zip) {

        File file = new File(directory, imageName + ".jpg");
        ALog.i("Image imageName: " + imageName);

        FutureTarget<Bitmap> futureTarget = Glide
                .with(Utils.getApp())
                .asBitmap()
                .load(imageUrl)
                .encodeFormat(Bitmap.CompressFormat.JPEG)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .submit();
        try {
            Bitmap bitmap = futureTarget.get();
            // 如果启用压缩
            if (zip) {
                bitmap = compressBitmap(bitmap);
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
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                        originalBitmap,
                        newWidth,
                        newHeight,
                        true
                );
                originalBitmap.recycle();
                originalBitmap = scaledBitmap;
                System.out.println("图片尺寸从 " + originalWidth + "x" + originalHeight +
                        " 压缩到 " + newWidth + "x" + newHeight);
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
