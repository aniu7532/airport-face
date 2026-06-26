package com.arcsoft.arcfacedemo.entity;

import java.util.List;

/**
 * 长期/临时通行证分页列表，对应后台同步接口返回结构。
 */
public class LongPassCards {
    /** 通行证列表 */
    public List<LongPassCard> list;
    /** 总记录数 */
    public int total;

    public List<LongPassCard> getList() {
        return list;
    }

    public void setList(List<LongPassCard> list) {
        this.list = list;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
