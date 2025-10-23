package com.arcsoft.arcfacedemo.util.glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class AESUtils {
    // 固定 IV（16 字节）
    private static final byte[] FIXED_IV = "1Hbfh667adfDEJ78".getBytes(StandardCharsets.UTF_8);

    // 加密 Bitmap 并保存到文件
    public static void encryptBitmapToFile(Bitmap bitmap, File outputFile, SecretKey key) {
        try {
            // 将 Bitmap 转换为字节流
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            byte[] imageData = byteStream.toByteArray();

            // 初始化加密器
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(FIXED_IV));

            // 加密数据
            byte[] encryptedData = cipher.doFinal(imageData);

            // 保存加密数据到文件
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(encryptedData);
            fos.close();
        } catch (Exception e) {
            Log.e("AESUtils", "加密失败", e);
        }
    }

    public static Bitmap decryptRegisterFileToBitmap(String fileName) {
        return decryptFileToBitmap("register", fileName);
    }

    public static Bitmap decryptPhotoFileToBitmap(String fileName) {
        return decryptFileToBitmap("photo", fileName);
    }

    public static Bitmap decryptFileToBitmap(String dir, String fileName) {
        try {
            File directory = new File(Utils.getApp().getExternalFilesDir(null), dir);
            File encryptedFile = new File(directory, fileName + ".jpg");
            ALog.i("加载图片路径: " + encryptedFile.getAbsolutePath());
            if (!encryptedFile.exists()) {
                ALog.e("文件不存在: " + encryptedFile.getAbsolutePath());
                return null;
            }

            // 读取加密数据
            byte[] encryptedData = new byte[(int) encryptedFile.length()];
            FileInputStream fis = new FileInputStream(encryptedFile);
            fis.read(encryptedData);
            fis.close();

            // 初始化解密器
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, AESUtils.generateKey(), new IvParameterSpec(FIXED_IV));

            // 解密数据
            byte[] decryptedData = cipher.doFinal(encryptedData);
            ALog.e("decryptedData.length:" + decryptedData.length);
            // 生成 Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(decryptedData, 0, decryptedData.length);
            ALog.e("bitmap.getWidth():" + bitmap.getWidth() + ",bitmap.getHeight():" + bitmap.getHeight());
            return bitmap;
        } catch (Exception e) {
            Log.e("AESUtils", "解密失败", e);
            return null;
        }
    }

    public static Bitmap decryptFileToBitmap(String filePath) {
        try {
            File encryptedFile = new File(filePath);
            ALog.i("加载图片路径: " + encryptedFile.getAbsolutePath());
            if (!encryptedFile.exists()) {
                ALog.e("文件不存在: " + encryptedFile.getAbsolutePath());
                return null;
            }

            // 读取加密数据
            byte[] encryptedData = new byte[(int) encryptedFile.length()];
            FileInputStream fis = new FileInputStream(encryptedFile);
            fis.read(encryptedData);
            fis.close();

            // 初始化解密器
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, AESUtils.generateKey(), new IvParameterSpec(FIXED_IV));

            // 解密数据
            byte[] decryptedData = cipher.doFinal(encryptedData);
            ALog.e("decryptedData.length:" + decryptedData.length);
            // 生成 Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(decryptedData, 0, decryptedData.length);
            ALog.e("bitmap.getWidth():" + bitmap.getWidth() + ",bitmap.getHeight():" + bitmap.getHeight());
            return bitmap;
        } catch (Exception e) {
            Log.e("AESUtils", "解密失败", e);
            return null;
        }
    }

    // 从加密文件解密为 Bitmap
    public static Bitmap decryptFileToBitmap(File encryptedFile, SecretKey key) {
        try {
            // 读取加密数据
            byte[] encryptedData = new byte[(int) encryptedFile.length()];
            FileInputStream fis = new FileInputStream(encryptedFile);
            fis.read(encryptedData);
            fis.close();

            // 初始化解密器
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(FIXED_IV));

            // 解密数据
            byte[] decryptedData = cipher.doFinal(encryptedData);

            // 生成 Bitmap
            return BitmapFactory.decodeByteArray(decryptedData, 0, decryptedData.length);
        } catch (Exception e) {
            Log.e("AESUtils", "解密失败", e);
            return null;
        }
    }

    public static byte[] decryptFileToByte(String dir, String fileName) {
        try {
            File directory = new File(Utils.getApp().getExternalFilesDir(null), dir);
            File encryptedFile = new File(directory, fileName + ".jpg");
            ALog.i("加载图片路径: " + encryptedFile.getAbsolutePath());
            if (!encryptedFile.exists()) {
                ALog.e("文件不存在: " + encryptedFile.getAbsolutePath());
                return null;
            }
            // 读取加密数据
            byte[] encryptedData = new byte[(int) encryptedFile.length()];
            FileInputStream fis = new FileInputStream(encryptedFile);
            fis.read(encryptedData);
            fis.close();

            // 初始化解密器
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, AESUtils.generateKey(), new IvParameterSpec(FIXED_IV));

            // 解密数据
            byte[] decryptedData = cipher.doFinal(encryptedData);
            return decryptedData;
        } catch (Exception e) {
            Log.e("AESUtils", "解密失败", e);
            return null;
        }
    }

    public static byte[] decryptFileToByte(File encryptedFile) {
        return decryptFileToByte(encryptedFile, AESUtils.generateKey());
    }

    public static byte[] decryptFileToByte(File encryptedFile, SecretKey key) {
        try {
            // 读取加密数据
            byte[] encryptedData = new byte[(int) encryptedFile.length()];
            FileInputStream fis = new FileInputStream(encryptedFile);
            fis.read(encryptedData);
            fis.close();

            // 初始化解密器
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(FIXED_IV));

            // 解密数据
            byte[] decryptedData = cipher.doFinal(encryptedData);
            return decryptedData;
        } catch (Exception e) {
            Log.e("AESUtils", "解密失败", e);
            return null;
        }
    }

    // 生成固定密钥（示例，实际应使用安全存储）
    public static SecretKey generateKey() {
        // 注意：此处仅用于演示，实际应使用 Android Keystore 存储密钥
        String keyString = "1Hbfh667adfDEJ78"; // 密钥长度需符合 AES 要求（如 16/24/32 字节）
        return new SecretKeySpec(keyString.getBytes(StandardCharsets.UTF_8), "AES");
    }

    public static File getPhotoPath(String name) {
        File directory = new File(Utils.getApp().getExternalFilesDir(null), "photo");
        return new File(directory, name + ".jpg");
    }

    public static File getRegisterPath(String name) {
        File directory = new File(Utils.getApp().getExternalFilesDir(null), "register");
        return new File(directory, name + ".jpg");
    }
}
