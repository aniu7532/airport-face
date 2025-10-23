//package com.arcsoft.arcfacedemo.db.model;
//
//import com.blankj.utilcode.util.GsonUtils;
//import com.google.gson.reflect.TypeToken;
//
//import org.greenrobot.greendao.annotation.Convert;
//import org.greenrobot.greendao.annotation.Entity;
//import org.greenrobot.greendao.annotation.Generated;
//import org.greenrobot.greendao.annotation.Id;
//import org.greenrobot.greendao.converter.PropertyConverter;
//
//import java.io.Serializable;
//import java.util.List;
//
//@Entity
//public class LongTermPass implements Serializable {
//    private static final long serialVersionUID = 1L;
//    @Id
//    private Long id;
//    public String applyId;
//    public String idCode;
//    public String cardId;
//    public int score;
//    public int status;
//    public int type;
//    public String userId;
//    public String companyId;
//    public String orgId;
//    public String nickname;
//    public String companyName;
//    public String orgName;
//    public String expiryDate;
//    @Convert(converter = StrsConvert.class, columnType = String.class)
//    public String[] areaRootIds;
//    @Convert(converter = StrsConvert.class, columnType = String.class)
//    public String[] areaRootCodes;
//    @Convert(converter = StrsConvert.class, columnType = String.class)
//    public String[] areaIds;
//    @Convert(converter = StrsConvert.class, columnType = String.class)
//    public String[] areaCodes;
//    public String startDate;
//    public String leadingPeople;
//    public String photo;
//    @Convert(converter = ByteConvert.class, columnType = String.class)
//    public byte[] photoBytes;
//    @Convert(converter = StrsConvert.class, columnType = String.class)
//    public String[] leadingPeopleId;
//    public String idNo;
//    public String checkPhoto;
//
//
//    @Convert(converter = ByteConvert.class, columnType = String.class)
//    public byte[] checkPhotoBytes;
//    public String unitName;
//    public int templateType;//证件模板类型,1:蓝，2：黄
//    public boolean isBlacklist;
//    public boolean isWithhold;
//    public boolean isWithdraw;
//    public String withholdStartDate;
//    public String withholdEndDate;
//    public String cardIdLong;
//    @Convert(converter = StrsConvert.class, columnType = String.class)
//    public String[] areaDisplayCode;//通行区域展示Code
//    public String businessScope;
//    public int sex;
//    public String updateTime;
//
//
//    @Generated(hash = 61519389)
//    public LongTermPass(Long id, String applyId, String idCode, String cardId,
//            int score, int status, int type, String userId, String companyId,
//            String orgId, String nickname, String companyName, String orgName,
//            String expiryDate, String[] areaRootIds, String[] areaRootCodes,
//            String[] areaIds, String[] areaCodes, String startDate,
//            String leadingPeople, String photo, byte[] photoBytes,
//            String[] leadingPeopleId, String idNo, String checkPhoto,
//            byte[] checkPhotoBytes, String unitName, int templateType,
//            boolean isBlacklist, boolean isWithhold, boolean isWithdraw,
//            String withholdStartDate, String withholdEndDate, String cardIdLong,
//            String[] areaDisplayCode, String businessScope, int sex,
//            String updateTime) {
//        this.id = id;
//        this.applyId = applyId;
//        this.idCode = idCode;
//        this.cardId = cardId;
//        this.score = score;
//        this.status = status;
//        this.type = type;
//        this.userId = userId;
//        this.companyId = companyId;
//        this.orgId = orgId;
//        this.nickname = nickname;
//        this.companyName = companyName;
//        this.orgName = orgName;
//        this.expiryDate = expiryDate;
//        this.areaRootIds = areaRootIds;
//        this.areaRootCodes = areaRootCodes;
//        this.areaIds = areaIds;
//        this.areaCodes = areaCodes;
//        this.startDate = startDate;
//        this.leadingPeople = leadingPeople;
//        this.photo = photo;
//        this.photoBytes = photoBytes;
//        this.leadingPeopleId = leadingPeopleId;
//        this.idNo = idNo;
//        this.checkPhoto = checkPhoto;
//        this.checkPhotoBytes = checkPhotoBytes;
//        this.unitName = unitName;
//        this.templateType = templateType;
//        this.isBlacklist = isBlacklist;
//        this.isWithhold = isWithhold;
//        this.isWithdraw = isWithdraw;
//        this.withholdStartDate = withholdStartDate;
//        this.withholdEndDate = withholdEndDate;
//        this.cardIdLong = cardIdLong;
//        this.areaDisplayCode = areaDisplayCode;
//        this.businessScope = businessScope;
//        this.sex = sex;
//        this.updateTime = updateTime;
//    }
//
//
//    @Generated(hash = 833676690)
//    public LongTermPass() {
//    }
//
//
//    public Long getId() {
//        return this.id;
//    }
//
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//
//    public String getApplyId() {
//        return this.applyId;
//    }
//
//
//    public void setApplyId(String applyId) {
//        this.applyId = applyId;
//    }
//
//
//    public String getIdCode() {
//        return this.idCode;
//    }
//
//
//    public void setIdCode(String idCode) {
//        this.idCode = idCode;
//    }
//
//
//    public String getCardId() {
//        return this.cardId;
//    }
//
//
//    public void setCardId(String cardId) {
//        this.cardId = cardId;
//    }
//
//
//    public int getScore() {
//        return this.score;
//    }
//
//
//    public void setScore(int score) {
//        this.score = score;
//    }
//
//
//    public int getStatus() {
//        return this.status;
//    }
//
//
//    public void setStatus(int status) {
//        this.status = status;
//    }
//
//
//    public int getType() {
//        return this.type;
//    }
//
//
//    public void setType(int type) {
//        this.type = type;
//    }
//
//
//    public String getUserId() {
//        return this.userId;
//    }
//
//
//    public void setUserId(String userId) {
//        this.userId = userId;
//    }
//
//
//    public String getCompanyId() {
//        return this.companyId;
//    }
//
//
//    public void setCompanyId(String companyId) {
//        this.companyId = companyId;
//    }
//
//
//    public String getOrgId() {
//        return this.orgId;
//    }
//
//
//    public void setOrgId(String orgId) {
//        this.orgId = orgId;
//    }
//
//
//    public String getNickname() {
//        return this.nickname;
//    }
//
//
//    public void setNickname(String nickname) {
//        this.nickname = nickname;
//    }
//
//
//    public String getCompanyName() {
//        return this.companyName;
//    }
//
//
//    public void setCompanyName(String companyName) {
//        this.companyName = companyName;
//    }
//
//
//    public String getOrgName() {
//        return this.orgName;
//    }
//
//
//    public void setOrgName(String orgName) {
//        this.orgName = orgName;
//    }
//
//
//    public String getExpiryDate() {
//        return this.expiryDate;
//    }
//
//
//    public void setExpiryDate(String expiryDate) {
//        this.expiryDate = expiryDate;
//    }
//
//
//    public String[] getAreaRootIds() {
//        return this.areaRootIds;
//    }
//
//
//    public void setAreaRootIds(String[] areaRootIds) {
//        this.areaRootIds = areaRootIds;
//    }
//
//
//    public String[] getAreaRootCodes() {
//        return this.areaRootCodes;
//    }
//
//
//    public void setAreaRootCodes(String[] areaRootCodes) {
//        this.areaRootCodes = areaRootCodes;
//    }
//
//
//    public String[] getAreaIds() {
//        return this.areaIds;
//    }
//
//
//    public void setAreaIds(String[] areaIds) {
//        this.areaIds = areaIds;
//    }
//
//
//    public String[] getAreaCodes() {
//        return this.areaCodes;
//    }
//
//
//    public void setAreaCodes(String[] areaCodes) {
//        this.areaCodes = areaCodes;
//    }
//
//
//    public String getStartDate() {
//        return this.startDate;
//    }
//
//
//    public void setStartDate(String startDate) {
//        this.startDate = startDate;
//    }
//
//
//    public String getLeadingPeople() {
//        return this.leadingPeople;
//    }
//
//
//    public void setLeadingPeople(String leadingPeople) {
//        this.leadingPeople = leadingPeople;
//    }
//
//
//    public String getPhoto() {
//        return this.photo;
//    }
//
//
//    public void setPhoto(String photo) {
//        this.photo = photo;
//    }
//
//
//    public byte[] getPhotoBytes() {
//        return this.photoBytes;
//    }
//
//
//    public void setPhotoBytes(byte[] photoBytes) {
//        this.photoBytes = photoBytes;
//    }
//
//
//    public String[] getLeadingPeopleId() {
//        return this.leadingPeopleId;
//    }
//
//
//    public void setLeadingPeopleId(String[] leadingPeopleId) {
//        this.leadingPeopleId = leadingPeopleId;
//    }
//
//
//    public String getIdNo() {
//        return this.idNo;
//    }
//
//
//    public void setIdNo(String idNo) {
//        this.idNo = idNo;
//    }
//
//
//    public String getCheckPhoto() {
//        return this.checkPhoto;
//    }
//
//
//    public void setCheckPhoto(String checkPhoto) {
//        this.checkPhoto = checkPhoto;
//    }
//
//
//    public byte[] getCheckPhotoBytes() {
//        return this.checkPhotoBytes;
//    }
//
//
//    public void setCheckPhotoBytes(byte[] checkPhotoBytes) {
//        this.checkPhotoBytes = checkPhotoBytes;
//    }
//
//
//    public String getUnitName() {
//        return this.unitName;
//    }
//
//
//    public void setUnitName(String unitName) {
//        this.unitName = unitName;
//    }
//
//
//    public int getTemplateType() {
//        return this.templateType;
//    }
//
//
//    public void setTemplateType(int templateType) {
//        this.templateType = templateType;
//    }
//
//
//    public boolean getIsBlacklist() {
//        return this.isBlacklist;
//    }
//
//
//    public void setIsBlacklist(boolean isBlacklist) {
//        this.isBlacklist = isBlacklist;
//    }
//
//
//    public boolean getIsWithhold() {
//        return this.isWithhold;
//    }
//
//
//    public void setIsWithhold(boolean isWithhold) {
//        this.isWithhold = isWithhold;
//    }
//
//
//    public boolean getIsWithdraw() {
//        return this.isWithdraw;
//    }
//
//
//    public void setIsWithdraw(boolean isWithdraw) {
//        this.isWithdraw = isWithdraw;
//    }
//
//
//    public String getWithholdStartDate() {
//        return this.withholdStartDate;
//    }
//
//
//    public void setWithholdStartDate(String withholdStartDate) {
//        this.withholdStartDate = withholdStartDate;
//    }
//
//
//    public String getWithholdEndDate() {
//        return this.withholdEndDate;
//    }
//
//
//    public void setWithholdEndDate(String withholdEndDate) {
//        this.withholdEndDate = withholdEndDate;
//    }
//
//
//    public String getCardIdLong() {
//        return this.cardIdLong;
//    }
//
//
//    public void setCardIdLong(String cardIdLong) {
//        this.cardIdLong = cardIdLong;
//    }
//
//
//    public String[] getAreaDisplayCode() {
//        return this.areaDisplayCode;
//    }
//
//
//    public void setAreaDisplayCode(String[] areaDisplayCode) {
//        this.areaDisplayCode = areaDisplayCode;
//    }
//
//
//    public String getBusinessScope() {
//        return this.businessScope;
//    }
//
//
//    public void setBusinessScope(String businessScope) {
//        this.businessScope = businessScope;
//    }
//
//
//    public int getSex() {
//        return this.sex;
//    }
//
//
//    public void setSex(int sex) {
//        this.sex = sex;
//    }
//
//
//    public String getUpdateTime() {
//        return this.updateTime;
//    }
//
//
//    public void setUpdateTime(String updateTime) {
//        this.updateTime = updateTime;
//    }
//
//
//    public static class StrsConvert implements PropertyConverter<String[], String> {
//
//        @Override
//        public String[] convertToEntityProperty(String databaseValue) {
//            return GsonUtils.fromJson(databaseValue, new TypeToken<List<String>>() {
//            }.getType());
//        }
//
//        @Override
//        public String convertToDatabaseValue(String[] entityProperty) {
//            return GsonUtils.toJson(entityProperty);
//        }
//    }
//
//
//    public static class ByteConvert implements PropertyConverter<byte[], String> {
//
//        @Override
//        public byte[] convertToEntityProperty(String databaseValue) {
//            return GsonUtils.fromJson(databaseValue, new TypeToken<List<String>>() {
//            }.getType());
//        }
//
//        @Override
//        public String convertToDatabaseValue(byte[] entityProperty) {
//            return GsonUtils.toJson(entityProperty);
//        }
//    }
//}
