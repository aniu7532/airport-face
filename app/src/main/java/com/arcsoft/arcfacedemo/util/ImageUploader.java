package com.arcsoft.arcfacedemo.util;

import android.graphics.Bitmap;

import com.arcsoft.arcfacedemo.entity.ApiResponse;
import com.arcsoft.arcfacedemo.entity.Base;
import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.OkHttpUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.request.PostRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 图片上传工具类，将 Bitmap 以 multipart 形式上传至加密文件接口。
 */
public class ImageUploader {

    private static final OkHttpClient client = OkHttpUtils.getUnsafeOkHttpClient(); // 使用安全的 OkHttpClient 实例
    private static final String TAG = "ImageUploader";
    Gson gson = new Gson();

    /**
     * 同步上传 Bitmap 图片，成功时返回服务端文件路径。
     */
    public String uploadBitmap2(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();
        // 创建一个MultipartBody对象
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("file", "image.jpg", RequestBody.create(MediaType.parse("image/jpeg"), bitmapData));

        MultipartBody requestBody = builder.build();

        PostRequest<String> request = OkGo.<String> post(UrlConstants.URL_UPLOAD_FILE).tag(this);

        request.headers("tenant-id", "1");
        // 检查是否有 accessToken，如果有则添加 Authorization 头
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }
        // request.generateRequest(requestBody);
        // 同步会阻塞主线程，必须开线程，不传callback即为同步请求
        com.lzy.okgo.adapter.Call<String> call =
                request.upRequestBody(requestBody).converter(new StringConvert()).adapt();
        try {
            com.lzy.okgo.model.Response<String> res = call.execute();
            if (res.code() == 200) {
                // ALog.d("上传临时证件图片成功返回");
                String data = res.body();
                if (data != null) {
                    Base<String> resData = gson.fromJson(data, new TypeToken<Base<String>>() {
                    }.getType());
                    if (resData.getCode() == 200) {
                        ALog.i("图片上传成功: " + resData.getMsg());
                        // 1. 及时回收 Bitmap（可选）
                        // if (bitmap != null && !bitmap.isRecycled()) {
                        // bitmap.recycle();
                        // }
                        return resData.getData();
                    } else {
                        ALog.e("图片上传失败: " + resData.getMsg());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ALog.e("上传临时证件日志失败返回: " + e.getMessage());
        }
        return null;
    }
}
