package com.arcsoft.arcfacedemo.db;

import android.content.Context;

import androidx.room.Room;

import net.sqlcipher.database.SupportFactory;

public class DatabaseClient {
    private static YinchuanAirportDB instance;

    public static YinchuanAirportDB getInstance(Context context) {
        if (instance == null) {
            // 设置加密密码
            String passphrase = "my-secret-password";
            byte[] passphraseBytes = passphrase.getBytes(); // 将字符串转换为字节数组
            SupportFactory factory = new SupportFactory(passphraseBytes);

            // 创建加密的 Room 数据库
            instance = Room.databaseBuilder(context, YinchuanAirportDB.class, "encrypted-db")
                    .openHelperFactory(factory) // 使用 SQLCipher 的 SupportFactory
                    .build();
        }
        return instance;
    }
}
