package com.arcsoft.arcfacedemo.util.glide;

import java.io.File;

import androidx.annotation.NonNull;

/**
 * Glide 加密文件模型，封装待解密的本地文件引用。
 */
public class EncryptedGlideFile {
    private final File file;

    public EncryptedGlideFile(@NonNull File file) {
        this.file = file;
    }

    @NonNull
    public File getFile() {
        return file;
    }
}
