package com.arcsoft.arcfacedemo.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    // 将网址图片转化为 byte[]
    public static byte[] getImageBytes(String imageUrl) {
        Log.d(TAG, "调用了一次在线图片转byte[]");
        HttpURLConnection connection = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        try {
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                bis = new BufferedInputStream(inputStream);
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = bis.read(buffer))!= -1) {
                    baos.write(buffer, 0, len);
                }
                return baos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos!= null) {
                    baos.close();
                }
                if (bis!= null) {
                    bis.close();
                }
                if (connection!= null) {
                    connection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // 将 byte[] 转化为 Bitmap 图片
    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
    // 将 Bitmap 转化为 byte[]
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
    /**
     * 将图片的 byte[] 数据转换为 BGR24 格式的 byte[]
     *
     * @param imageBytes 图片的字节数组
     * @return BGR24 格式的 byte[]
     */
    public static byte[] convertToBGR24(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            Log.e("ImageUtils", "图片字节数组为空或无效");
            return new byte[0];
        }

        // 解码图片字节数组为 Bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        if (bitmap == null) {
            Log.e("ImageUtils", "无法解码图片字节数组");
            return new byte[0];
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height * 3; // 每个像素 3 字节 (BGR)
        byte[] bgr24Data = new byte[size];

        // 遍历每个像素并转换为 BGR24 格式
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);

                int blue = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int red = pixel & 0xFF;

                int index = (y * width + x) * 3;
                bgr24Data[index] = (byte) blue;
                bgr24Data[index + 1] = (byte) green;
                bgr24Data[index + 2] = (byte) red;
            }
        }

        return bgr24Data;
    }

    /**
     * 从 byte[] 获取图片的高度和宽度
     *
     * @param imageBytes 图片的字节数组
     * @return 包含高度和宽度的数组 [height, width]
     */
    public static int[] getImageDimensions(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            Log.e("ImageUtils", "图片字节数组为空或无效");
            return new int[]{0, 0};
        }

        // 使用 BitmapFactory.Options 来仅获取图片的尺寸信息而不加载整个图片到内存
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 只解码边界信息，不分配像素数组

        // 解码字节数组以获取尺寸信息
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);

        // 获取图片的高度和宽度
        int height = options.outHeight;
        int width = options.outWidth;

        return new int[]{height, width};
    }

    public static Bitmap loadBitmapFromPath(String imagePath) {
        Bitmap bitmap = null;


        try (FileInputStream fis = new FileInputStream(new File(imagePath))) {
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + imagePath, e);
        } catch (IOException e) {
            Log.e(TAG, "Error reading file: " + imagePath, e);
        }


        return bitmap;
    }
}
