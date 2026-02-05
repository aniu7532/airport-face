package com.arcsoft.arcfacedemo.entity;

public class TimeControl {
    public int day;          // 星期几 (1-7，1=周一，7=周日)
    public String startTime; // 开始时间，格式如 "12:00"
    public String endTime;   // 结束时间，格式如 "13:00"

    public TimeControl() {
    }

    public TimeControl(int day, String startTime, String endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
