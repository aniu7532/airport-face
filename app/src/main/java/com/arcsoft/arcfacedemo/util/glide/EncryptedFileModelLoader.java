package com.arcsoft.arcfacedemo.util.glide;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arcsoft.arcfacedemo.util.log.ALog;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.SecretKey;

public class EncryptedFileModelLoader implements ModelLoader<EncryptedGlideFile, InputStream> {
    private final SecretKey secretKey;

    public EncryptedFileModelLoader(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull EncryptedGlideFile model, int width, int height,
            @NonNull Options options) {
        java.io.File file = model.getFile();
        ALog.d("EncryptedFileModelLoader buildLoadData: " + file.getAbsolutePath());
        return new LoadData<>(new ObjectKey(file.getAbsolutePath() + ":" + file.lastModified()),
                new EncryptedStreamFetcher(file, secretKey));
    }

    @Override
    public boolean handles(@NonNull EncryptedGlideFile model) {
        java.io.File file = model.getFile();
        boolean exists = file.exists();
        if (!exists) {
            ALog.e("EncryptedFileModelLoader missing file: " + file.getAbsolutePath());
        }
        return exists;
    }

    private static class EncryptedStreamFetcher implements DataFetcher<InputStream> {
        private final java.io.File file;
        private final SecretKey secretKey;
        private volatile boolean cancelled;
        private InputStream stream;

        EncryptedStreamFetcher(java.io.File file, SecretKey secretKey) {
            this.file = file;
            this.secretKey = secretKey;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
            ALog.d("EncryptedFileModelLoader loadData: " + file.getAbsolutePath());
            if (cancelled) {
                callback.onLoadFailed(new IOException("load cancelled"));
                return;
            }
            byte[] data = AESUtils.decryptFileToByte(file, secretKey);
            if (data == null || data.length == 0) {
                callback.onLoadFailed(new IOException("decrypt stream failed: " + file.getAbsolutePath()));
                return;
            }
            stream = new ByteArrayInputStream(data);
            callback.onDataReady(stream);
        }

        @Override
        public void cleanup() {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                }
                stream = null;
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        @NonNull
        @Override
        public Class<InputStream> getDataClass() {
            return InputStream.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }

    public static class Factory implements ModelLoaderFactory<EncryptedGlideFile, InputStream> {
        private final SecretKey secretKey;

        public Factory(SecretKey secretKey) {
            this.secretKey = secretKey;
        }

        @NonNull
        @Override
        public ModelLoader<EncryptedGlideFile, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new EncryptedFileModelLoader(secretKey);
        }

        @Override
        public void teardown() {
            // no-op
        }
    }
}
