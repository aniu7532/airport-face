package com.arcsoft.arcfacedemo.config;

/**
 * 银川河东渠道配置（仅 yinchuan 风味源码目录）。
 */
public final class ChannelConfig {

//    public static final String BASE_URL = "https://inc-kzqtxz.caacsri.com";// 二所测试环境
    public static final String BASE_URL = "https://inckzqtxz.caacsri.com";// 二所正式环境
//    public static final String BASE_URL = "http://test.sczhbf.com:58088";//慧能测试环境
    /** 无租户路径前缀，接口为 域名/app-api/... */
    public static final String TENANT_PREFIX = "";
    public static final String TENANT_ID = "1";

    private ChannelConfig() {
    }
}
