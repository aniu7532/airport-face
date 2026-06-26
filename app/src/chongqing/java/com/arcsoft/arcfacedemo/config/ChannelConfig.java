package com.arcsoft.arcfacedemo.config;

/**
 * 重庆江北渠道配置（仅 chongqing 风味源码目录）。
 */
public final class ChannelConfig {

    /** 测试环境 API 域名 */
    public static final String BASE_URL_Test0 = "https://caq-kzqtxz.caacsri.com";// 测试环境
    /** 正式环境 API 域名 */
    public static final String BASE_URL = "https://cqakzqtxz.caacsri.com";// 正式环境
    public static final String BASE_URL_Test1 = "http://test.sczhbf.com:58088";//慧能测试环境
    /** 无租户路径前缀，接口为 域名/app-api/... */
    public static final String TENANT_PREFIX = "";
    public static final String TENANT_ID = "3";
    /** 是否支持临时通行证业务 */
    public static final boolean SUPPORTS_TEMPORARY_PASS = true;

    private ChannelConfig() {
    }
}
