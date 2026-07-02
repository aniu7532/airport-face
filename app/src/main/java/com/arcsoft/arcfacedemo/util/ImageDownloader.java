package com.arcsoft.arcfacedemo.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.OkHttpUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.util.glide.AESUtils;
import com.arcsoft.arcfacedemo.util.log.ALog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 远程人脸图片下载工具，支持携带 Token 请求并 AES 加密后落盘存储。
 */
public class ImageDownloader {

    private static final String TAG = "ImageDownloader";
    private static final String ALGORITHM = "AES";
    public static final String KEY = "1Hbfh667adfDEJ78"; // 16字节密钥

    private static final int CONNECT_TIMEOUT_SECONDS = 15;
    private static final int READ_TIMEOUT_SECONDS = 30;
    private static final int MAX_DOWNLOAD_BYTES = 15 * 1024 * 1024;
    /** 注册照最长边上限（人脸注册用） */
    private static final int MAX_REGISTER_DIMENSION = 1024;
    /** 展示照最长边上限 */
    private static final int MAX_PHOTO_DIMENSION = 400;

    private static final OkHttpClient DOWNLOAD_CLIENT = OkHttpUtils.getUnsafeOkHttpClient().newBuilder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(READ_TIMEOUT_SECONDS + CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();

    public static boolean downloadImage(File directory, String imageUrl, String imageName, String nickname, boolean zip) {

        File file = new File(directory, imageName + ".jpg");
        ALog.i("Image imageName: " + imageName);

        String baseUrl = UrlConstants.fileStreamUrl(imageUrl);
        Request.Builder requestBuilder = new Request.Builder().url(baseUrl);
        if (ApiUtils.getAccessToken() != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + ApiUtils.getAccessToken());
        }

        Bitmap bitmap = null;
        try (Response response = DOWNLOAD_CLIENT.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                ALog.e("下载图片失败，HTTP " + response.code() + ", url=" + baseUrl);
                return false;
            }
            ResponseBody body = response.body();
            if (body == null) {
                ALog.e("下载图片失败，响应体为空, url=" + baseUrl);
                return false;
            }
            byte[] imageBytes = readBodyWithLimit(body);
            if (imageBytes == null) {
                ALog.e("下载图片失败，读取响应体失败或超出大小限制, url=" + baseUrl);
                return false;
            }

            int maxDimension = zip ? MAX_PHOTO_DIMENSION : MAX_REGISTER_DIMENSION;
            bitmap = decodeSampledBitmap(imageBytes, maxDimension);
            if (bitmap == null || bitmap.isRecycled()) {
                ALog.e("下载图片失败，解码 bitmap 失败, url=" + baseUrl);
                return false;
            }
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
            ALog.e("下载或加密图片失败, url=" + baseUrl, e);
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        return false;
    }

    private static byte[] readBodyWithLimit(ResponseBody body) throws java.io.IOException {
        long contentLength = body.contentLength();
        if (contentLength > MAX_DOWNLOAD_BYTES) {
            ALog.e("下载图片失败，文件过大: " + contentLength + " bytes");
            return null;
        }
        try (InputStream inputStream = body.byteStream();
             java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            int total = 0;
            while ((read = inputStream.read(buffer)) != -1) {
                total += read;
                if (total > MAX_DOWNLOAD_BYTES) {
                    ALog.e("下载图片失败，实际大小超出限制: " + total + " bytes");
                    return null;
                }
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toByteArray();
        }
    }

    private static Bitmap decodeSampledBitmap(byte[] imageBytes, int maxDimension) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            ALog.e("下载图片失败，无法解析图片尺寸");
            return null;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, decodeOptions);
    }

    private static int calculateInSampleSize(int width, int height, int maxDimension) {
        int inSampleSize = 1;
        if (height > maxDimension || width > maxDimension) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= maxDimension
                    || (halfWidth / inSampleSize) >= maxDimension) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
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
            int maxSize = MAX_PHOTO_DIMENSION;
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
