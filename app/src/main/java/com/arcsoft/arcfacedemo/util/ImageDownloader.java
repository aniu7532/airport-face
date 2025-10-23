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

        // // 加载网络图片并加密保存到本地
        // Glide.with(Utils.getApp()).asBitmap().load(imageUrl).into(new CustomTarget<Bitmap>() {
        // @Override
        // public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
        // new Thread(() -> {
        // try {
        // // 生成加密文件路径
        // // File encryptedFile = new File(Utils.getApp().getFilesDir(),
        // // "encrypted_image.dat");
        // // 加密保存
        // SecretKey secretKey = AESUtils.generateKey();
        // AESUtils.encryptBitmapToFile(bitmap, file, secretKey);
        // // Log.d("GlideDemo", "加密保存成功: " + file.getAbsolutePath());
        // ALog.i(nickname + ", Image downloaded successfully: " + file.getAbsolutePath());
        // } catch (Exception e) {
        // Log.e("GlideDemo", "加密失败", e);
        // }
        // }).start();
        // }
        //
        // @Override
        // public void onLoadCleared(@Nullable Drawable placeholder) {
        // }
        // });

        // Request request = new Request.Builder().url(imageUrl).build();
        // try (Response response = client.newCall(request).execute()) {
        // if (!response.isSuccessful()) {
        // throw new IOException("Failed to connect to the server for URL: " + imageUrl);
        // }
        //
        // InputStream inputStream = response.body().byteStream();
        // // File directory = new File(Constants.DEFAULT_REGISTER_FACES_DIR);
        //
        // File file = new File(directory, imageName + ".jpg");
        // ALog.i("Image imageName: " + imageName);
        //
        // // FileOutputStream 时会覆盖同名文件
        // // try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
        // // byte[] buffer = new byte[4096];
        // // int bytesRead;
        // // while ((bytesRead = inputStream.read(buffer)) != -1) {
        // // fileOutputStream.write(buffer, 0, bytesRead);
        // // }
        // // }
        // // 加密并保存文件
        // try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {// new
        // // FileOutputStream(file)覆盖模式保存
        // encrypt(inputStream, fileOutputStream, KEY);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // ALog.i("Image downloaded successfully: " + nickname + "," + file.getAbsolutePath());
        //
        // } catch (IOException e) {
        // e.printStackTrace();
        // ALog.e("Image downloaded failed: " + imageUrl);
        // }

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
            // if (bitmap != null && !bitmap.isRecycled()) {
            // bitmap.recycle();
            // }
            // bitmap = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            ALog.e("加密失败", e);
        }
        return false;

        // // 创建 UnsafeOkHttpClient 实例
        // UnsafeOkHttpClient unsafeClient = unsafeOkHttpClient();
        // OkHttpClient client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
        // .readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS)
        // .sslSocketFactory(unsafeClient.getSocketFactory(), unsafeClient.getTrustManager()).build();
        //
        // Request request = new Request.Builder().url(imageUrl).build();
        // try {
        // Response response = client.newCall(request).execute();
        // if (!response.isSuccessful()) {
        // throw new IOException("Failed to connect to the server for URL: " + imageUrl);
        // }
        // FileOutputStream fileOutputStream = null;
        // InputStream inputStream = null;
        // File file = new File(directory, imageName + ".jpg");
        // ALog.i("Image imageName: " + imageName);
        // // 加密并保存文件
        // try {
        // inputStream = response.body().byteStream();
        // fileOutputStream = new FileOutputStream(file);
        // encrypt(inputStream, fileOutputStream, KEY);
        // ALog.i("Image downloaded successfully: " + nickname + "," + file.getAbsolutePath() + ","
        // + FileUtils.getFileLength(file.getAbsolutePath()));
        // return true;
        // } catch (Exception e) {
        // e.printStackTrace();
        // } finally {
        // if (inputStream != null) {
        // inputStream.close();
        // }
        // if (fileOutputStream != null) {
        // fileOutputStream.close();
        // }
        // }
        // } catch (IOException e) {
        // e.printStackTrace();
        // ALog.e("Image downloaded failed: " + imageUrl);
        // }
        // return false;
    }

    public static boolean downloadImage2(File directory, String imageUrl, String imageName, String nickname) {

        File file = new File(directory, imageName + ".jpg");
        ALog.i("Image imageName: " + imageName);

        FutureTarget<Bitmap> futureTarget = Glide.with(Utils.getApp()).asBitmap().load(imageUrl).submit();
        try {
            Bitmap bitmap = futureTarget.get();
            // boolean result = ImageUtils.save(bitmap, file, Bitmap.CompressFormat.JPEG);
            SecretKey secretKey = AESUtils.generateKey();
            AESUtils.encryptBitmapToFile(bitmap, file, secretKey);
            // ALog.i("Image downloaded successfully: " + nickname + "," + file.getAbsolutePath() + "，" + result);
            ALog.i("Image downloaded successfully: " + nickname + "," + file.getAbsolutePath());
            // if (bitmap != null && !bitmap.isRecycled()) {
            // bitmap.recycle();
            // }
            // bitmap = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            ALog.e("加密失败", e);
        } finally {

        }
        return false;
        // // 创建 UnsafeOkHttpClient 实例
        // UnsafeOkHttpClient unsafeClient = unsafeOkHttpClient();
        // OkHttpClient client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
        // .readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS)
        // .sslSocketFactory(unsafeClient.getSocketFactory(), unsafeClient.getTrustManager()).build();
        //
        // Request request = new Request.Builder().url(imageUrl).build();
        // try {
        // Response response = client.newCall(request).execute();
        // if (!response.isSuccessful()) {
        // throw new IOException("Failed to connect to the server for URL: " + imageUrl);
        // }
        // FileOutputStream fileOutputStream = null;
        // InputStream inputStream = null;
        // File file = new File(directory, imageName + ".jpg");
        // ALog.i("Image imageName: " + imageName);
        // // 加密并保存文件
        // try {
        // inputStream = response.body().byteStream();
        // fileOutputStream = new FileOutputStream(file);
        // byte[] buffer = new byte[4096];
        // int bytesRead;
        // while ((bytesRead = inputStream.read(buffer)) != -1) {
        // fileOutputStream.write(buffer, 0, bytesRead);
        // }
        //
        // // encrypt(inputStream, fileOutputStream, KEY);
        // ALog.i("Image downloaded successfully: " + nickname + "," + file.getAbsolutePath() + ","
        // + FileUtils.getFileLength(file.getAbsolutePath()));
        // return true;
        // } catch (Exception e) {
        // e.printStackTrace();
        // } finally {
        // if (inputStream != null) {
        // inputStream.close();
        // }
        // if (fileOutputStream != null) {
        // fileOutputStream.close();
        // }
        // }
        // } catch (IOException e) {
        // e.printStackTrace();
        // ALog.e("Image downloaded failed: " + imageUrl);
        // }
        // return false;
    }

    // public static void downloadImage(File directory, String imageUrl, String imageName) {
    //
    // // 创建 UnsafeOkHttpClient 实例
    // UnsafeOkHttpClient unsafeClient = unsafeOkHttpClient();
    //
    // OkHttpClient client = new OkHttpClient.Builder()
    // .sslSocketFactory(unsafeClient.getSocketFactory(), unsafeClient.getTrustManager()).build();
    //
    // Request request = new Request.Builder().url(imageUrl).build();
    //
    // try (Response response = client.newCall(request).execute()) {
    // if (!response.isSuccessful()) {
    // throw new IOException("Failed to connect to the server for URL: " + imageUrl);
    // }
    //
    // InputStream inputStream = response.body().byteStream();
    // // File directory = new File(Constants.DEFAULT_REGISTER_FACES_DIR);
    //
    // File file = new File(directory, imageName + ".jpg");
    // ALog.i("Image imageName: " + imageName);
    //
    // // FileOutputStream 时会覆盖同名文件
    // // try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
    // // byte[] buffer = new byte[4096];
    // // int bytesRead;
    // // while ((bytesRead = inputStream.read(buffer)) != -1) {
    // // fileOutputStream.write(buffer, 0, bytesRead);
    // // }
    // // }
    // // 加密并保存文件
    // try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {// new
    // // FileOutputStream(file)覆盖模式保存
    // encrypt(inputStream, fileOutputStream, KEY);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // ALog.i("Image downloaded successfully: " + file.getAbsolutePath());
    // } catch (IOException e) {
    // e.printStackTrace();
    // ALog.e("Image downloaded failed: " + imageUrl);
    // }
    // }

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

    // public static Bitmap loadAndDecryptImage(String fileName, Context context) {
    // try {
    // File directory = new File(context.getExternalFilesDir(null), "register");
    // File file = new File(directory, fileName + ".jpg");
    // ALog.i("加载图片路径: " + file.getAbsolutePath());
    // if (!file.exists()) {
    // ALog.e("文件不存在: " + file.getAbsolutePath());
    // return null;
    // }
    // FileInputStream fileInputStream = new FileInputStream(file);
    // ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    // decrypt(fileInputStream, byteArrayOutputStream, KEY);
    // byte[] decryptedBytes = byteArrayOutputStream.toByteArray();
    // return BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.length);
    // } catch (Exception e) {
    // e.printStackTrace();
    // ALog.e("解密或加载图片时发生错误", e);
    // return null;
    // }
    // }

    // public static Bitmap loadAndDecryptImage2(String fileName, Context context) {
    // try {
    //
    // File directory = new File(context.getExternalFilesDir(null), "photo");
    // File file = new File(directory, fileName + ".jpg");
    // ALog.i("加载图片路径: " + file.getAbsolutePath());
    // if (!file.exists()) {
    // ALog.e("文件不存在: " + file.getAbsolutePath());
    // return null;
    // }
    // FileInputStream fileInputStream = new FileInputStream(file);
    // ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    // decrypt(fileInputStream, byteArrayOutputStream, KEY);
    //
    // byte[] decryptedBytes = byteArrayOutputStream.toByteArray();
    // return BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.length);
    // } catch (Exception e) {
    // e.printStackTrace();
    // ALog.e("解密或加载图片时发生错误", e);
    // return null;
    // }
    // }

    // public static Bitmap loadAndDecryptImage2(String filePath) {
    //
    // try {
    // File file = new File(filePath);
    // ALog.i("加载图片路径: " + file.getAbsolutePath());
    // if (!file.exists()) {
    // ALog.e("文件不存在: " + file.getAbsolutePath());
    // return null;
    // }
    // FileInputStream fileInputStream = new FileInputStream(file);
    // ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    // decrypt(fileInputStream, byteArrayOutputStream, KEY);
    //
    // byte[] decryptedBytes = byteArrayOutputStream.toByteArray();
    // return BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.length);
    // } catch (Exception e) {
    // e.printStackTrace();
    // ALog.e("解密或加载图片时发生错误", e);
    // return null;
    // }
    // }

    // private static void decrypt(InputStream inputStream, OutputStream outputStream, String key) throws Exception {
    // if (inputStream == null) {
    // ALog.e("inputStream不存在");
    // return;
    // }
    //
    // if (outputStream == null) {
    // ALog.e("outputStream");
    // return;
    // }
    // Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
    // Cipher cipher = Cipher.getInstance(ALGORITHM);
    // cipher.init(Cipher.DECRYPT_MODE, secretKey);
    //
    // byte[] buffer = new byte[4096];
    // int bytesRead;
    // while ((bytesRead = inputStream.read(buffer)) != -1) {
    // byte[] decryptedChunk = cipher.update(buffer, 0, bytesRead);
    // if (decryptedChunk != null) {
    // outputStream.write(decryptedChunk);
    // }
    // }
    // byte[] finalChunk = cipher.doFinal();
    // if (finalChunk != null) {
    // outputStream.write(finalChunk);
    // }
    // outputStream.flush();
    // }

    // /**
    // * 将byte数组解密
    // *
    // * @return
    // */
    // public static byte[] decryptByte(byte[] data) {
    // try {
    // Key secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
    // Cipher cipher = Cipher.getInstance(ALGORITHM);
    // cipher.init(Cipher.DECRYPT_MODE, secretKey);
    // return cipher.doFinal(data);
    // } catch (Exception e) {
    // e.printStackTrace();
    // ALog.e("byte数组解密失败");
    // return null;
    // }
    // }

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
