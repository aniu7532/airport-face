package com.arcsoft.arcfacedemo.entity;

public class LeadingPeople {
    public String userId;	//用户id
    public String nickname;	//用户姓名
    public String companyName;	//工作单位
    public String companyId;	//工作单位id
    public String orgId;	//所属部门id
    public String orgName;	//所属部门
    public String idCode;	//证件编码
    public String unitName;

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
