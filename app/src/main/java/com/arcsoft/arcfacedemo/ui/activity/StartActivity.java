package com.arcsoft.arcfacedemo.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.util.InfoStorage;

public class StartActivity extends AppCompatActivity {
    private static final String TAG = "StartActivity";
    InfoStorage infoStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        infoStorage = new InfoStorage(this);
        isFirstStart();
    }

    // 判断是否是第一次启动
    private void isFirstStart() {
        boolean isFirstStart = infoStorage.getBoolean("isFirstStart", true);
        if (isFirstStart) {
            infoStorage.saveBoolean("isFirstStart", false);
            // 跳转登录页面
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            // 跳转人脸识别页面
            Intent intent = new Intent(StartActivity.this, LivenessDetectJinActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
