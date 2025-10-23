package com.arcsoft.arcfacedemo.common;

/**
 * 一些常量设置
 * <p>
 * 双目偏移可在 [参数设置] -> [识别界面适配] 界面中进行设置
 */
public class Constants {

    /**
     * 方式一： 填好APP_ID等参数，进入激活界面激活
     */
    // public static final String APP_ID = "5N7kKLzxPyu4nDTU9XwBnWd3Zg1PATTwBCM7r8aEaDtq";
    // public static final String SDK_KEY = "HmJFBpgZCtzHciLov1dQDnkwwfbNdXGjp96QdPoeaKq8";
    // public static final String ACTIVE_KEY = "U571-11V8-R13E-1VCL";
    public static final String APP_ID = "GmRzWwTgM27MoXy2LbJKwcCcD4c29WnsJrwxRwhdUEoD";
    public static final String SDK_KEY = "GHQDrynWFYxCZbBrbcTG6zAowgxpdpGvUW9VkxbEGM4C";
    public static final String ACTIVE_KEY = "U571-11UZ-Y133-8VPQ";
    // APP_ID：3SeFvofpWtFvzXXjrfBqzMpsvbGMidHftPX5ZnSLuwCF
    // SDK_KEY：BBjX9UYpPT7dBpaKN6Y6wza57u1Q56gZeM4oEzuR6oFM
    // U571-11UZ-Y133-8VPQ
    // U571-11UZ-Y12K-G13Z

    /**
     * 方式二： 在激活界面读取本地配置文件进行激活
     *
     * 配置文件名称，格式如下：
     * APP_ID:XXXXXXXXXXXXX
     * SDK_KEY:XXXXXXXXXXXXXXX
     * ACTIVE_KEY:XXXX-XXXX-XXXX-XXXX
     */
    public static final String ACTIVE_CONFIG_FILE_NAME = "activeConfig.txt";

    /**
     * 注册图所在路径
     */
    public static final String DEFAULT_REGISTER_FACES_DIR = "sdcard/arcfacedemo/register";

    public static final String BASE_VPN = "https://kzqtxzvpn.caacsri.com:9998";
    // public static final String ZERO_USERNAME = "17628486201";
    // public static final String ZERO_PASSWORD = "6822078aA@";
    public static final String ZERO_USERNAME = "LS001";
    public static final String ZERO_PASSWORD = "6822078aA@";

}
