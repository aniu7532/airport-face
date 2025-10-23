package com.arcsoft.arcfacedemo.util;

import android.content.Context;
import android.content.SharedPreferences;


public class InfoStorage {
    private static final String PREF_NAME = "yunduanchayan";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public InfoStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // 存储字符串信息
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    // 获取字符串信息
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // 存储整数信息
    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    // 获取整数信息
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    // 存储布尔信息
    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    // 获取布尔信息
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    // 清除所有存储信息
    public void clear() {
        editor.clear();
        editor.apply();
    }

    // 删除单个键值对
    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }
}
