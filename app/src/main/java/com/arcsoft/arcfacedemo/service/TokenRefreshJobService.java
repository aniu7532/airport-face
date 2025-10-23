package com.arcsoft.arcfacedemo.service;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.util.Log;

import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TokenRefreshJobService extends JobService {
    private static final String TAG = "TokenRefreshJobService";
    private static final String REFRESH_TOKEN_URL = UrlConstants.URL_refresh_token;

    @Override
    public boolean onStartJob(JobParameters params) {
        new RefreshTokenTask().execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private class RefreshTokenTask extends AsyncTask<JobParameters, Void, Boolean> {
        @Override
        protected Boolean doInBackground(JobParameters... params) {
            Log.i(TAG, "调用了刷新token ");
            // 创建请求体
            FormBody.Builder formBodyBuilder = new FormBody.Builder()
                    .add("refreshToken", ApiUtils.getRefreshToken())
                    .add("clientId", UrlConstants.URL_ClIENTID);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(REFRESH_TOKEN_URL)
                    .addHeader("tenant-id", "1")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(formBodyBuilder.build())
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    // 处理新的 Token
                    Log.d(TAG, "Token refreshed successfully");
                    Log.i(TAG, "response: "+gson.toJson(response.body()));
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                // 任务完成
            }
        }
    }
}
