//package com.arcsoft.arcfacedemo.db.model;
//
//import com.arcsoft.arcfacedemo.entity.Records;
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
//public class LongTermRecords extends Records implements Serializable {
//    private static final long serialVersionUID = 1L;
//    @Id
//    public Long id;
//    public String cardId;// 通行证实体卡id
//    public String idCode;//系统生成的编号
//    public String applyId;// 通行申请id(二维码扫描信息)
//    public String direction;// 通行方向（1：进，-1出，2：核验)
//    public String deviceId;// 查验设备ID
//    public String deviceName;// 查验设备名称
//    public String checkUserId;// 查验人ID
//    public String checkUserName;// 查验人姓名
//    public String companyName;// 查验人姓名
//    public String expiryDate;// 过期日期
//    public int templateType;//证件模板类型,1:蓝，2：黄
//    @Convert(converter = StrsConvert.class, columnType = String.class)
//    public String[] areaDisplayCode;//通行区域展示Code
//    public String area;// 通行区域id
//    public String areaName;// 通行区域名称(通行区域编码+名称)
//    public String status;// false,通行状态（正常/异常）,默认正常
//    public String reason;// false,异常原因，根据校验异常抛出的原因填写
//    public String sitePhoto;// 照片名字
//    public String checkTime;// 查验时间
//
//    @Generated(hash = 1574785370)
//    public LongTermRecords(Long id, String cardId, String idCode, String applyId,
//            String direction, String deviceId, String deviceName,
//            String checkUserId, String checkUserName, String companyName,
//            String expiryDate, int templateType, String[] areaDisplayCode,
//            String area, String areaName, String status, String reason,
//            String sitePhoto, String checkTime) {
//        this.id = id;
//        this.cardId = cardId;
//        this.idCode = idCode;
//        this.applyId = applyId;
//        this.direction = direction;
//        this.deviceId = deviceId;
//        this.deviceName = deviceName;
//        this.checkUserId = checkUserId;
//        this.checkUserName = checkUserName;
//        this.companyName = companyName;
//        this.expiryDate = expiryDate;
//        this.templateType = templateType;
//        this.areaDisplayCode = areaDisplayCode;
//        this.area = area;
//        this.areaName = areaName;
//        this.status = status;
//        this.reason = reason;
//        this.sitePhoto = sitePhoto;
//        this.checkTime = checkTime;
//    }
//
//    @Generated(hash = 1534375094)
//    public LongTermRecords() {
//    }
//
//    public Long getId() {
//        return this.id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getCardId() {
//        return this.cardId;
//    }
//
//    public void setCardId(String cardId) {
//        this.cardId = cardId;
//    }
//
//    public String getIdCode() {
//        return this.idCode;
//    }
//
//    public void setIdCode(String idCode) {
//        this.idCode = idCode;
//    }
//
//    public String getApplyId() {
//        return this.applyId;
//    }
//
//    public void setApplyId(String applyId) {
//        this.applyId = applyId;
//    }
//
//    public String getDirection() {
//        return this.direction;
//    }
//
//    public void setDirection(String direction) {
//        this.direction = direction;
//    }
//
//    public String getDeviceId() {
//        return this.deviceId;
//    }
//
//    public void setDeviceId(String deviceId) {
//        this.deviceId = deviceId;
//    }
//
//    public String getDeviceName() {
//        return this.deviceName;
//    }
//
//    public void setDeviceName(String deviceName) {
//        this.deviceName = deviceName;
//    }
//
//    public String getCheckUserId() {
//        return this.checkUserId;
//    }
//
//    public void setCheckUserId(String checkUserId) {
//        this.checkUserId = checkUserId;
//    }
//
//    public String getCheckUserName() {
//        return this.checkUserName;
//    }
//
//    public void setCheckUserName(String checkUserName) {
//        this.checkUserName = checkUserName;
//    }
//
//    public String getCompanyName() {
//        return this.companyName;
//    }
//
//    public void setCompanyName(String companyName) {
//        this.companyName = companyName;
//    }
//
//    public String getExpiryDate() {
//        return this.expiryDate;
//    }
//
//    public void setExpiryDate(String expiryDate) {
//        this.expiryDate = expiryDate;
//    }
//
//    public int getTemplateType() {
//        return this.templateType;
//    }
//
//    public void setTemplateType(int templateType) {
//        this.templateType = templateType;
//    }
//
//    public String[] getAreaDisplayCode() {
//        return this.areaDisplayCode;
//    }
//
//    public void setAreaDisplayCode(String[] areaDisplayCode) {
//        this.areaDisplayCode = areaDisplayCode;
//    }
//
//    public String getArea() {
//        return this.area;
//    }
//
//    public void setArea(String area) {
//        this.area = area;
//    }
//
//    public String getAreaName() {
//        return this.areaName;
//    }
//
//    public void setAreaName(String areaName) {
//        this.areaName = areaName;
//    }
//
//    public String getStatus() {
//        return this.status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getReason() {
//        return this.reason;
//    }
//
//    public void setReason(String reason) {
//        this.reason = reason;
//    }
//
//    public String getSitePhoto() {
//        return this.sitePhoto;
//    }
//
//    public void setSitePhoto(String sitePhoto) {
//        this.sitePhoto = sitePhoto;
//    }
//
//    public String getCheckTime() {
//        return this.checkTime;
//    }
//
//    public void setCheckTime(String checkTime) {
//        this.checkTime = checkTime;
//    }
//
//    // public int upstate;// 上传与否0:未上传，1:上传了
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
//}
