package com.arcsoft.arcfacedemo.entity;

public class ApiResponse<T> {
    private int code;
    private T data;
    private String msg;


    public int getCode() {
        return code;
    }


    public T getData() {
        return data;
    }


    public String getMsg() {
        return msg;
    }

}
