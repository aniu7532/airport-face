package com.arcsoft.arcfacedemo.entity;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CardRecords implements Serializable {

    @SerializedName("list")
    private List<ListDTO> list;
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

    public static class ListDTO {
        @SerializedName("id")
        private String id;
        @SerializedName("idCode")
        private String idCode;
        @SerializedName("direction")
        private int direction;
        @SerializedName("userId")
        private String userId;
        @SerializedName("nickname")
        private String nickname;
        @SerializedName("sex")
        private int sex;
        @SerializedName("companyId")
        private String companyId;
        @SerializedName("companyName")
        private String companyName;
        @SerializedName("areaName")
        private String areaName;
        @SerializedName("status")
        private boolean status;
        @SerializedName("checkTime")
        private String checkTime;
        @SerializedName("passType")
        private int passType;
        @SerializedName("reason")
        private String reason;
        @SerializedName("goodsTypeName")
        private String goodsTypeName;
        @SerializedName("sitePhoto")
        private String sitePhoto;

        @SerializedName("checkPhoto")
        private String checkPhoto;

        @SerializedName("faceSimilar")
        private double faceSimilar;
        @SerializedName("checkUserName")
        private String checkUserName;
        @SerializedName("verifyRemark")
        private String verifyRemark;
        @SerializedName("deviceName")
        private String deviceName;
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
