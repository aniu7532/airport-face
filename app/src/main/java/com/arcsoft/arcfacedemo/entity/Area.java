package com.arcsoft.arcfacedemo.entity;

import java.util.List;

/**
 * 通行区域节点，支持树形结构，用于区域选择与权限校验。
 */
public class Area {
    /** 区域 ID */
    private String id;
    /** 区域名称 */
    private String name;
    /** 区域编码 */
    private String code;
    /** 父级区域 ID */
    private String parentId;
    /** 区域分类 */
    private int category;
    /** 区域类型 */
    private int type;
    /** 区域类型名称 */
    private String typeName;
    /** 子区域列表 */
    private List<Area> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public List<Area> getChildren() {
        return children;
    }

    public void setChildren(List<Area> children) {
        this.children = children;
    }
}
