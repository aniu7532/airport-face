package com.arcsoft.arcfacedemo.entity;

import java.util.List;

public class LongPassCards {
    public List<LongPassCard> list;
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
