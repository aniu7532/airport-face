package com.arcsoft.arcfacedemo.entity;

import java.io.Serializable;

/**
 * 通用接口响应基类，字段名为 message，用于部分后端接口的 JSON 反序列化。
 *
 * @param <T> 业务数据类型
 */
public class Base2<T> implements Serializable {
    /** 响应状态码 */
    private int code;
    /** 响应提示信息 */
    private String message;
    /** 业务数据载荷 */
    private T data;
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }



}
