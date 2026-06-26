package com.arcsoft.arcfacedemo.entity;

/**
 * 登录接口返回的令牌信息，用于后续接口鉴权。
 */
public class Login {
    /** 用户 ID */
    private String userId;
    /** 访问令牌 */
    private String accessToken;
    /** 刷新令牌 */
    private String refreshToken;
    /** 令牌过期时间 */
    private String expiresTime;

    /** 构造登录令牌对象 */
    public Login(String userId, String accessToken, String refreshToken, String expiresTime) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresTime = expiresTime;
    }

    public String getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getExpiresTime() {
        return expiresTime;
    }
}
