package com.arcsoft.arcfacedemo.entity

import com.google.gson.annotations.SerializedName

data class CheckUnit(
    @SerializedName("orgId")
    val orgId: String? = null,
    @SerializedName("name")
    val name: String? = null,
)
