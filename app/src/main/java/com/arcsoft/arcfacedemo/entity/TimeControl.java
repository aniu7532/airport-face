package com.arcsoft.arcfacedemo.entity;

public class TimeControl {
    public String type;      // 类型："week" 或 "date"
    
    // week 类型使用
    public int day;          // 星期几 (1-7，1=周一，7=周日)
    
    // date 类型使用
    public String startDate; // 开始日期，格式如 "2026-02-01"
    public String endDate;   // 结束日期，格式如 "2026-02-05"
    
    // 两种类型都使用
    public String startTime; // 开始时间，格式如 "12:00"
    public String endTime;   // 结束时间，格式如 "13:00"

    public TimeControl() {
    }

    // week 类型构造函数
    public TimeControl(int day, String startTime, String endTime) {
        this.type = "week";
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // date 类型构造函数
    public TimeControl(String startDate, String endDate, String startTime, String endTime) {
        this.type = "date";
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
