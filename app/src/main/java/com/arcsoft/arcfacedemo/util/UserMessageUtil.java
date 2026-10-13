package com.arcsoft.arcfacedemo.util;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import javax.net.ssl.SSLException;

/** 用户错误提示的中文转换；原始异常仅用于诊断日志。 */
public final class UserMessageUtil {
    private static final Pattern CHINESE = Pattern.compile("[\\u3400-\\u9fff]");
    private static final Pattern ENGLISH = Pattern.compile("[A-Za-z]");

    private UserMessageUtil() {
    }

    public static String message(String detail, String fallback) {
        if (detail == null || detail.trim().isEmpty()) {
            return fallback;
        }
        detail = detail.trim();
        int separator = detail.indexOf('：');
        if (separator < 0) {
            separator = detail.indexOf(':');
        }
        String reason = knownMessage(detail);
        if (reason != null) {
            if (separator > 0 && CHINESE.matcher(detail.substring(0, separator)).find()) {
                return detail.substring(0, separator) + "：" + reason;
            }
            return reason;
        }
        if (separator > 0) {
            String prefix = detail.substring(0, separator);
            String tail = detail.substring(separator + 1).trim();
            if (isErrorPrefix(prefix) && ENGLISH.matcher(tail).find() && !CHINESE.matcher(tail).find()
                    && !tail.matches("(?i)HTTP\\s*\\d{3}(\\s*/\\s*aTrust)?") && !tail.equals("aTrust")) {
                return prefix.equals(fallback) ? prefix : prefix + "：" + fallback;
            }
        }
        return CHINESE.matcher(detail).find() ? detail : fallback;
    }

    public static String message(Throwable error, String fallback) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        String preserved = fallback;
        while (error != null && visited.add(error)) {
            if (error instanceof SocketTimeoutException) {
                return "网络连接超时";
            }
            if (error instanceof UnknownHostException) {
                return "无法连接服务器";
            }
            if (error instanceof NoRouteToHostException) {
                return "当前无网络连接";
            }
            if (error instanceof SSLException) {
                return "安全连接失败，请联系管理员";
            }
            if (error instanceof ConnectException) {
                return "网络连接失败，请检查网络";
            }
            String translated = message(error.getMessage(), fallback);
            if (error.getMessage() != null && knownMessage(error.getMessage()) != null) {
                return translated;
            }
            if (fallback.equals(preserved) && !fallback.equals(translated)) {
                preserved = translated;
            }
            error = error.getCause();
        }
        return preserved;
    }

    public static String failure(String fallback, String detail) {
        return withReason(fallback, message(detail, fallback));
    }

    public static String failure(String fallback, Throwable error) {
        return withReason(fallback, message(error, fallback));
    }

    private static String withReason(String fallback, String reason) {
        return fallback.equals(reason) || reason.startsWith(fallback + "：") ? reason : fallback + "：" + reason;
    }

    private static String knownMessage(String detail) {
        if (CHINESE.matcher(detail).find()) {
            int separator = detail.indexOf('：');
            if (separator < 0) {
                separator = detail.indexOf(':');
            }
            if (separator < 0 || !isErrorPrefix(detail.substring(0, separator))) {
                return null;
            }
            detail = detail.substring(separator + 1).trim();
        }
        String lower = detail.toLowerCase(Locale.ROOT);
        if (lower.contains("username") && lower.contains("password")
                && (lower.contains("invalid") || lower.contains("incorrect"))) {
            return "用户名或密码错误";
        }
        if (lower.contains("no internet connection") || lower.contains("no network")
                || lower.contains("network is unreachable") || lower.contains("network unavailable")) {
            return "当前无网络连接";
        }
        if (lower.contains("timeout") || lower.contains("timed out")) {
            return "网络连接超时";
        }
        if (lower.contains("unable to resolve host") || lower.contains("unknownhost")
                || lower.contains("could not resolve host")) {
            return "无法连接服务器";
        }
        if (lower.contains("ssl") || lower.contains("handshake") || lower.contains("certificate")) {
            return "安全连接失败，请联系管理员";
        }
        if (lower.contains("failed to connect") || lower.contains("connection")
                || lower.contains("connectexception") || lower.contains("econnrefused")) {
            return "网络连接失败，请检查网络";
        }
        if (lower.contains("network failure") || lower.contains("network error")) {
            return "网络请求失败，请稍后重试";
        }
        return null;
    }

    private static boolean isErrorPrefix(String prefix) {
        return prefix.contains("失败") || prefix.contains("错误") || prefix.contains("异常");
    }

    /** 数值来自当前 Android Sangfor SDK，不与其他端同值错误码混用。 */
    public static String sdkFailure(String fallback, long code, String detail) {
        String reason;
        switch ((int) code) {
            case 1101:
            case 75500000: reason = "用户名或密码错误"; break;
            case 1102: reason = "账号已过期"; break;
            case 1103: reason = "当前账号已失效"; break;
            case 1104: reason = "用户登录超时，请重新登录"; break;
            case 1105:
            case 1402: reason = "验证码输入错误"; break;
            case 1405: reason = "验证码已过期，请重新获取"; break;
            case 1002: reason = "当前时间不允许登录"; break;
            case 1008: reason = "账号已锁定，请联系管理员"; break;
            case 1011: reason = "登录会话已过期，请重新登录"; break;
            case 7:
            case 302: reason = "网络连接失败，请检查网络"; break;
            case 28:
            case 2008: reason = "网络连接超时"; break;
            case 35: reason = "安全连接失败，请联系管理员"; break;
            case 2003: reason = "无法连接服务器"; break;
            case 2006: reason = "当前无网络连接"; break;
            case 3004: reason = "旧密码错误"; break;
            case 3008: reason = "新密码不能与旧密码相同"; break;
            default: reason = message(detail, fallback); break;
        }
        return withReason(fallback, reason) + "（错误码：" + code + "）";
    }
}
