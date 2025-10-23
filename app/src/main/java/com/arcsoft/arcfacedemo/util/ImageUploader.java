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

public class ImageUploader {

    private static final OkHttpClient client = OkHttpUtils.getUnsafeOkHttpClient(); // 使用安全的 OkHttpClient 实例
    private static final String TAG = "ImageUploader";
    Gson gson = new Gson();

    public String uploadBitmap(Bitmap bitmap) {
        final CompletableFuture<String> future = new CompletableFuture<>();

        // 将 Bitmap 转换为字节数组
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapData = bos.toByteArray();

            // 创建请求体
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg", RequestBody.create(MediaType.parse("image/jpeg"), bitmapData))
                    .build();

            // 创建请求
            Request request = new Request.Builder().url(UrlConstants.URL_UPLOAD_FILE).addHeader("tenant-id", "1") // 从配置文件获取
                    // tenant-id
                    .addHeader("Authorization", "Bearer " + ApiUtils.getAccessToken()).post(requestBody).build();

            // 执行请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) {
                            if (responseBody != null) {
                                String responseData = responseBody.string();
                                ApiResponse resData = gson.fromJson(responseData, ApiResponse.class);

                                if (resData.getCode() == 200) {
                                    Object data = resData.getData();
                                    if (data != null) {
                                        future.complete((String) data);
                                    } else {
                                        future.complete(null);
                                    }
                                } else {
                                    ALog.e("图片上传失败: " + resData.getMsg());
                                    future.complete(null);
                                }
                                ALog.i("图片上传返回: " + responseData);
                            }
                        }
                    } else {
                        ALog.e("图片上传失败: " + response.code());
                        future.complete(null);
                    }
                }
            });

        } catch (IOException e) {
            future.completeExceptionally(e);
        }

        try {
            return future.get(); // 等待子线程完成并返回结果
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        // finally {
        // // 1. 及时回收 Bitmap（可选）
        // if (bitmap != null && !bitmap.isRecycled()) {
        // bitmap.recycle();
        // }
        // }
    }

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
