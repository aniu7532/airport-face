package com.arcsoft.arcfacedemo.util.glide;

import java.io.File;

import androidx.annotation.NonNull;

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
