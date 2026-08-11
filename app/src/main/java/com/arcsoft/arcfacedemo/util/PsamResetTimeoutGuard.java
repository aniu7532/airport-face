package com.arcsoft.arcfacedemo.util;

import com.arcsoft.arcfacedemo.util.log.ALog;
import com.decard.NDKMethod.BasicOper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PsamResetTimeoutGuard {
    public static final long DEFAULT_RESET_TIMEOUT_MS = 1000L;

    private static final AtomicBoolean RESET_RUNNING = new AtomicBoolean(false);
    private static final ExecutorService RESET_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "PsamResetTimeoutGuard");
        }
    });

    private PsamResetTimeoutGuard() {
    }

    public static String dcCpuResetHex() {
        return dcCpuResetHex(DEFAULT_RESET_TIMEOUT_MS);
    }

    public static String dcCpuResetHex(long timeoutMs) {
        if (!RESET_RUNNING.compareAndSet(false, true)) {
            ALog.w("PSAM复位仍在执行，跳过本轮短距读卡");
            return null;
        }

        Future<String> future = RESET_EXECUTOR.submit(new Callable<String>() {
            @Override
            public String call() {
                try {
                    return BasicOper.dc_cpureset_hex();
                } finally {
                    RESET_RUNNING.set(false);
                }
            }
        });

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            ALog.e("PSAM复位超过" + timeoutMs + "ms，跳过本轮短距读卡");
            future.cancel(true);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ALog.e("PSAM复位等待被中断");
            future.cancel(true);
            return null;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            ALog.e("PSAM复位异常: " + (cause == null ? e.getMessage() : cause.getMessage()));
            return null;
        }
    }
}
