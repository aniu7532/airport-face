//package com.arcsoft.arcfacedemo.db.model;
//
//import com.blankj.utilcode.util.GsonUtils;
//import com.blankj.utilcode.util.ObjectUtils;
//import com.google.gson.reflect.TypeToken;
//
//import org.greenrobot.greendao.annotation.Convert;
//import org.greenrobot.greendao.annotation.Entity;
//import org.greenrobot.greendao.annotation.Generated;
//import org.greenrobot.greendao.annotation.Id;
//import org.greenrobot.greendao.annotation.NotNull;
//import org.greenrobot.greendao.converter.PropertyConverter;
//
//import java.io.Serializable;
//import java.util.List;
//
//@Entity()
//public class AnjiRecord implements Serializable {
//    private static final long serialVersionUID = 1L;
//
//    public static final int PING_QFM = 1;
//    public static final int PING_HE = 2;
//    public static final int PING_XIANG = 3;
//
//    // 瓶箱关系查询
//    public static final int PING_PXGXCX = 4;
//
//    // 瓶码信息查询
//    public static final int PING_PMXXCX = 5;
//
//    // 盒瓶信息查询
//    public static final int PING_HPXXCX = 6;
//
//    @Id(autoincrement = true)
//    private Long id;
//    private String box;
//
//    @NotNull
//    private String time;
//
//    private String cause;
//
//    private String wineName;
//    private String degree;
//    private String content;
//    private String spec;
//    private String dealerName;
//    private String pDate;
//    private String batch;
//
//    @NotNull
//    private int type;
//
//    private String uid;
//    private String qfm;
//    private String laser;
//
//    private String qrTop;
//    private String qrBody;
//    private boolean match;
//
//    @NotNull
//    @Convert(converter = BottlesConvert.class, columnType = String.class)
//    private List<String> bottles;
//
//    @Generated(hash = 711033386)
//    public AnjiRecord(Long id, String box, @NotNull String time, String cause, String wineName, String degree,
//            String content, String spec, String dealerName, String pDate, String batch, int type, String uid, String qfm,
//            String laser, String qrTop, String qrBody, boolean match, @NotNull List<String> bottles) {
//        this.id = id;
//        this.box = box;
//        this.time = time;
//        this.cause = cause;
//        this.wineName = wineName;
//        this.degree = degree;
//        this.content = content;
//        this.spec = spec;
//        this.dealerName = dealerName;
//        this.pDate = pDate;
//        this.batch = batch;
//        this.type = type;
//        this.uid = uid;
//        this.qfm = qfm;
//        this.laser = laser;
//        this.qrTop = qrTop;
//        this.qrBody = qrBody;
//        this.match = match;
//        this.bottles = bottles;
//    }
//
//    @Generated(hash = 2065636846)
//    public AnjiRecord() {
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
//    public int getType() {
//        return type;
//    }
//
//    public void setType(int type) {
//        this.type = type;
//    }
//
//    public String getBox() {
//        return this.box;
//    }
//
//    public void setBox(String box) {
//        this.box = box;
//    }
//
//    public List<String> getBottles() {
//        return bottles;
//    }
//
//    public void setBottles(List<String> bottles) {
//        this.bottles = bottles;
//    }
//
//    public String getTime() {
//        return this.time;
//    }
//
//    public void setTime(String time) {
//        this.time = time;
//    }
//
//    public String getCause() {
//        return cause;
//    }
//
//    public void setCause(String cause) {
//        this.cause = cause;
//    }
//
//    public String getWineName() {
//        return wineName;
//    }
//
//    public void setWineName(String wineName) {
//        this.wineName = wineName;
//    }
//
//    public String getPDate() {
//        return pDate;
//    }
//
//    public void setPDate(String pDate) {
//        this.pDate = pDate;
//    }
//
//    public String getBatch() {
//        return batch;
//    }
//
//    public void setBatch(String batch) {
//        this.batch = batch;
//    }
//
//    public boolean isMatch() {
//        return match;
//    }
//
//    public void setMatch(boolean match) {
//        this.match = match;
//    }
//
//    public static class BottlesConvert implements PropertyConverter<List<String>, String> {
//
//        @Override
//        public List<String> convertToEntityProperty(String databaseValue) {
//            return GsonUtils.fromJson(databaseValue, new TypeToken<List<String>>() {
//            }.getType());
//        }
//
//        @Override
//        public String convertToDatabaseValue(List<String> entityProperty) {
//            return GsonUtils.toJson(entityProperty);
//        }
//    }
//
//
//    public String getUid() {
//        return this.uid;
//    }
//
//    public void setUid(String uid) {
//        this.uid = uid;
//    }
//
//    public String getQfm() {
//        return this.qfm;
//    }
//
//    public void setQfm(String qfm) {
//        this.qfm = qfm;
//    }
//
//    public String getLaser() {
//        return this.laser;
//    }
//
//    public void setLaser(String laser) {
//        this.laser = laser;
//    }
//
//    public String getQrTop() {
//        return this.qrTop;
//    }
//
//    public void setQrTop(String qrTop) {
//        this.qrTop = qrTop;
//    }
//
//    public String getQrBody() {
//        return this.qrBody;
//    }
//
//    public void setQrBody(String qrBody) {
//        this.qrBody = qrBody;
//    }
//
//    @Override
//    public String toString() {
//        return "AnjiRecord{" + "id=" + id + ", box='" + box + '\'' + ", time='" + time + '\'' + ", cause='" + cause
//                + '\'' + "," + " wineName='" + wineName + '\'' + ", pDate='" + pDate + '\'' + ", batch='" + batch + '\''
//                + ", type=" + type + ", uid='" + uid + '\'' + ", qfm='" + qfm + '\'' + ", laser='" + laser + '\''
//                + ", qrTop='" + qrTop + '\'' + ", " + "qrBody='" + qrBody + '\'' + ", bottles="
//                + (ObjectUtils.isEmpty(bottles) ? null : bottles.toString()) + '}';
//    }
//
//    public boolean getMatch() {
//        return this.match;
//    }
//
//    public String getDegree() {
//        return this.degree;
//    }
//
//    public void setDegree(String degree) {
//        this.degree = degree;
//    }
//
//    public String getContent() {
//        return this.content;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }
//
//    public String getSpec() {
//        return this.spec;
//    }
//
//    public void setSpec(String spec) {
//        this.spec = spec;
//    }
//
//    public String getDealerName() {
//        return this.dealerName;
//    }
//
//    public void setDealerName(String dealerName) {
//        this.dealerName = dealerName;
//    }
//}
