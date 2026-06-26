package com.arcsoft.arcfacedemo.entity;

/**
 * 登录用户基本信息，对应后台用户档案。
 */
public class User {
    /** 用户 ID */
    private String id;
    /** 登录用户名 */
    private String username;
    /** 用户昵称/姓名 */
    private String nickname;
    /** 备注 */
    private String remark;
    /** 所属部门 ID */
    private String orgId;
    /** 所属部门名称 */
    private String orgName;
    /** 所属单位 ID */
    private String companyId;
    /** 所属单位名称 */
    private String companyName;
    /** 手机号 */
    private String mobile;
    /** 头像地址 */
    private String avatar;
    /** 最近登录 IP */
    private String loginIp;
    /** 最近登录时间 */
    private String loginDate;
    /** 账号创建时间 */
    private String createTime;
    /** 身份证号 */
    private String idNo;

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
