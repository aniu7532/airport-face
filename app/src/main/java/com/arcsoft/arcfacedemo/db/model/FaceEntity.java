//package com.arcsoft.arcfacedemo.db.model;
//
//import android.os.Parcel;
//import android.os.Parcelable;
//
//import org.greenrobot.greendao.annotation.Entity;
//import org.greenrobot.greendao.annotation.Generated;
//import org.greenrobot.greendao.annotation.Id;
//import org.greenrobot.greendao.annotation.Property;
//
//import java.util.Arrays;
//import java.util.Objects;
//
///**
// * 人脸库中的单挑人脸记录
// */
//@Entity()
//public class FaceEntity implements Parcelable {
//    /**
//     * 人脸id，主键
//     */
//    @Id(autoincrement = true)
//    private long id;
//    /**
//     * 用户名称
//     */
//    @Property(nameInDb = "user_name")
//    private String userName;
//    /**
//     * 图片路径
//     */
//    @Property(nameInDb = "image_path")
//    private String imagePath;
//    /**
//     * 人脸特征数据
//     */
//    @Property(nameInDb = "feature_data")
//    private byte[] featureData;
//    /**
//     * 注册时间
//     */
//    @Property(nameInDb = "register_time")
//    private long registerTime;
//
//
//    public FaceEntity(String userName, String imagePath, byte[] featureData) {
//        this.userName = userName;
//        this.imagePath = imagePath;
//        this.featureData = featureData;
//        registerTime = System.currentTimeMillis();
//    }
//
//    public FaceEntity(FaceEntity faceEntity) {
//        this.id = faceEntity.id;
//        this.userName = faceEntity.userName;
//        this.imagePath = faceEntity.imagePath;
//        this.featureData = faceEntity.featureData;
//        this.registerTime = faceEntity.registerTime;
//    }
//
//
//    protected FaceEntity(Parcel in) {
//        id = in.readLong();
//        registerTime = in.readLong();
//        userName = in.readString();
//        imagePath = in.readString();
//        featureData = in.createByteArray();
//    }
//
//    @Generated(hash = 1843852726)
//    public FaceEntity(long id, String userName, String imagePath,
//            byte[] featureData, long registerTime) {
//        this.id = id;
//        this.userName = userName;
//        this.imagePath = imagePath;
//        this.featureData = featureData;
//        this.registerTime = registerTime;
//    }
//
//    @Generated(hash = 1094488673)
//    public FaceEntity() {
//    }
//
//    public static final Creator<FaceEntity> CREATOR = new Creator<FaceEntity>() {
//        @Override
//        public FaceEntity createFromParcel(Parcel in) {
//            return new FaceEntity(in);
//        }
//
//        @Override
//        public FaceEntity[] newArray(int size) {
//            return new FaceEntity[size];
//        }
//    };
//
//    public long getFaceId() {
//        return id;
//    }
//
//    public void setFaceId(long faceId) {
//        this.id = faceId;
//    }
//
//    public String getUserName() {
//        return userName;
//    }
//
//    public void setUserName(String userName) {
//        this.userName = userName;
//    }
//
//    public String getImagePath() {
//        return imagePath;
//    }
//
//    public void setImagePath(String imagePath) {
//        this.imagePath = imagePath;
//    }
//
//    public byte[] getFeatureData() {
//        return featureData;
//    }
//
//    public void setFeatureData(byte[] featureData) {
//        this.featureData = featureData;
//    }
//
//    public long getRegisterTime() {
//        return registerTime;
//    }
//
//    public void setRegisterTime(long registerTime) {
//        this.registerTime = registerTime;
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeLong(id);
//        dest.writeLong(registerTime);
//        dest.writeString(userName);
//        dest.writeString(imagePath);
//        dest.writeByteArray(featureData);
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//        FaceEntity that = (FaceEntity) o;
//        return id == that.id &&
//                registerTime == that.registerTime &&
//                userName.equals(that.userName) &&
//                imagePath.equals(that.imagePath) &&
//                Arrays.equals(featureData, that.featureData);
//    }
//
//    @Override
//    public int hashCode() {
//        int result = Objects.hash(id, registerTime, userName, imagePath);
//        result = 31 * result + Arrays.hashCode(featureData);
//        return result;
//    }
//
//    public long getId() {
//        return this.id;
//    }
//
//    public void setId(long id) {
//        this.id = id;
//    }
//}
