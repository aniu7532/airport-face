package com.arcsoft.arcfacedemo.ui.model;


import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;

/**
 * 人脸识别比对结果，关联本地人脸库实体、相似度、追踪 ID 及耗时等信息。
 */
public class CompareResult {
    private FaceEntity faceEntity;
    private float similar;
    private int trackId;
    private int compareCode;
    private long cost;

    public CompareResult(FaceEntity faceEntity, float similar) {
        this.faceEntity = faceEntity;
        this.similar = similar;
    }

    public CompareResult(FaceEntity faceEntity, float similar, int compareCode,long cost) {
        this.faceEntity = faceEntity;
        this.similar = similar;
        this.compareCode = compareCode;
        this.cost = cost;
    }

    public FaceEntity getFaceEntity() {
        return faceEntity;
    }

    public void setFaceEntity(FaceEntity faceEntity) {
        this.faceEntity = faceEntity;
    }

    public float getSimilar() {
        return similar;
    }

    public void setSimilar(float similar) {
        this.similar = similar;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public int getCompareCode() {
        return compareCode;
    }

    public void setCompareCode(int compareCode) {
        this.compareCode = compareCode;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }
}
