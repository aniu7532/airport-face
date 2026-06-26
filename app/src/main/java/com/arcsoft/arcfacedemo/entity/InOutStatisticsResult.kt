package com.arcsoft.arcfacedemo.entity

/**
 * 指定日期的进出港通行统计数据。
 */
data class InOutStatisticsResult(
    /** 统计日期 */
    val date: String,
    /** 进港人次 */
    val inCount: Int,
    /** 出港人次 */
    val outCount: Int,
)
