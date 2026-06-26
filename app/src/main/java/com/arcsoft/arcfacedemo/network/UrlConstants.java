package com.arcsoft.arcfacedemo.network;

import com.arcsoft.arcfacedemo.config.ChannelConfig;

/**
 * 接口 URL 常量类，按渠道 {@link ChannelConfig} 拼接业务与系统 API 路径。
 */
public class UrlConstants {
    /** 各渠道在 src/{flavor}/java/.../config/ChannelConfig 中配置 BASE_URL */
    public static final String URL = ChannelConfig.BASE_URL;
    /** 请求头 tenant-id，与 ChannelConfig.TENANT_ID 一致 */
    public static final String TENANT_ID = ChannelConfig.TENANT_ID;
    /** 慧能测试环境域名（联调备用） */
    public static final String Test_URL = "http://test.sczhbf.com:58088";
    /** 预留 Token 字段，当前未使用 */
    public static final String URL_TOKEN = "";
    /** OAuth 客户端标识，登录请求体 clientId */
    public static final String URL_ClIENTID = "VERTICAL";

    /** system 接口根路径：域名/app-api/system（不带租户路径前缀） */
    private static final String SYSTEM_API = URL + "/app-api/system";

    /**
     * 业务接口根路径：无租户时为 域名/app-api；有租户时为 域名/{prefix}/app-api
     */
    private static String businessAppApiBase() {
        String prefix = ChannelConfig.TENANT_PREFIX;
        if (prefix == null || prefix.isEmpty()) {
            return URL + "/app-api";
        }
        return URL + "/" + prefix + "/app-api";
    }

    /**
     * 加密文件流下载地址。
     *
     * @param path 服务端文件 path 参数
     */
    public static String fileStreamUrl(String path) {
        return businessAppApiBase() + "/infra/file/stream?path=" + path;
    }

    /** 竖屏客户端登录 POST {@code /auth/vertical-client-login} */
    public static final String URL_LOGIN = SYSTEM_API + "/auth/vertical-client-login";
    /** 获取设备支持的查验方式 GET */
    public static final String URL_GETCHECKMETH = businessAppApiBase() + "/check/device/check-method";
    /** 通行证分页同步 GET {@code page-pass} */
    public static final String URL_GetLongPass = businessAppApiBase() + "/check/pass/page-pass";
    /** 按 MAC 获取设备详情 GET */
    public static final String URL_GET_MAC_DETAIL = businessAppApiBase() + "/check/device/detail-mac";
    /** 查验配置 GET，query type=5/6 */
    public static final String URL_GETCONFIGINFO = businessAppApiBase() + "/check/configInfo/get";
    /** 刷新 accessToken POST */
    public static final String URL_refresh_token = SYSTEM_API + "/auth/refresh-token";

    /** 上传长期通行记录 POST */
    public static final String URL_CREATE_LONG_RECORD = businessAppApiBase() + "/check/record/create-long";
    /** 上传临时通行记录 POST */
    public static final String URL_CREATE_TEMP_RECORD = businessAppApiBase() + "/check/record/create-temporary";
    /** 获取登录用户详情 GET */
    public static final String URL_GET_USER_DETAIL = businessAppApiBase() + "/check/user/get";
    /** 加密图片上传，返回 URL POST */
    public static final String URL_UPLOAD_FILE = businessAppApiBase() + "/infra/file/upload-encrypt-url";

    /** 同步服务端系统时间 GET */
    public static final String URL_GET_SYSTEM_TIME = businessAppApiBase() + "/check/configInfo/sync-time";

    /** 通行记录分页查询 GET（运维本地记录弹窗等） */
    public static final String URL_GET_RESORD_PAGE = businessAppApiBase() + "/check/record/page";
    /** 获取 App 最新版本 GET */
    public static final String URL_GET_APP_LAST_VERSION = SYSTEM_API + "/appVersion/get-lastVersion";

    /** 设备心跳 GET，携带 mac、interval */
    public static final String heartbeat = businessAppApiBase() + "/check/device/heartbeat";

    /** 通行证总数 GET，用于完整性校验 */
    public static final String passCount = businessAppApiBase() + "/check/pass/pass-count";
    /** 上报注册失败/异常通行证 POST */
    public static final String checkAbnormalCreate = businessAppApiBase() + "/check/check-abnormal/create";

    /** 施工人员：有进无出待核实记录分页 GET */
    public static final String checkRecordPageNeedVerifyNoOut = businessAppApiBase() + "/check/record/page-need-verify-no-out";
    /** 施工人员：待核实通行记录分页 GET */
    public static final String checkRecordPageNeedVerify = businessAppApiBase() + "/check/record/page-need-verify";
    /** 施工人员：每日进出统计 GET */
    public static final String checkRecordStatisticNeedVerify = businessAppApiBase() + "/check/record/statistic-need-verify";
    /** 管制区域树形详情 GET */
    public static final String checkAreaGetDetailChannelTree = businessAppApiBase() + "/check/area/get-detail-channel-tree";
    /** 设备列表 GET */
    public static final String checkDeviceList = businessAppApiBase() + "/check/device/list";
    /** 施工人员核销/核实 POST */
    public static final String checkRecordVerify = businessAppApiBase() + "/check/record/verify";
    /** 申办单位简易列表 GET */
    public static final String checkUnitSimpleList = businessAppApiBase() + "/check/unit/simpleList";
}
