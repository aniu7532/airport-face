package com.arcsoft.arcfacedemo.network;

public class UrlConstants {
    // 二所测试环境：https://inc-kzqtxz.caacsri.com
    // 二所正式环境：https://inckzqtxz.caacsri.com
    // public static final String URL = "https://inc-kzqtxz.caacsri.com";// 二所测试环境
    public static final String URL = "https://inckzqtxz.caacsri.com";// 二所正式环境
//     public static final String URL = "http://test.sczhbf.com:58088";//慧能测试环境
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
    public static final String URL_UPLOAD_FILE = URL + "/admin-api/infra/file/upload";

    // 获取系统时间
    public static final String URL_GET_SYSTEM_TIME = URL + "/app-api/check/configInfo/sync-time";

    // 分页加载通行记录
    public static final String URL_GET_RESORD_PAGE = URL + "/app-api/check/record/page";
    public static final String URL_GET_APP_LAST_VERSION = URL + "/app-api/system/appVersion/get-lastVersion";

    public static final String heartbeat = URL + "/app-api/check/device/heartbeat";

}
