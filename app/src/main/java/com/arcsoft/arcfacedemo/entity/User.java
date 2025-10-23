package com.arcsoft.arcfacedemo.entity;

public class User {
    private String id;
    private String username;
    private String nickname;
    private String remark;
    private String orgId;
    private String orgName;
    private String companyId;
    private String companyName;
    private String mobile;
    private String avatar;
    private String loginIp;
    private String loginDate;
    private String createTime;
    private String idNo;

    // 提供 Getter 方法，方便后续获取属性值
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getRemark() {
        return remark;
    }

    public String getOrgId() {
        return orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getMobile() {
        return mobile;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public String getLoginDate() {
        return loginDate;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getIdNo() {
        return idNo;
    }
}
