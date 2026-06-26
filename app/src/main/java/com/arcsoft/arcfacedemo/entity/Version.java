package com.arcsoft.arcfacedemo.entity;

/**
 * 应用版本信息，用于检查更新与强制升级策略。
 */
public class Version {

    /** 创建时间 */
    private String createTime;
    /** 更新时间 */
    private String updateTime;
    /** 创建人 */
    private String creator;
    /** 更新人 */
    private String updater;
    /** 是否已逻辑删除 */
    private boolean deleted;
    /** 租户 ID */
    private String tenantId;
    /** 版本记录 ID */
    private String id;
    /** 安装包下载地址 */
    private String url;
    /** 版本号字符串 */
    private String version;
    /** 版本序号，用于比较新旧版本 */
    private int sequence;
    /** 版本类型 */
    private int type;
    /** 是否已发布，1 已发布 */
    private int isReleased;
    /** 是否强制更新，1 强制 */
    private int isForceUpdate;
    /** 版本更新说明 */
    private String remark;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getIsReleased() {
        return isReleased;
    }

    public void setIsReleased(int isReleased) {
        this.isReleased = isReleased;
    }

    public int getIsForceUpdate() {
        return isForceUpdate;
    }

    public void setIsForceUpdate(int isForceUpdate) {
        this.isForceUpdate = isForceUpdate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
