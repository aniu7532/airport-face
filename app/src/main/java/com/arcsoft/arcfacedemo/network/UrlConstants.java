package com.arcsoft.arcfacedemo.network;

import com.arcsoft.arcfacedemo.config.ChannelConfig;

public class UrlConstants {
    /** 各渠道在 src/{flavor}/java/.../config/ChannelConfig 中配置 BASE_URL */
    public static final String URL = ChannelConfig.BASE_URL;
    /** 请求头 tenant-id，与 ChannelConfig.TENANT_ID 一致 */
    public static final String TENANT_ID = ChannelConfig.TENANT_ID;
    public static final String Test_URL = "http://test.sczhbf.com:58088";//慧能测试环境
    public static final String URL_TOKEN = "";
    public static final String URL_ClIENTID = "VERTICAL";

    /** system 接口：域名/app-api/system（不带租户路径前缀） */
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

    /** 加密文件流地址（infra，走业务 app-api 路径） */
    public static String fileStreamUrl(String path) {
        return businessAppApiBase() + "/infra/file/stream?path=" + path;
    }

    public static final String URL_LOGIN = SYSTEM_API + "/auth/vertical-client-login";// 登录
    public static final String URL_GETCHECKMETH = businessAppApiBase() + "/check/device/check-method";// 获取验证方式
    public static final String URL_GetLongPass = businessAppApiBase() + "/check/pass/page-pass";// 获取通信证
    public static final String URL_GET_MAC_DETAIL = businessAppApiBase() + "/check/device/detail-mac";// 获取设备信息
    public static final String URL_GETCONFIGINFO = businessAppApiBase() + "/check/configInfo/get";// 获取配置信息
    public static final String URL_refresh_token = SYSTEM_API + "/auth/refresh-token";// 刷新token

    // 创建长期记录
    public static final String URL_CREATE_LONG_RECORD = businessAppApiBase() + "/check/record/create-long";
    // 创建临时记录
    public static final String URL_CREATE_TEMP_RECORD = businessAppApiBase() + "/check/record/create-temporary";
    // 获得用户详情
    public static final String URL_GET_USER_DETAIL = businessAppApiBase() + "/check/user/get";
    // 上传文件
    public static final String URL_UPLOAD_FILE = businessAppApiBase() + "/infra/file/upload-encrypt-url";

    // 获取系统时间
    public static final String URL_GET_SYSTEM_TIME = businessAppApiBase() + "/check/configInfo/sync-time";

    // 分页加载通行记录
    public static final String URL_GET_RESORD_PAGE = businessAppApiBase() + "/check/record/page";
    public static final String URL_GET_APP_LAST_VERSION = SYSTEM_API + "/appVersion/get-lastVersion";

    public static final String heartbeat = businessAppApiBase() + "/check/device/heartbeat";

    //获取通行证总数
    public static final String passCount = businessAppApiBase() + "/check/pass/pass-count";
    //存储没注册成功的通行证
    public static final String checkAbnormalCreate = businessAppApiBase() + "/check/check-abnormal/create";

    // 施工人员有进无出信息
    public static final String checkRecordPageNeedVerifyNoOut = businessAppApiBase() + "/check/record/page-need-verify-no-out";
    // 施工人员通行证记录
    public static final String checkRecordPageNeedVerify = businessAppApiBase() + "/check/record/page-need-verify";
    // 施工人员每日进入统计
    public static final String checkRecordStatisticNeedVerify = businessAppApiBase() + "/check/record/statistic-need-verify";
    // 获得所有管制区域树形详情
    public static final String checkAreaGetDetailChannelTree = businessAppApiBase() + "/check/area/get-detail-channel-tree";
    // 获取设备列表
    public static final String checkDeviceList = businessAppApiBase() + "/check/device/list";
    // 核实
    public static final String checkRecordVerify = businessAppApiBase() + "/check/record/verify";
    // 申办单位简易列表
    public static final String checkUnitSimpleList = businessAppApiBase() + "/check/unit/simpleList";
}
