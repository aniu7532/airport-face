package com.arcsoft.arcfacedemo.entity

/**
 * 查验终端设备信息，用于设备注册与配置同步。
 */
data class DeviceResult(
    /** 设备 ID */
    val id: String?,
    /** 设备名称 */
    val name: String?,
    /** 设备编码 */
    val code: String?,
    /** 设备 MAC 地址 */
    val mac: String?,
    /** 备注 */
    val remark: String?,
    /** 创建时间 */
    val createTime: String?,
    /** 设备类型 */
    val type: String?,
)
