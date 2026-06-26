package com.arcsoft.arcfacedemo.config;

/**
 * 石河子花园机场渠道配置（仅 shihezi 风味源码目录）。
 * <p>
 * 业务接口：{@code BASE_URL}/{@link #TENANT_PREFIX}/app-api/...；
 * system 接口：{@code BASE_URL}/app-api/system/...（不带租户路径前缀）。
 */
public final class ChannelConfig {

    public static final String BASE_URL_Test0 = "https://txzcloudservice.caacsri.com";
    public static final String BASE_URL = "https://txzcloudservice.caacsri.com";
    public static final String BASE_URL_Test1 = "https://txzcloudservice.caacsri.com";
    /** 租户路径前缀，拼在域名与 app-api 之间 */
    public static final String TENANT_PREFIX = "shf";
    public static final String TENANT_ID = "1";
    /** 是否支持临时通行证业务 */
    public static final boolean SUPPORTS_TEMPORARY_PASS = true;

    private ChannelConfig() {
    }
}
