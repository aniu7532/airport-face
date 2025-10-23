package com.arcsoft.arcfacedemo.util.glide;

import java.io.File;
import java.io.IOException;

import javax.crypto.SecretKey;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

import android.graphics.Bitmap;

public class EncryptedFileDecoder implements ResourceDecoder<File, Bitmap> {
    private final BitmapPool bitmapPool;
    private final SecretKey secretKey;

    public EncryptedFileDecoder(BitmapPool bitmapPool, SecretKey secretKey) {
        this.bitmapPool = bitmapPool;
        this.secretKey = secretKey;
    }

    @Override
    public boolean handles(File source, Options options) {
        return true; // 处理所有 File 类型
    }

    @Override
    public Resource<Bitmap> decode(File source, int width, int height, Options options) throws IOException {
        Bitmap bitmap = AESUtils.decryptFileToBitmap(source, secretKey);
        if (bitmap == null) {
            throw new IOException("解密失败：无法生成 Bitmap");
        }
        return BitmapResource.obtain(bitmap, bitmapPool);
    }
}
