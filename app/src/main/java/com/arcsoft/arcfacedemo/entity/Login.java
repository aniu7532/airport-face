package com.arcsoft.arcfacedemo.entity;

public class Login {
    private String userId;
    private String accessToken;
    private String refreshToken;
    private String expiresTime;

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
