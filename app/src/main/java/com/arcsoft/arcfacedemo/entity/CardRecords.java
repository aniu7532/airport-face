package com.arcsoft.arcfacedemo.entity;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * 通行记录分页查询结果，对应后台通行记录列表接口。
 */
public class CardRecords implements Serializable {

    /** 通行记录列表 */
    @SerializedName("list")
    private List<ListDTO> list;
    /** 总记录数 */
    @SerializedName("total")
    private int total;

    public List<ListDTO> getList() {
        return list;
    }

    public void setList(List<ListDTO> list) {
        this.list = list;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    /** 单条通行记录详情 */
    public static class ListDTO {
        /** 记录 ID */
        @SerializedName("id")
        private String id;
        /** 证件编号 */
        @SerializedName("idCode")
        private String idCode;
        /** 通行方向，1 进，-1 出，2 核验 */
        @SerializedName("direction")
        private int direction;
        /** 持卡人用户 ID */
        @SerializedName("userId")
        private String userId;
        /** 持卡人姓名 */
        @SerializedName("nickname")
        private String nickname;
        /** 性别 */
        @SerializedName("sex")
        private int sex;
        /** 所属单位 ID */
        @SerializedName("companyId")
        private String companyId;
        /** 所属单位名称 */
        @SerializedName("companyName")
        private String companyName;
        /** 通行区域名称 */
        @SerializedName("areaName")
        private String areaName;
        /** 通行状态，true 正常，false 异常 */
        @SerializedName("status")
        private boolean status;
        /** 查验时间 */
        @SerializedName("checkTime")
        private String checkTime;
        /** 证件类型 */
        @SerializedName("passType")
        private int passType;
        /** 异常原因 */
        @SerializedName("reason")
        private String reason;
        /** 携带物品类型名称 */
        @SerializedName("goodsTypeName")
        private String goodsTypeName;
        /** 现场照片 */
        @SerializedName("sitePhoto")
        private String sitePhoto;

        /** 核验比对照片 */
        @SerializedName("checkPhoto")
        private String checkPhoto;

        /** 人脸相似度 */
        @SerializedName("faceSimilar")
        private double faceSimilar;
        /** 查验人姓名 */
        @SerializedName("checkUserName")
        private String checkUserName;
        /** 核验备注 */
        @SerializedName("verifyRemark")
        private String verifyRemark;
        /** 查验设备名称 */
        @SerializedName("deviceName")
        private String deviceName;
        /** 查验设备编码 */
        @SerializedName("deviceCode")
        private String deviceCode;


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIdCode() {
            return idCode;
        }

        public void setIdCode(String idCode) {
            this.idCode = idCode;
        }

        public int getDirection() {
            return direction;
        }

        public void setDirection(int direction) {
            this.direction = direction;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public int getSex() {
            return sex;
        }

        public void setSex(int sex) {
            this.sex = sex;
        }

        public String getCompanyId() {
            return companyId;
        }

        public void setCompanyId(String companyId) {
            this.companyId = companyId;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String getAreaName() {
            return areaName;
        }

        public void setAreaName(String areaName) {
            this.areaName = areaName;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public String getCheckTime() {
            return checkTime;
        }

        public void setCheckTime(String checkTime) {
            this.checkTime = checkTime;
        }

        public int getPassType() {
            return passType;
        }

        public void setPassType(int passType) {
            this.passType = passType;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getGoodsTypeName() {
            return goodsTypeName;
        }

        public void setGoodsTypeName(String goodsTypeName) {
            this.goodsTypeName = goodsTypeName;
        }

        public String getSitePhoto() {
            return sitePhoto;
        }

        public void setSitePhoto(String sitePhoto) {
            this.sitePhoto = sitePhoto;
        }

        public String getCheckPhoto() {
            return checkPhoto;
        }

        public void setCheckPhoto(String checkPhoto) {
            this.checkPhoto = checkPhoto;
        }

        public double getFaceSimilar() {
            return faceSimilar;
        }

        public void setFaceSimilar(double faceSimilar) {
            this.faceSimilar = faceSimilar;
        }

        public String getCheckUserName() {
            return checkUserName;
        }

        public void setCheckUserName(String checkUserName) {
            this.checkUserName = checkUserName;
        }

        public String getVerifyRemark() {
            return verifyRemark;
        }

        public void setVerifyRemark(String verifyRemark) {
            this.verifyRemark = verifyRemark;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDeviceCode() {
            return deviceCode;
        }

        public void setDeviceCode(String deviceCode) {
            this.deviceCode = deviceCode;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return super.equals(obj);
        }
    }
}
