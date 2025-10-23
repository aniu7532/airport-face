package com.arcsoft.arcfacedemo.entity;

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
    }
}
