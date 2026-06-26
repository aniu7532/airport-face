package com.arcsoft.arcfacedemo.entity

import com.google.gson.annotations.SerializedName

/**
 * 查验单位信息，用于选择或展示查验所属组织。
 */
data class CheckUnit(
    /** 组织 ID */
    @SerializedName("orgId")
    val orgId: String? = null,
    /** 组织名称 */
    @SerializedName("name")
    val name: String? = null,
)
