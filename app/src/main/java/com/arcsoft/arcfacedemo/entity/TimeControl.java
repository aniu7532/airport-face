package com.arcsoft.arcfacedemo.entity;

/**
 * 通行证通行时段限制规则，支持按星期或指定日期区间控制可通行时间。
 */
public class TimeControl {
    /** 规则类型："week" 按星期，"date" 按日期区间 */
    public String type;

    /** 星期几（1-7，1=周一，7=周日），week 类型使用 */
    public int day;

    /** 日期区间开始日期，格式如 "2026-02-01"，date 类型使用 */
    public String startDate;
    /** 日期区间结束日期，格式如 "2026-02-05"，date 类型使用 */
    public String endDate;

    /** 每日允许通行开始时间，格式如 "12:00" */
    public String startTime;
    /** 每日允许通行结束时间，格式如 "13:00" */
    public String endTime;

    public TimeControl() {
    }

    /** 构造按星期限制的通行时段 */
    public TimeControl(int day, String startTime, String endTime) {
        this.type = "week";
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /** 构造按日期区间限制的通行时段 */
    public TimeControl(String startDate, String endDate, String startTime, String endTime) {
        this.type = "date";
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
