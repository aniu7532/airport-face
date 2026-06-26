package com.arcsoft.arcfacedemo.entity;

/**
 * 通用 HTTP 接口响应包装，包含状态码、业务数据与提示信息。
 *
 * @param <T> 业务数据类型
 */
public class ApiResponse<T> {
    /** 响应状态码 */
    private int code;
    /** 业务数据载荷 */
    private T data;
    /** 响应提示信息 */
    private String msg;


    /** 获取响应状态码 */
    public int getCode() {
        return code;
    }


    /** 获取业务数据 */
    public T getData() {
        return data;
    }


    /** 获取响应提示信息 */
    public String getMsg() {
        return msg;
    }

}
