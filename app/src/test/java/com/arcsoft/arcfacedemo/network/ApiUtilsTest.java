package com.arcsoft.arcfacedemo.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.model.Response;

import org.junit.Test;

import okhttp3.Protocol;
import okhttp3.Request;

public class ApiUtilsTest {
    @Test
    public void gateway403GoesToFailure() {
        RecordingCallback callback = new RecordingCallback();
        ApiUtils.dispatchResponse(response(403, "<html>403 Forbidden</html>"), callback);

        assertNull(callback.body);
        assertTrue(callback.error instanceof HttpException);
        assertEquals(403, ((HttpException) callback.error).code());
    }

    @Test
    public void http200GoesToSuccess() {
        RecordingCallback callback = new RecordingCallback();
        ApiUtils.dispatchResponse(response(200, "{\"code\":200}"), callback);

        assertEquals("{\"code\":200}", callback.body);
        assertNull(callback.error);
    }

    private static Response<String> response(int status, String body) {
        okhttp3.Response raw = new okhttp3.Response.Builder()
                .request(new Request.Builder().url("https://example.test/login").build())
                .protocol(Protocol.HTTP_1_1)
                .code(status)
                .message(status == 200 ? "OK" : "Forbidden")
                .build();
        return Response.success(false, body, null, raw);
    }

    private static class RecordingCallback implements ApiUtils.ApiCallback {
        String body;
        Throwable error;

        @Override
        public void onSuccess(String response) {
            body = response;
        }

        @Override
        public void onFailure(Throwable e) {
            error = e;
        }
    }
}
