package com.arcsoft.arcfacedemo.entity;

import java.io.Serializable;
import java.util.List;

public class BaseList<T> implements Serializable {
    private String msg;
    private int code;
    private List<T> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return code == 1;
    }

    public boolean isNoData() {
        return code == 0;
    }

    public boolean isError() {
        return code == -1;
    }

}
