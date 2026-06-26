package com.arcsoft.arcfacedemo.entity;

/**
 * 引领人信息，C 类证件进出港时需校验引领人身份。
 */
public class LeadingPeople {
    /** 引领人用户 ID */
    public String userId;
    /** 引领人姓名 */
    public String nickname;
    /** 引领人工作单位名称 */
    public String companyName;
    /** 引领人工作单位 ID */
    public String companyId;
    /** 引领人所属部门 ID */
    public String orgId;
    /** 引领人所属部门名称 */
    public String orgName;
    /** 引领人证件编号 */
    public String idCode;
    /** 引领人查验单位名称 */
    public String unitName;

    /** 构造引领人信息 */
    public LeadingPeople(String userId, String nickname, String companyName, String companyId, String orgId, String orgName, String idCode, String unitName) {
        this.userId = userId;
        this.nickname = nickname;
        this.companyName = companyName;
        this.companyId = companyId;
        this.orgId = orgId;
        this.orgName = orgName;
        this.idCode = idCode;
        this.unitName = unitName;
    }
}
