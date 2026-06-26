package com.arcsoft.arcfacedemo.util

import java.util.Calendar

/**
 * Calendar 扩展函数，提供日初、日末及向前偏移等常用日期边界计算。
 */

/** 将时间归零到当天 00:00:00.000 */
val Calendar.dayStart: Calendar
    get() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        return this
    }

/** 将时间设为当天 23:59:59.999 */
val Calendar.dayEnd: Calendar
    get() {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
        return this
    }

/** 向前偏移指定天数并将时间归零到当天 00:00:00.000 */
fun Calendar.dayBefore(day: Int): Calendar {
    add(Calendar.DAY_OF_YEAR, -day)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return this
}