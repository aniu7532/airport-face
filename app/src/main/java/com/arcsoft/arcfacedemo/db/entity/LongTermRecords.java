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

@Entity(tableName = "long_term_records")
public class LongTermRecords extends Records {
    @PrimaryKey()
    @NonNull
    public String id;
    public String passid;// 通行证id"id"
    public String cardId;// 通行证实体卡id
    public String idCode;// 系统生成的编号
    public String applyId;// 通行申请id(二维码扫描信息)
    public String direction;// 通行方向（1：进，-1出，2：核验)
    public String nickname;
    public String photo;
    public String leadingPeople;
    public String deviceId;// 查验设备ID
    public String deviceName;// 查验设备名称
    public String checkUserId;// 查验人ID
    public String checkUserName;// 查验人姓名
    public String companyName;// 查验人姓名
    public String expiryDate;// 过期日期
    public int templateType;// 证件模板类型,1:蓝，2：黄
    public String[] areaDisplayCode;// 通行区域展示Code
    public String area;// 通行区域id
    public String areaName;// 通行区域名称(通行区域编码+名称)
    public String status;// false,通行状态（正常/异常）,默认正常
    public String reason;// false,异常原因，根据校验异常抛出的原因填写
    public String parentld;// 引领人通行记录id

    public String sitePhoto;// 照片名字
    public String checkTime;// 查验时间
    public String faceSimilar;// 相似度
    public String faceQuality;// 质量

    public String leadingPeopleld;// 引领人id，C类证件进出需要校验，查验不需要校验
    // public int upstate;// 上传与否0:未上传，1:上传了

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
        return "LongTermRecords{" +
                "id='" + id + '\'' +
                ", passid='" + passid + '\'' +
                ", cardId='" + cardId + '\'' +
                ", idCode='" + idCode + '\'' +
                ", applyId='" + applyId + '\'' +
                ", direction='" + direction + '\'' +
                ", nickname='" + nickname + '\'' +
                ", photo='" + photo + '\'' +
                ", leadingPeople='" + leadingPeople + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", checkUserId='" + checkUserId + '\'' +
                ", checkUserName='" + checkUserName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", templateType=" + templateType +
                ", areaDisplayCode=" + Arrays.toString(areaDisplayCode) +
                ", area='" + area + '\'' +
                ", areaName='" + areaName + '\'' +
                ", status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                ", parentld='" + parentld + '\'' +
                ", sitePhoto='" + sitePhoto + '\'' +
                ", checkTime='" + checkTime + '\'' +
                ", faceSimilar='" + faceSimilar + '\'' +
                ", faceQuality='" + faceQuality + '\'' +
                ", leadingPeopleld='" + leadingPeopleld + '\'' +
                '}';
    }
}
