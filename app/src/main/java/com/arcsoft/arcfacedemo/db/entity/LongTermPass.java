package com.arcsoft.arcfacedemo.db.entity;

import java.lang.reflect.Type;
import java.util.Arrays;

import com.arcsoft.arcfacedemo.entity.LeadingPeople;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "long_term_pass")
public class LongTermPass {
    @PrimaryKey
    @NonNull
    public String id;
    public String applyId;
    public String idCode;
    public String cardId;
    public int score;
    public int status;
    public int type;
    public String userId;
    public String companyId;
    public String orgId;
    public String nickname;
    public String companyName;
    public String orgName;
    public String expiryDate;
    public String[] areaRootIds;
    public String[] areaRootCodes;
    public String[] areaIds;
    public String[] areaCodes;
    public String startDate;
    public String leadingPeople;
    public String photo;
    public byte[] photoBytes;
    public String[] leadingPeopleId;
    public String idNo;
    public String checkPhoto;
    public byte[] checkPhotoBytes;
    public String unitName;
    public int templateType;// 证件模板类型,1:蓝，2：黄
    public boolean isBlacklist;
    public boolean isWithhold;
    public boolean isWithdraw;
    public String withholdStartDate;
    public String withholdEndDate;
    public String cardIdLong;
    public String[] areaDisplayCode;// 通行区域展示Code
    public String businessScope;
    public int sex;
    public String updateTime;
    // 数组类型暂时先按字符串数组定义，后续添加类型转换器处理util.Converters

    // 类型转换器：将 SomeObject[] 转换为 JSON 字符串
    public void setleadingPeople(LeadingPeople[] someObjectArray) {
        Gson gson = new Gson();
        this.leadingPeople = gson.toJson(someObjectArray);
    }

    // 类型转换器：将 JSON 字符串转换为 SomeObject[]
    public LeadingPeople[] getLeadingPeople() {
        Gson gson = new Gson();
        Type type = new TypeToken<LeadingPeople[]>() {
        }.getType();
        return gson.fromJson(leadingPeople, type);
    }

    @Override
    public String toString() {
        return "LongTermPass{" + "id='" + id + '\'' + ", applyId='" + applyId + '\'' + ", idCode='" + idCode + '\''
                + ", cardId='" + cardId + '\'' + ", score=" + score + ", status=" + status + ", type=" + type
                + ", userId='" + userId + "\\\n" + ", companyId='" + companyId + '\'' + ", orgId='" + orgId + '\''
                + ", nickname='" + nickname + '\'' + ", companyName='" + companyName + "\\\n" + ", orgName='" + orgName
                + '\'' + ", expiryDate='" + expiryDate + '\'' + ", areaRootIds=" + Arrays.toString(areaRootIds)
                + ", areaRootCodes=" + Arrays.toString(areaRootCodes) + ", areaIds=" + Arrays.toString(areaIds)
                + ", areaCodes=" + Arrays.toString(areaCodes) + ", startDate='" + startDate + '\'' + ", leadingPeople='"
                + leadingPeople + "\\\n" + ", photo='" + photo + '\'' + ", photoBytes=" + Arrays.toString(photoBytes)
                + ", leadingPeopleId=" + Arrays.toString(leadingPeopleId) + ", idNo='" + idNo + "\\\n"
                + ", checkPhoto='" + checkPhoto + '\'' + ", checkPhotoBytes=" + Arrays.toString(checkPhotoBytes)
                + ", unitName='" + unitName + '\'' + ", templateType=" + templateType + ", isBlacklist=" + isBlacklist
                + ", isWithhold=" + isWithhold + ", isWithdraw=" + isWithdraw + ", withholdStartDate='"
                + withholdStartDate + "\\\n" + ", withholdEndDate='" + withholdEndDate + "\\\n" + ", cardIdLong='"
                + cardIdLong + '\'' + ", areaDisplayCode=" + Arrays.toString(areaDisplayCode) + ", businessScope='"
                + businessScope + "\\\n" + ", sex=" + sex + ", updateTime='" + updateTime + '\'' + '}';
    }
}
