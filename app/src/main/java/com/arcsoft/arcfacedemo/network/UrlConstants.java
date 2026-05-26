package com.arcsoft.arcfacedemo.network;

public class UrlConstants {
    // 二所测试环境：https://inc-kzqtxz.caacsri.com
    // 二所正式环境：https://inckzqtxz.caacsri.com
//     public static final String URL = "https://inc-kzqtxz.caacsri.com";// 二所测试环境
    public static final String URL = "https://inckzqtxz.caacsri.com";// 二所正式环境
//     public static final String URL = "http://test.sczhbf.com:58088";//慧能测试环境
     public static final String Test_URL = "http://test.sczhbf.com:58088";//慧能测试环境
    public static final String URL_TOKEN = "";
    public static final String URL_ClIENTID = "VERTICAL";
    public static final String URL_LOGIN = URL + "/app-api/system/auth/vertical-client-login";// 登录
    public static final String URL_GETCHECKMETH = URL + "/app-api/check/device/check-method";// 获取验证方式
    public static final String URL_GetLongPass = URL + "/app-api/check/pass/page-pass";// 获取通信证
    public static final String URL_GET_MAC_DETAIL = URL + "/app-api/check/device/detail-mac";// 获取设备信息
    public static final String URL_GETCONFIGINFO = URL + "/app-api/check/configInfo/get";// 获取配置信息
    public static final String URL_refresh_token = URL + "/app-api/system/auth/refresh-token";// 刷新token

    // 创建长期记录
    public static final String URL_CREATE_LONG_RECORD = URL + "/app-api/check/record/create-long";
    // 创建临时记录
    public static final String URL_CREATE_TEMP_RECORD = URL + "/app-api/check/record/create-temporary";
    // 获得用户详情
    public static final String URL_GET_USER_DETAIL = URL + "/app-api/check/user/get";
    // 上传文件
    public static final String URL_UPLOAD_FILE = URL + "/app-api/infra/file/upload-encrypt-url";

    // 获取系统时间
    public static final String URL_GET_SYSTEM_TIME = URL + "/app-api/check/configInfo/sync-time";

    // 分页加载通行记录
    public static final String URL_GET_RESORD_PAGE = URL + "/app-api/check/record/page";
    public static final String URL_GET_APP_LAST_VERSION = URL + "/app-api/system/appVersion/get-lastVersion";

    public static final String heartbeat = URL + "/app-api/check/device/heartbeat";

    //获取通行证总数
    public static final String passCount = URL + "/app-api/check/pass/pass-count";
    //存储没注册成功的通行证
    public static final String checkAbnormalCreate = URL + "/app-api/check/check-abnormal/create";

    // 施工人员有进无出信息
    public static final String checkRecordPageNeedVerifyNoOut = URL + "/app-api/check/record/page-need-verify-no-out";
    // 施工人员通行证记录
    public static final String checkRecordPageNeedVerify = URL + "/app-api/check/record/page-need-verify";
    // 施工人员每日进入统计
    public static final String checkRecordStatisticNeedVerify = URL + "/app-api/check/record/statistic-need-verify";
    // 获得所有管制区域树形详情
    public static final String checkAreaGetDetailChannelTree = URL + "/app-api/check/area/get-detail-channel-tree";
    // 获取设备列表
    public static final String checkDeviceList = URL + "/app-api/check/device/list";
    // 核实
    public static final String checkRecordVerify = URL + "/app-api/check/record/verify";
    // 申办单位简易列表
    public static final String checkUnitSimpleList = URL + "/app-api/check/unit/simpleList";
}
