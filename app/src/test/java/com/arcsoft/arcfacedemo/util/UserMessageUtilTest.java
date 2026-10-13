package com.arcsoft.arcfacedemo.util;

import org.junit.Test;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import static org.junit.Assert.assertEquals;

public class UserMessageUtilTest {
    private static final String FALLBACK = "登录失败";

    @Test
    public void networkErrors_areTranslatedIncludingWrappedCauses() {
        assertEquals("当前无网络连接", UserMessageUtil.message("No internet connection", FALLBACK));
        assertEquals("网络连接超时", UserMessageUtil.message("timeout", FALLBACK));
        assertEquals("无法连接服务器", UserMessageUtil.message("Unable to resolve host api.example.com", FALLBACK));
        assertEquals("网络请求失败，请稍后重试", UserMessageUtil.message("Network failure: unexpected EOF", FALLBACK));
        assertEquals("网络连接超时", UserMessageUtil.message(new RuntimeException("request failed", new SocketTimeoutException()), FALLBACK));
        assertEquals("无法连接服务器", UserMessageUtil.message(new UnknownHostException(), FALLBACK));
        assertEquals("当前无网络连接", UserMessageUtil.message(new NoRouteToHostException(), FALLBACK));
        assertEquals("网络连接失败，请检查网络", UserMessageUtil.message(new ConnectException(), FALLBACK));
        assertEquals("安全连接失败，请联系管理员", UserMessageUtil.message(new SSLException("handshake failed"), FALLBACK));
    }

    @Test
    public void businessDetails_arePreservedAndEnglishDetailsUseFallback() {
        assertEquals(FALLBACK, UserMessageUtil.message((String) null, FALLBACK));
        assertEquals(FALLBACK, UserMessageUtil.message("  ", FALLBACK));
        assertEquals(FALLBACK, UserMessageUtil.message((Throwable) null, FALLBACK));
        assertEquals(FALLBACK, UserMessageUtil.message("Unexpected internal failure", FALLBACK));
        assertEquals(FALLBACK, UserMessageUtil.failure(FALLBACK, new IllegalStateException("bad state")));
        assertEquals("用户名或密码错误", UserMessageUtil.message(" 用户名或密码错误 ", FALLBACK));
        assertEquals("登录失败：网络连接超时",
                UserMessageUtil.message("登录失败：timeout 已超时", FALLBACK));
        assertEquals("连接失败：无法连接服务器", UserMessageUtil.message("连接失败：Unable to resolve host api.example.com", FALLBACK));
        String business = "设备 ABC123 未授权，用户 Alice 请联系管理员";
        assertEquals(business, UserMessageUtil.message(business, FALLBACK));
        String gateway = "后台登录失败：HTTP 403，aTrust 网关暂未放通，请稍后重试";
        assertEquals(gateway, UserMessageUtil.message(gateway, FALLBACK));
        assertEquals("登录失败：网络连接超时", UserMessageUtil.failure(FALLBACK, "timeout"));
    }

    @Test
    public void cyclicCauses_doNotLoop() {
        Throwable first = new Exception("first");
        Throwable second = new Exception("second", first);
        first.initCause(second);
        assertEquals(FALLBACK, UserMessageUtil.message(first, FALLBACK));
    }

    @Test
    public void prefixedEnglishFailures_areTranslatedWithoutRemovingBusinessIdentifiers() {
        String detail = "登录失败：Invalid username or password";
        assertEquals("登录失败：用户名或密码错误", UserMessageUtil.message(detail, "操作失败，请重试"));
        assertEquals("登录失败：用户名或密码错误", UserMessageUtil.failure(FALLBACK, detail));
        assertEquals("登录失败：操作失败，请重试",
                UserMessageUtil.message("登录失败：Unexpected internal failure", "操作失败，请重试"));
        assertEquals("用户名：Alice", UserMessageUtil.message("用户名：Alice", FALLBACK));
        assertEquals("设备：ABC123", UserMessageUtil.message("设备：ABC123", FALLBACK));
        assertEquals("登录失败：HTTP 403/aTrust", UserMessageUtil.message("登录失败：HTTP 403/aTrust", FALLBACK));
        assertEquals("网关：aTrust", UserMessageUtil.message("网关：aTrust", FALLBACK));
        assertEquals("错误码：1101", UserMessageUtil.message("错误码：1101", FALLBACK));
        assertEquals("登录失败：403", UserMessageUtil.message("登录失败：403", FALLBACK));
        assertEquals("错误码：-1, 7", UserMessageUtil.message("错误码：-1, 7", FALLBACK));
        String identifiers = "设备 SSL-001 未授权，用户 Timeout";
        assertEquals(identifiers, UserMessageUtil.message(identifiers, FALLBACK));
        assertEquals("用户名：Connection", UserMessageUtil.message("用户名：Connection", FALLBACK));
        assertEquals(identifiers, UserMessageUtil.message(new Exception(identifiers), FALLBACK));
        assertEquals("用户名：Connection", UserMessageUtil.message(new Exception("用户名：Connection"), FALLBACK));
    }

    @Test
    public void chineseWrapper_doesNotHideNetworkCause() {
        assertEquals("网络连接超时", UserMessageUtil.message(new RuntimeException("登录失败", new SocketTimeoutException()), FALLBACK));
        assertEquals("无法连接服务器", UserMessageUtil.message(new RuntimeException("请求异常", new UnknownHostException()), FALLBACK));
        assertEquals("网络连接超时", UserMessageUtil.message(new RuntimeException("请求失败", new Exception("timeout")), FALLBACK));
        assertEquals("账号未授权", UserMessageUtil.message(new RuntimeException("账号未授权", new Exception("unknown failure")), FALLBACK));
    }

    @Test
    public void sdkCodes_useAndroidValuesAndKeepUnknownDiagnosticCode() {
        assertSdk(1101, "用户名或密码错误");
        assertSdk(75500000, "用户名或密码错误");
        assertSdk(1002, "当前时间不允许登录");
        assertSdk(1102, "账号已过期");
        assertSdk(1103, "当前账号已失效");
        assertSdk(1105, "验证码输入错误");
        assertSdk(1405, "验证码已过期，请重新获取");
        assertSdk(7, "网络连接失败，请检查网络");
        assertSdk(28, "网络连接超时");
        assertSdk(35, "安全连接失败，请联系管理员");
        assertSdk(302, "网络连接失败，请检查网络");
        assertSdk(2003, "无法连接服务器");
        assertSdk(2006, "当前无网络连接");
        assertSdk(2008, "网络连接超时");
        assertEquals("登录失败（错误码：999999）", UserMessageUtil.sdkFailure(FALLBACK, 999999, "unknown error"));
        assertEquals("登录失败：账号未授权（错误码：999999）", UserMessageUtil.sdkFailure(FALLBACK, 999999, "账号未授权"));
    }

    private static void assertSdk(int code, String reason) {
        assertEquals("登录失败：" + reason + "（错误码：" + code + "）",
                UserMessageUtil.sdkFailure(FALLBACK, code, "raw English SDK error"));
    }
}
