package com.arcsoft.arcfacedemo.db.entity;

import java.lang.reflect.Type;
import java.util.Arrays;

import com.arcsoft.arcfacedemo.entity.LeadingPeople;
import com.arcsoft.arcfacedemo.entity.Records;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 临时证通行记录本地持久化实体，对应表 temporary_card_records。
 */
@Entity(tableName = "temporary_card_records")
public class TemporaryCardRecords extends Records {
    /** 记录 ID，主键 */
    @PrimaryKey()
    @NonNull
    public String id;
    /** 通行证 ID */
    public String passid;
    /** 实体卡号 */
    public String cardId;
    /** 系统证件编号 */
    public String idCode;
    /** 通行申请 ID（二维码扫描信息） */
    public String applyId;
    /** 通行方向，1 进，-1 出，2 核验 */
    public String direction;
    /** 持卡人姓名 */
    public String nickname;
    /** 证件照片 */
    public String photo;
    /** 引领人信息 JSON 字符串 */
    public String leadingPeople;
    /** 查验设备 ID */
    public String deviceId;
    /** 查验设备名称 */
    public String deviceName;
    /** 查验人 ID */
    public String checkUserId;
    /** 查验人姓名 */
    public String checkUserName;

    /** 持卡人所属单位名称 */
    public String companyName;
    /** 证件有效期截止日期 */
    public String expiryDate;
    /** 证件模板类型，1 蓝色，2 黄色 */
    public int templateType;
    /** 通行区域展示编码列表 */
    public String[] areaDisplayCode;

    /** 通行区域 ID */
    public String area;
    /** 通行区域名称（编码+名称） */
    public String areaName;
    /** 通行状态，正常/异常 */
    public String status;
    /** 异常原因 */
    public String reason;
    /** 引领人通行记录 ID */
    public String parentId;
    /** 引领人用户 ID */
    public String leadingPeopleId;
    /** 现场照片 */
    public String sitePhoto;
    /** 查验时间 */
    public String checkTime;
    /** 人脸相似度 */
    public String faceSimilar;
    /** 人脸质量评分 */
    public String faceQuality;

    /** 是否需要人工复核 */
    public Boolean needVerify;

    /** 将引领人数组序列化为 JSON 存入数据库 */
    public void setleadingPeople(LeadingPeople[] someObjectArray) {
        Gson gson = new Gson();
        this.leadingPeople = gson.toJson(someObjectArray);
    }

    /** 从 JSON 反序列化引领人数组 */
    public LeadingPeople[] getLeadingPeople() {
        Gson gson = new Gson();
        Type type = new TypeToken<LeadingPeople[]>() {
        }.getType();
        return gson.fromJson(leadingPeople, type);
    }

    @Override
    public String toString() {
        return "TemporaryCardRecords{" + "id='" + id + '\'' + ", passid='" + passid + '\'' + ", cardId='" + cardId
                + '\'' + ", idCode='" + idCode + '\'' + ", applyId='" + applyId + '\'' + ", direction='" + direction
                + '\'' + ", nickname='" + nickname + '\'' + ", photo='" + photo + '\'' + ", leadingPeople='"
                + leadingPeople + '\'' + ", deviceId='" + deviceId + '\'' + ", deviceName='" + deviceName + '\''
                + ", checkUserId='" + checkUserId + '\'' + ", checkUserName='" + checkUserName + '\''
                + ", companyName='" + companyName + '\'' + ", expiryDate='" + expiryDate + '\'' + ", templateType="
                + templateType + ", areaDisplayCode=" + Arrays.toString(areaDisplayCode) + ", area='" + area + '\''
                + ", areaName='" + areaName + '\'' + ", status='" + status + '\'' + ", reason='" + reason + '\''
                + ", parentId='" + parentId + '\'' + ", leadingPeopleId='" + leadingPeopleId + '\'' + ", sitePhoto='"
                + sitePhoto + '\'' + ", checkTime='" + checkTime + '\'' + ", faceSimilar='" + faceSimilar + '\''
                + ", faceQuality='" + faceQuality + '\'' + ", needVerify='" + needVerify + '\'' + '}';
    }
}
