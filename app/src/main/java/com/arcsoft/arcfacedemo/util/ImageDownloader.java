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
import com.bumptech.glide.request.FutureTarget;

import android.content.Context;
import android.graphics.Bitmap;

import okhttp3.OkHttpClient;

public class ImageDownloader {

    private static final String TAG = "ImageDownloader";
    private static final String ALGORITHM = "AES";
    public static final String KEY = "1Hbfh667adfDEJ78"; // 16字节密钥

    /**
     * 下载图片
     *
     * @param longPassCardList
     */
    public static void downloadImages(List<LongPassCard> longPassCardList, Context context) {
        // 创建 UnsafeOkHttpClient 实例
        UnsafeOkHttpClient unsafeClient = unsafeOkHttpClient();

        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(unsafeClient.getSocketFactory(), unsafeClient.getTrustManager()).build();

        for (LongPassCard longPassCard : longPassCardList) {
            ALog.i("下载图片:" + longPassCard.nickname);
            File directory1 = new File(context.getExternalFilesDir(null), "register");// 应用的私有目录
            if (!directory1.exists()) {
                directory1.mkdirs();
            }

            downloadImage(client, directory1, longPassCard.checkPhoto, longPassCard.id, longPassCard.nickname);
            File directory2 = new File(context.getExternalFilesDir(null), "photo");// 应用的私有目录
            if (!directory2.exists()) {
                directory2.mkdirs();
            }
            downloadImage(client, directory2, longPassCard.photo, longPassCard.id, longPassCard.nickname);
        }
    }

    public static void downloadImage(OkHttpClient client, File directory, String imageUrl, String imageName,
            String nickname) {

        File file = new File(directory, imageName + ".jpg");
        ALog.i("Image imageName: " + imageName);

        FutureTarget<Bitmap> futureTarget = Glide.with(Utils.getApp()).asBitmap().load(imageUrl).submit();
        try {
            Bitmap bitmap = futureTarget.get();
            SecretKey secretKey = AESUtils.generateKey();
            AESUtils.encryptBitmapToFile(bitmap, file, secretKey);
            ALog.i("Image downloaded successfully: " + nickname + "," + file.getAbsolutePath());
            // if (bitmap != null && !bitmap.isRecycled()) {
            // bitmap.recycle();
            // }
            // bitmap = null;
        } catch (Exception e) {
            e.printStackTrace();
            ALog.e("加密失败", e);
        } finally {

        }

    }

    public static boolean downloadImage(File directory, String imageUrl, String imageName, String nickname) {

        File file = new File(directory, imageName + ".jpg");
        ALog.i("Image imageName: " + imageName);

        FutureTarget<Bitmap> futureTarget = Glide.with(Utils.getApp()).asBitmap().load(imageUrl).submit();
        try {
            Bitmap bitmap = futureTarget.get();
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
