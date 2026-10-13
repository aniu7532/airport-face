package com.arcsoft.arcfacedemo;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.arcsoft.arcfacedemo.db.YinchuanAirportDB;
import com.arcsoft.arcfacedemo.entity.LongPassCard;
import com.arcsoft.arcfacedemo.util.InfoStorage;
import com.lzy.okgo.OkGo;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.ResponseBody;
import okio.Buffer;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LongPassSyncRegressionTest {
    @Test
    public void recordFailureIsReportedAndDoesNotStopDatabaseOrPagination() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        YinchuanAirportDB db = Room.inMemoryDatabaseBuilder(context, YinchuanAirportDB.class)
                .allowMainThreadQueries().build();
        CountDownLatch savedPages = new CountDownLatch(2);
        CountDownLatch reports = new CountDownLatch(2);
        CountDownLatch finished = new CountDownLatch(1);
        List<String> pages = Collections.synchronizedList(new ArrayList<>());
        List<String> reportBodies = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger pageTwoAttempts = new AtomicInteger();
        ArcFaceApplication application = new ArcFaceApplication() {
            @Override
            public File getExternalFilesDir(String type) {
                // Inject a per-record processing exception without accessing files or downloading photos.
                throw new IllegalStateException("injected unavailable picture directory");
            }

            @Override
            public void updateFace(List<LongPassCard> cards) {
                // The real database write has completed; face SDK/JNI is outside this regression.
                savedPages.countDown();
            }
        };
        Field databaseField = ArcFaceApplication.class.getDeclaredField("db");
        databaseField.setAccessible(true);
        databaseField.set(application, db);
        application.infoStorage = new InfoStorage(context) {
            @Override
            public int getInt(String key, int defaultValue) {
                return "interval".equals(key) ? 0 : defaultValue;
            }
        };
        OkHttpClient originalClient = OkGo.getInstance().getOkHttpClient();
        boolean originalTestMode = ArcFaceApplication.TEST;
        Field updateNext = ArcFaceApplication.class.getDeclaredField("updateNext");
        updateNext.setAccessible(true);
        boolean originalUpdateNext = updateNext.getBoolean(null);
        OkHttpClient client = originalClient.newBuilder().addInterceptor(chain -> {
            String json;
            int status = 200;
            if (chain.request().url().encodedPath().endsWith("/page-pass")) {
                String page = chain.request().url().queryParameter("pageNo");
                pages.add(page);
                assertEquals("2026-10-01 00:00:00", chain.request().url().queryParameter("startDate"));
                assertEquals("2026-10-13 17:00:00", chain.request().url().queryParameter("endDate"));
                if ("1".equals(page)) {
                    json = "{\"code\":200,\"data\":{\"list\":[null,{\"id\":\" \"},"
                            + "{\"id\":\"first\",\"status\":1},{\"id\":\"after-error\",\"status\":1}]}}";
                } else if ("2".equals(page) && pageTwoAttempts.getAndIncrement() == 0) {
                    status = 503;
                    json = "{}";
                } else if ("2".equals(page)) {
                    json = "{\"code\":200,\"data\":{\"list\":[{\"id\":\"next-page\",\"status\":1}]}}";
                } else {
                    assertEquals("3", page);
                    json = "{\"code\":200,\"data\":{\"list\":[]}}";
                    finished.countDown();
                }
            } else if (chain.request().url().encodedPath().endsWith("/check-abnormal/create")) {
                Buffer body = new Buffer();
                assertNotNull(chain.request().body());
                chain.request().body().writeTo(body);
                reportBodies.add(body.readUtf8());
                reports.countDown();
                json = "{\"code\":200,\"data\":\"ok\"}";
            } else {
                // Block any unrelated background request from reaching a real server.
                json = "{\"code\":200,\"data\":null}";
            }
            return new okhttp3.Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                    .code(status).message("test fixture")
                    .body(ResponseBody.create(MediaType.parse("application/json"), json)).build();
        }).build();
        boolean completed = false;
        try {
            ArcFaceApplication.TEST = false;
            updateNext.setBoolean(null, false);
            OkGo.getInstance().setOkHttpClient(client);
            Map<String, String> params = new HashMap<>();
            params.put("pageNo", "1");
            params.put("pageSize", "20");
            params.put("startDate", "2026-10-01 00:00:00");
            params.put("endDate", "2026-10-13 17:00:00");
            Method fetch = ArcFaceApplication.class.getDeclaredMethod("fetchNextPage", Map.class);
            fetch.setAccessible(true);
            fetch.invoke(application, params);
            assertTrue("all pages must finish", finished.await(10, TimeUnit.SECONDS));
            assertTrue("valid records must reach the database", savedPages.await(10, TimeUnit.SECONDS));
            assertTrue("record errors must be submitted", reports.await(10, TimeUnit.SECONDS));
            completed = true;
            assertEquals(Arrays.asList("1", "2", "2", "3"), pages);
            assertEquals(3, db.longTermPassDao().getCount());
            assertNotNull(db.longTermPassDao().getById("first"));
            assertNotNull(db.longTermPassDao().getById("after-error"));
            assertNotNull(db.longTermPassDao().getById("next-page"));
            String allReports = reportBodies.toString();
            assertTrue(allReports.contains("first"));
            assertTrue(allReports.contains("after-error"));
            assertTrue(allReports.contains("next-page"));
            assertTrue(allReports.contains("图片处理异常"));
            assertFalse("missing IDs must not be reported", allReports.contains("\"id\":\" \""));
        } finally {
            // On timeout, keep interception and the database alive until process exit for pending tasks.
            if (completed) {
                OkGo.getInstance().setOkHttpClient(originalClient);
                ArcFaceApplication.TEST = originalTestMode;
                updateNext.setBoolean(null, originalUpdateNext);
                db.close();
            }
        }
    }
}
