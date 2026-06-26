package com.arcsoft.arcfacedemo.db.entity;

import java.lang.reflect.Type;
import java.util.Arrays;

import com.arcsoft.arcfacedemo.entity.LeadingPeople;
import com.arcsoft.arcfacedemo.entity.TimeControl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 长期/临时通行证本地持久化实体，对应表 long_term_pass。
 * 复杂字段（引领人、时段限制）以 JSON 字符串存储。
 */
@Entity(tableName = "long_term_pass")
public class LongTermPass {
    /** 通行证 ID，主键 */
    @PrimaryKey
    @NonNull
    public String id;
    /** 通行申请 ID */
    public String applyId;
    /** 系统证件编号 */
    public String idCode;
    /** 实体卡号 */
    public String cardId;
    /** 证件积分 */
    public int score;
    /** 证件状态，2 表示已注销 */
    public int status;
    /** 证件类型，0 长期证，1 临时证 */
    public int type;
    /** 持卡人用户 ID */
    public String userId;
    /** 所属单位 ID */
    public String companyId;
    /** 所属部门 ID */
    public String orgId;
    /** 持卡人姓名 */
    public String nickname;
    /** 所属单位名称 */
    public String companyName;
    /** 所属部门名称 */
    public String orgName;
    /** 证件有效期截止日期 */
    public String expiryDate;
    /** 通行区域根节点 ID 列表 */
    public String[] areaRootIds;
    /** 通行区域根节点编码列表 */
    public String[] areaRootCodes;
    /** 可通行区域 ID 列表 */
    public String[] areaIds;
    /** 可通行区域编码列表 */
    public String[] areaCodes;
    /** 证件生效起始日期 */
    public String startDate;
    /** 引领人信息 JSON 字符串 */
    public String leadingPeople;
    /** 证件照片路径或 URL */
    public String photo;
    /** 证件照片二进制数据 */
    public byte[] photoBytes;
    /** 引领人用户 ID 列表 */
    public String[] leadingPeopleId;
    /** 身份证号 */
    public String idNo;
    /** 现场核验照片路径或 URL */
    public String checkPhoto;
    /** 现场核验照片二进制数据 */
    public byte[] checkPhotoBytes;
    /** 查验单位名称 */
    public String unitName;
    /** 证件模板类型，1 蓝色，2 黄色 */
    public int templateType;
    /** 是否黑名单人员 */
    public boolean isBlacklist;
    /** 是否暂扣证件 */
    public boolean isWithhold;
    /** 是否已撤回 */
    public boolean isWithdraw;
    /** 暂扣开始日期 */
    public String withholdStartDate;
    /** 暂扣结束日期 */
    public String withholdEndDate;
    /** 实体卡长卡号 */
    public String cardIdLong;
    /** 通行区域展示编码列表 */
    public String[] areaDisplayCode;
    /** 经营范围 */
    public String businessScope;
    /** 性别 */
    public int sex;
    /** 数据最后更新时间，用于增量同步 */
    public String updateTime;
    /** 通行时段限制 JSON 字符串 */
    public String timeControl;

    /** 将引领人数组序列化为 JSON 存入数据库 */
    public void setleadingPeople(LeadingPeople[] someObjectArray) {
        Gson gson = new Gson();
        this.leadingPeople = gson.toJson(someObjectArray);
    }

    /** 从 JSON 反序列化引领人数组 */
    public LeadingPeople[] getLeadingPeople() {
        Gson gson = new Gson();
        Type type = new TypeToken<LeadingPeople[]>() {
        }.getType();
        return gson.fromJson(leadingPeople, type);
    }

    /** 将时段限制数组序列化为 JSON 存入数据库 */
    public void setTimeControl(TimeControl[] timeControlArray) {
        if (timeControlArray == null) {
            this.timeControl = null;
            return;
        }
        Gson gson = new Gson();
        this.timeControl = gson.toJson(timeControlArray);
    }

    /** 从 JSON 反序列化时段限制数组 */
    public TimeControl[] getTimeControl() {
        if (timeControl == null || timeControl.isEmpty()) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<TimeControl[]>() {
        }.getType();
        return gson.fromJson(timeControl, type);
    }

    @Override
    public String toString() {
        return "LongTermPass{" + "id='" + id + '\'' + ", applyId='" + applyId + '\'' + ", idCode='" + idCode + '\''
                + ", cardId='" + cardId + '\'' + ", score=" + score + ", status=" + status + ", type=" + type
                + ", userId='" + userId + "\\\n" + ", companyId='" + companyId + '\'' + ", orgId='" + orgId + '\''
                + ", nickname='" + nickname + '\'' + ", companyName='" + companyName + "\\\n" + ", orgName='" + orgName
                + '\'' + ", expiryDate='" + expiryDate + '\'' + ", areaRootIds=" + Arrays.toString(areaRootIds)
                + ", areaRootCodes=" + Arrays.toString(areaRootCodes) + ", areaIds=" + Arrays.toString(areaIds)
                + ", areaCodes=" + Arrays.toString(areaCodes) + ", startDate='" + startDate + '\'' + ", leadingPeople='"
                + leadingPeople + "\\\n" + ", photo='" + photo + '\'' + ", photoBytes=" + Arrays.toString(photoBytes)
                + ", leadingPeopleId=" + Arrays.toString(leadingPeopleId) + ", idNo='" + idNo + "\\\n"
                + ", checkPhoto='" + checkPhoto + '\'' + ", checkPhotoBytes=" + Arrays.toString(checkPhotoBytes)
                + ", unitName='" + unitName + '\'' + ", templateType=" + templateType + ", isBlacklist=" + isBlacklist
                + ", isWithhold=" + isWithhold + ", isWithdraw=" + isWithdraw + ", withholdStartDate='"
                + withholdStartDate + "\\\n" + ", withholdEndDate='" + withholdEndDate + "\\\n" + ", cardIdLong='"
                + cardIdLong + '\'' + ", areaDisplayCode=" + Arrays.toString(areaDisplayCode) + ", businessScope='"
                + businessScope + "\\\n" + ", sex=" + sex + ", updateTime='" + updateTime + '\'' + '}';
    }
}
