package com.arcsoft.arcfacedemo.entity;

/**
 * 长期/临时通行证业务实体，包含持卡人信息、通行区域、时间限制及证件状态。
 */
public class LongPassCard {
    /** 通行证 ID */
    public String id;
    /** 通行申请 ID */
    public String applyId;
    /** 系统证件编号 */
    public String idCode;
    /** 实体卡号 */
    public String cardId;
    /** 证件积分/评分 */
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
    /** 引领人信息列表 */
    public LeadingPeople[] leadingPeople;
    /** 证件照片路径或 URL */
    public String photo;
    /** 证件照片二进制数据，用于本地人脸识别 */
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
    /** 通行时段限制规则列表 */
    public TimeControl[] timeControl;
}
